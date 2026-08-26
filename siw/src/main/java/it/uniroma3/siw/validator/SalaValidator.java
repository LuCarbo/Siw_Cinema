package it.uniroma3.siw.validator;

import it.uniroma3.siw.model.Sala;
import it.uniroma3.siw.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class SalaValidator implements Validator {

    @Autowired
    private SalaRepository salaRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return Sala.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Sala sala = (Sala) target;

        if (sala.getNome() != null && sala.getIndirizzo() != null) {
            if (sala.getId() == null) {
                if (salaRepository.existsByNomeAndIndirizzo(sala.getNome().trim(), sala.getIndirizzo().trim())) {
                    errors.reject("sala.duplicate", "Una sala con questo nome e indirizzo esiste già.");
                }
            } else {
                if (salaRepository.existsByNomeAndIndirizzoAndIdNot(sala.getNome().trim(), sala.getIndirizzo().trim(), sala.getId())) {
                    errors.reject("sala.duplicate", "Un'altra sala con questo nome e indirizzo esiste già.");
                }
            }
        }
    }
}
