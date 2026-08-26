package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.RecensioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class ProfiloController {

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private RecensioneService recensioneService;

    @GetMapping("/profilo")
    public String showProfile(Model model) {
        Utente currentUser = credentialsService.getCurrentUser();
        Credentials creds = credentialsService.getCurrentCredentials();

        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Recensione> recensioni = recensioneService.getRecensioniByAutore(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("credentials", creds);
        model.addAttribute("recensioni", recensioni);
        return "auth/profile";
    }
}
