package it.uniroma3.siw.controller.rest;

import it.uniroma3.siw.dto.FestivalDTO;
import it.uniroma3.siw.dto.FilmDTO;
import it.uniroma3.siw.dto.ProiezioneDTO;
import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.service.FestivalService;
import it.uniroma3.siw.service.ProiezioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/festivals")
@CrossOrigin(origins = "*")
public class FestivalRestController {

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private ProiezioneService proiezioneService;

    @GetMapping
    public List<FestivalDTO> getAllFestivals(@RequestParam(value = "citta", required = false) String citta,
                                             @RequestParam(value = "nome", required = false) String nome) {
        List<Festival> festivals;
        if (citta != null && !citta.trim().isEmpty()) {
            festivals = festivalService.searchByCitta(citta);
        } else if (nome != null && !nome.trim().isEmpty()) {
            festivals = festivalService.searchByNome(nome);
        } else {
            festivals = festivalService.getAllFestivals();
        }
        return festivals.stream().map(FestivalDTO::new).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FestivalDTO> getFestival(@PathVariable("id") Long id) {
        Festival festival = festivalService.getFestival(id);
        if (festival == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new FestivalDTO(festival));
    }

    @GetMapping({ "/{id}/movies", "/{id}/films" })
    public ResponseEntity<List<FilmDTO>> getFestivalMovies(@PathVariable("id") Long id) {
        Festival festival = festivalService.getFestival(id);
        if (festival == null) {
            return ResponseEntity.notFound().build();
        }
        List<FilmDTO> films = festival.getFilm().stream().map(FilmDTO::new).toList();
        return ResponseEntity.ok(films);
    }

    @GetMapping({ "/{id}/screenings", "/{id}/proiezioni" })
    public ResponseEntity<List<ProiezioneDTO>> getFestivalScreenings(@PathVariable("id") Long id) {
        Festival festival = festivalService.getFestival(id);
        if (festival == null) {
            return ResponseEntity.notFound().build();
        }
        List<ProiezioneDTO> list = proiezioneService.getProiezioniByFestival(festival)
                .stream().map(ProiezioneDTO::new).toList();
        return ResponseEntity.ok(list);
    }
}
