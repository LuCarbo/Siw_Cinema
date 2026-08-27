package it.uniroma3.siw;

import it.uniroma3.siw.model.*;
import it.uniroma3.siw.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProiezioneTransactionTest {

    @Autowired
    private ProiezioneService proiezioneService;

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private SalaService salaService;

    @Test
    void testAtomicScheduleProjectionSuccess() {
        Festival festival = new Festival("Festival Transazionale", 2026, "Milano",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10), "Descrizione");
        Festival savedFestival = festivalService.saveFestival(festival);

        Film film = new Film("Film Atomico", 2024, 110, "Azione", "Italia");
        Film savedFilm = filmService.saveFilm(film);

        Sala sala = new Sala("Sala Atomica", "Via Milano 1", 200);
        Sala savedSala = salaService.saveSala(sala);

        // Inizialmente il film non è nel festival
        assertFalse(savedFestival.getFilm().contains(savedFilm));

        Proiezione proiezione = new Proiezione();
        proiezione.setData(LocalDate.of(2026, 9, 5));
        proiezione.setOra(LocalTime.of(20, 0));

        // Esecuzione atomica del caso d'uso
        Proiezione saved = proiezioneService.programmaNuovaProiezione(proiezione, savedFestival.getId(), savedFilm.getId(), savedSala.getId());

        assertNotNull(saved.getId());
        assertEquals(savedFestival, saved.getFestival());
        assertEquals(savedFilm, saved.getFilm());
        assertEquals(savedSala, saved.getSala());

        // Verifica che l'associazione Film-Festival sia stata aggiornata coerentemente
        Festival updatedFestival = festivalService.getFestival(savedFestival.getId());
        assertTrue(updatedFestival.getFilm().contains(savedFilm));

        // Test ricerca per data
        List<Proiezione> proiezioniPerData = proiezioneService.searchProiezioni(null, null, null, LocalDate.of(2026, 9, 5));
        assertFalse(proiezioniPerData.isEmpty());
        assertTrue(proiezioniPerData.contains(saved));

        List<Proiezione> proiezioniAltraData = proiezioneService.searchProiezioni(null, null, null, LocalDate.of(2026, 9, 6));
        assertFalse(proiezioniAltraData.contains(saved));
    }

    @Test
    void testAtomicRollbackOnDateMismatch() {
        Festival festival = new Festival("Festival Date", 2026, "Milano",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10), "Descrizione");
        Festival savedFestival = festivalService.saveFestival(festival);

        Film film = new Film("Film Fuori Data", 2024, 110, "Azione", "Italia");
        Film savedFilm = filmService.saveFilm(film);

        Sala sala = new Sala("Sala Date", "Via Milano 2", 200);
        Sala savedSala = salaService.saveSala(sala);

        Proiezione proiezione = new Proiezione();
        proiezione.setData(LocalDate.of(2026, 8, 15)); // Data non valida (prima del festival)
        proiezione.setOra(LocalTime.of(20, 0));

        Long festId = savedFestival.getId();
        Long filmId = savedFilm.getId();
        Long salaId = savedSala.getId();

        // L'operazione deve fallire con eccezione e fare rollback
        assertThrows(IllegalStateException.class, () -> {
            proiezioneService.programmaNuovaProiezione(proiezione, festId, filmId, salaId);
        });

        // Verifica consistenza: il film non deve essere stato aggiunto al festival
        Festival updatedFestival = festivalService.getFestival(festId);
        assertFalse(updatedFestival.getFilm().contains(savedFilm));
    }
}
