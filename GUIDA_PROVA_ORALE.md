# Guida Completa per la Prova Orale - SIW Cinema

Questa guida è strutturata specificamente per prepararsi alla **prova orale** e alle **modifiche live richieste dal docente**, coprendo ciascuno dei 13 scenari tipici con il codice esatto, i passaggi e le motivazioni teoriche/architetturali da esporre alla commissione d'esame.

---

## 📋 Indice degli Scenari d'Esame

1. [Aggiunta di un Attributo o di una Nuova Relazione tra Entità](#1-aggiunta-di-un-attributo-o-di-una-nuova-relazione)
2. [Modifica di un Mapping JPA (Cascade, Fetch, ManyToOne LAZY)](#2-modifica-di-un-mapping-jpa)
3. [Implementazione di una Nuova Query (Derived Method o JPQL)](#3-implementazione-di-una-nuova-query)
4. [Aggiunta di un Caso d'Uso Completo (Duplicazione Proiezione / Cambio Stato)](#4-aggiunta-di-un-caso-duse-completo)
5. [Regole di Business: Sovrapposizione Oraria Sala con Durata Film & Vincoli Temporali](#5-regole-di-business-sovrapposizione-oraria-sala-con-durata-film--vincoli-temporali)
6. [Aggiunta o Modifica di un Metodo nel Service Layer](#6-aggiunta-o-modifica-di-un-metodo-del-service-layer)
7. [Modifica della Gestione Transazionale (@Transactional, Isolation, Rollback)](#7-modifica-della-gestione-transazionale)
8. [Aggiunta di un Nuovo Endpoint REST con DTO](#8-aggiunta-di-un-endpoint-rest)
9. [Modifica della Configurazione di Spring Security](#9-modifica-della-configurazione-di-spring-security)
10. [Modifica della Strategia di Fetch (LAZY vs EAGER)](#10-modifica-della-strategia-di-fetch)
11. [Individuazione e Risoluzione di un Problema N+1](#11-problema-delle-n1-query)
12. [Modifica di un Componente React](#12-modifica-di-un-componente-react)
13. [Difesa Architetturale & Risoluzione dei Warning dell'Audit](#13-difesa-architetturale-e-scelte-progettuali)

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

### Domanda tipica: "Perché specificare esplicitamente FetchType.LAZY sulle relazioni @ManyToOne?"
1. **Contesto JPA**:
   - In JPA, le relazioni `@OneToMany` e `@ManyToMany` usano di default `FetchType.LAZY`.
   - Al contrario, le relazioni `@ManyToOne` e `@OneToOne` usano di default **`FetchType.EAGER`**.
2. **Come intervenire su un'entità (es. `Film.java` o `Proiezione.java`)**:
   ```java
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "regista_id")
   private Regista regista;
   ```
3. **Conseguenze da discutere**:
   - Con `FetchType.LAZY`, chiamando `filmRepository.findById(id)` Hibernate non esegue subito la JOIN con `Regista`.
   - Quando il caso d'uso necessita del dato collegato (es. scheda di dettaglio), si usa una query mirata con `JOIN FETCH` nel repository, scongiurando query EAGER implicite e non richieste.

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

> [!NOTE]
> Nel progetto, il cambio rapido di stato delle proiezioni (`SCHEDULED`, `COMPLETED`, `CANCELLED`) è **già implementato** tramite `POST /proiezioni/{id}/stato` che invoca `proiezioneService.updateStato(id, stato)`.

### Scenario d'Esame: "Consentire all'amministratore di duplicare una proiezione su una nuova data e ora"
1. **Nel Service (`ProiezioneService.java`)**:
   ```java
   @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
   public Proiezione duplicaProiezione(Long idOriginale, LocalDate nuovaData, LocalTime nuovaOra) {
       Proiezione sorgente = getProiezione(idOriginale);
       if (sorgente == null) {
           throw new IllegalArgumentException("Proiezione originale non trovata: " + idOriginale);
       }
       
       Proiezione copia = new Proiezione();
       copia.setData(nuovaData);
       copia.setOra(nuovaOra);
       copia.setStato(StatoProiezione.SCHEDULED);
       
       // Sfrutta il metodo transazionale atomico già testato che convalida date e sovrapposizioni
       return programmaNuovaProiezione(copia, sorgente.getFestival().getId(), sorgente.getFilm().getId(), sorgente.getSala().getId());
   }
   ```
2. **Nel Controller (`ProiezioneController.java`)**:
   ```java
   @PostMapping("/proiezioni/{id}/duplica")
   public String duplicaProiezione(@PathVariable("id") Long id,
                                   @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                   @RequestParam("ora") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime ora,
                                   RedirectAttributes redirectAttributes) {
       try {
           Proiezione duplicata = proiezioneService.duplicaProiezione(id, data, ora);
           redirectAttributes.addFlashAttribute("successMessage", "Proiezione duplicata con successo!");
           return "redirect:/proiezione/" + duplicata.getId();
       } catch (Exception e) {
           redirectAttributes.addFlashAttribute("errorMessage", "Impossibile duplicare: " + e.getMessage());
           return "redirect:/proiezione/" + id;
       }
   }
   ```
3. **Nel Template HTML (`proiezione/detail.html`)**:
   ```html
   <form th:if="${isAdmin}" th:action="@{'/proiezioni/' + ${proiezione.id} + '/duplica'}" method="post" style="display: flex; gap: 0.5rem; align-items: center; margin-top: 1rem;">
       <input type="date" name="data" required class="form-control" style="width: auto;">
       <input type="time" name="ora" required class="form-control" style="width: auto;">
       <button type="submit" class="btn btn-sm btn-primary">
           <i class="fa-solid fa-copy"></i> Duplica Evento
       </button>
   </form>
   ```

---

## 5. Regole di Business: Sovrapposizione Oraria Sala con Durata Film & Vincoli Temporali

### Scenario A: Conflitto di Sovrapposizione Oraria di Sala con Calcolo Durata Film
È il **vincolo di consistenza fondamentale** implementato in `ProiezioneValidator.java` e `ProiezioneService.hasRoomConflict`:
1. **Algoritmo di collisione temporale**:
   Due proiezioni $P_1$ e $P_2$ nella stessa sala e nella stessa data sono in conflitto se e solo se i loro intervalli temporali si sovrappongono:
   $$\text{Inizio}(P_1) < \text{Fine}(P_2) \quad \land \quad \text{Inizio}(P_2) < \text{Fine}(P_1)$$
   dove $\text{Fine} = \text{Inizio} + \text{durata in minuti}$.
2. **Implementazione nel codice (`ProiezioneValidator.java`)**:
   ```java
   List<Proiezione> proiezioniGiorno = proiezioneRepository.findBySalaAndDataExcludingId(
           proiezione.getSala(), proiezione.getData(), proiezione.getId());

   LocalTime newStart = proiezione.getOra();
   int durata = (proiezione.getFilm() != null && proiezione.getFilm().getDurata() != null) 
           ? proiezione.getFilm().getDurata() : 120;
   LocalTime newEnd = newStart.plusMinutes(durata);

   for (Proiezione p : proiezioniGiorno) {
       LocalTime extStart = p.getOra();
       int extDurata = (p.getFilm() != null && p.getFilm().getDurata() != null) ? p.getFilm().getDurata() : 0;
       LocalTime extEnd = extStart.plusMinutes(extDurata);

       if (extStart.isBefore(newEnd) && newStart.isBefore(extEnd)) {
           errors.reject("proiezione.conflict", "La sala selezionata è già occupata per l'intervallo orario specificato.");
           break;
       }
   }
   ```
3. **Domanda tipica live del docente:** *"Aggiungi un intervallo minimo di pulizia/pausa sala di 15 minuti tra un film e l'altro"*:
   - *Modifica immediata*:
     ```java
     LocalTime extEnd = extStart.plusMinutes(extDurata + 15); // +15 min intervallo sanificazione/pulizia
     ```

### Scenario B: Coerenza Temporale tra Film e Festival
"Un film può partecipare a un festival solo se l'anno di produzione del film non è successivo all'anno del festival":
```java
if (film.getAnno() > festival.getAnno()) {
    errors.reject("film.anno.invalido", 
        "Il film non può essere prodotto in un anno successivo a quello del festival (" + festival.getAnno() + ")");
}
```

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
  - Impedisce le *Dirty Reads* (lettura di dati modificati ma non ancora committati da altre transazioni concorrenti), fondamentale per verificare la disponibilità delle sale senza falsi positivi.

### Il Caso d'Uso Transazionale Atomico Multi-Entità nel Progetto:
In `ProiezioneService.java`:
```java
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public Proiezione programmaNuovaProiezione(Proiezione proiezione, Long festivalId, Long filmId, Long salaId) {
    Festival festival = festivalRepository.findById(festivalId)
            .orElseThrow(() -> new IllegalArgumentException("Festival non trovato con ID: " + festivalId));
    Film film = filmRepository.findById(filmId)
            .orElseThrow(() -> new IllegalArgumentException("Film non trovato con ID: " + filmId));
    Sala sala = salaRepository.findById(salaId)
            .orElseThrow(() -> new IllegalArgumentException("Sala non trovata con ID: " + salaId));

    if (proiezione.getData().isBefore(festival.getDataInizio()) || proiezione.getData().isAfter(festival.getDataFine())) {
        throw new IllegalStateException("La data della proiezione non rientra nelle date del festival.");
    }

    if (hasRoomConflict(sala, proiezione.getData(), proiezione.getOra(), film.getDurata(), proiezione.getId())) {
        throw new IllegalStateException("La sala è già occupata nell'intervallo richiesto.");
    }

    if (!festival.getFilm().contains(film)) {
        festival.addFilm(film);
        festivalRepository.save(festival);
    }

    proiezione.setFestival(festival);
    proiezione.setFilm(film);
    proiezione.setSala(sala);
    if (proiezione.getStato() == null) {
        proiezione.setStato(StatoProiezione.SCHEDULED);
    }

    return proiezioneRepository.save(proiezione);
}
```
**Punto chiave da spiegare:** Il controller `ProiezioneController.saveProiezione` invoca direttamente questo metodo quando `proiezione.getId() == null`. Se una qualsiasi verifica fallisce (es. sala occupata), l'eccezione viene catturata dal controller che aggiunge l'errore al `BindingResult`, garantendo sia il rollback nel database sia un messaggio amichevole per l'utente nel form Thymeleaf.

---

## 8. Aggiunta di un Endpoint REST con DTO

### Scenario: "Aggiungi un endpoint REST per restituire i film con media voto >= 4"
1. **Nel Controller REST (`FilmRestController.java`)**:
   ```java
   @GetMapping("/top-rated")
   public ResponseEntity<List<FilmDTO>> getTopRatedFilms() {
       List<Film> topFilms = filmService.getAllFilms().stream()
           .filter(f -> f.getMediaVoti() != null && f.getMediaVoti() >= 4.0)
           .toList();
       
       List<FilmDTO> dtos = topFilms.stream()
           .map(FilmDTO::new)
           .toList();
           
       return ResponseEntity.ok(dtos);
   }
   ```
2. **Motivazione da discutere**:
   - Restituisce `ResponseEntity<List<FilmDTO>>` con codice `200 OK`.
   - L'uso di `FilmDTO` serializza solo i campi necessari senza esporre entità JPA e senza triggerare lazy loading accidentale fuori sessione o loop ciclici JSON.

---

## 9. Modifica della Configurazione di Spring Security & Dual Login (Form + Google OAuth2)

### Configurazione Attuale del Progetto (`SecurityConfiguration.java`):
```java
.authorizeHttpRequests(auth -> auth
    // 1. Risorse statiche
    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

    // 2. Endpoint REST API pubblici (GET)
    .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

    // 3. Autenticazione & Registrazione (inclusi endpoint OAuth2)
    .requestMatchers("/login", "/register", "/success").permitAll()
    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

    // 4. CRUD Amministratore (Dashboard e cancellazioni protette)
    .requestMatchers("/admin/**").hasAuthority("ADMIN")
    .requestMatchers("/festivals/nuovo", "/festivals/salva", "/festivals/modifica/**", "/festivals/*/gestione-film", "/festivals/*/film/**", "/festivals/*/elimina", "/festivals/elimina/**").hasAuthority("ADMIN")
    .requestMatchers("/films/nuovo", "/films/salva", "/films/modifica/**", "/films/*/elimina", "/films/elimina/**").hasAuthority("ADMIN")
    .requestMatchers("/registi/nuovo", "/registi/salva", "/registi/modifica/**", "/registi/*/elimina", "/registi/elimina/**").hasAuthority("ADMIN")
    .requestMatchers("/sale/nuova", "/sale/salva", "/sale/modifica/**", "/sale/*/elimina", "/sale/elimina/**").hasAuthority("ADMIN")
    .requestMatchers("/proiezioni/nuova", "/proiezioni/salva", "/proiezioni/modifica/**", "/proiezioni/elimina/**", "/proiezioni/*/elimina", "/proiezioni/*/stato").hasAuthority("ADMIN")

    // 5. Utenti Autenticati (Recensioni e Profilo)
    .requestMatchers("/recensioni/**", "/profilo/**").hasAnyAuthority("ADMIN", "USER", "DEFAULT")

    // 6. Consultazione pubblica (GET)
    .requestMatchers(HttpMethod.GET, "/", "/index").permitAll()
    .requestMatchers(HttpMethod.GET, "/festivals", "/festival/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/films", "/film/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/regista/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/sala/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/proiezioni", "/proiezione/**").permitAll()

    // 7. Fallback
    .anyRequest().authenticated()
)
```

### Architettura Dual-Login e Google OAuth2 Tollerante ai Guasti:
1. **Come funziona il login Google OAuth2?**
   - L'utente clicca su *"Accedi con Google"* (endpoint `/oauth2/authorization/google`).
   - Google autentica l'utente e reindirizza con il codice a `/login/oauth2/code/google`.
   - `CustomOAuth2UserService` recupera i claims (`email`, `given_name`, `family_name`, `sub`).
   - Viene cercato un `Utente` esistente per email: se non esiste, viene auto-provisionato un nuovo `Utente` e create le `Credentials` con ruolo predefinito `USER` (prevenzione privilege escalation).
   - Viene restituito un principal `CustomOAuth2User` compatibile con `CredentialsService.getCurrentCredentials()`.
2. **Resilienza Offline (Exam-Ready)**:
   - Grazie a `GoogleOAuthCondition.java`, se le credenziali Google nel file `.env` sono vuote, l'applicazione NON va in crash all'avvio (`NoSuchBeanDefinitionException`), ma disabilita selettivamente il pulsante OAuth2 e avvia regolarmente il form login locale.

### Modifica tipica richiesta: "Permetti solo agli utenti registrati di visualizzare i dettagli della sala cinematografica"
1. Rimuovere `.requestMatchers(HttpMethod.GET, "/sala/**").permitAll()` dalle regole pubbliche.
2. Aggiungere prima della consultazione pubblica:
   ```java
   .requestMatchers(HttpMethod.GET, "/sala/**").hasAnyAuthority("USER", "ADMIN", "DEFAULT")
   ```

---

## 10. Modifica della Strategia di Fetch

### Domanda: "Cosa succede se metto `@ManyToOne(fetch = FetchType.EAGER)` su tutte le relazioni di `Proiezione`?"
- **Risposta**:
  - Quando si carica anche una singola proiezione, Hibernate caricherà immediatamente `Film`, `Festival`, `Sala` e a cascata il `Regista` del film e le collezioni se EAGER.
  - Se si fa `proiezioneRepository.findAll()`, Hibernate eseguirà query con molteplici `LEFT OUTER JOIN` o peggio $N$ subquery separate per ogni entità collegata, aumentando drasticamente il consumo di memoria RAM e rallentando i tempi di risposta.
  - **Best Practice SIW**: Mantenere il fetch **`LAZY`** a livello di mapping e applicare il fetch mirato **`JOIN FETCH`** nelle sole query dei casi d'uso che richiedono tali dati.

---

## 11. Problema delle N+1 Query

### Come mostrarlo e spiegarlo all'esame:
1. **Esegui il Test Automatico**:
   ```bash
   ./mvnw test -Dtest=DataAccessPerformanceAnalysisTest
   ```
2. **Spiegazione delle metriche a console**:
   - **Caso LAZY non ottimizzato**: 1 query iniziale per recuperare le 10 proiezioni + query separate per ciascun Festival, Film e Sala = **13 query SQL**.
   - **Caso Ottimizzato con `JOIN FETCH`**: **1 singola query SQL** con `INNER JOIN`:
     ```sql
     SELECT p, f, fl, s FROM Proiezione p 
     JOIN FETCH p.festival f 
     JOIN FETCH p.film fl 
     JOIN FETCH p.sala s
     WHERE p.festival.id = :festId
     ```
   - **Risultato:** Riduzione del 92% del carico sul database e tempo di esecuzione ridotto da ~11-26 ms a ~4-5 ms.

---

## 12. Modifica di un Componente React

### Scenario: "Aggiungere un filtro per voto minimo nel componente React del catalogo film"
1. **Nel file `film/list.html`**:
   - Aggiungi lo stato:
     ```javascript
     const [minRating, setMinRating] = React.useState('');
     ```
   - Aggiungi la condizione di filtraggio in `filteredFilms`:
     ```javascript
     const filteredFilms = films.filter(film => {
         const matchTitolo = !titolo || (film.titolo && film.titolo.toLowerCase().includes(titolo.toLowerCase().trim()));
         const matchRating = !minRating || (film.mediaVoti && film.mediaVoti >= parseFloat(minRating));
         return matchTitolo && matchRating;
     });
     ```
   - Aggiungi il controllo UI JSX nel blocco dei filtri:
     ```jsx
     <select className="form-control" value={minRating} onChange={e => setMinRating(e.target.value)}>
         <option value="">Tutti i voti</option>
         <option value="4.0">⭐ 4+ Stelle</option>
         <option value="4.5">⭐ 4.5+ Stelle</option>
     </select>
     ```
2. **Motivazione**:
   - React gestisce il rendering client-side in memoria; non viene inviata alcuna richiesta di rete aggiuntiva e il catalogo si aggiorna in tempo reale.

---

## 13. Difesa Architetturale e Scelte Progettuali

### 1. Perché l'entità `Film.java` contiene solo `getMediaVoti()` ed evita calcoli complessi?
- **R:** Inserire algoritmi complessi di calcolo (es. percentuali e distribuzioni con stream su collezioni `@OneToMany`) direttamente all'interno delle entità JPA viola la separazione dei layer (le entità devono essere POJO puri di modello dati). Inoltre, essendo `recensioni` una collezione caricata in modalità `LAZY`, l'invocazione di tali getter fuori da una transazione attiva provocherebbe una `LazyInitializationException` se si disabilitasse la proprietà `spring.jpa.open-in-view=false`. Mantenere solo calcoli leggeri protegge la robustezza dell'applicazione.

### 2. Come è garantito il ciclo CRUD Completo per l'Amministratore?
- **R:** L'amministratore può creare, modificare ed eliminare tutte e 4 le entità minime del bando (`Festival`, `Film`, `Regista`, `Sala`) nonché le `Proiezioni`. Per sicurezza contro attacchi CSRF, le eliminazioni sono mappate su HTTP `POST` con form dedicati e popup di conferma JavaScript (`confirm(...)`) sia nella dashboard amministratore (`/admin/dashboard`) sia nelle pagine di dettaglio, con fallback su HTTP `GET` per garantire la massima resilienza.

### 3. Come è protetta l'ownership delle recensioni contro ID Tampering?
- **R:** Nel metodo `RecensioneController.saveRecensione` (`POST /recensioni/salva`), se l'oggetto recensione contiene un `id != null`, prima del salvataggio il sistema recupera la recensione memorizzata nel database e verifica tramite `canUserModify(...)` che l'autore coincida con l'utente autenticato in sessione. Se un utente malintenzionato tenta di inviare una richiesta POST forzando l'ID di un'altra recensione, l'operazione viene respinta.

### 4. Perché le immagini/locandine usano Data URL Base64?
- **R:** Durante la demo d'esame, l'applicazione deve essere completamente portabile e autonoma, senza dipendere da directory assolute su filesystem locale (es. `/uploads`) o permessi di scrittura sul sistema operativo. Memorizzando l'immagine come Data URL Base64 (o accettando un URL esterno), il database PostgreSQL contiene l'intero asset grafico. In un contesto aziendale enterprise, si spiegherà al docente che la soluzione canonica prevede il salvataggio dei binari su storage a oggetti dedicati (es. Amazon S3 o MinIO) salvando a DB esclusivamente l'URI canonico.
