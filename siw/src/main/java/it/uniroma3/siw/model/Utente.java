package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "utenti")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{utente.nome.notblank}")
    private String nome;

    @NotBlank(message = "{utente.cognome.notblank}")
    private String cognome;

    @Email(message = "{utente.email.valid}")
    @NotBlank(message = "{utente.email.notblank}")
    private String email;

    @OneToMany(mappedBy = "autore", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Recensione> recensioni = new ArrayList<>();

    public Utente() {
    }

    public Utente(String nome, String cognome, String email) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Recensione> getRecensioni() {
        return recensioni;
    }

    public void setRecensioni(List<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    public String getNomeCompleto() {
        return this.nome + " " + this.cognome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utente utente)) return false;
        return Objects.equals(email, utente.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
