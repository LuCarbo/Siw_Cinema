package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.repository.CredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CredentialsService {

    @Autowired
    private CredentialsRepository credentialsRepository;

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
        } else {
            username = principal.toString();
        }
        return getCredentials(username);
    }

    @Transactional(readOnly = true)
    public Utente getCurrentUser() {
        Credentials creds = getCurrentCredentials();
        return (creds != null) ? creds.getUser() : null;
    }
}
