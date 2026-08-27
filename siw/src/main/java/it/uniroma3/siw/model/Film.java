package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "film")
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{film.titolo.notblank}")
    private String titolo;

    @NotNull(message = "{film.anno.notnull}")
    @Min(value = 1888, message = "{film.anno.min}")
    private Integer anno;

    @NotNull(message = "{film.durata.notnull}")
    @Min(value = 1, message = "{film.durata.min}")
    private Integer durata; // durata in minuti

    @NotBlank(message = "{film.genere.notblank}")
    private String genere;

    @NotBlank(message = "{film.paeseProduzione.notblank}")
    private String paeseProduzione;

    @Column(columnDefinition = "TEXT")
    private String locandina;

    @ManyToOne
    @JoinColumn(name = "regista_id")
    private Regista regista;

    @ManyToMany(mappedBy = "film")
    private Set<Festival> festival = new HashSet<>();

    @OneToMany(mappedBy = "film", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Proiezione> proiezioni = new ArrayList<>();

    @OneToMany(mappedBy = "film", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Recensione> recensioni = new ArrayList<>();

    public Film() {
    }

    public Film(String titolo, Integer anno, Integer durata, String genere, String paeseProduzione) {
        this.titolo = titolo;
        this.anno = anno;
        this.durata = durata;
        this.genere = genere;
        this.paeseProduzione = paeseProduzione;
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

    public Integer getAnno() {
        return anno;
    }

    public void setAnno(Integer anno) {
        this.anno = anno;
    }

    public Integer getDurata() {
        return durata;
    }

    public void setDurata(Integer durata) {
        this.durata = durata;
    }

    public String getGenere() {
        return genere;
    }

    public void setGenere(String genere) {
        this.genere = genere;
    }

    public String getPaeseProduzione() {
        return paeseProduzione;
    }

    public void setPaeseProduzione(String paeseProduzione) {
        this.paeseProduzione = paeseProduzione;
    }

    public String getLocandina() {
        return locandina;
    }

    public void setLocandina(String locandina) {
        this.locandina = locandina;
    }

    public Regista getRegista() {
        return regista;
    }

    public void setRegista(Regista regista) {
        this.regista = regista;
    }

    public Set<Festival> getFestival() {
        return festival;
    }

    public void setFestival(Set<Festival> festival) {
        this.festival = festival;
    }

    public List<Proiezione> getProiezioni() {
        return proiezioni;
    }

    public void setProiezioni(List<Proiezione> proiezioni) {
        this.proiezioni = proiezioni;
    }

    public List<Recensione> getRecensioni() {
        return recensioni;
    }

    public void setRecensioni(List<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    public Double getMediaVoti() {
        if (recensioni == null || recensioni.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Recensione r : recensioni) {
            sum += r.getVoto();
        }
        return Math.round((sum / recensioni.size()) * 10.0) / 10.0;
    }

    public int getNumeroRecensioni() {
        return recensioni != null ? recensioni.size() : 0;
    }

    /**
     * Calcola il conteggio delle recensioni per ogni voto da 5 a 1.
     */
    public Map<Integer, Integer> getDistribuzioneVoti() {
        Map<Integer, Integer> dist = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            dist.put(i, 0);
        }
        if (recensioni != null) {
            for (Recensione r : recensioni) {
                if (r.getVoto() != null && r.getVoto() >= 1 && r.getVoto() <= 5) {
                    dist.put(r.getVoto(), dist.get(r.getVoto()) + 1);
                }
            }
        }
        return dist;
    }

    /**
     * Calcola la percentuale di recensioni per ogni voto da 5 a 1.
     */
    public Map<Integer, Integer> getPercentualiVoti() {
        Map<Integer, Integer> perc = new LinkedHashMap<>();
        int total = getNumeroRecensioni();
        Map<Integer, Integer> dist = getDistribuzioneVoti();
        for (int i = 5; i >= 1; i--) {
            int count = dist.get(i);
            perc.put(i, total > 0 ? (int) Math.round(((double) count / total) * 100) : 0);
        }
        return perc;
    }

    /**
     * Calcola il tasso di gradimento (percentuale di voti >= 4).
     */
    public int getTassoGradimento() {
        int total = getNumeroRecensioni();
        if (total == 0) return 0;
        long positivi = recensioni.stream().filter(r -> r.getVoto() != null && r.getVoto() >= 4).count();
        return (int) Math.round(((double) positivi / total) * 100);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Film film)) return false;
        return Objects.equals(titolo, film.titolo) &&
               Objects.equals(anno, film.anno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titolo, anno);
    }
}
