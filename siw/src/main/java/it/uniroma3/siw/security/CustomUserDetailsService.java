package it.uniroma3.siw.security;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.repository.CredentialsRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CredentialsRepository credentialsRepository;

    public CustomUserDetailsService(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Credentials credentials = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con username: " + username));

        String role = credentials.getRole() != null ? credentials.getRole().toUpperCase() : Credentials.DEFAULT_ROLE;
        String roleAuth = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        String rawAuth = role.startsWith("ROLE_") ? role.substring(5) : role;

        return User.builder()
                .username(credentials.getUsername())
                .password(credentials.getPassword())
                .authorities(roleAuth, rawAuth)
                .build();
    }
}
