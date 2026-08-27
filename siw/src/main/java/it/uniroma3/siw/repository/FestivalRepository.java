package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FestivalRepository extends CrudRepository<Festival, Long> {

    List<Festival> findAll();

    Page<Festival> findAll(Pageable pageable);

    List<Festival> findByOrderByAnnoDescDataInizioDesc();

    Page<Festival> findByOrderByAnnoDescDataInizioDesc(Pageable pageable);

    boolean existsByNomeAndAnno(String nome, Integer anno);

    boolean existsByNomeAndAnnoAndIdNot(String nome, Integer anno, Long id);

    List<Festival> findByNomeContainingIgnoreCase(String nome);

    Page<Festival> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    List<Festival> findByCittaContainingIgnoreCase(String citta);

    Page<Festival> findByCittaContainingIgnoreCase(String citta, Pageable pageable);

    @Query("SELECT f FROM Festival f WHERE f.dataFine >= CURRENT_DATE ORDER BY f.dataInizio ASC")
    List<Festival> findUpcomingFestivals();
}
