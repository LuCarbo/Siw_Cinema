# Domande & Risposte per la Discussione del Progetto (SIW Cinema)

**Corso:** Sistemi Informativi sul Web (SIW) - Università degli Studi Roma Tre  
**Studente:** Luca Carbonetti  
**Progetto:** SIW Cinema - Gestione Festival Cinematografici  

---

## 📌 Indice dei Temi d'Esame

1. [Architettura Generale e Tecnologie](#1-architettura-generale-e-tecnologie)
2. [Modello dei Dati e Relazioni JPA](#2-modello-dei-dati-e-relazioni-jpa)
3. [Prestazioni e Accesso ai Dati (Fetch, N+1, DTO)](#3-prestazioni-e-accesso-ai-dati)
4. [Gestione delle Transazioni (@Transactional)](#4-gestione-delle-transazioni)
5. [Sicurezza, Autenticazione e Autorizzazione (Spring Security)](#5-sicurezza-autenticazione-e-autorizzazione)
6. [Frontend React e Integrazione REST](#6-frontend-react-e-integrazione-rest)
7. [Funzionalità Bonus e Validazione](#7-funzionalit%C3%A0-bonus-e-validazione)
8. [Analisi Sperimentale dell'Accesso ai Dati (Sezione 8.2)](#8-analisi-sperimentale-dellaccesso-ai-dati-sezione-82)

---

## 1. Architettura Generale e Tecnologie

### D: Come è strutturata l'architettura dell'applicazione?
**R:** L'applicazione segue la canonica **architettura a livelli (Layered Architecture)** di Spring Boot:
- **`controller`**: gestisce le richieste HTTP e indirizza le viste Thymeleaf (`Controller`) o le risposte JSON (`RestController`).
- **`service`**: contiene la business logic, orchestra le operazioni e delimita i confini transazionali (`@Transactional`).
- **`repository`**: interfaccia di accesso ai dati basata su **Spring Data JPA** (`CrudRepository` / `JpaRepository`).
- **`model`**: entità di dominio annotate con JPA (`@Entity`) e vincoli Bean Validation (`@NotNull`, `@NotBlank`, `@Min`).
- **`dto`**: oggetti di trasferimento dati per disaccoppiare il dominio dalla serializzazione JSON verso il frontend.
- **`validator`**: validatori personalizzati (`org.springframework.validation.Validator`) per verificare vincoli complessi e unicità.
- **`security`**: configurazione dei filtri di autenticazione e autorizzazione (`SecurityFilterChain`).

### D: Come funziona la Dependency Injection in Spring?
**R:** Spring gestisce il ciclo di vita dei componenti tramite il suo **IoC Container (Inversion of Control)**. Le classi annotate con `@Service`, `@Repository`, `@Controller`, `@Component` vengono istanziate automaticamente come Bean singleton e iniettate dove richiesto tramite l'annotazione `@Autowired`.

---

## 2. Modello dei Dati e Relazioni JPA

### D: Quali sono le entità principali e come sono collegate tra loro?
**R:** Il dominio applicativo è modellato tramite **8 entità**:
1. **`Festival`**: kermesse cinematografica.
2. **`Film`**: opera cinematografica.
3. **`Regista`**: autore della regia del film.
4. **`Sala`**: sala in cui si tengono gli spettacoli.
5. **`Proiezione`**: evento di proiezione programmato ad una certa data e ora.
6. **`Recensione`**: opinione e voto (1-5) rilasciata da un utente registrato per un film.
7. **`Utente`**: dati anagrafici dell'utente (nome, cognome, email).
8. **`Credentials`**: credenziali di autenticazione (username, password cifrata, ruolo `USER` o `ADMIN`).

### D: Come sono mappate le relazioni tra le entità?
- **`ManyToMany`**: `Festival` $\leftrightarrow$ `Film`. Mappata con `@ManyToMany` e tabella di join `festival_film` (`@JoinTable`).
- **`ManyToOne` / `OneToMany`**:
  - `Film` (N) $\rightarrow$ `Regista` (1): ogni film ha un regista.
  - `Proiezione` (N) $\rightarrow$ `Festival` (1): ogni proiezione appartiene a un festival.
  - `Proiezione` (N) $\rightarrow$ `Film` (1): ogni proiezione proietta un film.
  - `Proiezione` (N) $\rightarrow$ `Sala` (1): ogni proiezione ha luogo in una sala.
  - `Recensione` (N) $\rightarrow$ `Film` (1): ogni recensione è associata a un film.
  - `Recensione` (N) $\rightarrow$ `Utente` (1): ogni recensione ha un autore.
- **`OneToOne`**: `Credentials` (1) $\leftrightarrow$ `Utente` (1).

---

## 3. Prestazioni e Accesso ai Dati

### D: Qual è la differenza tra fetch `LAZY` ed `EAGER` e come sono state scelte?
**R:** 
- **`EAGER`**: l'entità associata viene caricata immediatamente insieme all'entità principale.
- **`LAZY`**: l'entità o collezione associata viene caricata su richiesta solo quando viene invocato il relativo getter all'interno di una transazione attiva.

**Scelte adottate:**
- Tutte le collezioni `@OneToMany` e `@ManyToMany` (`Festival.film`, `Film.proiezioni`, `Film.recensioni`, `Festival.proiezioni`) sono **`LAZY`** (default). In questo modo, quando si richiede la lista dei festival o dei film per il catalogo, non vengono caricate in memoria inutilmente centinaia di righe correlate.
- Le relazioni `@ManyToOne` necessarie per l'identificazione degli elementi (es. le proiezioni che richiedono Sala, Film e Festival) vengono caricate in modo mirato tramite **`JOIN FETCH`**.

### D: Cos'è il problema delle N+1 Query e come è stato risolto?
**R:** Il problema si verifica quando, dopo una query iniziale per $N$ record padre, Hibernate esegue $N$ query separate per recuperare i record figli associati (totale: $1 + N$ query SQL).
- **Nel nostro progetto:**
  Per la visualizzazione del calendario delle proiezioni, anziché lasciare che Hibernate faccia $1 + 3N$ query per caricare Festival, Film e Sala per ciascuna riga, in `ProiezioneRepository` è stata creata la query con `JOIN FETCH`:
  ```java
  @Query("SELECT p FROM Proiezione p JOIN FETCH p.festival JOIN FETCH p.film JOIN FETCH p.sala ORDER BY p.data ASC, p.ora ASC")
  List<Proiezione> findByOrderByDataAscOraAsc();
  ```
  Allo stesso modo, in `RecensioneRepository`, le recensioni di un film caricano l'autore con `JOIN FETCH r.autore`. In questo modo viene eseguita **una sola query SQL con JOIN**.

### D: Perché usate i DTO nelle API REST invece delle entità JPA?
**R:**
1. **Evita la `LazyInitializationException`**: Jackson proverebbe a serializzare campi `LAZY` fuori dalla sessione Hibernate.
2. **Evita ricorsioni infinite**: Festival contiene Film, che contiene Festival, creando loop ciclici nel JSON.
3. **Disaccoppiamento e Performance**: Vengono inviati sul canale di rete solo i dati strettamente necessari al frontend (payload ridotti).

---

## 4. Gestione delle Transazioni

### D: Perché differenziare `@Transactional(readOnly = true)` da `@Transactional`?
**R:**
- **`@Transactional(readOnly = true)`** (utilizzato per tutte le letture nei Service):
  - Hibernate imposta la modalità `FlushType.MANUAL` e disattiva il dirty checking automatico delle entità in memoria, risparmiando cicli CPU e memoria.
  - Consente al database relazionale di instradare la query su eventuali repliche di sola lettura e ottimizzare i lock.
- **`@Transactional`** (utilizzato per salvataggi, modifiche e cancellazioni):
  - Apre una transazione in lettura/scrittura garantendo commit o rollback automatico in caso di eccezioni.

### D: Descrivi il caso d'uso transazionale multi-entità del progetto.
**R:** Il caso d'uso è **`programmaNuovaProiezione(...)`** in `ProiezioneService`:
```java
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public Proiezione programmaNuovaProiezione(Proiezione proiezione, Long festivalId, Long filmId, Long salaId)
```
1. **Recupera Festival, Film e Sala** dai rispettivi repository verificandone l'esistenza.
2. **Verifica coerenza temporale**: la data della proiezione deve rientrare tra `dataInizio` e `dataFine` del festival.
3. **Verifica disponibilità della sala**: controlla tramite `hasRoomConflict` che la sala non presenti sovrapposizioni temporali nell'intervallo calcolato con la durata del film (`ora_inizio` fino a `ora_inizio + durata`).
4. **Aggiorna l'associazione**: se il film non è ancora presente tra i partecipanti al festival, lo aggiunge e aggiorna il festival.
5. **Salva la proiezione** con stato iniziale `SCHEDULED`.
6. **Atomicità & Rollback**: Se uno qualsiasi dei controlli fallisce (es. sala occupata o data errata), viene sollevata un'eccezione che attiva il rollback automatico della transazione, lasciando il database in uno stato perfettamente consistente.
- **Livello di isolamento `READ_COMMITTED`**: previene la lettura di dati non committati (dirty reads) garantendo che i controlli di disponibilità sala siano sempre aggiornati.

---

## 5. Sicurezza, Autenticazione e Autorizzazione

### D: Come è strutturata la sicurezza con Spring Security?
**R:** In `SecurityConfiguration.java` è configurato il `SecurityFilterChain`:
- **Password Cifrate**: Le password degli utenti e dell'admin nel database sono protette con **BCrypt** (`BCryptPasswordEncoder`).
- **Ruoli**:
  - `USER`: utente registrato standard.
  - `ADMIN`: amministratore di sistema.
- **Autorizzazione per Percorso**:
  - Pagine pubbliche (`/`, `/festivals`, `/films`, `/proiezioni`, `/regista/**`, `/sala/**`) e risorse statiche: accessibili liberamente (`permitAll`).
  - Creazione/modifica/cancellazione e `/admin/**`: accessibili **solo ad `ADMIN`** (`hasAuthority("ADMIN")`).
  - Recensioni e profilo (`/recensioni/**`, `/profilo/**`): accessibili solo ad utenti autenticati.

### D: Come viene garantito che un utente modifichi o cancelli solo le proprie recensioni?
**R:**
1. Il metodo `canUserModify(recensione, currentUser, isAdmin)` in `RecensioneService` confronta l'ID dell'utente autenticato con quello dell'autore della recensione (`recensione.getAutore().equals(currentUser)`).
2. Nel `RecensioneController` (e in `RecensioneRestController`), se l'utente che richiede la modifica o cancellazione non è l'autore (e non è admin), l'operazione viene rifiutata restituendo un errore o lo stato `403 Forbidden`.
3. Nel salvataggio via POST (`/recensioni/salva`), se è presente un ID viene controllata l'appartenenza prima del salvataggio, bloccando attacchi di ID tampering.
4. Viene garantito il vincolo di unicità: un utente può inserire **al massimo 1 recensione per ciascun film** (verificato tramite `RecensioneValidator`, vincolo di tabella `@UniqueConstraint` e `hasUserReviewedFilm`).

---

## 6. Frontend React e Integrazione REST

### D: Come è integrato React nel progetto?
**R:** Il frontend include una sezione dinamica in React direttamente all'interno della pagina di catalogo e ricerca film ([film/list.html](siw/src/main/resources/templates/film/list.html)):
- È integrato **direttamente nel template HTML** tramite script CDN (React 18, ReactDOM e Babel standalone), rendendolo pulito, leggero e autosufficiente senza richiedere configurazioni complesse come Webpack o Node.js.
- Il componente React `FilmCatalogApp` gestisce lo stato dei filtri tramite gli Hooks (`useState`, `useEffect`).
- Comunica con il backend Spring Boot effettuando la chiamata asincrona `fetch('/api/movies')` per caricare in memoria il catalogo film.
- Fornisce una barra di ricerca e filtri combinati in tempo reale (Titolo, Genere, Regista, Anno) con aggiornamento istantaneo del catalogo e conteggio risultati senza dover ricaricare la pagina web.

### D: Come vengono gestiti gli errori nelle API REST?
**R:** Tramite la classe `@RestControllerAdvice` ([RestExceptionHandler.java](siw/src/main/java/it/uniroma3/siw/controller/rest/RestExceptionHandler.java)):
- Intercetta eccezioni e restituisce risposte JSON standardizzate contenenti `timestamp`, codice `status` HTTP (`400`, `403`, `404`, `409`), descrizione `error`, messaggio `message` e `path`.

---

## 7. Funzionalità Bonus e Validazione

### D: Quali funzionalità bonus sono state implementate nel progetto?
**R:**
1. **Paginazione Server-Side**:
   - Implementata con Spring Data `Pageable` e `Page<T>` in `FestivalRepository` e `FestivalService`, con navigazione a pagine e query ottimizzate con `LIMIT`/`OFFSET`.
2. **Ricerca Avanzata & Filtri Combinati**:
   - Ricerca film combinabile per **Titolo** (case-insensitive), **Genere**, **Regista** e **Anno**.
3. **Ricerca Proiezioni Multi-Criterio**:
   - Filtro combinabile per Festival, Film, Sala e Data specifica.
4. **Upload di Locandine/Immagini (Multipart & Base64 Data URL)**:
   - Caricamento di file grafici locali tramite `MultipartFile` convertiti in Base64 Data URL per la massima portabilità del DB.
5. **CRUD Completo per l'Amministratore**:
   - Gestione completa (Create, Read, Update, Delete con protezione CSRF e schede di dettaglio) per Festival, Film, Registi, Sale e Proiezioni.
6. **Documentazione Dedicata delle API REST**:
   - File [REST_API.md](REST_API.md) dettagliato con tutti gli endpoint, query parameters, codici HTTP e payload JSON di esempio.

---

## 8. Analisi Sperimentale dell'Accesso ai Dati (Sezione 8.2)

### D: Come mostrare durante l'esame il comportamento di JPA/Hibernate e il confronto tra strategie di fetch?
**R:** È stato predisposto il test eseguibile [DataAccessPerformanceAnalysisTest.java](siw/src/test/java/it/uniroma3/siw/DataAccessPerformanceAnalysisTest.java).

#### Comando per eseguirlo durante l'esame:
```bash
./mvnw test -Dtest=DataAccessPerformanceAnalysisTest
```

#### Output Prodotto sulla Console:
```
=================================================================
=== ANALISI SPERIMENTALE DELL'ACCESSO AI DATI (JPA / HIBERNATE) ===
=================================================================
Caso d'uso: Caricamento proiezioni con dati di Festival, Film e Sala

--- Strategia 1: Accesso Standard LAZY (Problema N+1 Query) ---
Proiezioni caricate: 10
Query SQL eseguite:  13  (1 query iniziale + query separate per ogni entità collegata)
Tempo complessivo:   26 ms

--- Strategia 2: Accesso Ottimizzato con JOIN FETCH ---
Proiezioni caricate: 10
Query SQL eseguite:  1  (1 singola query SQL con JOIN)
Tempo complessivo:   5 ms

--- CONCLUSIONE & CONFRONTO ---
Risparmio Query:    12 query SQL risparmiate
Fattore Efficienza: Da 13 query a 1 query singola
=================================================================
```

#### Punti Chiave da Discutere all'Esame:
1. **Strategia 1 (LAZY)**: Esegue 1 query iniziale per le proiezioni. Quando il codice accede a `p.getFilm().getTitolo()` o `p.getSala().getNome()`, Hibernate si accorge che il proxy non è inizializzato ed emette query `SELECT` aggiuntive sul database (problema N+1).
2. **Strategia 2 (JOIN FETCH)**: La query JPQL istruisce esplicitamente Hibernate ad effettuare una `INNER JOIN` in fase di caricamento, portando tutti i record in memoria con **un'unica query SQL**.
3. **Comportamento al crescere delle entità**: Con 100 proiezioni, la strategia LAZY genererebbe oltre 100-300 query SQL, saturando le connessioni al database, mentre la strategia `JOIN FETCH` rimarrebbe a **1 query singola**.
