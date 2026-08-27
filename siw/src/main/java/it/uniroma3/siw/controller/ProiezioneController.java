package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.*;
import it.uniroma3.siw.service.*;
import it.uniroma3.siw.validator.ProiezioneValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ProiezioneController {

    @Autowired
    private ProiezioneService proiezioneService;

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private SalaService salaService;

    @Autowired
    private ProiezioneValidator proiezioneValidator;

    @GetMapping("/proiezioni")
    public String listProiezioni(@RequestParam(value = "festivalId", required = false) Long festivalId,
                                 @RequestParam(value = "filmId", required = false) Long filmId,
                                 @RequestParam(value = "salaId", required = false) Long salaId,
                                 @RequestParam(value = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                 Model model) {
        List<Proiezione> proiezioni = proiezioneService.searchProiezioni(festivalId, filmId, salaId, data);

        model.addAttribute("proiezioni", proiezioni);
        model.addAttribute("festivals", festivalService.getAllFestivals());
        model.addAttribute("films", filmService.getAllFilms());
        model.addAttribute("sale", salaService.getAllSale());
        model.addAttribute("selectedFestivalId", festivalId);
        model.addAttribute("selectedFilmId", filmId);
        model.addAttribute("selectedSalaId", salaId);
        model.addAttribute("selectedData", data);
        return "proiezione/list";
    }

    @GetMapping("/proiezione/{id}")
    public String getProiezione(@PathVariable("id") Long id, Model model) {
        Proiezione proiezione = proiezioneService.getProiezione(id);
        if (proiezione == null) {
            return "redirect:/proiezioni";
        }
        model.addAttribute("proiezione", proiezione);
        return "proiezione/detail";
    }

    @GetMapping("/proiezioni/nuova")
    public String showCreateProiezioneForm(@RequestParam(value = "festivalId", required = false) Long festivalId,
                                           @RequestParam(value = "filmId", required = false) Long filmId,
                                           Model model) {
        Proiezione proiezione = new Proiezione();
        if (festivalId != null) {
            proiezione.setFestival(festivalService.getFestival(festivalId));
        }
        if (filmId != null) {
            proiezione.setFilm(filmService.getFilm(filmId));
        }

        model.addAttribute("proiezione", proiezione);
        model.addAttribute("festivals", festivalService.getAllFestivals());
        model.addAttribute("films", filmService.getAllFilms());
        model.addAttribute("sale", salaService.getAllSale());
        model.addAttribute("stati", StatoProiezione.values());
        return "proiezione/form";
    }

    @PostMapping("/proiezioni/salva")
    public String saveProiezione(@Valid @ModelAttribute("proiezione") Proiezione proiezione,
                                 BindingResult bindingResult,
                                 @RequestParam("festivalId") Long festivalId,
                                 @RequestParam("filmId") Long filmId,
                                 @RequestParam("salaId") Long salaId,
                                 Model model) {

        Festival festival = festivalService.getFestival(festivalId);
        Film film = filmService.getFilm(filmId);
        Sala sala = salaService.getSala(salaId);

        proiezione.setFestival(festival);
        proiezione.setFilm(film);
        proiezione.setSala(sala);

        proiezioneValidator.validate(proiezione, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("festivals", festivalService.getAllFestivals());
            model.addAttribute("films", filmService.getAllFilms());
            model.addAttribute("sale", salaService.getAllSale());
            model.addAttribute("stati", StatoProiezione.values());
            return "proiezione/form";
        }

        proiezioneService.saveProiezione(proiezione);
        return "redirect:/proiezione/" + proiezione.getId();
    }

    @GetMapping("/proiezioni/modifica/{id}")
    public String showEditProiezioneForm(@PathVariable("id") Long id, Model model) {
        Proiezione proiezione = proiezioneService.getProiezione(id);
        if (proiezione == null) {
            return "redirect:/proiezioni";
        }
        model.addAttribute("proiezione", proiezione);
        model.addAttribute("festivals", festivalService.getAllFestivals());
        model.addAttribute("films", filmService.getAllFilms());
        model.addAttribute("sale", salaService.getAllSale());
        model.addAttribute("stati", StatoProiezione.values());
        return "proiezione/form";
    }

    @GetMapping("/proiezioni/elimina/{id}")
    public String deleteProiezione(@PathVariable("id") Long id) {
        proiezioneService.deleteProiezione(id);
        return "redirect:/proiezioni";
    }
}
