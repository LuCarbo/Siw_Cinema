package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Utente;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteRepository extends CrudRepository<Utente, Long> {

    List<Utente> findAll();

    Optional<Utente> findByEmail(String email);

    boolean existsByEmail(String email);
}
