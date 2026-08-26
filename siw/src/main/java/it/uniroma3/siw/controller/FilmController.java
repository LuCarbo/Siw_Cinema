package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.FilmService;
import it.uniroma3.siw.service.RecensioneService;
import it.uniroma3.siw.service.RegistaService;
import it.uniroma3.siw.validator.FilmValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Controller
public class FilmController {

    @Autowired
    private FilmService filmService;

    @Autowired
    private RegistaService registaService;

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private FilmValidator filmValidator;

    @GetMapping("/films")
    public String listFilms(@RequestParam(value = "titolo", required = false) String titolo,
                            @RequestParam(value = "genere", required = false) String genere,
                            @RequestParam(value = "anno", required = false) Integer anno,
                            Model model) {
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

        model.addAttribute("films", films);
        model.addAttribute("titolo", titolo);
        model.addAttribute("genere", genere);
        model.addAttribute("anno", anno);
        return "film/list";
    }

    @GetMapping("/film/{id}")
    public String getFilm(@PathVariable("id") Long id, Model model) {
        Film film = filmService.getFilm(id);
        if (film == null) {
            return "redirect:/films";
        }
        model.addAttribute("film", film);
        model.addAttribute("recensioni", recensioneService.getRecensioniByFilm(film));

        // Controlla se l'utente loggato ha già recensito il film
        Utente currentUser = credentialsService.getCurrentUser();
        if (currentUser != null) {
            Optional<Recensione> userReview = recensioneService.getRecensioneByUserAndFilm(film, currentUser);
            model.addAttribute("hasReviewed", userReview.isPresent());
            model.addAttribute("userReview", userReview.orElse(null));
        } else {
            model.addAttribute("hasReviewed", false);
            model.addAttribute("userReview", null);
        }

        return "film/detail";
    }

    @GetMapping("/films/nuovo")
    public String showCreateFilmForm(Model model) {
        model.addAttribute("film", new Film());
        model.addAttribute("registi", registaService.getAllRegisti());
        return "film/form";
    }

    @PostMapping("/films/salva")
    public String saveFilm(@Valid @ModelAttribute("film") Film film,
                           BindingResult bindingResult,
                           @RequestParam(value = "registaId", required = false) Long registaId,
                           Model model) {
        filmValidator.validate(film, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("registi", registaService.getAllRegisti());
            return "film/form";
        }

        filmService.saveFilmWithRegista(film, registaId);
        return "redirect:/film/" + film.getId();
    }

    @GetMapping("/films/modifica/{id}")
    public String showEditFilmForm(@PathVariable("id") Long id, Model model) {
        Film film = filmService.getFilm(id);
        if (film == null) {
            return "redirect:/films";
        }
        model.addAttribute("film", film);
        model.addAttribute("registi", registaService.getAllRegisti());
        return "film/form";
    }
}
