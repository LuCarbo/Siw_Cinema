package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "proiezioni")
public class Proiezione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{proiezione.data.notnull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    @NotNull(message = "{proiezione.ora.notnull}")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime ora;

    @NotNull(message = "{proiezione.stato.notnull}")
    @Enumerated(EnumType.STRING)
    private StatoProiezione stato;

    @NotNull(message = "{proiezione.festival.notnull}")
    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @NotNull(message = "{proiezione.film.notnull}")
    @ManyToOne
    @JoinColumn(name = "film_id", nullable = false)
    private Film film;

    @NotNull(message = "{proiezione.sala.notnull}")
    @ManyToOne
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    public Proiezione() {
        this.stato = StatoProiezione.SCHEDULED;
    }

    public Proiezione(LocalDate data, LocalTime ora, StatoProiezione stato, Festival festival, Film film, Sala sala) {
        this.data = data;
        this.ora = ora;
        this.stato = stato;
        this.festival = festival;
        this.film = film;
        this.sala = sala;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getOra() {
        return ora;
    }

    public void setOra(LocalTime ora) {
        this.ora = ora;
    }

    public StatoProiezione getStato() {
        return stato;
    }

    public void setStato(StatoProiezione stato) {
        this.stato = stato;
    }

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
    }

    public Film getFilm() {
        return film;
    }

    public void setFilm(Film film) {
        this.film = film;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proiezione that)) return false;
        return Objects.equals(data, that.data) &&
               Objects.equals(ora, that.ora) &&
               Objects.equals(sala, that.sala);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, ora, sala);
    }
}
