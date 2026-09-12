package it.uniroma3.siw.service;

import it.uniroma3.siw.exception.DuplicateEntityException;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.repository.RecensioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RecensioneService {

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private FilmService filmService;

    @Transactional(readOnly = true)
    public Recensione getRecensione(Long id) {
        return recensioneRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Recensione> getRecensioniByFilm(Film film) {
        return recensioneRepository.findByFilmOrderByDataDesc(film);
    }

    @Transactional(readOnly = true)
    public List<Recensione> getRecensioniByAutore(Utente autore) {
        return recensioneRepository.findByAutoreOrderByDataDesc(autore);
    }

    @Transactional(readOnly = true)
    public boolean hasUserReviewedFilm(Film film, Utente autore) {
        if (film == null || autore == null) return false;
        return recensioneRepository.existsByFilmAndAutore(film, autore);
    }

    @Transactional(readOnly = true)
    public Optional<Recensione> getRecensioneByUserAndFilm(Film film, Utente autore) {
        if (film == null || autore == null) return Optional.empty();
        return recensioneRepository.findByFilmAndAutore(film, autore);
    }

    @Transactional
    public Recensione saveRecensione(Recensione recensione) {
        if (recensione.getFilm() != null && recensione.getAutore() != null) {
            Optional<Recensione> existing = recensioneRepository.findByFilmAndAutore(recensione.getFilm(), recensione.getAutore());
            if (existing.isPresent()) {
                if (recensione.getId() == null || !existing.get().getId().equals(recensione.getId())) {
                    throw new DuplicateEntityException("Hai già inserito una recensione per questo film. Puoi modificare quella esistente.");
                }
            }
        }
        if (recensione.getData() == null) {
            recensione.setData(LocalDate.now());
        }
        return recensioneRepository.save(recensione);
    }

    @Transactional
    public Recensione createRecensione(Recensione recensione, Long filmId, Utente autore) {
        Film film = filmService.getFilm(filmId);
        recensione.setFilm(film);
        recensione.setAutore(autore);
        return saveRecensione(recensione);
    }

    @Transactional(readOnly = true)
    public boolean canUserModify(Recensione recensione, Utente utente, boolean isAdmin) {
        if (recensione == null || utente == null) return false;
        return isAdmin || (recensione.getAutore() != null && recensione.getAutore().equals(utente));
    }

    @Transactional
    public boolean deleteRecensioneIfAuthorized(Long id, Utente utente, boolean isAdmin) {
        Recensione recensione = getRecensione(id);
        if (recensione != null && canUserModify(recensione, utente, isAdmin)) {
            recensioneRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public void deleteRecensione(Long id) {
        recensioneRepository.deleteById(id);
    }
}
