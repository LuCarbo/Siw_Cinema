package it.uniroma3.siw;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.service.FestivalService;
import it.uniroma3.siw.service.FilmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FestivalServiceTest {

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Test
    void testCreateAndRetrieveFestival() {
        Festival festival = new Festival("Torino Film Festival", 2026, "Torino",
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 28), "Festival di cinema indipendente.");
        Festival saved = festivalService.saveFestival(festival);

        assertNotNull(saved.getId());
        Festival retrieved = festivalService.getFestival(saved.getId());
        assertEquals("Torino Film Festival", retrieved.getNome());
        assertEquals(2026, retrieved.getAnno());
        assertEquals("Torino", retrieved.getCitta());
    }

    @Test
    void testAddAndRemoveFilmFromFestival() {
        Festival festival = new Festival("Giffoni Film Festival", 2026, "Giffoni",
                LocalDate.of(2026, 7, 16), LocalDate.of(2026, 7, 25), "Festival per ragazzi.");
        Festival savedFestival = festivalService.saveFestival(festival);

        Film film = new Film("Inside Out 2", 2024, 96, "Animazione", "USA");
        Film savedFilm = filmService.saveFilm(film);

        festivalService.addFilmToFestival(savedFestival.getId(), savedFilm.getId());

        Festival updated = festivalService.getFestival(savedFestival.getId());
        assertTrue(updated.getFilm().contains(savedFilm));

        festivalService.removeFilmFromFestival(savedFestival.getId(), savedFilm.getId());
        Festival afterRemove = festivalService.getFestival(savedFestival.getId());
        assertFalse(afterRemove.getFilm().contains(savedFilm));
    }

    @Test
    void testSearchFestivalByCitta() {
        List<Festival> veneziaFestivals = festivalService.searchByCitta("Venezia");
        assertFalse(veneziaFestivals.isEmpty());
        assertTrue(veneziaFestivals.stream().anyMatch(f -> f.getCitta().equalsIgnoreCase("Venezia")));
    }
}
