package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.repository.FestivalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FestivalService {

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private FilmService filmService;

    @Transactional(readOnly = true)
    public Festival getFestival(Long id) {
        return festivalRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Festival> getAllFestivals() {
        return festivalRepository.findByOrderByAnnoDescDataInizioDesc();
    }

    @Transactional(readOnly = true)
    public Page<Festival> getFestivalsPaginated(String citta, String nome, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (citta != null && !citta.trim().isEmpty()) {
            return festivalRepository.findByCittaContainingIgnoreCase(citta.trim(), pageable);
        } else if (nome != null && !nome.trim().isEmpty()) {
            return festivalRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        } else {
            return festivalRepository.findByOrderByAnnoDescDataInizioDesc(pageable);
        }
    }

    @Transactional(readOnly = true)
    public List<Festival> getUpcomingFestivals() {
        return festivalRepository.findUpcomingFestivals();
    }

    @Transactional(readOnly = true)
    public List<Festival> searchByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return getAllFestivals();
        }
        return festivalRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    @Transactional(readOnly = true)
    public List<Festival> searchByCitta(String citta) {
        if (citta == null || citta.trim().isEmpty()) {
            return getAllFestivals();
        }
        return festivalRepository.findByCittaContainingIgnoreCase(citta.trim());
    }

    @Transactional
    public Festival saveFestival(Festival festival) {
        return festivalRepository.save(festival);
    }

    @Transactional
    public void addFilmToFestival(Long festivalId, Long filmId) {
        Festival festival = getFestival(festivalId);
        Film film = filmService.getFilm(filmId);
        if (festival != null && film != null) {
            festival.addFilm(film);
            festivalRepository.save(festival);
        }
    }

    @Transactional
    public void removeFilmFromFestival(Long festivalId, Long filmId) {
        Festival festival = getFestival(festivalId);
        Film film = filmService.getFilm(filmId);
        if (festival != null && film != null) {
            festival.removeFilm(film);
            festivalRepository.save(festival);
        }
    }

    @Transactional
    public void deleteFestival(Long id) {
        festivalRepository.deleteById(id);
    }
}
