package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.service.FestivalService;
import it.uniroma3.siw.service.FilmService;
import it.uniroma3.siw.validator.FestivalValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class FestivalController {

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private FestivalValidator festivalValidator;

    @GetMapping("/festivals")
    public String listFestivals(@RequestParam(value = "citta", required = false) String citta,
                                @RequestParam(value = "nome", required = false) String nome,
                                Model model) {
        List<Festival> festivals;
        if (citta != null && !citta.trim().isEmpty()) {
            festivals = festivalService.searchByCitta(citta);
        } else if (nome != null && !nome.trim().isEmpty()) {
            festivals = festivalService.searchByNome(nome);
        } else {
            festivals = festivalService.getAllFestivals();
        }
        model.addAttribute("festivals", festivals);
        model.addAttribute("citta", citta);
        model.addAttribute("nome", nome);
        return "festival/list";
    }

    @GetMapping("/festival/{id}")
    public String getFestival(@PathVariable("id") Long id, Model model) {
        Festival festival = festivalService.getFestival(id);
        if (festival == null) {
            return "redirect:/festivals";
        }
        model.addAttribute("festival", festival);
        return "festival/detail";
    }

    @GetMapping("/festivals/nuovo")
    public String showCreateFestivalForm(Model model) {
        model.addAttribute("festival", new Festival());
        return "festival/form";
    }

    @PostMapping("/festivals/salva")
    public String saveFestival(@Valid @ModelAttribute("festival") Festival festival,
                               BindingResult bindingResult,
                               Model model) {
        festivalValidator.validate(festival, bindingResult);

        if (bindingResult.hasErrors()) {
            return "festival/form";
        }

        festivalService.saveFestival(festival);
        return "redirect:/festival/" + festival.getId();
    }

    @GetMapping("/festivals/modifica/{id}")
    public String showEditFestivalForm(@PathVariable("id") Long id, Model model) {
        Festival festival = festivalService.getFestival(id);
        if (festival == null) {
            return "redirect:/festivals";
        }
        model.addAttribute("festival", festival);
        return "festival/form";
    }

    @GetMapping("/festivals/{id}/gestione-film")
    public String manageFestivalFilms(@PathVariable("id") Long id, Model model) {
        Festival festival = festivalService.getFestival(id);
        if (festival == null) {
            return "redirect:/festivals";
        }
        List<Film> availableFilms = filmService.getFilmsNotInFestival(id);
        model.addAttribute("festival", festival);
        model.addAttribute("availableFilms", availableFilms);
        return "festival/manage-film";
    }

    @PostMapping("/festivals/{id}/film/aggiungi")
    public String addFilmToFestival(@PathVariable("id") Long id, @RequestParam("filmId") Long filmId) {
        festivalService.addFilmToFestival(id, filmId);
        return "redirect:/festivals/" + id + "/gestione-film";
    }

    @PostMapping("/festivals/{id}/film/rimuovi/{filmId}")
    public String removeFilmFromFestival(@PathVariable("id") Long id, @PathVariable("filmId") Long filmId) {
        festivalService.removeFilmFromFestival(id, filmId);
        return "redirect:/festivals/" + id + "/gestione-film";
    }
}
