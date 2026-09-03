package it.uniroma3.siw;

import it.uniroma3.siw.model.*;
import it.uniroma3.siw.service.*;
import it.uniroma3.siw.validator.ProiezioneValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProiezioneValidatorTest {

    @Autowired
    private ProiezioneValidator proiezioneValidator;

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private SalaService salaService;

    @Autowired
    private ProiezioneService proiezioneService;

    @Test
    void testProiezioneDateMismatchWithFestival() {
        Festival festival = new Festival("Festival Test", 2026, "Roma",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), "Descrizione test");
        festival = festivalService.saveFestival(festival);

        Film film = filmService.getAllFilms().getFirst();
        Sala sala = salaService.getAllSale().getFirst();

        // Data prima dell'inizio del festival
        Proiezione pInvalid = new Proiezione(LocalDate.of(2026, 5, 25), LocalTime.of(20, 0), StatoProiezione.SCHEDULED, festival, film, sala);
        Errors errors = new BeanPropertyBindingResult(pInvalid, "proiezione");
        proiezioneValidator.validate(pInvalid, errors);
        assertTrue(errors.hasFieldErrors("data"));

        // Data corretta all'interno del festival
        Proiezione pValid = new Proiezione(LocalDate.of(2026, 6, 5), LocalTime.of(20, 0), StatoProiezione.SCHEDULED, festival, film, sala);
        Errors errorsValid = new BeanPropertyBindingResult(pValid, "proiezione");
        proiezioneValidator.validate(pValid, errorsValid);
        assertFalse(errorsValid.hasErrors());
    }

    @Test
    void testProiezioneRoomConflict() {
        Festival festival = festivalService.getAllFestivals().getFirst();
        Film film = filmService.getAllFilms().getFirst();
        Sala sala = salaService.getAllSale().getFirst();

        LocalDate data = festival.getDataInizio();
        LocalTime ora = LocalTime.of(15, 0);

        Proiezione p1 = new Proiezione(data, ora, StatoProiezione.SCHEDULED, festival, film, sala);
        proiezioneService.saveProiezione(p1);

        // Altra proiezione nella stessa sala alla stessa ora e data
        Proiezione p2Conflict = new Proiezione(data, ora, StatoProiezione.SCHEDULED, festival, film, sala);
        Errors errors = new BeanPropertyBindingResult(p2Conflict, "proiezione");
        proiezioneValidator.validate(p2Conflict, errors);

        assertTrue(errors.hasErrors());
    }

    @Test
    void testProiezioneRoomDurationOverlapConflict() {
        Festival festival = festivalService.getAllFestivals().getFirst();
        // Creiamo due film con durata nota: film1 (120 min), film2 (90 min)
        Film film1 = new Film("Film Test 1", 2024, 120, "Drammatico", "Italia");
        film1 = filmService.saveFilm(film1);

        Film film2 = new Film("Film Test 2", 2024, 90, "Commedia", "Italia");
        film2 = filmService.saveFilm(film2);

        Sala sala = salaService.getAllSale().getFirst();
        LocalDate data = festival.getDataInizio();

        // Proiezione 1: 20:00 - 22:00 (durata 120 min)
        Proiezione p1 = new Proiezione(data, LocalTime.of(20, 0), StatoProiezione.SCHEDULED, festival, film1, sala);
        proiezioneService.saveProiezione(p1);

        // Proiezione 2: inizio alle 20:45 (in piena sovrapposizione temporale con film1)
        Proiezione p2Overlap = new Proiezione(data, LocalTime.of(20, 45), StatoProiezione.SCHEDULED, festival, film2, sala);
        Errors errorsOverlap = new BeanPropertyBindingResult(p2Overlap, "proiezione");
        proiezioneValidator.validate(p2Overlap, errorsOverlap);
        assertTrue(errorsOverlap.hasErrors(), "La proiezione deve essere rifiutata per sovrapposizione con la durata del film precedente!");

        // Proiezione 3: inizio alle 22:30 (dopo la fine di film1, nessuna sovrapposizione)
        Proiezione p3NoOverlap = new Proiezione(data, LocalTime.of(22, 30), StatoProiezione.SCHEDULED, festival, film2, sala);
        Errors errorsNoOverlap = new BeanPropertyBindingResult(p3NoOverlap, "proiezione");
        proiezioneValidator.validate(p3NoOverlap, errorsNoOverlap);
        assertFalse(errorsNoOverlap.hasErrors(), "La proiezione alle 22:30 non deve presentare conflitti!");
    }
}
