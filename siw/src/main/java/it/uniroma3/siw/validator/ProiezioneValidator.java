package it.uniroma3.siw.validator;

import it.uniroma3.siw.model.Proiezione;
import it.uniroma3.siw.repository.ProiezioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.util.List;

@Component
public class ProiezioneValidator implements Validator {

    @Autowired
    private ProiezioneRepository proiezioneRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return Proiezione.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Proiezione proiezione = (Proiezione) target;

        // Validazione date rispetto al Festival
        if (proiezione.getFestival() != null && proiezione.getData() != null) {
            if (proiezione.getData().isBefore(proiezione.getFestival().getDataInizio()) ||
                proiezione.getData().isAfter(proiezione.getFestival().getDataFine())) {
                errors.rejectValue("data", "proiezione.festivalDateMismatch",
                        "La data della proiezione deve essere compresa tra la data di inizio (" +
                        proiezione.getFestival().getDataInizio() + ") e di fine (" +
                        proiezione.getFestival().getDataFine() + ") del festival.");
            }
        }

        // Validazione conflitti di sala e orario
        if (proiezione.getSala() != null && proiezione.getData() != null && proiezione.getOra() != null) {
            List<Proiezione> conflicts = proiezioneRepository.findConflictingProjections(
                    proiezione.getSala(),
                    proiezione.getData(),
                    proiezione.getOra(),
                    proiezione.getId()
            );

            if (!conflicts.isEmpty()) {
                errors.reject("proiezione.conflict", "La sala selezionata è già occupata per la data e l'ora specificate.");
            }
        }
    }
}
