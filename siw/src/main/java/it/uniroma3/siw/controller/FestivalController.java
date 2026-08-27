package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.service.FestivalService;
import it.uniroma3.siw.service.FilmService;
import it.uniroma3.siw.validator.FestivalValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;
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
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                Model model) {
        int pageSize = 6;
        Page<Festival> festivalPage = festivalService.getFestivalsPaginated(citta, nome, page, pageSize);

        model.addAttribute("festivals", festivalPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", festivalPage.getTotalPages());
        model.addAttribute("totalElements", festivalPage.getTotalElements());
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
                               @RequestParam(value = "immagineFile", required = false) MultipartFile immagineFile,
                               Model model) {
        festivalValidator.validate(festival, bindingResult);

        if (bindingResult.hasErrors()) {
            return "festival/form";
        }

        if (immagineFile != null && !immagineFile.isEmpty()) {
            try {
                String mimeType = (immagineFile.getContentType() != null && !immagineFile.getContentType().isEmpty()) 
                        ? immagineFile.getContentType() : "image/jpeg";
                String base64 = Base64.getEncoder().encodeToString(immagineFile.getBytes());
                festival.setImmagine("data:" + mimeType + ";base64," + base64);
            } catch (Exception ignored) {
            }
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
