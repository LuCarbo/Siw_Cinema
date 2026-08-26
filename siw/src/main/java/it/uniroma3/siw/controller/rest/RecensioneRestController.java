package it.uniroma3.siw.controller.rest;

import it.uniroma3.siw.dto.RecensioneDTO;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Film;
import it.uniroma3.siw.model.Recensione;
import it.uniroma3.siw.model.Utente;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.FilmService;
import it.uniroma3.siw.service.RecensioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/reviews", "/api/recensioni"})
@CrossOrigin(origins = "*")
public class RecensioneRestController {

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private CredentialsService credentialsService;

    @GetMapping("/{id}")
    public ResponseEntity<RecensioneDTO> getRecensione(@PathVariable("id") Long id) {
        Recensione r = recensioneService.getRecensione(id);
        if (r == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new RecensioneDTO(r));
    }

    @GetMapping("/film/{filmId}")
    public ResponseEntity<List<RecensioneDTO>> getRecensioniByFilm(@PathVariable("filmId") Long filmId) {
        Film film = filmService.getFilm(filmId);
        if (film == null) {
            return ResponseEntity.notFound().build();
        }
        List<RecensioneDTO> list = recensioneService.getRecensioniByFilm(film)
                .stream().map(RecensioneDTO::new).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> createRecensione(@RequestBody Map<String, Object> payload) {
        Utente currentUser = credentialsService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Utente non autenticato"));
        }

        Object filmIdObj = payload.get("filmId");
        if (filmIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "filmId obbligatorio"));
        }
        Long filmId = Long.valueOf(filmIdObj.toString());
        Film film = filmService.getFilm(filmId);
        if (film == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Film non trovato"));
        }

        if (recensioneService.hasUserReviewedFilm(film, currentUser)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Hai già inserito una recensione per questo film"));
        }

        Recensione r = new Recensione();
        r.setTitolo((String) payload.get("titolo"));
        r.setTesto((String) payload.get("testo"));
        if (payload.get("voto") != null) {
            r.setVoto(Integer.valueOf(payload.get("voto").toString()));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Voto obbligatorio (1-5)"));
        }

        if (r.getVoto() < 1 || r.getVoto() > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Il voto deve essere compreso tra 1 e 5"));
        }

        r.setFilm(film);
        r.setAutore(currentUser);
        Recensione saved = recensioneService.saveRecensione(r);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(new RecensioneDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecensione(@PathVariable("id") Long id, @RequestBody Map<String, Object> payload) {
        Recensione existing = recensioneService.getRecensione(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        Utente currentUser = credentialsService.getCurrentUser();
        Credentials creds = credentialsService.getCurrentCredentials();
        boolean isAdmin = creds != null && creds.isAdmin();

        if (!recensioneService.canUserModify(existing, currentUser, isAdmin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Non autorizzato a modificare questa recensione"));
        }

        if (payload.containsKey("titolo")) {
            existing.setTitolo((String) payload.get("titolo"));
        }
        if (payload.containsKey("testo")) {
            existing.setTesto((String) payload.get("testo"));
        }
        if (payload.containsKey("voto") && payload.get("voto") != null) {
            int voto = Integer.parseInt(payload.get("voto").toString());
            if (voto < 1 || voto > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Il voto deve essere compreso tra 1 e 5"));
            }
            existing.setVoto(voto);
        }

        Recensione updated = recensioneService.saveRecensione(existing);
        return ResponseEntity.ok(new RecensioneDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecensione(@PathVariable("id") Long id) {
        Recensione existing = recensioneService.getRecensione(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        Utente currentUser = credentialsService.getCurrentUser();
        Credentials creds = credentialsService.getCurrentCredentials();
        boolean isAdmin = creds != null && creds.isAdmin();

        if (!recensioneService.canUserModify(existing, currentUser, isAdmin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Non autorizzato a eliminare questa recensione"));
        }

        recensioneService.deleteRecensione(id);
        return ResponseEntity.noContent().build();
    }
}
