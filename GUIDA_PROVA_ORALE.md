# Guida Completa per la Prova Orale - SIW Cinema

Questa guida è strutturata specificamente per prepararsi alla **prova orale** e alle **modifiche live richieste dal docente**, coprendo ciascuno dei 12 scenari tipici con il codice esatto, i passaggi e le motivazioni teoriche/architetturali da esporre.

---

## 📋 Indice degli Scenari d'Esame

1. [Aggiunta di un Attributo o di una Nuova Relazione tra Entità](#1-aggiunta-di-un-attributo-o-di-una-nuova-relazione)
2. [Modifica di un Mapping JPA (Cascade, Fetch, JoinTable)](#2-modifica-di-un-mapping-jpa)
3. [Implementazione di una Nuova Query (Derived Method o JPQL)](#3-implementazione-di-una-nuova-query)
4. [Aggiunta di un Caso d'Uso Completo](#4-aggiunta-di-un-caso-duse-completo)
5. [Modifica o Aggiunta di una Regola di Business e Validatore](#5-modifica-di-una-regola-di-business)
6. [Aggiunta o Modifica di un Metodo nel Service Layer](#6-aggiunta-o-modifica-di-un-metodo-del-service-layer)
7. [Modifica della Gestione Transazionale (@Transactional, Isolation, Rollback)](#7-modifica-della-gestione-transazionale)
8. [Aggiunta di un Nuovo Endpoint REST con DTO](#8-aggiunta-di-un-endpoint-rest)
9. [Modifica della Configurazione di Spring Security](#9-modifica-della-configurazione-di-spring-security)
10. [Modifica della Strategia di Fetch (LAZY vs EAGER)](#10-modifica-della-strategia-di-fetch)
11. [Individuazione e Risoluzione di un Problema N+1](#11-problema-delle-n1-query)
12. [Modifica di un Componente React](#12-modifica-di-un-componente-react)

---

## 1. Aggiunta di un Attributo o di una Nuova Relazione

### Scenario A: Aggiungere un attributo `prezzoBiglietto` a `Proiezione`
1. **Entità (`Proiezione.java`)**:
   ```java
   @Min(value = 0, message = "Il prezzo non può essere negativo")
   @Column(nullable = true)
   private Double prezzoBiglietto;

   public Double getPrezzoBiglietto() { return prezzoBiglietto; }
   public void setPrezzoBiglietto(Double prezzoBiglietto) { this.prezzoBiglietto = prezzoBiglietto; }
   ```
2. **Conseguenze da discutere**:
   - *Database*: Hibernate con `ddl-auto=update` aggiunge la colonna `prezzo_biglietto DOUBLE PRECISION` alla tabella `proiezione`.
   - *Validazione*: `@Min(0)` impedisce prezzi negativi nel binding del form.

### Scenario B: Aggiungere l'entità `Attore` con relazione `@ManyToMany` con `Film`
1. **Nuova Entità (`Attore.java`)**:
   ```java
   @Entity
   public class Attore {
       @Id @GeneratedValue(strategy = GenerationType.AUTO)
       private Long id;
       @NotBlank private String nome;
       @NotBlank private String cognome;
       
       @ManyToMany(mappedBy = "attori")
       private List<Film> film = new ArrayList<>();
       // getter e setter
   }
   ```
2. **Modifica `Film.java` (lato proprietario con `@JoinTable`)**:
   ```java
   @ManyToMany
   @JoinTable(
       name = "film_attori",
       joinColumns = @JoinColumn(name = "film_id"),
       inverseJoinColumns = @JoinColumn(name = "attore_id")
   )
   private List<Attore> attori = new ArrayList<>();
   ```
3. **Conseguenze da discutere**:
   - Viene creata una tabella di giunzione `film_attori` con due chiavi esterne.
   - `Film` è l'owning side della relazione (contiene `@JoinTable`).
   - Il fetch è `LAZY` per impostazione predefinita su `@ManyToMany`.

---

## 2. Modifica di un Mapping JPA

### Domanda tipica: "Come cambieresti il mapping per eliminare a cascata le recensioni se viene cancellato un film?"
1. **In `Film.java`**:
   ```java
   @OneToMany(mappedBy = "film", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<Recensione> recensioni = new ArrayList<>();
   ```
2. **Conseguenze da discutere**:
   - `CascadeType.ALL` (o `CascadeType.REMOVE`): se si cancella un `Film`, Hibernate rimuove a cascata tutte le relative `Recensioni`.
   - `orphanRemoval = true`: se si rimuove una recensione dalla lista `film.getRecensioni().remove(rec)`, al momento del flush Hibernate esegue la query SQL `DELETE FROM recensione WHERE id = ?` per eliminare l'orfano.

---

## 3. Implementazione di una Nuova Query

### Scenario: "Trovami tutte le proiezioni di un certo festival successive a una certa data e ora"
1. **Nel repository (`ProiezioneRepository.java`)**:
   - **Opzione A (Derived Query Method)**:
     ```java
     List<Proiezione> findByFestivalIdAndDataGreaterThanEqualOrderByDataAscOraAsc(Long festivalId, LocalDate data);
     ```
   - **Opzione B (Query JPQL con JOIN FETCH)**:
     ```java
     @Query("SELECT p FROM Proiezione p " +
            "JOIN FETCH p.film " +
            "JOIN FETCH p.sala " +
            "WHERE p.festival.id = :festivalId AND p.data >= :data " +
            "ORDER BY p.data ASC, p.ora ASC")
     List<Proiezione> findProiezioniFutureDelFestival(@Param("festivalId") Long festivalId, 
                                                      @Param("data") LocalDate data);
     ```
2. **Motivazione da discutere**:
   - JPQL lavora su **oggetti ed entità di dominio** e non su tabelle SQL fisiche.
   - `JOIN FETCH` evita il problema N+1 caricando contestualmente i dati di `Film` e `Sala`.

---

## 4. Aggiunta di un Caso d'Uso Completo

### Scenario: "Consentire all'admin di annullare una proiezione"
1. **Nel Service (`ProiezioneService.java`)**:
   ```java
   @Transactional
   public void annullaProiezione(Long proiezioneId) {
       Proiezione p = proiezioneRepository.findById(proiezioneId)
           .orElseThrow(() -> new IllegalArgumentException("Proiezione non trovata: " + proiezioneId));
       p.setStato(StatoProiezione.CANCELLED);
       proiezioneRepository.save(p);
   }
   ```
2. **Nel Controller (`ProiezioneController.java`)**:
   ```java
   @PostMapping("/admin/proiezioni/{id}/annulla")
   public String annullaProiezione(@PathVariable Long id, RedirectAttributes redirectAttributes) {
       proiezioneService.annullaProiezione(id);
       redirectAttributes.addFlashAttribute("successMessage", "Proiezione annullata con successo!");
       return "redirect:/proiezione/" + id;
   }
   ```
3. **Nel Template HTML (`proiezione/detail.html`)**:
   ```html
   <form th:if="${isAdmin and proiezione.stato.name() != 'CANCELLED'}" 
         th:action="@{'/admin/proiezioni/' + ${proiezione.id} + '/annulla'}" method="post">
       <button type="submit" class="btn btn-sm btn-danger">Annulla Proiezione</button>
   </form>
   ```

---

## 5. Modifica di una Regola di Business

### Scenario: "Un film può partecipare a un festival solo se l'anno di produzione del film non è successivo all'anno del festival"
1. **Nel validatore (`FestivalValidator.java` o custom logic nel Service)**:
   ```java
   if (film.getAnno() > festival.getAnno()) {
       errors.reject("film.anno.invalido", 
           "Il film non può essere prodotto in un anno successivo a quello del festival (" + festival.getAnno() + ")");
   }
   ```
2. **Conseguenze da discutere**:
   - La regola garantisce la **coerenza semantica temporale** dei dati.
   - L'errore viene intercettato prima del salvataggio nel database evitando stati inconsistenti.

---

## 6. Aggiunta o Modifica di un Metodo del Service Layer

### Scenario: "Calcolare la capienza totale dei posti disponibili per tutte le proiezioni di un festival"
1. **In `FestivalService.java`**:
   ```java
   @Transactional(readOnly = true)
   public int calcolaPostiTotaliFestival(Long festivalId) {
       Festival festival = festivalRepository.findById(festivalId)
           .orElseThrow(() -> new IllegalArgumentException("Festival non trovato"));
       
       return festival.getProiezioni().stream()
           .filter(p -> p.getStato() != StatoProiezione.CANCELLED)
           .mapToInt(p -> p.getSala().getCapienza())
           .sum();
   }
   ```
2. **Motivazione architetturale**:
   - La logica risiede esclusivamente nel **Service Layer**, garantendo la separazione delle responsabilità (il Controller delega, non effettua calcoli di business).
   - `@Transactional(readOnly = true)` velocizza l'esecuzione disabilitando il dirty checking automatico di Hibernate.

---

## 7. Modifica della Gestione Transazionale

### Domande tipiche su `@Transactional`:
- **Cosa fa `rollbackFor = Exception.class`?**
  - Di default Spring esegue il rollback solo per `RuntimeException` (unchecked). Aggiungendo `rollbackFor = Exception.class`, la transazione fa rollback anche per le eccezioni controllate (`checked exceptions` come `IOException` o `SQLException`).
- **Differenza tra `Propagation.REQUIRED` e `Propagation.REQUIRES_NEW`**:
  - `REQUIRED` (default): se esiste già una transazione attiva, il metodo si unisce ad essa; altrimenti ne apre una nuova.
  - `REQUIRES_NEW`: sospende la transazione corrente ed apre una **nuova transazione indipendente** su una connessione DB separata (utile ad es. per loggare audit immutabili che non devono essere rolltati se la transazione principale fallisce).
- **Isolamento `Isolation.READ_COMMITTED`**:
  - Impedisce le *Dirty Reads* (lettura di dati modificati ma non ancora committati da altre transazioni concorrenti).

---

## 8. Aggiunta di un Endpoint REST

### Scenario: "Aggiungi un endpoint REST per restituire i film con media voto >= 4"
1. **Nel Controller REST (`MovieRestController.java`)**:
   ```java
   @GetMapping("/top-rated")
   public ResponseEntity<List<MovieDto>> getTopRatedMovies() {
       List<Film> topFilms = filmService.findAll().stream()
           .filter(f -> f.getMediaVoti() >= 4.0)
           .toList();
       
       List<MovieDto> dtos = topFilms.stream()
           .map(MovieDto::fromEntity)
           .toList();
           
       return ResponseEntity.ok(dtos);
   }
   ```
2. **Motivazione da discutere**:
   - Restituisce `ResponseEntity<List<MovieDto>>` con codice `200 OK`.
   - L'uso di `MovieDto` serializza solo i campi necessari senza esporre entità JPA e senza triggerare lazy loading accidentale o loop ciclici.

---

## 9. Modifica della Configurazione di Spring Security

### Scenario: "Permetti solo agli utenti registrati (USER o ADMIN) di visualizzare i dettagli della sala cinematografica"
1. **In `SecurityConfiguration.java`**:
   ```java
   .authorizeHttpRequests(auth -> auth
       // Pagine pubbliche
       .requestMatchers("/", "/index", "/festivals", "/festival/**", "/films", "/film/**", "/proiezioni").permitAll()
       .requestMatchers("/css/**", "/images/**", "/js/**", "/favicon.ico", "/api/**").permitAll()
       .requestMatchers("/login", "/register").anonymous()

       // Modifica richiesta: dettagli sala solo per utenti loggati
       .requestMatchers("/sala/**").hasAnyAuthority("USER", "ADMIN")

       // Solo Admin
       .requestMatchers("/admin/**", "/festivals/nuovo", "/films/nuovo", "/proiezioni/nuova").hasAuthority("ADMIN")
       .anyRequest().authenticated()
   )
   ```
2. **Motivazione da discutere**:
   - `hasAnyAuthority("USER", "ADMIN")` richiede una sessione autenticata. Gli utenti anonimi vengono reindirizzati al form di login.

---

## 10. Modifica della Strategia di Fetch

### Domanda: "Cosa succede se metto `@ManyToOne(fetch = FetchType.EAGER)` su tutte le relazioni di `Proiezione`?"
- **Risposta**:
  - Quando si carica anche una singola proiezione, Hibernate caricherà immediatamente `Film`, `Festival`, `Sala` e a cascata il `Regista` del film e le collezioni se EAGER.
  - Se si fa `proiezioneRepository.findAll()`, Hibernate eseguirà query con molteplici `LEFT OUTER JOIN` o peggio $N$ subquery separate per ogni entità collegata, aumentando drasticamente il consumo di memoria RAM e rallentando i tempi di risposta.
  - **Best Practice SIW**: Mantenere il fetch **`LAZY`** a livello di mapping e applicare il fetch mirato **`JOIN FETCH`** nelle sole query dei casi d'uso che richiedono tali dati.

---

## 11. Problema delle N+1 Query

### Come mostrarlo e spiegarlo:
1. **Mostra il Test Automatico**:
   ```bash
   ./mvnw test -Dtest=DataAccessPerformanceAnalysisTest
   ```
2. **Spiegazione del comportamento**:
   - **Caso LAZY non ottimizzato**: 1 query per recuperare le 10 proiezioni + 10 query per caricare i rispettivi Festival + query per Sale = **13 query SQL**.
   - **Caso Ottimizzato con `JOIN FETCH`**: **1 singola query SQL** con `INNER JOIN`:
     ```sql
     SELECT p, f, fl, s FROM Proiezione p 
     JOIN FETCH p.festival f 
     JOIN FETCH p.film fl 
     JOIN FETCH p.sala s
     ```

---

## 12. Modifica di un Componente React

### Scenario: "Aggiungere un filtro per voto minimo nel componente React del catalogo film"
1. **Nel file `film/list.html`**:
   - Aggiungi lo stato: `const [minRating, setMinRating] = React.useState('');`
   - Aggiungi la condizione di filtraggio:
     ```javascript
     const filteredFilms = films.filter(film => {
         const matchTitolo = !titolo || (film.titolo && film.titolo.toLowerCase().includes(titolo.toLowerCase().trim()));
         const matchRating = !minRating || (film.mediaVoti && film.mediaVoti >= parseFloat(minRating));
         return matchTitolo && matchRating;
     });
     ```
   - Aggiungi il controllo UI JSX:
     ```jsx
     <select className="form-control" value={minRating} onChange={e => setMinRating(e.target.value)}>
         <option value="">Tutti i voti</option>
         <option value="4.0">⭐ 4+ Stelle</option>
         <option value="4.5">⭐ 4.5+ Stelle</option>
     </select>
     ```
2. **Motivazione**:
   - React ricalcola istantaneamente `filteredFilms` al cambio di stato (`useState`) e ri-renderizza il DOM in modo efficiente senza interpellare il server.
