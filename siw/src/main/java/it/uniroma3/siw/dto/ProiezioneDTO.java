package it.uniroma3.siw.dto;

import it.uniroma3.siw.model.Proiezione;
import java.time.LocalDate;
import java.time.LocalTime;

public class ProiezioneDTO {
    private Long id;
    private LocalDate data;
    private LocalTime ora;
    private String stato;
    private String statoLabel;
    
    private Long festivalId;
    private String festivalNome;
    
    private Long filmId;
    private String filmTitolo;
    private Integer filmDurata;
    private String filmGenere;
    private String filmLocandina;
    
    private Long salaId;
    private String salaNome;
    private String salaIndirizzo;
    private Integer salaCapienza;

    public ProiezioneDTO() {
    }

    public ProiezioneDTO(Proiezione p) {
        this.id = p.getId();
        this.data = p.getData();
        this.ora = p.getOra();
        if (p.getStato() != null) {
            this.stato = p.getStato().name();
            this.statoLabel = p.getStato().getLabel();
        }
        if (p.getFestival() != null) {
            this.festivalId = p.getFestival().getId();
            this.festivalNome = p.getFestival().getNome();
        }
        if (p.getFilm() != null) {
            this.filmId = p.getFilm().getId();
            this.filmTitolo = p.getFilm().getTitolo();
            this.filmDurata = p.getFilm().getDurata();
            this.filmGenere = p.getFilm().getGenere();
            this.filmLocandina = p.getFilm().getLocandina();
        }
        if (p.getSala() != null) {
            this.salaId = p.getSala().getId();
            this.salaNome = p.getSala().getNome();
            this.salaIndirizzo = p.getSala().getIndirizzo();
            this.salaCapienza = p.getSala().getCapienza();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getOra() { return ora; }
    public void setOra(LocalTime ora) { this.ora = ora; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public String getStatoLabel() { return statoLabel; }
    public void setStatoLabel(String statoLabel) { this.statoLabel = statoLabel; }

    public Long getFestivalId() { return festivalId; }
    public void setFestivalId(Long festivalId) { this.festivalId = festivalId; }

    public String getFestivalNome() { return festivalNome; }
    public void setFestivalNome(String festivalNome) { this.festivalNome = festivalNome; }

    public Long getFilmId() { return filmId; }
    public void setFilmId(Long filmId) { this.filmId = filmId; }

    public String getFilmTitolo() { return filmTitolo; }
    public void setFilmTitolo(String filmTitolo) { this.filmTitolo = filmTitolo; }

    public Integer getFilmDurata() { return filmDurata; }
    public void setFilmDurata(Integer filmDurata) { this.filmDurata = filmDurata; }

    public String getFilmGenere() { return filmGenere; }
    public void setFilmGenere(String filmGenere) { this.filmGenere = filmGenere; }

    public String getFilmLocandina() { return filmLocandina; }
    public void setFilmLocandina(String filmLocandina) { this.filmLocandina = filmLocandina; }

    public Long getSalaId() { return salaId; }
    public void setSalaId(Long salaId) { this.salaId = salaId; }

    public String getSalaNome() { return salaNome; }
    public void setSalaNome(String salaNome) { this.salaNome = salaNome; }

    public String getSalaIndirizzo() { return salaIndirizzo; }
    public void setSalaIndirizzo(String salaIndirizzo) { this.salaIndirizzo = salaIndirizzo; }

    public Integer getSalaCapienza() { return salaCapienza; }
    public void setSalaCapienza(Integer salaCapienza) { this.salaCapienza = salaCapienza; }
}
