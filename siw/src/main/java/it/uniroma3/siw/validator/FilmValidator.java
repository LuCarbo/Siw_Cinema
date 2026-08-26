package it.uniroma3.siw.validator;

import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class FilmValidator implements Validator {

    @Autowired
    private FilmRepository filmRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return Film.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Film film = (Film) target;

        if (film.getTitolo() != null && film.getAnno() != null) {
            if (film.getId() == null) {
                if (filmRepository.existsByTitoloAndAnno(film.getTitolo().trim(), film.getAnno())) {
                    errors.reject("film.duplicate", "Un film con questo titolo e per questo anno è già presente nel catalogo.");
                }
            } else {
                if (filmRepository.existsByTitoloAndAnnoAndIdNot(film.getTitolo().trim(), film.getAnno(), film.getId())) {
                    errors.reject("film.duplicate", "Un altro film con questo titolo e per questo anno è già presente nel catalogo.");
                }
            }
        }
    }
}
