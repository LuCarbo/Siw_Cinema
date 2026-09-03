# TASK: AUDIT RIGOROSO DI CONFORMITÀ - PROGETTO D'ESAME SIW (SETTEMBRE 2026)

Sei un Senior Software Engineer e Revisore Accademico esperto di Spring Boot, JPA/Hibernate, Spring Security, React e Thymeleaf.
Il tuo compito è ispezionare approfonditamente l'INTERO codebase di questo repository ed effettuare un audit rigoroso rispetto alle specifiche ufficiali del progetto d'esame assegnato dal docente (Sistemi Informativi su Web - Appello Settembre 2026).

Non modificare il codice sorgente in questa fase. Devi analizzare i file sorgente (Java, template Thymeleaf, componenti React, script SQL, pom.xml/build.gradle) e generare DUE FILE DI REPORT distinti nella root del progetto:
1. `SIW_COMPLIANCE_REPORT.md` (Verifica puntuale dei requisiti del professore)
2. `EXTRA_FEATURES_AUDIT.md` (Censimento e analisi delle funzionalità extra da valutare)

Segui scrupolosamente le istruzioni riportate di seguito.

---

## FASE 1: VERIFICA DETTAGLIATA DEI REQUISITI DEL DOCENTE

Ispeziona ogni package, classe, interfaccia, file di configurazione e componente frontend verificando i seguenti punti:

### 1. Modello Dati ed Entità JPA (Specifiche Sez. 3 & 11)
Verifica la presenza e la corretta modellazione delle seguenti entità e attributi minimi:
- **Festival**: `nome`, `anno`, `città`, `dataInizio`, `dataFine`, `descrizione`.
  - Relazioni: `@ManyToMany` con Film, `@OneToMany` con Proiezione.
- **Film**: `titolo`, `anno`, `durata`, `genere`, `paeseProduzione`.
  - Relazioni: `@ManyToMany` con Festival, `@ManyToOne` con Regista, `@OneToMany` con Proiezione, `@OneToMany` con Recensione.
- **Regista**: `nome`, `cognome`, `dataNascita`, `nazionalità`.
  - Relazioni: `@OneToMany` con Film.
- **Sala**: `nome`, `indirizzo`, `capienza`.
  - Relazioni: `@OneToMany` con Proiezione.
- **Proiezione**: `data`, `ora`, `stato` (SCHEDULED, COMPLETED, CANCELLED o simili).
  - Relazioni: `@ManyToOne` con Festival, `@ManyToOne` con Film, `@ManyToOne` con Sala.
- **Recensione**: `testo`, `voto`, `data`.
  - Relazioni: `@ManyToOne` con Film, `@ManyToOne` con Utente.
  - Vincolo: max 1 recensione per coppia (utente, film) — sia a livello logico che preferibilmente con `@Table(uniqueConstraints = ...)`.
- **Utente**: `username`, `password`, `ruolo` (USER, ADMIN).

*Controlli specifici JPA:*
- I metodi `equals()` e `hashCode()` sono implementati correttamente (evitando loop o dipendenze da id generati non persistiti)?
- I metodi `toString()` evitano cicli infiniti sulle relazioni bidirezionali?
- Le associazioni `@OneToMany` usano `FetchType.LAZY` di default?

### 2. Casi d'Uso e Logica Applicativa (Sez. 4 & 11)
- **Funzionalità Pubbliche (Anonime):**
  - Elenco festival, dettaglio festival (con film e proiezioni associate).
  - Film partecipanti al festival, programma proiezioni.
  - Dettaglio film (info, regista, festival, proiezioni, recensioni), dati del regista, recensioni del film.
- **Funzionalità Utente Registrato (Ruolo USER):**
  - Inserimento recensione per un film.
  - Modifica della propria recensione.
  - Eliminazione della propria recensione.
  - *VINCOLO CRITICO:* Verifica che un utente NON possa recensire due volte lo stesso film e NON possa modificare o cancellare recensioni altrui (ownership check a livello di Service/Security).
- **Funzionalità Amministratore (Ruolo ADMIN):**
  - CRUD Festival, Film, Regista, Sala.
  - Associazione e disassociazione Film <-> Festival.
  - Programmazione, modifica e cancellazione Proiezioni.
  - *VINCOLO CRITICO CONSISTENZA:* Verifica se nella programmazione di una proiezione viene controllata la sovrapposizione temporale nella stessa sala (tenendo conto dell'orario di inizio e della durata del film).

### 3. Architettura a Livelli e Transazioni (Sez. 6 & 7)
- **Separazione dei layer:**
  - Nessuna logica di business o query diretta nei Controller/RestController.
  - I Controller validano gli input (`@Valid`, `BindingResult`) e delegano ai Service.
- **Gestione Transazioni (`@Transactional`):**
  - I metodi di sola lettura nel Service usano `@Transactional(readOnly = true)`?
  - I metodi di scrittura usano `@Transactional`?
  - È presente almeno un caso d'uso multi-entità/multi-repository (es. `creaProiezione`) progettato con gestione atomica del rollback?

### 4. Sicurezza (Sez. 5)
- Spring Security configurato con `SecurityFilterChain`.
- Password salvate tramite hashing sicuro (`PasswordEncoder` / `BCrypt`).
- Endpoint protetti coerentemente:
  - Risorse e viste pubbliche accessibili a tutti (`permitAll`).
  - Funzionalità di recensione limitate a utenti autenticati (`hasRole('USER')` o autenticati).
  - Funzionalità di gestione e admin limitate a `hasRole('ADMIN')`.
- Controllo di proprietà (ownership) delle recensioni eseguito lato backend e non solo nascosto nella vista.

### 5. Frontend Ibrido e API REST (Sez. 9 & 10)
- Presenza di Thymeleaf per la parte principale dell'applicazione.
- Presenza di almeno una funzionalità implementata in **React** (es. programma proiezioni, recensioni, ricerca film).
- Controller REST (`@RestController`) dedicati a fornire dati a React, con corretta semantica HTTP (GET, POST, PUT/PATCH, DELETE, codici 200, 201, 204, 400, 404).

### 6. Analisi Sperimentale N+1 e Prestazioni JPA (Sez. 8.2)
- Verifica se esiste uno script o un test eseguibile (es. JUnit o `CommandLineRunner`) che:
  1. Esegue lo stesso caso d'uso con almeno due strategie (es. LAZY vs JOIN FETCH o EntityGraph).
  2. Stampa chiaramente a console: record caricati, numero di query SQL e tempo impiegato in ms.
  3. Può essere eseguito immediatamente durante l'orale senza modificare il codice.
- Se manca o è incompleto, segnalalo come **BLOCCANTE**.

---

## FASE 2: CENSIMENTO FUNZIONALITÀ EXTRA E BONUS

Identifica qualsiasi funzionalità, entità, endpoint, libreria o logica presente nel codice che NON è esplicitamente richiesta dai requisiti minimi obbligatori del PDF, inclusi i bonus facoltativi menzionati nella Sezione 13 (es. paginazione, filtri avanzati, upload immagini, statistiche, OAuth, Swagger, notifiche, ecc.) o feature aggiunte di propria iniziativa.

Valuta ogni feature extra considerando il contesto dell'esame orale di SIW:
- **Domande del docente:** Il professore richiederà modifiche al codice al momento. Il codice extra complica la comprensione rapida o aumenta il rischio di errori durante le modifiche live?
- **Rapporto Utilità / Rischio:** È un bonus spendibile o un elemento di distrazione/sovra-ingegnerizzazione?

---

## FORMATO DEGLI OUTPUT RICHIESTI

Genera i seguenti due file con formattazione Markdown dettagliata e professionale:

### FILE 1: `SIW_COMPLIANCE_REPORT.md`
Struttura il documento come segue:
1. **Executive Summary**: Tabella con lo stato complessivo di conformità (Percentuale di conformità, Requisiti Soddisfatti, Mancanti, Warning).
2. **Audit Sezione per Sezione**:
   - Per ciascuno dei punti (Modello dati, Casi d'uso, Architettura, Transazioni, Sicurezza, Frontend React/Thymeleaf, REST API, Script N+1):
     - Esito: `[PASS]`, `[FAIL]` o `[WARNING]`.
     - File e classi coinvolti (con riferimenti precisi a percorsi e nomi metodo).
     - Dettaglio di cosa è stato implementato correttamente.
     - Eventuali discrepanze o violazioni rispetto alle specifiche del PDF.
3. **Punti Critici / Azioni Correttive Obbligatorie**:
   - Lista ordinata per priorità dei fix indispensabili da effettuare prima dell'esame per evitare penalità o bocciature.

### FILE 2: `EXTRA_FEATURES_AUDIT.md`
Struttura il documento come segue:
1. **Elenco delle Funzionalità Extra Rilevate**:
   - Tabella riassuntiva: `Nome Feature | File/Componenti Coinvolti | Categoria (Bonus PDF / Feature Esterna) | Livello di Rischio all'Orale (Basso/Medio/Alto) | Raccomandazione (Mantieni / Rimuovi / Isola)`.
2. **Scheda Dettagliata per Singola Feature Extra**:
   - Descrizione di cosa fa e come è implementata.
   - **Vantaggi nel tenerla** (es. bonus formale previsto dal professore nella Sez. 13).
   - **Rischi all'esame orale** (es. possibilità che il professore chieda di modificarla live, dipendenze esterne fragili, codice che sporca i controller).
   - **Piano di Rimozione/Disattivazione Pulita**: Istruzioni passo-passo su cosa eliminare se si decide di toglierla, senza rompere il resto del progetto.
3. **Consiglio Strategico Finale**:
   - Raccomandazione sintetica su quale configurazione sia la più sicura e brillante per sostenere l'orale con successo.

---
Esegui ora l'analisi completa scansionando i file del workspace e genera i due file `SIW_COMPLIANCE_REPORT.md` e `EXTRA_FEATURES_AUDIT.md`.