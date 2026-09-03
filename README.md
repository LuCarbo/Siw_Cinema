# SIW Cinema - Sistema di Gestione per Festival Cinematografici

**Progetto per il corso di Sistemi Informativi sul Web (SIW)**  
**Università degli Studi Roma Tre**  
**Autore:** Luca Carbonetti  

---

## 📌 Descrizione del Progetto

**SIW Cinema** è un'applicazione web sviluppata in **Spring Boot** per la consultazione e la gestione di festival cinematografici, dei film partecipanti, delle proiezioni in sala e delle recensioni rilasciate dagli utenti registrati.

L'applicazione include:
- Un'interfaccia web renderizzata lato server con **Thymeleaf**.
- Un'integrazione client-side in **React 18** per l'esplorazione interattiva dei festival.
- Un insieme di **endpoint REST** conformi agli standard HTTP con codici di stato espliciti e gestione degli errori in formato JSON.
- Gestione della persistenza relazionale tramite **Spring Data JPA / Hibernate** su database **PostgreSQL**.
- Autenticazione e autorizzazione basate su ruoli gestite con **Spring Security**.

---

## 🛠️ Tecnologie Utilizzate

- **Linguaggio & Piattaforma:** Java 21
- **Framework Backend:** Spring Boot 4.1.1 (Spring Framework 7)
- **Persistenza:** Spring Data JPA, Hibernate ORM
- **Database:** PostgreSQL
- **Sicurezza:** Spring Security 6 (hashing password con BCrypt)
- **Frontend MVC:** HTML5, CSS3 Vanilla, Thymeleaf Template Engine
- **Frontend React:** React 18, Babel standalone (integrato nel template HTML)
- **Build Tool:** Apache Maven (con Maven Wrapper `mvnw`)

---

## 🏛️ Architettura e Struttura del Progetto

Il progetto rispetta la classica architettura a livelli di Spring Boot:

```
it.uniroma3.siw/
├── controller/         # Controller MVC per le viste Thymeleaf
│   └── rest/           # Controller REST per le API JSON e RestExceptionHandler
├── dto/                # Data Transfer Objects per le risposte REST
├── model/              # Entità JPA (@Entity) con annotazioni di validazione
├── repository/         # Repository Spring Data JPA (JpaRepository)
├── service/            # Servizi di business logic con gestione @Transactional
├── validator/          # Spring Validator personalizzati per vincoli di dominio
├── security/           # Configurazione di sicurezza e filtri (SecurityConfiguration)
└── init/               # DataInitializer per il popolamento iniziale dei dati di test
```

---

## 🗄️ Modello dei Dati (Entità e Relazioni)

Il dominio applicativo è modellato tramite **8 entità JPA**:

1. **`Festival`**: Rappresenta una kermesse cinematografica (nome, anno, città, date inizio/fine, descrizione, locandina/immagine).
2. **`Film`**: Opera cinematografica (titolo, anno, durata, genere, paese di produzione, locandina).
3. **`Regista`**: Persona che ha diretto l'opera (nome, cognome, nazionalità, data di nascita, foto).
4. **`Sala`**: Sala cinematografica in cui si tengono le proiezioni (nome, capienza, indirizzo).
5. **`Proiezione`**: Evento di proiezione (data, ora, stato: `SCHEDULED`, `COMPLETED`, `CANCELLED`).
6. **`Recensione`**: Valutazione rilasciata da un utente per un film (voto da 1 a 5, titolo, testo, data).
7. **`Utente`**: Profilo anagrafico dell'utente (nome, cognome, email).
8. **`Credentials`**: Credenziali di accesso al sistema (username, password cifrata, ruolo `USER` o `ADMIN`).

### Relazioni tra le Entità:
- **`ManyToMany`**: `Festival` $\leftrightarrow$ `Film` (un festival presenta più film; un film può partecipare a più festival).
- **`ManyToOne` / `OneToMany`**:
  - `Film` (N) $\rightarrow$ `Regista` (1): ogni film ha un regista.
  - `Proiezione` (N) $\rightarrow$ `Festival` (1): ogni proiezione fa parte di un festival.
  - `Proiezione` (N) $\rightarrow$ `Film` (1): ogni proiezione proietta un film.
  - `Proiezione` (N) $\rightarrow$ `Sala` (1): ogni proiezione si tiene in una sala.
  - `Recensione` (N) $\rightarrow$ `Film` (1): ogni recensione è associata a un film.
  - `Recensione` (N) $\rightarrow$ `Utente` (1): ogni recensione ha un autore.
- **`OneToOne`**: `Credentials` (1) $\leftrightarrow$ `Utente` (1).

---

## 📋 Casi d'Uso Implementati

### 1. Funzionalità Pubbliche (Accessibili da chiunque)
- **Visualizzazione elenco dei festival** (`GET /festivals`): Elenco completo delle manifestazioni con filtri per città o nome.
- **Visualizzazione dettaglio di un festival** (`GET /festival/{id}`): Informazioni del festival, film partecipanti al festival e programma delle relative proiezioni.
- **Visualizzazione catalogo film** (`GET /films`): Elenco film con filtri per titolo, genere e anno.
- **Visualizzazione dettaglio film** (`GET /film/{id}`): Dati completi del film, scheda del regista, festival a cui partecipa, proiezioni programmate e lista recensioni della community.
- **Visualizzazione dati del regista** (`GET /regista/{id}`): Scheda biografica e filmografia completa del regista.
- **Visualizzazione programma generale delle proiezioni** (`GET /proiezioni`): Calendario degli eventi filtrabile per festival, film e sala.
- **Dettaglio sala cinematografica** (`GET /sala/{id}`): Informazioni della sala e calendario delle relative proiezioni programmate.
- **Catalogo Film con Filtro React 18** (`GET /films`): Componente client-side interattivo in React con ricerca dinamica istantanea in tempo reale.

### 2. Funzionalità Utenti Registrati (Ruolo `USER`)
- **Inserimento recensione per un film** (`GET /recensioni/nuova/{filmId}`, `POST /recensioni/salva`):
  - È consentita **al massimo una recensione per film** da parte dello stesso utente (verificato da `RecensioneValidator` e service).
- **Modifica di una propria recensione** (`GET /recensioni/modifica/{id}`):
  - L'utente può modificare **esclusivamente** le recensioni di cui è l'autore.
- **Cancellazione di una propria recensione** (`/recensioni/elimina/{id}`):
  - Rimozione consentita solo all'autore o all'amministratore.
- **Profilo personale** (`GET /profilo`):
  - Riepilogo dei dati personali e pannello di gestione delle proprie recensioni pubblicate.

### 3. Funzionalità Amministratore (Ruolo `ADMIN`)
- **Pannello di controllo unificato** (`GET /admin/dashboard`).
- **CRUD completo Festival** (`/festivals/nuovo`, `/festivals/modifica/{id}`, `/festivals/{id}/elimina`).
- **CRUD completo Film** (`/films/nuovo`, `/films/modifica/{id}`, `/films/{id}/elimina`).
- **CRUD completo Regista** (`/registi/nuovo`, `/registi/modifica/{id}`, `/registi/{id}/elimina`).
- **CRUD completo Sala** (`/sale/nuova`, `/sale/modifica/{id}`, `/sale/{id}/elimina`).
- **Associazione ed eliminazione di un film da un festival** (`/festivals/{id}/gestione-film`).
- **Programmazione, modifica, cambio rapido di stato e cancellazione di una proiezione** (`/proiezioni/nuova`, `/proiezioni/modifica/{id}`, `/proiezioni/{id}/stato`, `/proiezioni/{id}/elimina`).
  - **Verifiche di consistenza**:
    1. *Controllo sovrapposizioni orarie*: Verifica automatica che la sala non sia già occupata, calcolando l'intervallo temporale completo in base all'orario di inizio e alla durata del film (`ora_inizio + film.durata`).
    2. *Controllo date festival*: La data della proiezione deve rientrare nell'intervallo di date del festival.

---

## ⚡ Caso d'Uso Transazionale Multi-Entità

Nel service `ProiezioneService` è implementato il metodo transazionale:
```java
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public Proiezione programmaNuovaProiezione(Long festivalId, Long filmId, Long salaId, LocalDate data, LocalTime ora)
```
Questo caso d'uso coordina contemporaneamente **4 entità** (`Festival`, `Film`, `Sala`, `Proiezione`):
1. Recupera e verifica l'esistenza di `Festival`, `Film` e `Sala`.
2. Verifica che non vi siano conflitti orari nella sala richiesta (`existsBySalaAndDataAndOra`).
3. Verifica la coerenza temporale con le date del festival.
4. Associa automaticamente il film al festival (se non ancora presente).
5. Crea e salva la nuova proiezione con stato iniziale `SCHEDULED`.
6. In caso di errore o conflitto, scatta il **rollback automatico** della transazione.

---

## ⚛️ Frontend React & Integrazione REST

Nella pagina del catalogo film (template [film/list.html](file:///Users/lucacarbonetti/SIW/Siw_Cinema/siw/src/main/resources/templates/film/list.html)) è integrata un'interfaccia di ricerca dinamica sviluppata in **React 18**:
- Il componente `FilmCatalogApp` utilizza gli Hooks (`useState`, `useEffect`).
- Effettua la chiamata asincrona `fetch('/api/movies')` per caricare in tempo reale l'intero catalogo film in formato JSON.
- Permette di filtrare istantaneamente i film per **Titolo**, **Genere**, **Regista** e **Anno** in tempo reale (client-side) senza ricaricare la pagina HTML.
- Il codice React è incorporato direttamente nel template HTML tramite script `<script type="text/babel" th:inline="none">` e CDN (React 18 + Babel), rendendolo leggero, pulito, autosufficiente e senza necessità di build tool o configurazioni esterne.

---

## 📡 Documentazione delle API REST

Tutti gli endpoint REST (`/api/**`) restituiscono dati in formato JSON tramite appositi DTO per evitare problemi di ricorsione ciclica:

| Metodo | Endpoint | Descrizione | Codici di Stato |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/festivals` | Lista di tutti i festival | `200 OK` |
| `GET` | `/api/festivals/{id}` | Dettaglio del singolo festival | `200 OK`, `404 Not Found` |
| `GET` | `/api/festivals/{id}/movies` | Film partecipanti a un festival | `200 OK`, `404 Not Found` |
| `GET` | `/api/festivals/{id}/screenings` | Proiezioni programmate per un festival | `200 OK`, `404 Not Found` |
| `GET` | `/api/movies` | Catalogo di tutti i film | `200 OK` |
| `GET` | `/api/movies/{id}` | Dettaglio del singolo film | `200 OK`, `404 Not Found` |
| `GET` | `/api/movies/{id}/reviews` | Elenco recensioni di un film | `200 OK`, `404 Not Found` |
| `GET` | `/api/screenings` | Calendario di tutte le proiezioni | `200 OK` |
| `GET` | `/api/screenings/{id}` | Dettaglio di una proiezione | `200 OK`, `404 Not Found` |
| `GET` | `/api/reviews/{id}` | Recupero di una singola recensione | `200 OK`, `404 Not Found` |
| `POST`| `/api/reviews` | Creazione recensione (richiede auth) | `201 Created`, `400 Bad Request`, `409 Conflict` |
| `PUT` | `/api/reviews/{id}` | Modifica recensione (solo autore) | `200 OK`, `403 Forbidden`, `404 Not Found` |
| `DELETE`| `/api/reviews/{id}` | Eliminazione recensione (solo autore) | `204 No Content`, `403 Forbidden`, `404 Not Found` |

### Gestione Centralizzata Errori REST (`RestExceptionHandler`)
Gli errori generano risposte JSON standardizzate:
```json
{
  "timestamp": "2026-08-26T23:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Festival non trovato con ID: 999",
  "path": "/api/festivals/999"
}
```

---

## 🔒 Sicurezza e Autenticazione

- **Password Hashing:** Tutte le password nel database sono cifrate con algoritmo **BCrypt**.
- **Ruoli:**
  - `USER`: Utente registrato standard.
  - `ADMIN`: Amministratore con accesso completo al pannello `/admin/**`.
- **Protezione Rotte (Spring Security):**
  - Risorse statiche, pagine pubbliche (`/`, `/festivals`, `/films`, `/proiezioni`, `/regista/**`, `/sala/**`) e endpoint `GET /api/**` accessibili liberamente (`permitAll`).
  - `/recensioni/**` e `/profilo/**` accessibili solo ad utenti autenticati (`USER` o `ADMIN`).
  - Rotte di creazione/modifica e dashboard `/admin/**` accessibili esclusivamente al ruolo `ADMIN`.

---

## 🚀 Guida all'Avvio e Configurazione

### 1. Configurazione Database PostgreSQL
Assicurarsi che PostgreSQL sia in esecuzione su `localhost:5432` con il database `siw_cinema`.  
Parametri in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/siw_cinema
spring.datasource.username=lucacarbonetti
spring.datasource.password=Lcarbo05
spring.jpa.hibernate.ddl-auto=update
```

### 2. Avvio dell'Applicazione
Dalla cartella principale del progetto:
```bash
./mvnw spring-boot:run
```
L'applicazione sarà accessibile all'indirizzo:  
👉 **http://localhost:8080**

### 3. Esecuzione dei Test Automatici
Il progetto include una suite completa di test unitari, di integrazione, di sicurezza e transazionali:
```bash
./mvnw test
```

---

## 👤 Credenziali di Esempio Precaricate

All'avvio dell'applicazione vengono automaticamente creati i seguenti account di test tramite `DataInitializer`:

| Username | Password | Ruolo | Descrizione |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin` | `ADMIN` | Amministratore di sistema (accesso al pannello admin e a tutti i form CRUD) |
| **`mario`** | `password` | `USER` | Utente registrato standard |
| **`giulia`** | `password` | `USER` | Utente registrato standard |
