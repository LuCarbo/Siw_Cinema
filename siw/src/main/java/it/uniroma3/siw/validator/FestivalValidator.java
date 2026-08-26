package it.uniroma3.siw.validator;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.repository.FestivalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class FestivalValidator implements Validator {

    @Autowired
    private FestivalRepository festivalRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return Festival.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Festival festival = (Festival) target;

        if (festival.getNome() != null && festival.getAnno() != null) {
            if (festival.getId() == null) {
                if (festivalRepository.existsByNomeAndAnno(festival.getNome().trim(), festival.getAnno())) {
                    errors.reject("festival.duplicate", "Un festival con questo nome e per questo anno esiste già.");
                }
            } else {
                if (festivalRepository.existsByNomeAndAnnoAndIdNot(festival.getNome().trim(), festival.getAnno(), festival.getId())) {
                    errors.reject("festival.duplicate", "Un altro festival con questo nome e per questo anno esiste già.");
                }
            }
        }

        if (festival.getDataInizio() != null && festival.getDataFine() != null) {
            if (festival.getDataFine().isBefore(festival.getDataInizio())) {
                errors.rejectValue("dataFine", "festival.dateCoherence", "La data di fine festival deve essere successiva o uguale alla data di inizio.");
            }
        }
    }
}
