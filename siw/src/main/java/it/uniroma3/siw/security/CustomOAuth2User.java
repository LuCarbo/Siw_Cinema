package it.uniroma3.siw.security;

import it.uniroma3.siw.model.Credentials;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Principal personalizzato per utenti autenticati tramite OAuth2 (Google).
 * Mantiene il riferimento ai dati di dominio (username, ruolo, ID credenziali e utente)
 * evitando la serializzazione di entità JPA detached nella sessione HTTP.
 */
public class CustomOAuth2User implements OAuth2User, Serializable {

    private static final long serialVersionUID = 1L;

    private final OAuth2User oauth2User;
    private final String username;
    private final String role;
    private final Long credentialsId;
    private final Long utenteId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(OAuth2User oauth2User, Credentials credentials, Collection<? extends GrantedAuthority> authorities) {
        this.oauth2User = oauth2User;
        this.username = credentials.getUsername();
        this.role = credentials.getRole();
        this.credentialsId = credentials.getId();
        this.utenteId = (credentials.getUser() != null) ? credentials.getUser().getId() : null;
        this.authorities = authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return this.username;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Long getCredentialsId() {
        return credentialsId;
    }

    public Long getUtenteId() {
        return utenteId;
    }

    public String getEmail() {
        Object emailAttr = oauth2User.getAttributes().get("email");
        return emailAttr != null ? emailAttr.toString() : null;
    }
}
