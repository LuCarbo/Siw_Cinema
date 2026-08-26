package it.uniroma3.siw.validator;

import it.uniroma3.siw.model.Regista;
import it.uniroma3.siw.repository.RegistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RegistaValidator implements Validator {

    @Autowired
    private RegistaRepository registaRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return Regista.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Regista regista = (Regista) target;

        if (regista.getNome() != null && regista.getCognome() != null && regista.getDataNascita() != null) {
            if (regista.getId() == null) {
                if (registaRepository.existsByNomeAndCognomeAndDataNascita(
                        regista.getNome().trim(), regista.getCognome().trim(), regista.getDataNascita())) {
                    errors.reject("regista.duplicate", "Un regista con lo stesso nome, cognome e data di nascita è già presente.");
                }
            } else {
                if (registaRepository.existsByNomeAndCognomeAndDataNascitaAndIdNot(
                        regista.getNome().trim(), regista.getCognome().trim(), regista.getDataNascita(), regista.getId())) {
                    errors.reject("regista.duplicate", "Un altro regista con lo stesso nome, cognome e data di nascita è già presente.");
                }
            }
        }
    }
}
