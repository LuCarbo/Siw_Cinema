package it.uniroma3.siw;

import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.service.FilmService;
import it.uniroma3.siw.service.RecensioneService;
import it.uniroma3.siw.service.CredentialsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SecurityAndControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilmService filmService;

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private CredentialsService credentialsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testPublicPagesAccessibleByAnonymous() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(get("/festivals"))
                .andExpect(status().isOk())
                .andExpect(view().name("festival/list"));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(view().name("film/list"));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void testAdminEndpointsForbiddenForAnonymous() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/festivals/nuovo"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "mario", authorities = {"USER"})
    void testAdminEndpointsForbiddenForUser() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/festivals/nuovo"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testAdminEndpointsAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));

        mockMvc.perform(get("/festivals/nuovo"))
                .andExpect(status().isOk())
                .andExpect(view().name("festival/form"));

        mockMvc.perform(get("/films/nuovo"))
                .andExpect(status().isOk())
                .andExpect(view().name("film/form"));
    }

    @Test
    @WithMockUser(username = "giulia", authorities = {"DEFAULT"})
    void testSaveReviewSuccess() throws Exception {
        Film film = new Film("Film Per Test Recensione", 2025, 110, "Commedia", "Italia");
        film = filmService.saveFilm(film);

        mockMvc.perform(post("/recensioni/salva")
                .with(csrf())
                .param("filmId", film.getId().toString())
                .param("voto", "5")
                .param("titolo", "Test titolo da test")
                .param("testo", "Recensione eccellente scritta da utente autenticato"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/film/" + film.getId()));
    }

    @Test
    @WithMockUser(username = "giulia", authorities = {"DEFAULT"})
    void testSaveReviewValidationErrors() throws Exception {
        Film film = filmService.getAllFilms().getFirst();
        mockMvc.perform(post("/recensioni/salva")
                .with(csrf())
                .param("filmId", film.getId().toString())
                .param("voto", "")
                .param("testo", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("recensione/form"))
                .andExpect(model().attributeHasFieldErrors("recensione", "voto", "testo"));
    }
}
