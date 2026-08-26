package it.uniroma3.siw.controller.rest;

import it.uniroma3.siw.dto.FilmDTO;
import it.uniroma3.siw.dto.RecensioneDTO;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.service.FilmService;
import it.uniroma3.siw.service.RecensioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/api/movies", "/api/films"})
@CrossOrigin(origins = "*")
public class FilmRestController {

    @Autowired
    private FilmService filmService;

    @Autowired
    private RecensioneService recensioneService;

    @GetMapping
    public List<FilmDTO> getAllFilms(@RequestParam(value = "titolo", required = false) String titolo,
                                     @RequestParam(value = "genere", required = false) String genere,
                                     @RequestParam(value = "anno", required = false) Integer anno) {
        List<Film> films;
        if (titolo != null && !titolo.trim().isEmpty()) {
            films = filmService.searchByTitolo(titolo);
        } else if (genere != null && !genere.trim().isEmpty()) {
            films = filmService.filterByGenere(genere);
        } else if (anno != null) {
            films = filmService.filterByAnno(anno);
        } else {
            films = filmService.getAllFilms();
        }
        return films.stream().map(FilmDTO::new).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDTO> getFilm(@PathVariable("id") Long id) {
        Film film = filmService.getFilm(id);
        if (film == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new FilmDTO(film));
    }

    @GetMapping({ "/{id}/reviews", "/{id}/recensioni" })
    public ResponseEntity<List<RecensioneDTO>> getFilmReviews(@PathVariable("id") Long id) {
        Film film = filmService.getFilm(id);
        if (film == null) {
            return ResponseEntity.notFound().build();
        }
        List<RecensioneDTO> recensioni = recensioneService.getRecensioniByFilm(film)
                .stream().map(RecensioneDTO::new).toList();
        return ResponseEntity.ok(recensioni);
    }
}
