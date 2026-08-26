package it.uniroma3.siw;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.service.FestivalService;
import it.uniroma3.siw.service.FilmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class RestApiControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testGetFestivalsApi() throws Exception {
        mockMvc.perform(get("/api/festivals")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetFestivalDetailAndSubResources() throws Exception {
        List<Festival> festivals = festivalService.getAllFestivals();
        if (!festivals.isEmpty()) {
            Long festId = festivals.get(0).getId();

            mockMvc.perform(get("/api/festivals/" + festId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(festId));

            mockMvc.perform(get("/api/festivals/" + festId + "/movies")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            mockMvc.perform(get("/api/festivals/" + festId + "/screenings")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Test
    void testGetMoviesAndReviewsApi() throws Exception {
        List<Film> films = filmService.getAllFilms();
        if (!films.isEmpty()) {
            Long filmId = films.get(0).getId();

            mockMvc.perform(get("/api/movies/" + filmId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(filmId));

            mockMvc.perform(get("/api/movies/" + filmId + "/reviews")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Test
    void testGetScreeningsApi() throws Exception {
        mockMvc.perform(get("/api/screenings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testNotFoundStatusCodes() throws Exception {
        mockMvc.perform(get("/api/festivals/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/movies/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
