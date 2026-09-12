package it.uniroma3.siw.security;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.repository.CredentialsRepository;
import it.uniroma3.siw.repository.UtenteRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UtenteRepository utenteRepository;
    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UtenteRepository utenteRepository,
                                   CredentialsRepository credentialsRepository,
                                   PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.credentialsRepository = credentialsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");
        String name = oAuth2User.getAttribute("name");

        if (email == null || email.trim().isEmpty()) {
            email = oAuth2User.getName() + "@google.oauth";
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
                credentials = new Credentials(email, passwordEncoder.encode(UUID.randomUUID().toString()), Credentials.DEFAULT_ROLE, utente);
                credentials = credentialsRepository.save(credentials);
            }
        } else {
            utente = new Utente(givenName, familyName, email);
            utente = utenteRepository.save(utente);

            credentials = new Credentials(email, passwordEncoder.encode(UUID.randomUUID().toString()), Credentials.DEFAULT_ROLE, utente);
            credentials = credentialsRepository.save(credentials);
        }

        String role = credentials.getRole() != null ? credentials.getRole().toUpperCase() : Credentials.DEFAULT_ROLE;
        String roleAuth = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        String rawAuth = role.startsWith("ROLE_") ? role.substring(5) : role;

        Set<GrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities());
        authorities.add(new SimpleGrantedAuthority(roleAuth));
        authorities.add(new SimpleGrantedAuthority(rawAuth));
        authorities.add(new SimpleGrantedAuthority("USER"));

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        if (userNameAttributeName == null || userNameAttributeName.isEmpty()) {
            userNameAttributeName = "sub";
        }

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), userNameAttributeName);
    }
}
