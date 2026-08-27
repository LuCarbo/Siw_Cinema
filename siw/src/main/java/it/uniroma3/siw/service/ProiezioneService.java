package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Proiezione;
import it.uniroma3.siw.model.Sala;
import it.uniroma3.siw.model.StatoProiezione;
import it.uniroma3.siw.repository.FestivalRepository;
import it.uniroma3.siw.repository.FilmRepository;
import it.uniroma3.siw.repository.ProiezioneRepository;
import it.uniroma3.siw.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProiezioneService {

    @Autowired
    private ProiezioneRepository proiezioneRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private SalaRepository salaRepository;

    // ==========================================
    // OPERAZIONI DI SOLA LETTURA (readOnly = true)
    // ==========================================

    @Transactional(readOnly = true)
    public Proiezione getProiezione(Long id) {
        return proiezioneRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Proiezione> getAllProiezioni() {
        return proiezioneRepository.findByOrderByDataAscOraAsc();
    }

    @Transactional(readOnly = true)
    public List<Proiezione> getProiezioniByData(LocalDate data) {
        if (data == null) {
            return getAllProiezioni();
        }
        return proiezioneRepository.findByDataOrderByOraAsc(data);
    }

    @Transactional(readOnly = true)
    public List<Proiezione> searchProiezioni(Long festivalId, Long filmId, Long salaId, LocalDate data) {
        List<Proiezione> list = getAllProiezioni();
        return list.stream()
                .filter(p -> festivalId == null || (p.getFestival() != null && p.getFestival().getId().equals(festivalId)))
                .filter(p -> filmId == null || (p.getFilm() != null && p.getFilm().getId().equals(filmId)))
                .filter(p -> salaId == null || (p.getSala() != null && p.getSala().getId().equals(salaId)))
                .filter(p -> data == null || (p.getData() != null && p.getData().equals(data)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Proiezione> getUpcomingProiezioni() {
        return proiezioneRepository.findByDataGreaterThanEqualOrderByDataAscOraAsc(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Proiezione> getProiezioniByFestival(Festival festival) {
        return proiezioneRepository.findByFestivalOrderByDataAscOraAsc(festival);
    }

    @Transactional(readOnly = true)
    public List<Proiezione> getProiezioniByFilm(Film film) {
        return proiezioneRepository.findByFilmOrderByDataAscOraAsc(film);
    }

    @Transactional(readOnly = true)
    public List<Proiezione> getProiezioniBySala(Sala sala) {
        return proiezioneRepository.findBySalaOrderByDataAscOraAsc(sala);
    }

    // ==========================================
    // OPERAZIONI DI AGGIORNAMENTO / SCRITTURA
    // ==========================================

    /**
     * Caso d'uso complesso multi-entità: Programmazione di una nuova Proiezione.
     * Coordina 4 repository (Festival, Film, Sala, Proiezione) e aggiorna lo stato in modo atomico.
     * Livello di isolamento READ_COMMITTED per prevenire dirty reads.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Proiezione programmaNuovaProiezione(Proiezione proiezione, Long festivalId, Long filmId, Long salaId) {
        // 1. Recupero del festival
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("Festival non trovato con ID: " + festivalId));

        // 2. Recupero del film
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new IllegalArgumentException("Film non trovato con ID: " + filmId));

        // 3. Recupero della sala
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala non trovata con ID: " + salaId));

        // 4. Verifica date rispetto al festival
        if (proiezione.getData().isBefore(festival.getDataInizio()) || proiezione.getData().isAfter(festival.getDataFine())) {
            throw new IllegalStateException("La data della proiezione (" + proiezione.getData() + 
                    ") non rientra nelle date del festival (" + festival.getDataInizio() + " - " + festival.getDataFine() + ")");
        }

        // 5. Verifica disponibilità della sala (assenza conflitti)
        List<Proiezione> conflitti = proiezioneRepository.findConflictingProjections(
                sala, proiezione.getData(), proiezione.getOra(), proiezione.getId());
        if (!conflitti.isEmpty()) {
            throw new IllegalStateException("La sala " + sala.getNome() + " è già occupata per la data " + 
                    proiezione.getData() + " alle ore " + proiezione.getOra());
        }

        // 6. Aggiornamento associazione Film-Festival se non ancora presente
        if (!festival.getFilm().contains(film)) {
            festival.addFilm(film);
            festivalRepository.save(festival);
        }

        // 7. Impostazione e salvataggio atomico della proiezione
        proiezione.setFestival(festival);
        proiezione.setFilm(film);
        proiezione.setSala(sala);
        if (proiezione.getStato() == null) {
            proiezione.setStato(StatoProiezione.SCHEDULED);
        }

        return proiezioneRepository.save(proiezione);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Proiezione saveProiezione(Proiezione proiezione) {
        Festival festival = proiezione.getFestival();
        Film film = proiezione.getFilm();
        if (festival != null && film != null && !festival.getFilm().contains(film)) {
            festival.addFilm(film);
            festivalRepository.save(festival);
        }
        return proiezioneRepository.save(proiezione);
    }

    @Transactional
    public Proiezione updateStato(Long id, StatoProiezione stato) {
        Proiezione proiezione = getProiezione(id);
        if (proiezione != null) {
            proiezione.setStato(stato);
            return proiezioneRepository.save(proiezione);
        }
        return null;
    }

    @Transactional
    public void deleteProiezione(Long id) {
        proiezioneRepository.deleteById(id);
    }
}
