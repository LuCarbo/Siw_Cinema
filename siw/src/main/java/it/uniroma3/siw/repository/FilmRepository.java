package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Regista;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FilmRepository extends CrudRepository<Film, Long> {

    List<Film> findAll();

    List<Film> findByOrderByTitoloAsc();

    List<Film> findByOrderByAnnoDesc();

    boolean existsByTitoloAndAnno(String titolo, Integer anno);

    boolean existsByTitoloAndAnnoAndIdNot(String titolo, Integer anno, Long id);

    List<Film> findByTitoloContainingIgnoreCase(String titolo);

    List<Film> findByGenereIgnoreCase(String genere);

    List<Film> findByAnno(Integer anno);

    List<Film> findByRegista(Regista regista);

    @Query("SELECT f FROM Film f WHERE f NOT IN (SELECT festFilm FROM Festival fest JOIN fest.film festFilm WHERE fest.id = :festivalId)")
    List<Film> findFilmsNotInFestival(@Param("festivalId") Long festivalId);
}
