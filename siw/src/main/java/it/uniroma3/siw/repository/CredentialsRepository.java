package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Utente;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialsRepository extends CrudRepository<Credentials, Long> {

    List<Credentials> findAll();

    Optional<Credentials> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Credentials> findByUser(Utente user);
}
