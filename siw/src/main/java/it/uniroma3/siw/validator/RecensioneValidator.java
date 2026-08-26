package it.uniroma3.siw.validator;

import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.repository.RecensioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.util.Optional;

@Component
public class RecensioneValidator implements Validator {

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return Recensione.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Recensione recensione = (Recensione) target;

        if (recensione.getFilm() != null && recensione.getAutore() != null) {
            Optional<Recensione> existing = recensioneRepository.findByFilmAndAutore(recensione.getFilm(), recensione.getAutore());
            if (existing.isPresent()) {
                if (recensione.getId() == null || !existing.get().getId().equals(recensione.getId())) {
                    errors.reject("recensione.duplicate", "Hai già inserito una recensione per questo film. Puoi modificare quella esistente.");
                }
            }
        }

        if (recensione.getVoto() != null) {
            if (recensione.getVoto() < 1 || recensione.getVoto() > 5) {
                errors.rejectValue("voto", "recensione.voto.range", "Il voto deve essere compreso tra 1 e 5.");
            }
        }
    }
}
