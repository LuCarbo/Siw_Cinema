package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.repository.CredentialsRepository;
import it.uniroma3.siw.repository.UtenteRepository;
import it.uniroma3.siw.security.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CredentialsService {

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Credentials getCredentials(Long id) {
        return credentialsRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Credentials getCredentials(String username) {
        return credentialsRepository.findByUsername(username).orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<Credentials> getCredentialsByUser(Utente user) {
        return credentialsRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Credentials> getAllCredentials() {
        return credentialsRepository.findAll();
    }

    @Transactional
    public Credentials saveCredentials(Credentials credentials) {
        if (credentials.getRole() == null) {
            credentials.setRole(Credentials.DEFAULT_ROLE);
        }
        credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
        return credentialsRepository.save(credentials);
    }

    @Transactional
    public Credentials updateCredentials(Credentials credentials) {
        return credentialsRepository.save(credentials);
    }

    @Transactional(readOnly = true)
    public Credentials getCurrentCredentials() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof CustomOAuth2User customOAuth2User) {
            username = customOAuth2User.getUsername();
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            username = (email != null && !email.trim().isEmpty()) ? email : oauth2User.getName();
        } else {
            username = principal.toString();
        }

        Credentials credentials = getCredentials(username);
        if (credentials == null && principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null && !email.trim().isEmpty()) {
                Optional<Utente> userOpt = utenteRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    credentials = credentialsRepository.findByUser(userOpt.get()).orElse(null);
                }
            }
        }
        return credentials;
    }

    @Transactional(readOnly = true)
    public Utente getCurrentUser() {
        Credentials creds = getCurrentCredentials();
        return (creds != null) ? creds.getUser() : null;
    }
}
