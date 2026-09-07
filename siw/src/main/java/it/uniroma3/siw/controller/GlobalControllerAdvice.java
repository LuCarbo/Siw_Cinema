package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.service.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CredentialsService credentialsService;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @ModelAttribute("currentCredentials")
    public Credentials getCurrentCredentials() {
        return credentialsService.getCurrentCredentials();
    }

    @ModelAttribute("currentUser")
    public Utente getCurrentUser() {
        return credentialsService.getCurrentUser();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Credentials creds = credentialsService.getCurrentCredentials();
        return creds != null && creds.isAdmin();
    }

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn() {
        return credentialsService.getCurrentCredentials() != null;
    }

    @ModelAttribute("googleAuthEnabled")
    public boolean isGoogleAuthEnabled() {
        return clientRegistrationRepository != null;
    }
}
