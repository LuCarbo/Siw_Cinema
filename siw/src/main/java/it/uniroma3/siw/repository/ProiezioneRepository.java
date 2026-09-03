package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Proiezione;
import it.uniroma3.siw.model.Sala;
import it.uniroma3.siw.model.StatoProiezione;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ProiezioneRepository extends CrudRepository<Proiezione, Long> {

    List<Proiezione> findAll();

    @Query("SELECT p FROM Proiezione p JOIN FETCH p.festival JOIN FETCH p.film JOIN FETCH p.sala ORDER BY p.data ASC, p.ora ASC")
    List<Proiezione> findByOrderByDataAscOraAsc();

    @Query("SELECT p FROM Proiezione p JOIN FETCH p.festival JOIN FETCH p.film JOIN FETCH p.sala WHERE p.festival = :festival ORDER BY p.data ASC, p.ora ASC")
    List<Proiezione> findByFestivalOrderByDataAscOraAsc(@Param("festival") Festival festival);

    @Query("SELECT p FROM Proiezione p JOIN FETCH p.festival JOIN FETCH p.film JOIN FETCH p.sala WHERE p.film = :film ORDER BY p.data ASC, p.ora ASC")
    List<Proiezione> findByFilmOrderByDataAscOraAsc(@Param("film") Film film);

    @Query("SELECT p FROM Proiezione p JOIN FETCH p.festival JOIN FETCH p.film JOIN FETCH p.sala WHERE p.sala = :sala ORDER BY p.data ASC, p.ora ASC")
    List<Proiezione> findBySalaOrderByDataAscOraAsc(@Param("sala") Sala sala);

    List<Proiezione> findByStatoOrderByDataAscOraAsc(StatoProiezione stato);

    @Query("SELECT p FROM Proiezione p JOIN FETCH p.festival JOIN FETCH p.film JOIN FETCH p.sala WHERE p.data = :data ORDER BY p.ora ASC")
    List<Proiezione> findByDataOrderByOraAsc(@Param("data") LocalDate data);

    @Query("SELECT p FROM Proiezione p JOIN FETCH p.festival JOIN FETCH p.film JOIN FETCH p.sala WHERE p.data >= :data ORDER BY p.data ASC, p.ora ASC")
    List<Proiezione> findByDataGreaterThanEqualOrderByDataAscOraAsc(@Param("data") LocalDate data);

    @Query("SELECT p FROM Proiezione p JOIN FETCH p.film WHERE p.sala = :sala AND p.data = :data AND (:id IS NULL OR p.id <> :id)")
    List<Proiezione> findBySalaAndDataExcludingId(@Param("sala") Sala sala,
                                                 @Param("data") LocalDate data,
                                                 @Param("id") Long id);

    @Query("SELECT p FROM Proiezione p WHERE p.sala = :sala AND p.data = :data AND p.ora = :ora AND (:id IS NULL OR p.id <> :id)")
    List<Proiezione> findConflictingProjections(@Param("sala") Sala sala,
                                              @Param("data") LocalDate data,
                                              @Param("ora") LocalTime ora,
                                              @Param("id") Long id);
}
