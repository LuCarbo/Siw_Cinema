# SIW Compliance Report - Audit di Conformità Rigoroso
**Corso:** Sistemi Informativi sul Web (SIW) - Appello Settembre 2026  
**Ateneo:** Università degli Studi Roma Tre  
**Progetto:** SIW Cinema (Gestione Festival Cinematografici)  
**Candidato:** Luca Carbonetti  
**Ruolo del Revisore:** Senior Software Engineer & Revisore Accademico  
**Data Audit:** 3 Settembre 2026  

---

## 1. Executive Summary

Il presente documento costituisce l'audit formale, rigoroso e indipendente dello stato di conformità del progetto software **SIW Cinema** rispetto ai requisiti obbligatori e alle linee guida didattico-accademiche del corso di Sistemi Informativi sul Web (SIW).

L'ispezione ha esaminato tutti i file sorgente Java (Entità, Repository, Service, Controller MVC, RestController, Validatori, Security, Init), i template Thymeleaf, i componenti React, la configurazione Maven (`pom.xml`), le proprietà applicative (`application.properties`) e la suite di test automatizzati (inclusa la verifica sperimentale delle prestazioni di accesso ai dati N+1).

### Tabella Riassuntiva dello Stato di Conformità

| Area di Valutazione | Requisito del Docente | Esito | Note Sintetiche |
| :--- | :--- | :---: | :--- |
| **1. Modello Dati ed Entità JPA** | 7 Entità minime + vincoli relazionali | `PASS` *(con Warning)* | Tutte le 7 entità presenti + Credentials. Warning su `@ManyToOne` con fetch EAGER di default. |
| **2.1 Funzionalità Pubbliche** | Esplorazione festival, film, registi, proiezioni | `PASS` | Viste Thymeleaf complete e navigabili, arricchite da catalogo React. |
| **2.2 Funzionalità Utente USER** | Recensioni film: inserimento, modifica, cancellazione | `PASS` *(con Warning)* | Unicità garantita. Ownership protetta a livello Service e REST, ma debole su POST MVC (`/salva`). |
| **2.3 Funzionalità Admin** | CRUD Festival, Film, Regista, Sala | `FAIL` | Operazione di cancellazione (Delete) assente dai Controller/Viste per Festival, Film, Registi e Sale. Manca scheda dettaglio Sala. |
| **2.4 Consistenza Sovrapposizioni** | Conflitto temporale sala tenendo conto della durata film | `FAIL` | Controllo basato solo sull'uguaglianza dell'orario di inizio (`p.ora = :ora`); non calcola `ora + durata`. |
| **3. Architettura e Transazioni** | Layered Architecture, `@Transactional`, Rollback atomico | `PASS` *(con Warning)* | Separazione layer impeccabile. Caso d'uso atomico implementato e testato, ma non agganciato al Controller web. |
| **4. Sicurezza (Spring Security)** | BCrypt, filtri per ruolo, protezione URL | `PASS` *(con Warning)* | Configurazione solida. Warning per cancellazione recensione esposta via HTTP GET (CSRF). |
| **5. Frontend Ibrido e REST API** | Thymeleaf + Componente React + REST Controller | `PASS` | Ottima integrazione React 18, semantica HTTP rigorosa, DTO puliti ed ExceptionHandler centralizzato. |
| **6. Analisi Sperimentale N+1** | Test eseguibile LAZY vs JOIN FETCH con metriche console | `PASS` | Test JUnit `DataAccessPerformanceAnalysisTest` pienamente funzionante ed eseguibile live all'orale. |

### Indice Globale di Conformità Stimato: **86%**
- **Requisiti Pienamente Soddisfatti (`[PASS]`):** 6 / 8 macro-aree.
- **Punti Critici Non Conformi (`[FAIL]`):** 2 violazioni puntuali (CRUD incompleto per Admin e Controllo sovrapposizione sala parziale).
- **Avvisi di Rischio Accademico (`[WARNING]`):** 4 punti (fetch default ManyToOne, mancato ownership check su POST recensione, delete recensione via GET, mancata invocazione del metodo multi-entità transazionale dal form web).

---

## 2. Audit Sezione per Sezione

### 1. Modello Dati ed Entità JPA (Specifiche Sez. 3 & 11)
**Esito Complessivo:** `[PASS]` *(con Avvisi Tecnici)*

#### Classi Ispezionate:
- `it.uniroma3.siw.model.Festival`
- `it.uniroma3.siw.model.Film`
- `it.uniroma3.siw.model.Regista`
- `it.uniroma3.siw.model.Sala`
- `it.uniroma3.siw.model.Proiezione`
- `it.uniroma3.siw.model.Recensione`
- `it.uniroma3.siw.model.Utente`
- `it.uniroma3.siw.model.Credentials`
- `it.uniroma3.siw.model.StatoProiezione`

#### Riscontro Dettagliato dei Requisiti:
1. **`Festival`**: Modella tutti gli attributi richiesti: `nome` (@NotBlank), `anno` (@NotNull, @Min(1900)), `citta` (@NotBlank), `dataInizio` (@NotNull), `dataFine` (@NotNull), `descrizione` (@Column(length = 3000)). Include l'attributo extra `immagine` (TEXT). Relazioni modellate correttamente: `@ManyToMany` con `Film` (owning side con tabella `festival_film`), `@OneToMany` con `Proiezione` (`mappedBy = "festival"`, `cascade = CascadeType.REMOVE`, `orphanRemoval = true`).
2. **`Film`**: Modella tutti gli attributi richiesti: `titolo` (@NotBlank), `anno` (@NotNull, @Min(1888)), `durata` (@NotNull, @Min(1)), `genere` (@NotBlank), `paeseProduzione` (@NotBlank). Include l'attributo extra `locandina` (TEXT). Relazioni: `@ManyToMany(mappedBy = "film")` con `Festival`, `@ManyToOne` con `Regista` (`@JoinColumn(name = "regista_id")`), `@OneToMany` con `Proiezione`, `@OneToMany` con `Recensione`.
3. **`Regista`**: Modella `nome` (@NotBlank), `cognome` (@NotBlank), `dataNascita` (@NotNull, @Past), `nazionalita` (@NotBlank) e l'extra `foto`. Relazione: `@OneToMany(mappedBy = "regista")` con `Film`.
4. **`Sala`**: Modella `nome` (@NotBlank), `indirizzo` (@NotBlank), `capienza` (@NotNull, @Min(1)). Relazione: `@OneToMany(mappedBy = "sala")` con `Proiezione`.
5. **`Proiezione`**: Modella `data` (@NotNull), `ora` (@NotNull), `stato` (@NotNull, enum `StatoProiezione`: `SCHEDULED`, `COMPLETED`, `CANCELLED`). Relazioni: `@ManyToOne` con `Festival`, `Film` e `Sala` con vincolo `nullable = false`.
6. **`Recensione`**: Modella `testo` (@NotBlank, length = 2000), `voto` (@NotNull, @Min(1), @Max(5)), `data` (LocalDate). Extra: `titolo`. Relazioni: `@ManyToOne` con `Film` e `Utente` (`autore`).
   - **Vincolo di Unicità:** È implementato a livello DB tramite annotazione fisica `@Table(name = "recensioni", uniqueConstraints = { @UniqueConstraint(columnNames = {"film_id", "utente_id"}) })`. Pienamente conforme alla specifica.
7. **`Utente` & `Credentials`**: I requisiti di autenticazione (`username`, `password`, `ruolo` USER/ADMIN) sono modellati dividendo l'anagrafica (`Utente`: nome, cognome, email) dalle credenziali di accesso (`Credentials`: username univoco, password BCrypt, role). Questa suddivisione rispecchia fedelmente l'impostazione didattica canonica del corso SIW di Roma Tre.

#### Verifiche Specifiche JPA:
- **`equals()` e `hashCode()`**: Nessuna entità fa affidamento su chiavi surrogate primarie autogenerate (`id`), evitando anomalie con entità transient prima della persistenza. Vengono usate chiavi di business: `Festival(nome, anno)`, `Film(titolo, anno)`, `Regista(nome, cognome, dataNascita)`, `Sala(nome, indirizzo)`, `Utente(email)`, `Credentials(username)`.
  - *Nota di attenzione:* In `Proiezione`, `equals()` usa `Objects.equals(sala, that.sala)` e in `Recensione` usa `Objects.equals(film, that.film)` e `Objects.equals(autore, that.autore)`. Sebbene non errato, confrontare riferimenti a entità correlate può scatenare l'inizializzazione di proxy se gestite come LAZY.
- **`toString()`**: Nessuna entità sovrascrive `toString()` includendo collezioni bidirezionali, scongiurando completamente loop infiniti e `StackOverflowError`.
- **Strategie di Fetching (`FetchType`):**
  - Le relazioni `@OneToMany` e `@ManyToMany` ereditano il default JPA `FetchType.LAZY`: **CONFORME**.
  - **`[WARNING]`**: Le associazioni `@ManyToOne` (`Film.regista`, `Proiezione.festival`, `Proiezione.film`, `Proiezione.sala`, `Recensione.film`, `Recensione.autore`) non definiscono esplicitamente `fetch = FetchType.LAZY`. In JPA, il default per `@ManyToOne` è **`EAGER`**. Sebbene le query personalizzate usino `JOIN FETCH`, invocazioni standard dei repository (come `findById`) caricano le entità collegate in modalità eager. Nelle discussioni orali di SIW viene frequentemente richiesta la dichiarazione esplicita di `fetch = FetchType.LAZY` su tutti i mapping.

---

### 2. Casi d'Uso e Logica Applicativa (Sez. 4 & 11)
**Esito Complessivo:** `[FAIL]` *(a causa di 2 violazioni critiche)*

#### 2.1 Funzionalità Pubbliche (Anonime): `[PASS]`
- Consultazione Festival (`GET /festivals`): Elenco completo con filtri per città e nome.
- Dettaglio Festival (`GET /festival/{id}`): Informazioni della rassegna, film presentati e calendario proiezioni associate.
- Consultazione Catalogo Film (`GET /films`): Implementata con visualizzazione integrata React e filtri client-side.
- Dettaglio Film (`GET /film/{id}`): Scheda tecnica, regista associato, festival partecipanti, proiezioni programmate e lista recensioni della community.
- Dettaglio Regista (`GET /regista/{id}`): Scheda anagrafica e filmografia completa.
- Calendario Proiezioni (`GET /proiezioni`): Visualizzazione di tutti gli eventi filtrabili per festival, film, sala e data.

#### 2.2 Funzionalità Utente Registrato (Ruolo USER): `[PASS]` *(con Warning)*
- Inserimento Recensione (`GET /recensioni/nuova/{filmId}`, `POST /recensioni/salva`): L'utente autenticato può scrivere la propria recensione. Se ha già recensito, viene reindirizzato alla modifica.
- Modifica Recensione (`GET /recensioni/modifica/{id}`): Form precompilato accessibile solo se l'utente loggato è l'autore (o admin).
- Cancellazione Recensione (`GET /recensioni/elimina/{id}`): Rimozione autorizzata con verifica di proprietà.
- Profilo Utente (`GET /profilo`): Riepilogo dati personali e storico delle recensioni pubblicate.
- **Verifica Vincolo Recensione Univoca:** Pienamente rispettato grazie al triplo livello di controllo:
  1. Constraint di tabella PostgreSQL (`film_id`, `utente_id`);
  2. `RecensioneValidator.validate(...)` che rifiuta duplicati generando l'errore `recensione.duplicate`;
  3. `RecensioneRestController` che risponde con codice `409 Conflict`.
- **`[WARNING 1 - Sicurezza Ownership su POST]`**: Nel metodo `RecensioneController.saveRecensione` (`POST /recensioni/salva`), se un utente malintenzionato invia una richiesta POST forzando l'`id` di una recensione altrui (ID tampering), il controller imposta `autore = currentUser` ed esegue `saveRecensione()`. Se l'utente non aveva ancora recensito quel film, il validatore non rileva duplicati e la recensione originaria della vittima viene sovrascritta. È indispensabile aggiungere un controllo esplicito: se `recensione.getId() != null`, verificare che appartenga all'utente in sessione prima di salvare.
- **`[WARNING 2 - Metodo HTTP per la Cancellazione]`**: L'eliminazione della recensione è mappata su HTTP GET (`@GetMapping("/elimina/{id}")`). In ambiente web, le operazioni distruttive di stato devono avvenire tramite HTTP POST o DELETE per proteggere l'utente da attacchi CSRF veicolati tramite tag `<img>` o link malevoli.

#### 2.3 Funzionalità Amministratore (Ruolo ADMIN): `[FAIL]`
- **Associazione e Disassociazione Film <-> Festival:** `[PASS]`. Gestita correttamente tramite `GET /festivals/{id}/gestione-film`, `POST /festivals/{id}/film/aggiungi` e `POST /festivals/{id}/film/rimuovi/{filmId}`.
- **Programmazione, Modifica e Cancellazione Proiezioni:** `[PASS]`. Endpoint dedicati (`/proiezioni/nuova`, `/proiezioni/modifica/{id}`, `/proiezioni/elimina/{id}`).
- **`[FAIL 1 - CRUD Incompleto su Festival, Film, Registi e Sale]`**:
  - La specifica didattica richiede il ciclo **CRUD completo** gestito dall'amministratore per le 4 entità principali.
  - Nel layer di servizio (`FestivalService`, `FilmService`, `RegistaService`, `SalaService`), i metodi `deleteFestival()`, `deleteFilm()`, `deleteRegista()` e `deleteSala()` sono regolarmente implementati.
  - Tuttavia, **nei rispettivi Controller (`FestivalController`, `FilmController`, `RegistaController`, `SalaController`) non esiste alcun endpoint di eliminazione**, né nella dashboard amministratore (`admin/dashboard.html`) è presente alcun pulsante o form per eliminare festival, film, registi o sale.
  - Inoltre, per l'entità `Sala`, **manca completamente la pagina di dettaglio** (`templates/sala/detail.html`) e il relativo endpoint `GET /sala/{id}` in `SalaController`, nonostante nel template `proiezione/detail.html` (righe 33 e 87) siano presenti collegamenti ipertestuali espliciti `<a th:href="@{'/sala/' + ${proiezione.sala.id}}">` che andranno in `404 Not Found`.
- **`[FAIL 2 - VINCOLO CRITICO CONSISTENZA: Controllo Sovrapposizione Oraria nella Stessa Sala]`**:
  - *Requisito della specifica:* "Verifica se nella programmazione di una proiezione viene controllata la sovrapposizione temporale nella stessa sala (tenendo conto dell'orario di inizio e della durata del film)".
  - *Ispezione del codice:* In `ProiezioneValidator.java` (riga 39) e in `ProiezioneRepository.java` (riga 41), il controllo conflitti è implementato come segue:
    ```sql
    SELECT p FROM Proiezione p 
    WHERE p.sala = :sala AND p.data = :data AND p.ora = :ora AND (:id IS NULL OR p.id <> :id)
    ```
  - **Gravità:** Il controllo verifica unicamente che due film non inizino allo **stesso esatto minuto**. Se la Proiezione A inizia alle ore 20:00 con durata 120 minuti (fine prevista ore 22:00) e la Proiezione B viene inserita nella medesima sala alle ore 20:30, il sistema **NON rileva alcun conflitto** e permette l'inserimento, violando frontalmente il vincolo di sovrapposizione fisica della sala.

---

### 3. Architettura a Livelli e Gestione delle Transazioni (Sez. 6 & 7)
**Esito Complessivo:** `[PASS]` *(con Avviso Architetturale)*

#### File Ispezionati:
- `it.uniroma3.siw.service.*` (8 classi di servizio)
- `it.uniroma3.siw.controller.*` (11 classi controller)
- `it.uniroma3.siw.ProiezioneTransactionTest`

#### Riscontro Dettagliato:
1. **Separazione delle Responsabilità (Layering):**
   - I Controller e i RestController non eseguono logica di calcolo, aggregazione o query JPQL dirette; delegano interamente ai rispettivi Service.
   - La validazione dell'input avviene prima della persistenza tramite `@Valid`, `BindingResult` e validatori Spring dedicati (`Validator`).
2. **Gestione Transazionale (`@Transactional`):**
   - Tutti i metodi di interrogazione e consultazione nei Service sono annotati in modo rigoroso con `@Transactional(readOnly = true)`. Questo consente a Hibernate di disattivare il dirty checking (risparmio di memoria e CPU) e di operare in sola lettura sul database PostgreSQL.
   - Tutti i metodi di scrittura/aggiornamento sono protetti da `@Transactional`.
3. **Caso d'Uso Multi-Entità Atomico con Rollback:**
   - In `ProiezioneService` è presente il metodo:
     ```java
     @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
     public Proiezione programmaNuovaProiezione(Proiezione proiezione, Long festivalId, Long filmId, Long salaId)
     ```
   - Il metodo coordina 4 repository (`FestivalRepository`, `FilmRepository`, `SalaRepository`, `ProiezioneRepository`), garantisce l'isolamento contro letture sporche, valida la coerenza delle date, associa automaticamente il film al festival se assente e salva la proiezione.
   - In caso di anomalie (es. sala occupata o festival errato), solleva un'eccezione che induce il rollback atomico completo.
   - La correttezza del rollback è validata e confermata dal test automatico `ProiezioneTransactionTest`.
   - **`[WARNING Architetturale]`**: Nel controller web `ProiezioneController.saveProiezione` (riga 109), viene invocato `proiezioneService.saveProiezione(proiezione)` anziché `proiezioneService.programmaNuovaProiezione(...)`. Di conseguenza, mentre il metodo transazionale multi-entità esiste ed è testato, il form web esegue una versione semplificata che demanda la validazione unicamente al validatore Spring.

---

### 4. Sicurezza e Controllo degli Accessi (Sez. 5)
**Esito Complessivo:** `[PASS]` *(con Avviso)*

#### File Ispezionati:
- `it.uniroma3.siw.security.SecurityConfiguration`
- `it.uniroma3.siw.security.CustomUserDetailsService`
- `it.uniroma3.siw.controller.RecensioneController`
- `it.uniroma3.siw.controller.rest.RecensioneRestController`

#### Riscontro Dettagliato:
1. **Configurazione Spring Security 6:**
   - La classe `SecurityConfiguration` utilizza la convenzione moderna `SecurityFilterChain` bean.
   - Il componente `CustomUserDetailsService` converte l'utente e il ruolo nel formato `org.springframework.security.core.userdetails.User`.
2. **Cifratura Password:**
   - Tutte le password memorizzate nel database PostgreSQL vengono cifrate tramite algoritmo crittografico sicuro **BCrypt** (`BCryptPasswordEncoder`).
3. **Autorizzazione per Percorsi:**
   - Le risorse statiche (`/css/**`, `/js/**`, `/images/**`) e le viste pubbliche di consultazione (`/`, `/festivals`, `/films`, `/regista/**`, `/proiezioni`) sono accessibili a chiunque (`permitAll()`).
   - Gli endpoint GET delle API REST (`/api/**`) sono aperti a tutti in sola lettura (`permitAll()`).
   - L'accesso al pannello di controllo `/admin/**` e a tutti i form di creazione/modifica è riservato agli utenti aventi autorità `ADMIN`.
   - I percorsi di gestione recensioni e profilo (`/recensioni/**`, `/profilo/**`) richiedono autenticazione (`hasAnyAuthority("ADMIN", "USER", "DEFAULT")`).
   - Tutte le altre richieste non previste richiedono autenticazione (`anyRequest().authenticated()`).
4. **Verifica della Proprietà delle Recensioni (Ownership Check):**
   - Lato backend, il metodo `recensioneService.canUserModify(recensione, currentUser, isAdmin)` verifica che l'ID dell'autore coincida con l'ID dell'utente autenticato.
   - Nelle API REST (`RecensioneRestController`), le richieste PUT e DELETE di utenti non proprietari vengono respinte con lo stato HTTP `403 Forbidden`.
   - Nelle viste Thymeleaf, i pulsanti di modifica ed eliminazione sono renderizzati condizionalmente solo se l'utente loggato è l'autore effettivo o un amministratore.

---

### 5. Frontend Ibrido e API REST (Sez. 9 & 10)
**Esito Complessivo:** `[PASS]`

#### File Ispezionati:
- Template Thymeleaf: 21 file HTML in `src/main/resources/templates/`
- Componente React: integrato nel template `templates/film/list.html`
- Controller REST: `FestivalRestController`, `FilmRestController`, `ProiezioneRestController`, `RecensioneRestController`
- DTO: `FestivalDTO`, `FilmDTO`, `ProiezioneDTO`, `RecensioneDTO`
- Exception Handler: `RestExceptionHandler`

#### Riscontro Dettagliato:
1. **Frontend Server-Side Thymeleaf:**
   - L'applicazione adotta una struttura pulita e modulare, sfruttando il frammento comune `fragments/base.html` per l'intestazione HTML, la barra di navigazione con stato dinamico di autenticazione, la visualizzazione dei messaggi flash di alert e il footer.
   - Design moderno basato su CSS personalizzato (`style.css`), dark mode con palette coerente e responsive layout.
2. **Integrazione Client-Side React 18:**
   - Nel template `film/list.html`, è integrata l'applicazione React `FilmCatalogApp` tramite script CDN (`react.production.min.js`, `react-dom.production.min.js`, `babel.min.js`).
   - Utilizza gli standard correnti di React: `ReactDOM.createRoot()`, Hooks funzionali (`useState`, `useEffect`).
   - All'inizializzazione, esegue una richiesta asincrona `fetch('/api/movies')` e implementa il filtraggio dinamico e istantaneo in tempo reale su 4 parametri contemporanei (Titolo, Genere, Regista, Anno), con contatore dinamico dei risultati e pulsante di reset.
3. **Architettura RESTful e Conformità HTTP:**
   - I controller REST restituiscono oggetti **DTO** dedicati, disaccoppiando completamente il dominio JPA e scongiurando il rischio di ricorsioni cicliche JSON o eccezioni di lazy loading fuori sessione.
   - **Codici di Stato Rispettati:**
     - `GET`: restituisce `200 OK` oppure `404 Not Found`.
     - `POST`: restituisce `201 Created` con header `Location` contenente la URI della risorsa creata; `400 Bad Request` in caso di validazione fallita; `409 Conflict` se il film è già stato recensito dall'utente.
     - `PUT`: restituisce `200 OK` con il DTO aggiornato, `403 Forbidden` per tentativi non autorizzati, `404 Not Found` se inesistente.
     - `DELETE`: restituisce `204 No Content` in caso di successo, `403 Forbidden` o `404 Not Found`.
   - **Gestione Errori Centralizzata (`RestExceptionHandler`):** Classe `@RestControllerAdvice` che intercetta eccezioni (`IllegalArgumentException`, `IllegalStateException`, `MethodArgumentNotValidException`) e formatta payload JSON standardizzati con timestamp, status HTTP, messaggio descrittivo e path della richiesta.
4. **`[Discrepanza di Documentazione]`**: Nel file `README.md` (riga 89) è documentato un endpoint `GET /explorer` ("Esploratore React"). Tale endpoint non esiste nei controller applicativi (la vista React è incorporata direttamente in `GET /films`).

---

### 6. Analisi Sperimentale N+1 e Prestazioni JPA (Sez. 8.2)
**Esito Complessivo:** `[PASS]`

#### File Ispezionato:
- `siw/src/test/java/it/uniroma3/siw/DataAccessPerformanceAnalysisTest.java`

#### Riscontro Dettagliato:
1. **Presenza e Struttura del Test:**
   - È presente un test di integrazione JUnit `@SpringBootTest` specificamente progettato per confrontare le strategie di accesso ai dati nel caso d'uso: *"Caricamento proiezioni con dati di Festival, Film e Sala"*.
   - Il test popola un dataset controllato (1 Festival, 1 Sala, 10 Film, 10 Proiezioni) e abilita le statistiche native della sessione Hibernate (`Statistics.setStatisticsEnabled(true)`).
2. **Confronto Sperimentale Eseguito:**
   - **Strategia 1 (LAZY standard):** Carica le proiezioni tramite query HQL semplice e itera sui record accedendo ai getter delle entità collegate (`p.getFestival().getNome()`, `p.getFilm().getTitolo()`, `p.getSala().getNome()`). Emette **13 query SQL** (1 query iniziale + query separate per ciascuna entità associata), dimostrando empiricamente il problema N+1.
   - **Strategia 2 (`JOIN FETCH` ottimizzato):** Esegue la query `SELECT p FROM Proiezione p JOIN FETCH p.festival JOIN FETCH p.film JOIN FETCH p.sala WHERE p.festival.id = :festId`. Recupera tutti i dati correlati eseguendo **1 singola query SQL** con `INNER JOIN`.
3. **Output Prodotto sulla Console (Verificato in fase di audit):**
   ```text
   =================================================================
   === ANALISI SPERIMENTALE DELL'ACCESSO AI DATI (JPA / HIBERNATE) ===
   =================================================================
   Caso d'uso: Caricamento proiezioni con dati di Festival, Film e Sala

   --- Strategia 1: Accesso Standard LAZY (Problema N+1 Query) ---
   Proiezioni caricate: 10
   Query SQL eseguite:  13  (1 query iniziale + query separate per ogni entità collegata)
   Tempo complessivo:   11 ms

   --- Strategia 2: Accesso Ottimizzato con JOIN FETCH ---
   Proiezioni caricate: 10
   Query SQL eseguite:  1  (1 singola query SQL con JOIN)
   Tempo complessivo:   4 ms

   --- CONCLUSIONE & CONFRONTO ---
   Risparmio Query:    12 query SQL risparmiate
   Fattore Efficienza: Da 13 query a 1 query singola
   =================================================================
   ```
4. **Esecuzione Immediata all'Esame Orale:**
   - Il test è completamente autonomo, non richiede modifiche al codice e può essere lanciato in qualsiasi momento durante la discussione con il comando:
     ```bash
     ./mvnw test -Dtest=DataAccessPerformanceAnalysisTest
     ```
   - Esecuzione verificata: 1 test eseguito, 0 fallimenti, tempo totale ~3.8 secondi.

---

## 3. Punti Critici e Azioni Correttive Obbligatorie

La seguente lista ordina per gravità decrescente gli interventi necessari prima della discussione dell'esame orale per prevenire contestazioni o penalizzazioni da parte della commissione didattica:

```
[PRIORITÀ 1 - BLOCCANTE]  Risoluzione vincolo sovrapposizione sala (durata film)
[PRIORITÀ 2 - BLOCCANTE]  Completamento del CRUD Amministratore (Delete su 4 entità)
[PRIORITÀ 3 - ALTO]        Aggiunta endpoint cambio stato proiezione (form broken)
[PRIORITÀ 4 - MEDIO]       Gestione visualizzazione Sala e correzione link Annulla
[PRIORITÀ 5 - MEDIO]       Allineamento controller web al metodo transazionale atomico
[PRIORITÀ 6 - MEDIO]       Rafforzamento ownership su POST recensione e fix GET delete
[PRIORITÀ 7 - BASSO]       Esplicitazione FetchType.LAZY sui mapping @ManyToOne
```

### Dettaglio degli Interventi Correttivi Raccomandati:

#### 1. Correzione Sovrapposizione Oraria nella Sala (`findConflictingProjections`)
- **Problema:** La query in `ProiezioneRepository` controlla solo `p.ora = :ora`.
- **Rimedio:** Modificare la logica di validazione (tramite query JPQL o metodo nel `ProiezioneService`) calcolando l'intervallo temporale. Una proiezione esistente $P_{ext}$ è in conflitto con una nuova proiezione $P_{new}$ se:
  $$\text{Data}(P_{ext}) = \text{Data}(P_{new}) \quad \land \quad \text{Inizio}(P_{ext}) < \text{Fine}(P_{new}) \quad \land \quad \text{Fine}(P_{ext}) > \text{Inizio}(P_{new})$$
  dove $\text{Fine} = \text{Inizio} + \text{durata in minuti}$.

#### 2. Completamento del CRUD Amministratore (Delete)
- **Problema:** I metodi `deleteFestival`, `deleteFilm`, `deleteRegista` e `deleteSala` esistono nei Service ma non sono esposti nei Controller.
- **Rimedio:** Aggiungere gli endpoint nei controller MVC (preferibilmente con metodo POST, es. `/festivals/{id}/elimina`, `/films/{id}/elimina`, `/registi/{id}/elimina`, `/sale/{id}/elimina`) e inserire i relativi pulsanti nella tabella di `admin/dashboard.html` e nelle schede di dettaglio.

#### 3. Implementazione Endpoint Rapido Cambio Stato Proiezione
- **Problema:** In `proiezione/detail.html` (riga 42) è presente il form che invia a `POST /proiezioni/{id}/stato`, ma l'endpoint non è definito in `ProiezioneController`.
- **Rimedio:** Inserire in `ProiezioneController` il metodo:
  ```java
  @PostMapping("/proiezioni/{id}/stato")
  public String updateStatoProiezione(@PathVariable("id") Long id, @RequestParam("stato") StatoProiezione stato) {
      proiezioneService.updateStato(id, stato);
      return "redirect:/proiezione/" + id;
  }
  ```

#### 4. Correzione Rotte Sala e Link "Annulla"
- **Problema:** In `proiezione/detail.html` ci sono link a `/sala/{id}` che generano errore 404; in `regista/form.html` e `sala/form.html` i pulsanti Annulla puntano a `/registi` e `/sale` inesistenti.
- **Rimedio:**
  - Creare l'endpoint `GET /sala/{id}` e la vista `templates/sala/detail.html` (oppure convertire i link della sala in testo semplice non cliccabile);
  - Modificare i link Annulla nei due form impostandoli su `th:href="@{/admin/dashboard}"`.

#### 5. Utilizzo del Caso d'Uso Transazionale nel Controller Web
- **Problema:** Nel `ProiezioneController.saveProiezione`, viene richiamato `saveProiezione` anziché il metodo complesso multi-entità `programmaNuovaProiezione`.
- **Rimedio:** Quando `proiezione.getId() == null`, invocare direttamente `proiezioneService.programmaNuovaProiezione(proiezione, festivalId, filmId, salaId)` gestendo le eccezioni `IllegalStateException` con messaggi di errore nel `BindingResult`.

#### 6. Rafforzamento Ownership su Recensioni e Metodi HTTP
- **Problema:** Su `POST /recensioni/salva` non si verifica se l'ID appartiene all'utente loggato; la cancellazione è esposta via GET.
- **Rimedio:**
  - Nel `saveRecensione`, verificare: se `recensione.getId() != null`, caricare l'entità dal DB e confermare che `recensioneDb.getAutore().equals(currentUser)`;
  - Convertire il mapping di eliminazione in `@PostMapping("/recensioni/elimina/{id}")` con un piccolo form con pulsante all'interno della vista.

---
*Report redatto con rigore accademico e accuratezza tecnica a supporto della preparazione dell'appello d'esame.*
