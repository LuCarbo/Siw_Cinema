package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Sala;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalaRepository extends CrudRepository<Sala, Long> {

    List<Sala> findAll();

    List<Sala> findByOrderByNomeAsc();

    boolean existsByNomeAndIndirizzo(String nome, String indirizzo);

    boolean existsByNomeAndIndirizzoAndIdNot(String nome, String indirizzo, Long id);

    List<Sala> findByNomeContainingIgnoreCase(String nome);
}
