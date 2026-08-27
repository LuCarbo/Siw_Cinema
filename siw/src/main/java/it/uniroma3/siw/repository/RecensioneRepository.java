package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Utente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecensioneRepository extends CrudRepository<Recensione, Long> {

    List<Recensione> findAll();

    @Query("SELECT r FROM Recensione r JOIN FETCH r.autore WHERE r.film = :film ORDER BY r.data DESC")
    List<Recensione> findByFilmOrderByDataDesc(@Param("film") Film film);

    @Query("SELECT r FROM Recensione r JOIN FETCH r.film WHERE r.autore = :autore ORDER BY r.data DESC")
    List<Recensione> findByAutoreOrderByDataDesc(@Param("autore") Utente autore);

    boolean existsByFilmAndAutore(Film film, Utente autore);

    Optional<Recensione> findByFilmAndAutore(Film film, Utente autore);
}
