# Extra Features Audit - Censimento & Analisi Rischi Esame Orale
**Corso:** Sistemi Informativi sul Web (SIW) - Appello Settembre 2026  
**Ateneo:** Università degli Studi Roma Tre  
**Progetto:** SIW Cinema (Gestione Festival Cinematografici)  
**Candidato:** Luca Carbonetti  
**Ruolo del Revisore:** Senior Software Engineer & Revisore Accademico  
**Data Audit:** 3 Settembre 2026  

---

## 1. Elenco delle Funzionalità Extra Rilevate

Di seguito è riportato il censimento completo delle funzionalità, librerie, componenti architetturali e logiche presenti nel repository che non rientrano strettamente nei requisiti minimi obbligatori di base, bensì rappresentano bonus formalmente previsti dal bando d'esame (Sez. 13) o arricchimenti introdotti dallo sviluppatore.

### Tabella Sinottica delle Funzionalità Extra

| Nome Feature Extra | Componenti e File Coinvolti | Categoria | Livello di Rischio all'Orale | Raccomandazione |
| :--- | :--- | :--- | :---: | :---: |
| **1. Paginazione Server-Side (`Pageable` & `Page<T>`)** | `FestivalRepository`, `FilmRepository`, `FestivalService`, `FilmService`, `FestivalController`, `templates/festival/list.html` | Bonus Bando (Sez. 13) | **Basso** | **MANTIENI** |
| **2. Upload File Immagini Locali (Multipart & Base64)** | `FestivalController`, `FilmController`, `Festival.immagine`, `Film.locandina`, `templates/festival/form.html`, `templates/film/form.html` | Bonus Bando (Sez. 13) | **Medio** | **MANTIENI CON CAUTELA** |
| **3. Statistiche Avanzate Recensioni & Visualizzazione Grafica** | `Film.java` (metodi business nell'entità), `templates/film/detail.html` | Feature Esterna | **Alto** | **ISOLA / RIFATTORIZZA** |
| **4. Ricerca e Filtri Combinati React 18 su Catalogo Film** | `templates/film/list.html`, `FilmRestController.java`, CDN React 18 + Babel | Requisito Ibrido (Sez. 9) / Bonus | **Basso** | **MANTIENI** |
| **5. Endpoint REST Completi di Scrittura (POST, PUT, DELETE)** | `RecensioneRestController.java`, `RecensioneDTO.java` | Bonus / Best Practice REST | **Basso** | **MANTIENI** |
| **6. Global Controller Advice (`@ControllerAdvice`)** | `GlobalControllerAdvice.java` | Feature Esterna (Best Practice) | **Basso** | **MANTIENI** |
| **7. Gestore Centralizzato Errori REST (`@RestControllerAdvice`)** | `RestExceptionHandler.java` | Feature Esterna (Best Practice) | **Basso** | **MANTIENI** |
| **8. Seeding Dataset Completo con Media Ufficiali (TMDB/Unsplash)** | `DataInitializer.java` | Feature Esterna di Popolamento | **Basso** | **MANTIENI** |

---

## 2. Schede Dettagliate per Singola Feature Extra

---

### FEATURE 1: Paginazione Server-Side con Spring Data JPA (`Pageable` e `Page<T>`)

#### Descrizione Tecnica
La paginazione è implementata per l'elenco dei festival (6 elementi per pagina) e predisposta per i film. Sfrutta le interfacce native di Spring Data:
- `FestivalRepository` estende `CrudRepository` ed espone metodi che accettano `Pageable` e restituiscono `Page<Festival>` (es. `findAll(Pageable)`, `findByOrderByAnnoDescDataInizioDesc(Pageable)`).
- `FestivalService.getFestivalsPaginated(citta, nome, page, size)` istanzia `PageRequest.of(page, size)`.
- Nel controller `FestivalController`, i parametri `page` vengono ricevuti tramite `@RequestParam(defaultValue = "0")`.
- Nel template `festival/list.html`, viene renderizzata una barra di navigazione con pulsanti *Precedente*, *Pagina X di Y*, *Successiva*, disabilitati dinamicamente al raggiungimento dei limiti.

#### Vantaggi all'Orale
1. **Bonus Formale Esplicito:** La Sezione 13 delle specifiche del docente menziona esplicitamente la "Paginazione dei risultati" tra i bonus valutabili con punteggio aggiuntivo.
2. **Efficienza Reale:** A livello SQL, Hibernate converte la richiesta in clausole `LIMIT ? OFFSET ?`, evitando di trasferire migliaia di tuple in memoria JVM.
3. **Ottima Impressione:** Dimostra padronanza di Spring Data e costrutti avanzati di paginazione.

#### Rischi all'Esame Orale
- **Domanda tipica live del docente:** *"Mi modifichi al volo la dimensione della pagina da 6 a 10"* oppure *"Mi aggiunga la paginazione anche all'elenco proiezioni"*.
- *Soluzione pronta:* È sufficiente modificare il valore `int pageSize = 10;` in `FestivalController.java` (riga 36). Per aggiungerla a Proiezione, basta aggiungere il parametro `Pageable pageable` al repository e usare `PageRequest.of(...)`.

#### Piano di Rimozione Pulita (Se richiesta dal docente)
1. In `FestivalController.java`, sostituire la chiamata a `festivalService.getFestivalsPaginated(...)` con `festivalService.getAllFestivals()`.
2. Nel template `festival/list.html`, rimuovere il blocco `<!-- Controlli di Paginazione -->` (righe 70-93).
3. Il resto dell'applicazione continuerà a funzionare senza alcun errore.

---

### FEATURE 2: Upload File Locandine/Immagini (Multipart & Base64 Data URL)

#### Descrizione Tecnica
Permette all'amministratore di caricare file grafici direttamente dal file system locale durante la creazione/modifica di un Festival o di un Film:
- Form HTML annotati con `enctype="multipart/form-data"`.
- I Controller (`FestivalController`, `FilmController`) ricevono `@RequestParam(value = "immagineFile", required = false) MultipartFile immagineFile`.
- Il file viene letto come array di byte e convertito in stringa `data:image/...;base64,...` salvata direttamente nelle colonne `TEXT` delle tabelle `festival` e `film`.
- In alternativa, l'utente può inserire un URL HTTP/HTTPS standard.

#### Vantaggi all'Orale
1. **Autonomia Totale:** L'applicazione non dipende da cartelle statiche sul filesystem locale del computer portatile, né da permessi di scrittura OS (`/var/www` o `/tmp`), rendendo il database PostgreSQL totalmente portabile e autosufficiente durante la demo.
2. **Nessun 404 su Immagini:** Il database contiene l'intero asset grafico; avviando l'applicazione su qualsiasi macchina le immagini caricate rimangono integre.

#### Rischi all'Esame Orale
- **Rischio Architetturale:** Un docente severo di basi di dati potrebbe contestare che memorizzare immagini Base64 nel database relazionale comporta un overhead del +33% in dimensione e gonfia il database con stringhe di centinaia di kilobyte, rallentando le query `SELECT *`.
- **Come difendersi all'orale:** Rispondere che si tratta di una scelta consapevole orientata alla prototipazione e alla portabilità per la prova d'esame, ma che in un ambiente enterprise di produzione l'approccio corretto prevede il salvataggio dei file binari su uno storage a oggetti (es. Amazon S3, MinIO) o su filesystem dedicato, memorizzando nel database unicamente l'URL canonico.

#### Piano di Rimozione Pulita
1. In `templates/festival/form.html` e `templates/film/form.html`, rimuovere l'input `<input type="file" name="immagineFile">` e l'attributo `enctype="multipart/form-data"` dal tag `<form>`.
2. Nei controller `FestivalController.java` e `FilmController.java`, eliminare il blocco `if (immagineFile != null && !immagineFile.isEmpty()) { ... }` e il relativo parametro dal metodo `save...`.

---

### FEATURE 3: Statistiche Avanzate Recensioni (Distribuzione Voti, Percentuali, Tasso di Gradimento)

#### Descrizione Tecnica
All'interno della scheda film (`film/detail.html`), è presente un box statistico sofisticato che visualizza:
- Media voto con rendering grafico a stelle (es. 4.5 / 5).
- Tasso di gradimento percentuale (recensioni con voto $\ge 4$).
- Cinque barre di progressione orizzontali animate con conteggio e percentuale per ciascun voto da 1 a 5 stelle.
- **Implementazione nel codice:** I metodi di calcolo (`getDistribuzioneVoti()`, `getPercentualiVoti()`, `getTassoGradimento()`) sono collocati **direttamente dentro la classe di modello `Film.java`** (righe 167-204), che naviga in memoria la collezione `List<Recensione> recensioni`.

#### Vantaggi
- Grande impatto visivo nella UI: la pagina del film si presenta come una vera piattaforma di recensioni cinema (stile IMDb o Rotten Tomatoes).

#### Rischi all'Esame Orale (**LIVELLO DI RISCHIO: ALTO**)
1. **Violazione della Separazione dei Layer:** Le entità `@Entity` JPA dovrebbero essere POJO puri con soli campi, getter, setter e vincoli di mapping. Inserire logica computazionale, streaming e aggregazioni dentro l'entità è un "code smell" accademico frequentemente penalizzato dai docenti di SIW.
2. **Rischio `LazyInitializationException`:** Poiché `Film.recensioni` è una collezione `@OneToMany` LAZY, l'invocazione di `film.getPercentualiVoti()` richiede che la sessione Hibernate sia aperta. Funziona attualmente solo perché Spring Boot abilita di default Open-Session-In-View (`spring.jpa.open-in-view=true`). Se il docente chiede di disabilitarlo (prassi comune per verificare l'architettura dei service), la chiamata genererà immediatamente un crash della pagina con `LazyInitializationException`!
3. **Mancanza di Query Aggregata:** Il docente potrebbe chiedere: *"Perché non ha scritto una query JPQL aggregata con `COUNT` e `AVG` nel repository anziché ciclare su una lista in memoria Java?"*.

#### Piano di Rimozione o Rifattorizzazione Pulita
- **Opzione A (Rifattorizzazione Raccomandata nel Service):**
  1. Spostare i metodi da `Film.java` a `RecensioneService.java` (o `FilmService.java`), annotandoli con `@Transactional(readOnly = true)`.
  2. Passare i risultati al Model in `FilmController.getFilm` (es. `model.addAttribute("statisticheVoti", ...)`);
  3. Nel template `film/detail.html`, aggiornare i riferimenti da `film.getPercentualiVoti()` a `statisticheVoti`.
- **Opzione B (Disattivazione Rapida della UI):**
  Nel file `templates/film/detail.html`, rimuovere il blocco `<!-- Box Statistiche Recensioni -->` (righe 145-170), lasciando unicamente l'indicazione testuale della media voto.

---

### FEATURE 4: Filtro Dinamico React 18 Integrato nel Catalogo Film

#### Descrizione Tecnica
Nel template `film/list.html` è integrato un client React 18 senza tooling esterno:
- Script CDN per React, ReactDOM e Babel standalone.
- Componente funzionale `FilmCatalogApp` con stati `films`, `titolo`, `genere`, `regista`, `anno`, `loading`.
- Effettua una singola chiamata asincrona iniziale `fetch('/api/movies')` e filtra localmente in memoria senza chiamate al server.

#### Vantaggi all'Orale
1. **Soddisfa Pienamente la Sezione 9 & 10:** Il progetto rispetta la richiesta di avere una componente sviluppata in React.
2. **Semplicità Architetturale Estrema:** L'assenza di Node.js, `npm`, `package.json` o server Vite/Webpack elimina qualsiasi rischio di fallimento di avvio o incompatibilità di porte (evita conflitti tra porte 3000 e 8080). Tutto gira monoliticamente su Spring Boot alla porta `8080`.
3. **Reattività Fluida:** Il filtraggio istantaneo durante la digitazione offre un'esperienza utente eccellente durante la demo.

#### Rischi all'Esame Orale
- **Basso.** L'architettura è trasparente. Se il docente chiede di aggiungere un filtro (es. per voto minimo o paese di produzione), la modifica si esegue in 3 righe di JavaScript direttamente nel template `list.html` (come illustrato nello Scenario 12 di `GUIDA_PROVA_ORALE.md`).

#### Raccomandazione: **MANTIENI ASSOLUTAMENTE.**

---

### FEATURE 5: Endpoint REST Completi di Scrittura (POST, PUT, DELETE su Recensioni)

#### Descrizione Tecnica
Oltre agli endpoint di sola lettura richiesti per alimentare il client React, in `RecensioneRestController.java` sono implementati tutti i verbi HTTP:
- `POST /api/reviews`: Creazione recensione con risposte standard `201 Created` (e header `Location`) o `409 Conflict`.
- `PUT /api/reviews/{id}`: Aggiornamento recensione con controllo proprietà e stato `403 Forbidden`.
- `DELETE /api/reviews/{id}`: Cancellazione con stato `204 No Content` o `403 Forbidden`.

#### Vantaggi all'Orale
- Mostra padronanza del modello di maturità di Richardson per le API REST (status code precisi, header di localizzazione della risorsa, corretta applicazione di idempotenza).

#### Rischi all'Esame Orale
- **Basso.** Gli endpoint non interferiscono con l'applicazione web Thymeleaf e possono essere dimostrati facilmente tramite `curl` o Postman.

#### Raccomandazione: **MANTIENI.**

---

### FEATURE 6: Global Controller Advice (`@ControllerAdvice`)

#### Descrizione Tecnica
La classe `GlobalControllerAdvice.java` inietta automaticamente in tutti i modelli Thymeleaf di tutte le viste i seguenti attributi:
- `currentUser`: entità `Utente` autenticata.
- `currentCredentials`: entità `Credentials` in sessione.
- `isAdmin`: booleano indicante se l'utente possiede ruolo `ADMIN`.
- `isLoggedIn`: booleano indicante lo stato di autenticazione.

#### Vantaggi all'Orale
- Elimina completamente il codice duplicato: i vari `FestivalController`, `FilmController`, `ProiezioneController` non devono ripetere chiamate a `SecurityContextHolder` o iniettare manualmente `isAdmin` in ogni singolo metodo `@GetMapping`.
- Design architetturale pulito conforme alle best practice ufficiali di Spring MVC.

#### Rischi all'Esame Orale
- **Quasi nulli.** È un pattern standard previsto e lodato nel contesto accademico.

#### Raccomandazione: **MANTIENI.**

---

### FEATURE 7: Gestione Centralizzata degli Errori REST (`@RestControllerAdvice`)

#### Descrizione Tecnica
La classe `RestExceptionHandler.java` cattura le eccezioni sollevate nei controller REST (`IllegalArgumentException`, `IllegalStateException`, `MethodArgumentNotValidException`) e produce un payload JSON standardizzato:
```json
{
  "timestamp": "2026-09-03T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Festival non trovato con ID: 99",
  "path": "/api/festivals/99"
}
```

#### Vantaggi all'Orale
- Previene la visualizzazione di stacktrace Java grezzi sul client o risposte di errore vuote.
- Standardizza le risposte REST secondo le convenzioni moderne (RFC 7807).

#### Rischi all'Esame Orale
- **Nulli.** Riconosciuto universalmente come pattern eccellente.

#### Raccomandazione: **MANTIENI.**

---

### FEATURE 8: Inizializzazione Dati con Media Reali (TMDB e Unsplash)

#### Descrizione Tecnica
In `DataInitializer.java`, all'avvio dell'applicazione vengono create istanze reali del mondo del cinema:
- Registi: Christopher Nolan, Denis Villeneuve, Alice Rohrwacher, Matteo Garrone, Hayao Miyazaki (con ritratti ufficiali TMDB).
- Film: Oppenheimer, Dune 2, La Chimera, Io Capitano, Il ragazzo e l'airone (con locandine ufficiali TMDB).
- Festival: Mostra del Cinema di Venezia, Festival di Cannes, Festa del Cinema di Roma.
- Sale e proiezioni pre-programmate.

#### Vantaggi all'Orale
- Presentazione visiva eccellente durante la discussione con il docente.
- Nessun dato fittizio sgradevole ("Film 1", "Regista Test") durante la navigazione live.

#### Rischi all'Esame Orale
- Se il computer su cui si sostiene l'esame non ha connessione a Internet, le immagini caricate da TMDB e Unsplash non saranno visibili (verrà mostrata l'icona di fallback o il testo alternativo). L'applicazione funzionerà comunque regolarmente senza crash.

#### Raccomandazione: **MANTIENI.**

---

## 3. Consiglio Strategico Finale per la Prova Orale

### Quale Assetto Garantisce il Massimo Successo?

Il progetto SIW Cinema possiede una base tecnica di **altissimo livello**, superiore alla media dei progetti accademici per cura del design, conformità delle API REST e completezza della suite di test.

Tuttavia, per affrontare la prova orale con la massima serenità e azzerare i punti di vulnerabilità su cui il docente potrebbe insistere, si raccomanda la seguente **strategia operativa in 3 passi**:

#### Passo 1: Sanare Immediatamente i 2 Bug "Bloccanti" (Priorità Assoluta)
1. **Sovrapposizione Oraria:** Aggiornare la logica di conflitto in modo che consideri anche la durata del film:
   ```java
   LocalTime fineProiezione = proiezione.getOra().plusMinutes(film.getDurata());
   ```
   Dimostrare al docente che due film consecutivi non possono accavallarsi nella medesima sala.
2. **Completare i Delete del CRUD Admin:** Esporre i metodi di cancellazione mancanti per Festival, Film, Regista e Sala, e inserire i relativi pulsanti nella dashboard admin (`admin/dashboard.html`).

#### Passo 2: Mettere in Sicurezza le Statistiche delle Recensioni
- Spostare la logica dei calcoli percentuali da `Film.java` a `RecensioneService.java`. Questo protegge da eventuali crash per `LazyInitializationException` se il docente chiede di disabilitare OpenSessionInView all'orale.

#### Passo 3: Come Esporre le Feature Extra durante il Colloquio
- **NON esaltare le feature extra come fine a se stesse**, ma presentarle come **risposte motivate a requisiti tecnici**:
  - *"Ho adottato la paginazione Spring Data per garantire scalabilità e risparmiare risorse di rete e DB tramite LIMIT/OFFSET."*
  - *"Ho isolato il frontend React incorporando Babel e React 18 direttamente nel template per evitare complesse e fragili catene di build Node.js mantenendo l'intera applicazione in un singolo deployabile Spring Boot."*
  - *"Ho usato DTO dedicati nelle API REST specificamente per troncare le relazioni bidirezionali JPA ed evitare sia cicli infiniti JSON che il caricamento accidentale di collezioni LAZY fuori sessione."*

Seguendo queste indicazioni, il progetto si presenterà solido, immune da domande a trabocchetto e pienamente conforme agli standard di eccellenza accademica.
