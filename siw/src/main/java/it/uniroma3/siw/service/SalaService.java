package it.uniroma3.siw.service;

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
        return salaRepository.save(sala);
    }

    @Transactional
    public void deleteSala(Long id) {
        salaRepository.deleteById(id);
    }
}
