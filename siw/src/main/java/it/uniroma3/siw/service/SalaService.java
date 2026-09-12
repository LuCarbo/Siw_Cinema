package it.uniroma3.siw.service;

import it.uniroma3.siw.exception.DuplicateEntityException;
import it.uniroma3.siw.model.Sala;
import it.uniroma3.siw.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    @Transactional(readOnly = true)
    public Sala getSala(Long id) {
        return salaRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Sala> getAllSale() {
        return salaRepository.findByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Sala> searchByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return getAllSale();
        }
        return salaRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    @Transactional
    public Sala saveSala(Sala sala) {
        if (sala.getNome() != null && sala.getIndirizzo() != null) {
            String nome = sala.getNome().trim();
            String indirizzo = sala.getIndirizzo().trim();
            if (sala.getId() == null) {
                if (salaRepository.existsByNomeAndIndirizzo(nome, indirizzo)) {
                    throw new DuplicateEntityException("Una sala con questo nome e indirizzo esiste già.");
                }
            } else {
                if (salaRepository.existsByNomeAndIndirizzoAndIdNot(nome, indirizzo, sala.getId())) {
                    throw new DuplicateEntityException("Un'altra sala con questo nome e indirizzo esiste già.");
                }
            }
        }
        return salaRepository.save(sala);
    }

    @Transactional
    public void deleteSala(Long id) {
        salaRepository.deleteById(id);
    }
}
