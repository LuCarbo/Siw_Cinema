package it.uniroma3.siw.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condizione Spring per abilitare l'integrazione Google OAuth2
 * solo se sia il Client ID sia il Client Secret sono configurati e non vuoti.
 * Garantisce che l'applicazione possa avviarsi regolarmente anche in modalità offline/demo d'esame.
 */
public class GoogleOAuthCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String clientId = context.getEnvironment().getProperty("google.oauth.client-id");
        String clientSecret = context.getEnvironment().getProperty("google.oauth.client-secret");

        return clientId != null && !clientId.trim().isEmpty()
                && clientSecret != null && !clientSecret.trim().isEmpty();
    }
}
