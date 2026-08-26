package it.uniroma3.siw.dto;

import it.uniroma3.siw.model.Recensione;
import java.time.LocalDate;

public class RecensioneDTO {
    private Long id;
    private String titolo;
    private String testo;
    private Integer voto;
    private LocalDate data;
    private Long filmId;
    private String filmTitolo;
    private String autoreNome;

    public RecensioneDTO() {
    }

    public RecensioneDTO(Recensione r) {
        this.id = r.getId();
        this.titolo = r.getTitolo();
        this.testo = r.getTesto();
        this.voto = r.getVoto();
        this.data = r.getData();
        if (r.getFilm() != null) {
            this.filmId = r.getFilm().getId();
            this.filmTitolo = r.getFilm().getTitolo();
        }
        if (r.getAutore() != null) {
            this.autoreNome = r.getAutore().getNomeCompleto();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getTesto() { return testo; }
    public void setTesto(String testo) { this.testo = testo; }

    public Integer getVoto() { return voto; }
    public void setVoto(Integer voto) { this.voto = voto; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Long getFilmId() { return filmId; }
    public void setFilmId(Long filmId) { this.filmId = filmId; }

    public String getFilmTitolo() { return filmTitolo; }
    public void setFilmTitolo(String filmTitolo) { this.filmTitolo = filmTitolo; }

    public String getAutoreNome() { return autoreNome; }
    public void setAutoreNome(String autoreNome) { this.autoreNome = autoreNome; }
}
