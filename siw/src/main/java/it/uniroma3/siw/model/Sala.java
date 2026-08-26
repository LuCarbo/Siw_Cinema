package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "sale")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{sala.nome.notblank}")
    private String nome;

    @NotBlank(message = "{sala.indirizzo.notblank}")
    private String indirizzo;

    @NotNull(message = "{sala.capienza.notnull}")
    @Min(value = 1, message = "{sala.capienza.min}")
    private Integer capienza;

    @OneToMany(mappedBy = "sala", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Proiezione> proiezioni = new ArrayList<>();

    public Sala() {
    }

    public Sala(String nome, String indirizzo, Integer capienza) {
        this.nome = nome;
        this.indirizzo = indirizzo;
        this.capienza = capienza;
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

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public Integer getCapienza() {
        return capienza;
    }

    public void setCapienza(Integer capienza) {
        this.capienza = capienza;
    }

    public List<Proiezione> getProiezioni() {
        return proiezioni;
    }

    public void setProiezioni(List<Proiezione> proiezioni) {
        this.proiezioni = proiezioni;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sala sala)) return false;
        return Objects.equals(nome, sala.nome) &&
               Objects.equals(indirizzo, sala.indirizzo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, indirizzo);
    }
}
