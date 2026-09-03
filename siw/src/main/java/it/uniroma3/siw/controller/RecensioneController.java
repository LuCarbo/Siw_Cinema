package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.FilmService;
import it.uniroma3.siw.service.RecensioneService;
import it.uniroma3.siw.validator.RecensioneValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/recensioni")
public class RecensioneController {

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private RecensioneValidator recensioneValidator;

    @GetMapping("/nuova/{filmId}")
    public String showCreateRecensioneForm(@PathVariable("filmId") Long filmId, Model model) {
        Film film = filmService.getFilm(filmId);
        if (film == null) {
            return "redirect:/films";
        }

        Utente currentUser = credentialsService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Se l'utente ha già recensito il film, reindirizza alla modifica
        Optional<Recensione> existing = recensioneService.getRecensioneByUserAndFilm(film, currentUser);
        if (existing.isPresent()) {
            return "redirect:/recensioni/modifica/" + existing.get().getId();
        }

        Recensione recensione = new Recensione();
        recensione.setFilm(film);
        recensione.setAutore(currentUser);

        model.addAttribute("recensione", recensione);
        model.addAttribute("film", film);
        return "recensione/form";
    }

    @PostMapping("/salva")
    public String saveRecensione(@ModelAttribute("recensione") Recensione recensione,
                                 BindingResult bindingResult,
                                 @RequestParam("filmId") Long filmId,
                                 Model model) {

        Film film = filmService.getFilm(filmId);
        if (film == null) {
            return "redirect:/films";
        }

        Utente currentUser = credentialsService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Se l'id è 0 o negativo, trattalo come nuovo inserimento
        if (recensione.getId() != null && recensione.getId() <= 0) {
            recensione.setId(null);
        }

        // Controllo di sicurezza: se si modifica una recensione esistente, verificare che appartenga all'utente
        if (recensione.getId() != null) {
            Recensione existing = recensioneService.getRecensione(recensione.getId());
            if (existing == null) {
                return "redirect:/films";
            }
            Credentials creds = credentialsService.getCurrentCredentials();
            boolean isAdmin = creds != null && creds.isAdmin();
            if (!recensioneService.canUserModify(existing, currentUser, isAdmin)) {
                return "redirect:/film/" + filmId;
            }
        }

        recensione.setFilm(film);
        recensione.setAutore(currentUser);

        recensioneValidator.validate(recensione, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("film", film);
            return "recensione/form";
        }

        recensioneService.saveRecensione(recensione);
        return "redirect:/film/" + filmId;
    }

    @GetMapping("/modifica/{id}")
    public String showEditRecensioneForm(@PathVariable("id") Long id, Model model) {
        Recensione recensione = recensioneService.getRecensione(id);
        if (recensione == null) {
            return "redirect:/films";
        }

        Utente currentUser = credentialsService.getCurrentUser();
        Credentials creds = credentialsService.getCurrentCredentials();
        boolean isAdmin = creds != null && creds.isAdmin();

        if (!recensioneService.canUserModify(recensione, currentUser, isAdmin)) {
            return "redirect:/film/" + recensione.getFilm().getId();
        }

        model.addAttribute("recensione", recensione);
        model.addAttribute("film", recensione.getFilm());
        return "recensione/form";
    }

    @PostMapping("/elimina/{id}")
    public String deleteRecensionePost(@PathVariable("id") Long id) {
        return deleteRecensione(id);
    }

    @GetMapping("/elimina/{id}")
    public String deleteRecensione(@PathVariable("id") Long id) {
        Recensione recensione = recensioneService.getRecensione(id);
        if (recensione == null) {
            return "redirect:/films";
        }

        Long filmId = recensione.getFilm().getId();
        Utente currentUser = credentialsService.getCurrentUser();
        Credentials creds = credentialsService.getCurrentCredentials();
        boolean isAdmin = creds != null && creds.isAdmin();

        recensioneService.deleteRecensioneIfAuthorized(id, currentUser, isAdmin);
        return "redirect:/film/" + filmId;
    }
}
