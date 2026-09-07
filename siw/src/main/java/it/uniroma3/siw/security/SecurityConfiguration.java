package it.uniroma3.siw.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository,
            @Autowired(required = false) CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // 1. Risorse statiche
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // 2. Endpoint REST API pubblici (GET)
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                // 3. Autenticazione & Registrazione (inclusi percorsi Google OAuth2)
                .requestMatchers("/login", "/register", "/success").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                // 4. Funzionalità riservate ad Amministratori (prima dei pattern generici)
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/festivals/nuovo", "/festivals/salva", "/festivals/modifica/**", "/festivals/*/gestione-film", "/festivals/*/film/**", "/festivals/*/elimina", "/festivals/elimina/**").hasAuthority("ADMIN")
                .requestMatchers("/films/nuovo", "/films/salva", "/films/modifica/**", "/films/*/elimina", "/films/elimina/**").hasAuthority("ADMIN")
                .requestMatchers("/registi/nuovo", "/registi/salva", "/registi/modifica/**", "/registi/*/elimina", "/registi/elimina/**").hasAuthority("ADMIN")
                .requestMatchers("/sale/nuova", "/sale/salva", "/sale/modifica/**", "/sale/*/elimina", "/sale/elimina/**").hasAuthority("ADMIN")
                .requestMatchers("/proiezioni/nuova", "/proiezioni/salva", "/proiezioni/modifica/**", "/proiezioni/elimina/**", "/proiezioni/*/elimina", "/proiezioni/*/stato").hasAuthority("ADMIN")

                // 5. Funzionalità per Utenti Registrati e Admin (Recensioni e Profilo)
                .requestMatchers("/recensioni/**", "/profilo/**").hasAnyAuthority("ADMIN", "USER", "DEFAULT")

                // 6. Pagine pubbliche e di consultazione (GET)
                .requestMatchers(HttpMethod.GET, "/", "/index").permitAll()
                .requestMatchers(HttpMethod.GET, "/festivals", "/festival/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/films", "/film/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/regista/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/sala/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/proiezioni", "/proiezione/**").permitAll()

                // 7. Tutte le altre richieste richiedono autenticazione
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/success", true)
                .failureUrl("/login?error=true")
                .permitAll()
            );

        if (clientRegistrationRepository != null && customOAuth2UserService != null) {
            http.oauth2Login(oauth2 -> oauth2
                .clientRegistrationRepository(clientRegistrationRepository)
                .loginPage("/login")
                .defaultSuccessUrl("/success", true)
                .failureUrl("/login?error=true")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
            );
        }

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
