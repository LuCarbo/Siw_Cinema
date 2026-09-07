package it.uniroma3.siw.security;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.repository.CredentialsRepository;
import it.uniroma3.siw.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service per l'elaborazione dei profili utente restituiti da Google OAuth2.
 * Esegue il provisioning automatico o il collegamento all'account Utente e Credentials locale,
 * assegnando rigorosamente il ruolo predefinito (USER) per prevenire privilege escalation.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");
        String name = oAuth2User.getAttribute("name");
        String sub = oAuth2User.getAttribute("sub");

        if (email == null || email.trim().isEmpty()) {
            email = sub != null ? sub + "@google.oauth" : "utente_" + UUID.randomUUID().toString().substring(0, 8) + "@google.oauth";
        }
        email = email.trim().toLowerCase();

        if (givenName == null || givenName.trim().isEmpty()) {
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.trim().split("\\s+", 2);
                givenName = parts[0];
                if (familyName == null && parts.length > 1) {
                    familyName = parts[1];
                }
            } else {
                givenName = "Utente";
            }
        }

        if (familyName == null || familyName.trim().isEmpty()) {
            familyName = "Google";
        }

        // Cerca se esiste già un Utente locale associato a questa email
        Optional<Utente> optionalUtente = utenteRepository.findByEmail(email);
        Utente utente;
        Credentials credentials;

        if (optionalUtente.isPresent()) {
            utente = optionalUtente.get();
            Optional<Credentials> optionalCredentials = credentialsRepository.findByUser(utente);
            if (optionalCredentials.isPresent()) {
                credentials = optionalCredentials.get();
            } else {
                String username = generateUniqueUsername(email, sub);
                credentials = new Credentials(username, passwordEncoder.encode(UUID.randomUUID().toString()), Credentials.DEFAULT_ROLE, utente);
                credentials = credentialsRepository.save(credentials);
            }
        } else {
            utente = new Utente(givenName, familyName, email);
            utente = utenteRepository.save(utente);

            String username = generateUniqueUsername(email, sub);
            credentials = new Credentials(username, passwordEncoder.encode(UUID.randomUUID().toString()), Credentials.DEFAULT_ROLE, utente);
            credentials = credentialsRepository.save(credentials);
        }

        // Assegna autorità coerenti con la SecurityFilterChain
        Set<GrantedAuthority> authorities = new HashSet<>();
        String role = credentials.getRole() != null ? credentials.getRole() : Credentials.DEFAULT_ROLE;
        authorities.add(new SimpleGrantedAuthority(role));
        if (!role.startsWith("ROLE_")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        // Aggiunge USER / DEFAULT per accesso uniforme
        authorities.add(new SimpleGrantedAuthority("USER"));

        return new CustomOAuth2User(oAuth2User, credentials, authorities);
    }

    private String generateUniqueUsername(String email, String sub) {
        String base = email;
        if (base.length() > 50) {
            base = base.substring(0, 50);
        }

        if (!credentialsRepository.existsByUsername(base)) {
            return base;
        }

        String suffix = "_" + (sub != null && sub.length() >= 4 ? sub.substring(sub.length() - 4) : UUID.randomUUID().toString().substring(0, 4));
        int maxBaseLength = 50 - suffix.length();
        if (base.length() > maxBaseLength) {
            base = base.substring(0, maxBaseLength);
        }

        String candidate = base + suffix;
        if (!credentialsRepository.existsByUsername(candidate)) {
            return candidate;
        }

        // Ulteriore fallback con timestamp breve
        String timeSuffix = "_" + (System.currentTimeMillis() % 10000);
        int maxLen = 50 - timeSuffix.length();
        return (base.length() > maxLen ? base.substring(0, maxLen) : base) + timeSuffix;
    }
}
