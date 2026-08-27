package it.uniroma3.siw.controller.rest;

import it.uniroma3.siw.dto.ProiezioneDTO;
import it.uniroma3.siw.model.Proiezione;
import it.uniroma3.siw.service.ProiezioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/screenings", "/api/proiezioni"})
@CrossOrigin(origins = "*")
public class ProiezioneRestController {

    @Autowired
    private ProiezioneService proiezioneService;

    @GetMapping
    public List<ProiezioneDTO> getProiezioni(@RequestParam(value = "festivalId", required = false) Long festivalId,
                                             @RequestParam(value = "filmId", required = false) Long filmId,
                                             @RequestParam(value = "salaId", required = false) Long salaId,
                                             @RequestParam(value = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<Proiezione> list = proiezioneService.searchProiezioni(festivalId, filmId, salaId, data);
        return list.stream().map(ProiezioneDTO::new).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProiezioneDTO> getProiezione(@PathVariable("id") Long id) {
        Proiezione p = proiezioneService.getProiezione(id);
        if (p == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ProiezioneDTO(p));
    }
}
