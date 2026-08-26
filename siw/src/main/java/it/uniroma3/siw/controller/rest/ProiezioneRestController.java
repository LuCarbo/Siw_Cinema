package it.uniroma3.siw.controller.rest;

import it.uniroma3.siw.dto.ProiezioneDTO;
import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Proiezione;
import it.uniroma3.siw.model.Sala;
import it.uniroma3.siw.service.FestivalService;
import it.uniroma3.siw.service.FilmService;
import it.uniroma3.siw.service.ProiezioneService;
import it.uniroma3.siw.service.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/api/screenings", "/api/proiezioni"})
@CrossOrigin(origins = "*")
public class ProiezioneRestController {

    @Autowired
    private ProiezioneService proiezioneService;

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private SalaService salaService;

    @GetMapping
    public List<ProiezioneDTO> getProiezioni(@RequestParam(value = "festivalId", required = false) Long festivalId,
                                             @RequestParam(value = "filmId", required = false) Long filmId,
                                             @RequestParam(value = "salaId", required = false) Long salaId) {
        List<Proiezione> list;
        if (festivalId != null) {
            Festival f = festivalService.getFestival(festivalId);
            list = (f != null) ? proiezioneService.getProiezioniByFestival(f) : List.of();
        } else if (filmId != null) {
            Film film = filmService.getFilm(filmId);
            list = (film != null) ? proiezioneService.getProiezioniByFilm(film) : List.of();
        } else if (salaId != null) {
            Sala sala = salaService.getSala(salaId);
            list = (sala != null) ? proiezioneService.getProiezioniBySala(sala) : List.of();
        } else {
            list = proiezioneService.getAllProiezioni();
        }
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
