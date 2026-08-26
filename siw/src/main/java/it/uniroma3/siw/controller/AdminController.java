package it.uniroma3.siw.controller;

import it.uniroma3.siw.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private RegistaService registaService;

    @Autowired
    private SalaService salaService;

    @Autowired
    private ProiezioneService proiezioneService;

    @Autowired
    private RecensioneService recensioneService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("festivals", festivalService.getAllFestivals());
        model.addAttribute("films", filmService.getAllFilms());
        model.addAttribute("registi", registaService.getAllRegisti());
        model.addAttribute("sale", salaService.getAllSale());
        model.addAttribute("proiezioni", proiezioneService.getAllProiezioni());

        return "admin/dashboard";
    }
}
