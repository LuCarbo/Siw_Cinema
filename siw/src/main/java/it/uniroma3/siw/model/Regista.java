package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "registi")
public class Regista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{regista.nome.notblank}")
    private String nome;

    @NotBlank(message = "{regista.cognome.notblank}")
    private String cognome;

    @NotNull(message = "{regista.dataNascita.notnull}")
    @Past(message = "{regista.dataNascita.past}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascita;

    @NotBlank(message = "{regista.nazionalita.notblank}")
    private String nazionalita;

    private String foto;

    @OneToMany(mappedBy = "regista", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Film> film = new ArrayList<>();

    public Regista() {
    }

    public Regista(String nome, String cognome, LocalDate dataNascita, String nazionalita) {
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.nazionalita = nazionalita;
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

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(LocalDate dataNascita) {
        this.dataNascita = dataNascita;
    }

    public String getNazionalita() {
        return nazionalita;
    }

    public void setNazionalita(String nazionalita) {
        this.nazionalita = nazionalita;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public List<Film> getFilm() {
        return film;
    }

    public void setFilm(List<Film> film) {
        this.film = film;
    }

    public String getNomeCompleto() {
        return this.nome + " " + this.cognome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Regista regista)) return false;
        return Objects.equals(nome, regista.nome) &&
               Objects.equals(cognome, regista.cognome) &&
               Objects.equals(dataNascita, regista.dataNascita);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, cognome, dataNascita);
    }
}
