package it.uniroma3.siw;

import it.uniroma3.siw.model.*;
import it.uniroma3.siw.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FilmAndReviewServiceTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private RegistaService registaService;

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private UtenteService utenteService;

    @Test
    void testCreateFilmWithRegista() {
        Regista regista = new Regista("Paolo", "Sorrentino", LocalDate.of(1970, 5, 31), "Italiana");
        Regista savedRegista = registaService.saveRegista(regista);

        Film film = new Film("La grande bellezza", 2013, 142, "Drammatico", "Italia");
        Film savedFilm = filmService.saveFilmWithRegista(film, savedRegista.getId());

        assertNotNull(savedFilm.getId());
        assertEquals(savedRegista, savedFilm.getRegista());
    }

    @Test
    void testReviewCalculationAndUniqueness() {
        Film film = new Film("Interstellar", 2014, 169, "Fantascienza", "USA");
        Film savedFilm = filmService.saveFilm(film);

        Utente user1 = new Utente("Marco", "Neri", "marco.neri@test.it");
        Utente savedUser1 = utenteService.saveUtente(user1);

        Utente user2 = new Utente("Sara", "Gialli", "sara.gialli@test.it");
        Utente savedUser2 = utenteService.saveUtente(user2);

        Recensione r1 = new Recensione("Capolavoro sci-fi", "Emozionante e visivamente spettacolare", 5, savedFilm, savedUser1);
        recensioneService.saveRecensione(r1);

        Recensione r2 = new Recensione("Molto bello", "Ottima colonna sonora", 4, savedFilm, savedUser2);
        recensioneService.saveRecensione(r2);

        List<Recensione> reviews = recensioneService.getRecensioniByFilm(savedFilm);
        assertEquals(2, reviews.size());
        assertTrue(recensioneService.hasUserReviewedFilm(savedFilm, savedUser1));
        assertTrue(recensioneService.hasUserReviewedFilm(savedFilm, savedUser2));

        // Media voti (5 + 4) / 2 = 4.5
        savedFilm.setRecensioni(reviews);
        assertEquals(4.5, savedFilm.getMediaVoti());

        // Test statistiche avanzate
        Map<Integer, Integer> dist = savedFilm.getDistribuzioneVoti();
        assertEquals(1, dist.get(5));
        assertEquals(1, dist.get(4));
        assertEquals(0, dist.get(3));

        Map<Integer, Integer> perc = savedFilm.getPercentualiVoti();
        assertEquals(50, perc.get(5));
        assertEquals(50, perc.get(4));

        assertEquals(100, savedFilm.getTassoGradimento());
    }
}
