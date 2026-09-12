package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "recensioni", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"film_id", "utente_id"})
})
public class Recensione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titolo;

    @NotBlank(message = "{recensione.testo.notblank}")
    @Column(length = 2000, nullable = false)
    private String testo;

    @NotNull(message = "{recensione.voto.notnull}")
    @Min(value = 1, message = "{recensione.voto.range}")
    @Max(value = 5, message = "{recensione.voto.range}")
    @Column(nullable = false)
    private Integer voto;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "film_id", nullable = false)
    private Film film;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente autore;

    public Recensione() {
        this.data = LocalDate.now();
    }

    public Recensione(String titolo, String testo, Integer voto, Film film, Utente autore) {
        this.titolo = titolo;
        this.testo = testo;
        this.voto = voto;
        this.film = film;
        this.autore = autore;
        this.data = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public Integer getVoto() {
        return voto;
    }

    public void setVoto(Integer voto) {
        this.voto = voto;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Film getFilm() {
        return film;
    }

    public void setFilm(Film film) {
        this.film = film;
    }

    public Utente getAutore() {
        return autore;
    }

    public void setAutore(Utente autore) {
        this.autore = autore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recensione that)) return false;
        return Objects.equals(film, that.film) &&
               Objects.equals(autore, that.autore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(film, autore);
    }
}
