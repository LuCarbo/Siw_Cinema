package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.validator.CredentialsValidator;
import it.uniroma3.siw.validator.UtenteValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private CredentialsValidator credentialsValidator;

    @Autowired
    private UtenteValidator utenteValidator;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "registered", required = false) String registered,
                                Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Credenziali non valide o autenticazione annullata. Riprova.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registrazione completata con successo! Ora puoi effettuare il login.");
        }
        model.addAttribute("googleAuthEnabled", clientRegistrationRepository != null);
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("utente", new Utente());
        model.addAttribute("credentials", new Credentials());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("utente") Utente utente,
                               BindingResult utenteBindingResult,
                               @Valid @ModelAttribute("credentials") Credentials credentials,
                               BindingResult credentialsBindingResult,
                               Model model) {

        this.utenteValidator.validate(utente, utenteBindingResult);
        this.credentialsValidator.validate(credentials, credentialsBindingResult);

        if (utenteBindingResult.hasErrors() || credentialsBindingResult.hasErrors()) {
            return "auth/register";
        }

        credentials.setUser(utente);
        credentials.setRole(Credentials.DEFAULT_ROLE);
        this.credentialsService.saveCredentials(credentials);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/success")
    public String defaultAfterLogin() {
        Credentials creds = this.credentialsService.getCurrentCredentials();
        if (creds != null && creds.isAdmin()) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/";
    }
}
