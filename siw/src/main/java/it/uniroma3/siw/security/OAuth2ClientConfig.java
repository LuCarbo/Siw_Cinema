package it.uniroma3.siw.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class OAuth2ClientConfig {

    @Bean
    @Conditional(GoogleOAuthCondition.class)
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${google.oauth.client-id:}") String clientId,
            @Value("${google.oauth.client-secret:}") String clientSecret,
            @Value("${google.oauth.redirect-uri:http://localhost:8080/login/oauth2/code/google}") String redirectUri) {

        ClientRegistration googleRegistration = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId.trim())
                .clientSecret(clientSecret.trim())
                .redirectUri(redirectUri.trim())
                .scope("email", "profile")
                .build();

        return new InMemoryClientRegistrationRepository(googleRegistration);
    }
}
