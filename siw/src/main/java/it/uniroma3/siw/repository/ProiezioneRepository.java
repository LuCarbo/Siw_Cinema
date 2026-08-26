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

    List<Proiezione> findByOrderByDataAscOraAsc();

    List<Proiezione> findByFestivalOrderByDataAscOraAsc(Festival festival);

    List<Proiezione> findByFilmOrderByDataAscOraAsc(Film film);

    List<Proiezione> findBySalaOrderByDataAscOraAsc(Sala sala);

    List<Proiezione> findByStatoOrderByDataAscOraAsc(StatoProiezione stato);

    List<Proiezione> findByDataGreaterThanEqualOrderByDataAscOraAsc(LocalDate data);

    @Query("SELECT p FROM Proiezione p WHERE p.sala = :sala AND p.data = :data AND p.ora = :ora AND (:id IS NULL OR p.id <> :id)")
    List<Proiezione> findConflictingProjections(@Param("sala") Sala sala,
                                              @Param("data") LocalDate data,
                                              @Param("ora") LocalTime ora,
                                              @Param("id") Long id);
}
