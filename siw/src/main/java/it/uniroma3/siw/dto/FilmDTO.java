package it.uniroma3.siw.dto;

import it.uniroma3.siw.model.Film;

public class FilmDTO {
    private Long id;
    private String titolo;
    private Integer anno;
    private Integer durata;
    private String genere;
    private String paeseProduzione;
    private String locandina;
    private Double mediaVoti;
    private int numeroRecensioni;
    private Long registaId;
    private String registaNome;

    public FilmDTO() {
    }

    public FilmDTO(Film f) {
        this.id = f.getId();
        this.titolo = f.getTitolo();
        this.anno = f.getAnno();
        this.durata = f.getDurata();
        this.genere = f.getGenere();
        this.paeseProduzione = f.getPaeseProduzione();
        this.locandina = f.getLocandina();
        this.mediaVoti = f.getMediaVoti();
        this.numeroRecensioni = f.getNumeroRecensioni();
        if (f.getRegista() != null) {
            this.registaId = f.getRegista().getId();
            this.registaNome = f.getRegista().getNomeCompleto();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public Integer getAnno() { return anno; }
    public void setAnno(Integer anno) { this.anno = anno; }

    public Integer getDurata() { return durata; }
    public void setDurata(Integer durata) { this.durata = durata; }

    public String getGenere() { return genere; }
    public void setGenere(String genere) { this.genere = genere; }

    public String getPaeseProduzione() { return paeseProduzione; }
    public void setPaeseProduzione(String paeseProduzione) { this.paeseProduzione = paeseProduzione; }

    public String getLocandina() { return locandina; }
    public void setLocandina(String locandina) { this.locandina = locandina; }

    public Double getMediaVoti() { return mediaVoti; }
    public void setMediaVoti(Double mediaVoti) { this.mediaVoti = mediaVoti; }

    public int getNumeroRecensioni() { return numeroRecensioni; }
    public void setNumeroRecensioni(int numeroRecensioni) { this.numeroRecensioni = numeroRecensioni; }

    public Long getRegistaId() { return registaId; }
    public void setRegistaId(Long registaId) { this.registaId = registaId; }

    public String getRegistaNome() { return registaNome; }
    public void setRegistaNome(String registaNome) { this.registaNome = registaNome; }

    public String getNomeRegista() { return registaNome; }
    public void setNomeRegista(String nomeRegista) { this.registaNome = nomeRegista; }
}
