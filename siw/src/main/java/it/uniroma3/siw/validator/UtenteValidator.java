package it.uniroma3.siw.validator;

import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UtenteValidator implements Validator {

    @Autowired
    private UtenteRepository utenteRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return Utente.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Utente utente = (Utente) target;

        if (utente.getEmail() != null) {
            String email = utente.getEmail().trim();
            if (utente.getId() == null) {
                if (utenteRepository.existsByEmail(email)) {
                    errors.rejectValue("email", "utente.email.duplicate", "Questa email è già associata a un account.");
                }
            }
        }
    }
}
