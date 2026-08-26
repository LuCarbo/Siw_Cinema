package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "festival")
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{festival.nome.notblank}")
    private String nome;

    @NotNull(message = "{festival.anno.notnull}")
    @Min(value = 1900, message = "{festival.anno.min}")
    private Integer anno;

    @NotBlank(message = "{festival.citta.notblank}")
    private String citta;

    @NotNull(message = "{festival.dataInizio.notnull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInizio;

    @NotNull(message = "{festival.dataFine.notnull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFine;

    @Column(length = 3000)
    private String descrizione;

    private String immagine;

    @ManyToMany
    @JoinTable(
        name = "festival_film",
        joinColumns = @JoinColumn(name = "festival_id"),
        inverseJoinColumns = @JoinColumn(name = "film_id")
    )
    private Set<Film> film = new HashSet<>();

    @OneToMany(mappedBy = "festival", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Proiezione> proiezioni = new ArrayList<>();

    public Festival() {
    }

    public Festival(String nome, Integer anno, String citta, LocalDate dataInizio, LocalDate dataFine, String descrizione) {
        this.nome = nome;
        this.anno = anno;
        this.citta = citta;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.descrizione = descrizione;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getAnno() {
        return anno;
    }

    public void setAnno(Integer anno) {
        this.anno = anno;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public void setDataFine(LocalDate dataFine) {
        this.dataFine = dataFine;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getImmagine() {
        return immagine;
    }

    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }

    public Set<Film> getFilm() {
        return film;
    }

    public void setFilm(Set<Film> film) {
        this.film = film;
    }

    public List<Proiezione> getProiezioni() {
        return proiezioni;
    }

    public void setProiezioni(List<Proiezione> proiezioni) {
        this.proiezioni = proiezioni;
    }

    public void addFilm(Film f) {
        this.film.add(f);
        f.getFestival().add(this);
    }

    public void removeFilm(Film f) {
        this.film.remove(f);
        f.getFestival().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Festival festival)) return false;
        return Objects.equals(nome, festival.nome) &&
               Objects.equals(anno, festival.anno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, anno);
    }
}
