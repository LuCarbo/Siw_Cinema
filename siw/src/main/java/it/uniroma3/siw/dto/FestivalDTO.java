package it.uniroma3.siw.dto;

import it.uniroma3.siw.model.Festival;
import java.time.LocalDate;
import java.util.List;

public class FestivalDTO {
    private Long id;
    private String nome;
    private Integer anno;
    private String citta;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private String descrizione;
    private String immagine;
    private int numeroFilm;
    private int numeroProiezioni;
    private List<FilmDTO> film;

    public FestivalDTO() {
    }

    public FestivalDTO(Festival f) {
        this.id = f.getId();
        this.nome = f.getNome();
        this.anno = f.getAnno();
        this.citta = f.getCitta();
        this.dataInizio = f.getDataInizio();
        this.dataFine = f.getDataFine();
        this.descrizione = f.getDescrizione();
        this.immagine = f.getImmagine();
        this.numeroFilm = f.getFilm() != null ? f.getFilm().size() : 0;
        this.numeroProiezioni = f.getProiezioni() != null ? f.getProiezioni().size() : 0;
        if (f.getFilm() != null) {
            this.film = f.getFilm().stream().map(FilmDTO::new).toList();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getAnno() { return anno; }
    public void setAnno(Integer anno) { this.anno = anno; }

    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta; }

    public LocalDate getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDate dataInizio) { this.dataInizio = dataInizio; }

    public LocalDate getDataFine() { return dataFine; }
    public void setDataFine(LocalDate dataFine) { this.dataFine = dataFine; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getImmagine() { return immagine; }
    public void setImmagine(String immagine) { this.immagine = immagine; }

    public int getNumeroFilm() { return numeroFilm; }
    public void setNumeroFilm(int numeroFilm) { this.numeroFilm = numeroFilm; }

    public int getNumeroProiezioni() { return numeroProiezioni; }
    public void setNumeroProiezioni(int numeroProiezioni) { this.numeroProiezioni = numeroProiezioni; }

    public List<FilmDTO> getFilm() { return film; }
    public void setFilm(List<FilmDTO> film) { this.film = film; }
}
