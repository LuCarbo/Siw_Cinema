package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Proiezione;
import it.uniroma3.siw.service.FestivalService;
import it.uniroma3.siw.service.FilmService;
import it.uniroma3.siw.service.ProiezioneService;
import it.uniroma3.siw.service.RegistaService;
import it.uniroma3.siw.service.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private ProiezioneService proiezioneService;

    @Autowired
    private RegistaService registaService;

    @Autowired
    private SalaService salaService;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        List<Festival> allFestivals = festivalService.getAllFestivals();
        List<Film> allFilms = filmService.getAllFilms();
        List<Proiezione> upcomingProiezioni = proiezioneService.getUpcomingProiezioni();

        model.addAttribute("festivals", allFestivals.size() > 3 ? allFestivals.subList(0, 3) : allFestivals);
        model.addAttribute("films", allFilms.size() > 6 ? allFilms.subList(0, 6) : allFilms);
        model.addAttribute("proiezioni", upcomingProiezioni.size() > 5 ? upcomingProiezioni.subList(0, 5) : upcomingProiezioni);

        // Statistiche
        model.addAttribute("numFestivals", allFestivals.size());
        model.addAttribute("numFilms", allFilms.size());
        model.addAttribute("numRegisti", registaService.getAllRegisti().size());
        model.addAttribute("numSale", salaService.getAllSale().size());

        return "index";
    }
}
