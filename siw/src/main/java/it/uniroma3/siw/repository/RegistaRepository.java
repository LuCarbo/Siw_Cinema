package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Regista;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegistaRepository extends CrudRepository<Regista, Long> {

    List<Regista> findAll();

    List<Regista> findByOrderByCognomeAscNomeAsc();

    boolean existsByNomeAndCognomeAndDataNascita(String nome, String cognome, LocalDate dataNascita);

    boolean existsByNomeAndCognomeAndDataNascitaAndIdNot(String nome, String cognome, LocalDate dataNascita, Long id);

    List<Regista> findByCognomeContainingIgnoreCaseOrNomeContainingIgnoreCase(String cognome, String nome);
}
