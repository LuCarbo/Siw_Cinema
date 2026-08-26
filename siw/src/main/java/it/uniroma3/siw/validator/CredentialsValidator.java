package it.uniroma3.siw.validator;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.repository.CredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CredentialsValidator implements Validator {

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return Credentials.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Credentials credentials = (Credentials) target;

        if (credentials.getUsername() != null) {
            String username = credentials.getUsername().trim();
            if (credentials.getId() == null) {
                if (credentialsRepository.existsByUsername(username)) {
                    errors.rejectValue("username", "credentials.username.duplicate", "Questo username è già in uso. Scegline un altro.");
                }
            }
        }
    }
}
