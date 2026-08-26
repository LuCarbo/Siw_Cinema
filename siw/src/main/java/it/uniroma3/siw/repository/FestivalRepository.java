package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Festival;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FestivalRepository extends CrudRepository<Festival, Long> {

    List<Festival> findAll();

    List<Festival> findByOrderByAnnoDescDataInizioDesc();

    boolean existsByNomeAndAnno(String nome, Integer anno);

    boolean existsByNomeAndAnnoAndIdNot(String nome, Integer anno, Long id);

    List<Festival> findByNomeContainingIgnoreCase(String nome);

    List<Festival> findByCittaContainingIgnoreCase(String citta);

    @Query("SELECT f FROM Festival f WHERE f.dataFine >= CURRENT_DATE ORDER BY f.dataInizio ASC")
    List<Festival> findUpcomingFestivals();
}
