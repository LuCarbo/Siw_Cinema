package it.uniroma3.siw.service;

import it.uniroma3.siw.exception.DuplicateEntityException;
import it.uniroma3.siw.model.Regista;
import it.uniroma3.siw.repository.RegistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RegistaService {

    @Autowired
    private RegistaRepository registaRepository;

    @Transactional(readOnly = true)
    public Regista getRegista(Long id) {
        return registaRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Regista> getAllRegisti() {
        return registaRepository.findByOrderByCognomeAscNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Regista> searchRegisti(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllRegisti();
        }
        return registaRepository.findByCognomeContainingIgnoreCaseOrNomeContainingIgnoreCase(query.trim(), query.trim());
    }

    @Transactional
    public Regista saveRegista(Regista regista) {
        if (regista.getNome() != null && regista.getCognome() != null && regista.getDataNascita() != null) {
            String nome = regista.getNome().trim();
            String cognome = regista.getCognome().trim();
            if (regista.getId() == null) {
                if (registaRepository.existsByNomeAndCognomeAndDataNascita(nome, cognome, regista.getDataNascita())) {
                    throw new DuplicateEntityException("Un regista con lo stesso nome, cognome e data di nascita è già presente.");
                }
            } else {
                if (registaRepository.existsByNomeAndCognomeAndDataNascitaAndIdNot(nome, cognome, regista.getDataNascita(), regista.getId())) {
                    throw new DuplicateEntityException("Un altro regista con lo stesso nome, cognome e data di nascita è già presente.");
                }
            }
        }
        return registaRepository.save(regista);
    }

    @Transactional
    public void deleteRegista(Long id) {
        Regista regista = getRegista(id);
        if (regista != null) {
            // Dissocia i film prima dell'eliminazione se necessario
            if (regista.getFilm() != null) {
                regista.getFilm().forEach(f -> f.setRegista(null));
            }
            registaRepository.deleteById(id);
        }
    }
}
