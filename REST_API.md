# Documentazione delle API REST - SIW Cinema

Questa documentazione descrive gli endpoint REST messi a disposizione dall'applicazione per il frontend e per l'integrazione client.

Tutti gli endpoint rispondono con dati in formato **JSON** e utilizzano codici di stato HTTP standard.

---

## 📌 Indice degli Endpoint

- [1. Festival (`/api/festivals`)](#1-festival)
  - `GET /api/festivals`
  - `GET /api/festivals/{id}`
  - `GET /api/festivals/{id}/movies`
  - `GET /api/festivals/{id}/screenings`
- [2. Film (`/api/movies`)](#2-film)
  - `GET /api/movies`
  - `GET /api/movies/{id}`
  - `GET /api/movies/{id}/reviews`
- [3. Proiezioni (`/api/screenings`)](#3-proiezioni)
  - `GET /api/screenings`
  - `GET /api/screenings/{id}`
- [4. Recensioni (`/api/reviews`)](#4-recensioni)
  - `GET /api/reviews/{id}`
  - `POST /api/reviews`
  - `PUT /api/reviews/{id}`
  - `DELETE /api/reviews/{id}`
- [5. Gestione Errori e Formato Risposte](#5-gestione-degli-errori)

---

## 1. Festival

### `GET /api/festivals`
Restituisce la lista di tutti i festival registrati.

- **Parametri opzionali di query:**
  - `citta` (stringa): filtra per città
  - `nome` (stringa): filtra per nome
- **Codici di stato:**
  - `200 OK`: richiesta eseguita con successo
- **Esempio di risposta (`200 OK`):**
```json
[
  {
    "id": 1,
    "nome": "Festival del Cinema di Roma",
    "anno": 2026,
    "citta": "Roma",
    "dataInizio": "2026-10-15",
    "dataFine": "2026-10-25",
    "descrizione": "Kermesse internazionale di cinema a Roma",
    "immagine": "https://images.unsplash.com/...",
    "numeroFilm": 3,
    "numeroProiezioni": 4
  }
]
```

---

### `GET /api/festivals/{id}`
Restituisce i dettagli del singolo festival con l'ID specificato.

- **Codici di stato:**
  - `200 OK`: festival trovato
  - `404 Not Found`: nessun festival con l'ID specificato
- **Esempio di risposta (`200 OK`):**
```json
{
  "id": 1,
  "nome": "Festival del Cinema di Roma",
  "anno": 2026,
  "citta": "Roma",
  "dataInizio": "2026-10-15",
  "dataFine": "2026-10-25",
  "descrizione": "Kermesse internazionale di cinema a Roma",
  "immagine": "https://images.unsplash.com/...",
  "numeroFilm": 3,
  "numeroProiezioni": 4
}
```

---

### `GET /api/festivals/{id}/movies` *(alias: `/api/festivals/{id}/films`)*
Restituisce l'elenco dei film partecipanti al festival selezionato (utilizzato dal componente React).

- **Codici di stato:**
  - `200 OK`: lista film recuperata
  - `404 Not Found`: festival inesistente
- **Esempio di risposta (`200 OK`):**
```json
[
  {
    "id": 1,
    "titolo": "La Grande Bellezza",
    "anno": 2013,
    "durata": 142,
    "genere": "Drammatico",
    "paeseProduzione": "Italia",
    "locandina": "https://images.unsplash.com/...",
    "registaId": 1,
    "nomeRegista": "Paolo Sorrentino",
    "mediaVoti": 4.5,
    "numeroRecensioni": 2
  }
]
```

---

### `GET /api/festivals/{id}/screenings` *(alias: `/api/festivals/{id}/proiezioni`)*
Restituisce il programma delle proiezioni previste per il festival.

- **Codici di stato:**
  - `200 OK`: lista proiezioni recuperata
  - `404 Not Found`: festival inesistente
- **Esempio di risposta (`200 OK`):**
```json
[
  {
    "id": 1,
    "data": "2026-10-16",
    "ora": "20:30",
    "stato": "SCHEDULED",
    "statoLabel": "Programmata",
    "festivalId": 1,
    "nomeFestival": "Festival del Cinema di Roma",
    "filmId": 1,
    "titoloFilm": "La Grande Bellezza",
    "salaId": 1,
    "nomeSala": "Sala Fellini",
    "capienzaSala": 250,
    "indirizzoSala": "Via Nazionale 12"
  }
]
```

---

## 2. Film

### `GET /api/movies` *(alias: `/api/films`)*
Restituisce l'elenco di tutti i film presenti nel catalogo.

- **Parametri opzionali di query:**
  - `titolo` (stringa): ricerca per titolo (case-insensitive)
  - `genere` (stringa): filtro per genere
  - `anno` (intero): filtro per anno
  - `registaId` (intero): filtro per ID del regista
- **Codici di stato:**
  - `200 OK`: lista film

---

### `GET /api/movies/{id}` *(alias: `/api/films/{id}`)*
Restituisce i dettagli del singolo film.

- **Codici di stato:**
  - `200 OK`: film trovato
  - `404 Not Found`: film non presente

---

### `GET /api/movies/{id}/reviews` *(alias: `/api/films/{id}/recensioni`)*
Restituisce tutte le recensioni rilasciate dagli utenti per il film specificato.

- **Codici di stato:**
  - `200 OK`: lista recensioni
  - `404 Not Found`: film non trovato
- **Esempio di risposta (`200 OK`):**
```json
[
  {
    "id": 1,
    "voto": 5,
    "titolo": "Capolavoro assoluto",
    "testo": "Fotografia magistrale e colonna sonora indimenticabile.",
    "data": "2026-08-20",
    "filmId": 1,
    "titoloFilm": "La Grande Bellezza",
    "autoreId": 2,
    "nomeAutore": "Mario Rossi"
  }
]
```

---

## 3. Proiezioni

### `GET /api/screenings` *(alias: `/api/proiezioni`)*
Restituisce tutte le proiezioni programmate.

- **Parametri opzionali:**
  - `festivalId`: filtra per ID del festival
  - `filmId`: filtra per ID del film
  - `salaId`: filtra per ID della sala
  - `data` (formato `YYYY-MM-DD`): filtra per data della proiezione
- **Codici di stato:** `200 OK`

---

### `GET /api/screenings/{id}`
Restituisce i dettagli di una specifica proiezione.

- **Codici di stato:**
  - `200 OK`: proiezione trovata
  - `404 Not Found`: proiezione non trovata

---

## 4. Recensioni

### `GET /api/reviews/{id}`
Restituisce una singola recensione tramite il suo ID.

- **Codici di stato:**
  - `200 OK`: recensione trovata
  - `404 Not Found`: recensione non trovata

---

### `POST /api/reviews`
Crea una nuova recensione per un film. Richiede autenticazione.

- **Corpo della richiesta (JSON):**
```json
{
  "filmId": 1,
  "voto": 5,
  "titolo": "Esperienza fantastica",
  "testo": "Un'opera emozionante dall'inizio alla fine."
}
```
- **Codici di stato:**
  - `201 Created`: recensione creata con successo (include header `Location: /api/reviews/{id}`)
  - `400 Bad Request`: voto non valido (deve essere tra 1 e 5) o parametri obbligatori mancanti
  - `401 Unauthorized`: utente non autenticato
  - `404 Not Found`: film inesistente
  - `409 Conflict`: l'utente ha già recensito questo film (vincolo di unicità rispettato)

---

### `PUT /api/reviews/{id}`
Modifica una recensione esistente. Riservato all'autore della recensione o all'amministratore.

- **Corpo della richiesta (JSON):**
```json
{
  "voto": 4,
  "titolo": "Titolo aggiornato",
  "testo": "Testo modificato..."
}
```
- **Codici di stato:**
  - `200 OK`: recensione modificata con successo
  - `400 Bad Request`: voto non valido
  - `403 Forbidden`: l'utente autenticato non è l'autore della recensione
  - `404 Not Found`: recensione non trovata

---

### `DELETE /api/reviews/{id}`
Elimina una recensione. Riservato all'autore della recensione o all'amministratore.

- **Codici di stato:**
  - `204 No Content`: recensione eliminata con successo
  - `403 Forbidden`: utente non autorizzato a cancellare questa recensione
  - `404 Not Found`: recensione non trovata

---

## 5. Gestione degli Errori

Quando una richiesta fallisce, il gestore globale `@RestControllerAdvice` ([RestExceptionHandler.java](file:///Users/lucacarbonetti/SIW/Siw_Cinema/siw/src/main/java/it/uniroma3/siw/controller/rest/RestExceptionHandler.java)) restituisce una risposta JSON strutturata con il relativo codice di errore HTTP:

### Esempio Errore `404 Not Found`:
```json
{
  "timestamp": "2026-08-26T23:45:00",
  "status": 404,
  "error": "Not Found",
  "message": "Film non trovato con ID: 999",
  "path": "/api/movies/999"
}
```

### Esempio Errore `409 Conflict`:
```json
{
  "timestamp": "2026-08-26T23:45:00",
  "status": 409,
  "error": "Conflict",
  "message": "Hai già inserito una recensione per questo film",
  "path": "/api/reviews"
}
```

### Esempio Errore `400 Bad Request` (Validazione):
```json
{
  "timestamp": "2026-08-26T23:45:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Il voto deve essere compreso tra 1 e 5",
  "path": "/api/reviews"
}
```
