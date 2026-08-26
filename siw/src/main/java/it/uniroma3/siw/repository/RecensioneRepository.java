package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Utente;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecensioneRepository extends CrudRepository<Recensione, Long> {

    List<Recensione> findAll();

    List<Recensione> findByFilmOrderByDataDesc(Film film);

    List<Recensione> findByAutoreOrderByDataDesc(Utente autore);

    boolean existsByFilmAndAutore(Film film, Utente autore);

    Optional<Recensione> findByFilmAndAutore(Film film, Utente autore);
}
