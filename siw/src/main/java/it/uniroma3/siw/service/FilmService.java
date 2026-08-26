package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Regista;
import it.uniroma3.siw.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FilmService {

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private RegistaService registaService;

    @Transactional(readOnly = true)
    public Film getFilm(Long id) {
        return filmRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Film> getAllFilms() {
        return filmRepository.findByOrderByTitoloAsc();
    }

    @Transactional(readOnly = true)
    public List<Film> searchByTitolo(String titolo) {
        if (titolo == null || titolo.trim().isEmpty()) {
            return getAllFilms();
        }
        return filmRepository.findByTitoloContainingIgnoreCase(titolo.trim());
    }

    @Transactional(readOnly = true)
    public List<Film> filterByGenere(String genere) {
        if (genere == null || genere.trim().isEmpty()) {
            return getAllFilms();
        }
        return filmRepository.findByGenereIgnoreCase(genere.trim());
    }

    @Transactional(readOnly = true)
    public List<Film> filterByAnno(Integer anno) {
        if (anno == null) {
            return getAllFilms();
        }
        return filmRepository.findByAnno(anno);
    }

    @Transactional(readOnly = true)
    public List<Film> getFilmsNotInFestival(Long festivalId) {
        return filmRepository.findFilmsNotInFestival(festivalId);
    }

    @Transactional
    public Film saveFilm(Film film) {
        return filmRepository.save(film);
    }

    @Transactional
    public Film saveFilmWithRegista(Film film, Long registaId) {
        if (registaId != null) {
            Regista regista = registaService.getRegista(registaId);
            film.setRegista(regista);
        } else {
            film.setRegista(null);
        }
        return filmRepository.save(film);
    }

    @Transactional
    public void deleteFilm(Long id) {
        Film film = getFilm(id);
        if (film != null) {
            // Rimuovi associazioni con i festival
            for (Festival festival : film.getFestival()) {
                festival.getFilm().remove(film);
            }
            filmRepository.deleteById(id);
        }
    }
}
