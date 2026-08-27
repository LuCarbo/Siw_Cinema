package it.uniroma3.siw.init;

import it.uniroma3.siw.model.*;
import it.uniroma3.siw.repository.*;
import it.uniroma3.siw.service.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private RegistaRepository registaRepository;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private ProiezioneRepository proiezioneRepository;

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Override
    public void run(String... args) throws Exception {
        if (credentialsRepository.count() > 0) {
            updateMediaToAuthentic();
            return; // Dati già inizializzati, media aggiornati
        }

        System.out.println("--- Inizializzazione Dati SIW Cinema con Foto e Locandine Originali ---");

        // 1. Utenti & Credenziali
        Utente adminUser = new Utente("Alessandro", "Rossi", "admin@siwcinema.it");
        Credentials adminCreds = new Credentials("admin", "admin", Credentials.ADMIN_ROLE, adminUser);
        credentialsService.saveCredentials(adminCreds);

        Utente marioUser = new Utente("Mario", "Bianchi", "mario.bianchi@email.it");
        Credentials marioCreds = new Credentials("mario", "password", Credentials.DEFAULT_ROLE, marioUser);
        credentialsService.saveCredentials(marioCreds);

        Utente giuliaUser = new Utente("Giulia", "Verdi", "giulia.verdi@email.it");
        Credentials giuliaCreds = new Credentials("giulia", "password", Credentials.DEFAULT_ROLE, giuliaUser);
        credentialsService.saveCredentials(giuliaCreds);

        // 2. Registi con foto originali ufficiali TMDB
        Regista nolan = new Regista("Christopher", "Nolan", LocalDate.of(1970, 7, 30), "Britannica");
        nolan.setFoto("https://image.tmdb.org/t/p/w500/xuAIuYSmsUzKlUMBFGVZaWsY3DZ.jpg");
        registaRepository.save(nolan);

        Regista villeneuve = new Regista("Denis", "Villeneuve", LocalDate.of(1967, 10, 3), "Canadese");
        villeneuve.setFoto("https://image.tmdb.org/t/p/w500/zdDx9Xs93UIrJFWYApYR28J8M6b.jpg");
        registaRepository.save(villeneuve);

        Regista rohrwacher = new Regista("Alice", "Rohrwacher", LocalDate.of(1981, 12, 29), "Italiana");
        rohrwacher.setFoto("https://image.tmdb.org/t/p/w500/8xDpjRUr6hlS7aGh8W0DkNbfby7.jpg");
        registaRepository.save(rohrwacher);

        Regista garrone = new Regista("Matteo", "Garrone", LocalDate.of(1968, 10, 15), "Italiana");
        garrone.setFoto("https://image.tmdb.org/t/p/w500/bszKQINwwZKe9Ybwdaz8G5a6H7S.jpg");
        registaRepository.save(garrone);

        Regista miyazaki = new Regista("Hayao", "Miyazaki", LocalDate.of(1941, 1, 5), "Giapponese");
        miyazaki.setFoto("https://image.tmdb.org/t/p/w500/ouhjt9KugzhWtdEyBPipihB3ic8.jpg");
        registaRepository.save(miyazaki);

        // 3. Film con locandine originali ufficiali TMDB
        Film oppenheimer = new Film("Oppenheimer", 2023, 180, "Biografico / Storico", "Stati Uniti");
        oppenheimer.setLocandina("https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg");
        oppenheimer.setRegista(nolan);
        filmRepository.save(oppenheimer);

        Film dune2 = new Film("Dune - Parte Due", 2024, 166, "Fantascienza / Avventura", "Stati Uniti");
        dune2.setLocandina("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg");
        dune2.setRegista(villeneuve);
        filmRepository.save(dune2);

        Film chimera = new Film("La Chimera", 2023, 130, "Drammatico / Avventura", "Italia");
        chimera.setLocandina("https://image.tmdb.org/t/p/w500/dV6SYHmmFLXQU7oKRfK4pOFCX6M.jpg");
        chimera.setRegista(rohrwacher);
        filmRepository.save(chimera);

        Film capitano = new Film("Io Capitano", 2023, 121, "Drammatico", "Italia");
        capitano.setLocandina("https://image.tmdb.org/t/p/w500/kGlZFwUQI5gAUdySNFfqGIkAF9n.jpg");
        capitano.setRegista(garrone);
        filmRepository.save(capitano);

        Film airone = new Film("Il ragazzo e l'airone", 2023, 124, "Animazione / Fantasy", "Giappone");
        airone.setLocandina("https://image.tmdb.org/t/p/w500/f4oZTcfGrVTXKTWg157AwikXqmP.jpg");
        airone.setRegista(miyazaki);
        filmRepository.save(airone);

        // 4. Sale
        Sala salaGrande = new Sala("Sala Grande", "Lungomare Marconi, Lido di Venezia", 1032);
        salaRepository.save(salaGrande);

        Sala salaPasolini = new Sala("Sala Pasolini", "Auditorium Parco della Musica, Roma", 450);
        salaRepository.save(salaPasolini);

        Sala salaFellini = new Sala("Sala Federico Fellini", "Via Veneto 45, Roma", 320);
        salaRepository.save(salaFellini);

        Sala salaLumiere = new Sala("Grand Théâtre Lumière", "Boulevard de la Croisette, Cannes", 850);
        salaRepository.save(salaLumiere);

        // 5. Festival con immagini originali dei luoghi e delle kermesse
        Festival venezia = new Festival(
                "Mostra Internazionale d'Arte Cinematografica di Venezia",
                2026,
                "Venezia",
                LocalDate.of(2026, 8, 27),
                LocalDate.of(2026, 9, 6),
                "La Mostra Internazionale d'Arte Cinematografica è il più antico festival cinematografico del mondo, fondato nel 1932. Si svolge ogni anno al Lido di Venezia."
        );
        venezia.setImmagine("https://images.unsplash.com/photo-1514890547357-a9ee288728e0?w=1200&auto=format&fit=crop&q=80");
        venezia.addFilm(oppenheimer);
        venezia.addFilm(chimera);
        venezia.addFilm(capitano);
        festivalRepository.save(venezia);

        Festival cannes = new Festival(
                "Festival di Cannes",
                2026,
                "Cannes",
                LocalDate.of(2026, 5, 12),
                LocalDate.of(2026, 5, 23),
                "Il celebre festival della Costa Azzurra dedicato all'eccellenza e all'innovazione del cinema mondiale d'autore con la prestigiosa Palma d'Oro."
        );
        cannes.setImmagine("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=1200&auto=format&fit=crop&q=80");
        cannes.addFilm(chimera);
        cannes.addFilm(airone);
        festivalRepository.save(cannes);

        Festival roma = new Festival(
                "Festa del Cinema di Roma",
                2026,
                "Roma",
                LocalDate.of(2026, 10, 15),
                LocalDate.of(2026, 10, 25),
                "Grande kermesse autunnale che trasforma l'Auditorium Parco della Musica e la città di Roma nella capitale internazionale del cinema e del pubblico."
        );
        roma.setImmagine("https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=1200&auto=format&fit=crop&q=80");
        roma.addFilm(dune2);
        roma.addFilm(oppenheimer);
        roma.addFilm(capitano);
        festivalRepository.save(roma);

        // 6. Proiezioni
        Proiezione p1 = new Proiezione(
                LocalDate.of(2026, 8, 28),
                LocalTime.of(20, 30),
                StatoProiezione.SCHEDULED,
                venezia,
                oppenheimer,
                salaGrande
        );
        proiezioneRepository.save(p1);

        Proiezione p2 = new Proiezione(
                LocalDate.of(2026, 8, 29),
                LocalTime.of(17, 0),
                StatoProiezione.SCHEDULED,
                venezia,
                capitano,
                salaGrande
        );
        proiezioneRepository.save(p2);

        Proiezione p3 = new Proiezione(
                LocalDate.of(2026, 8, 30),
                LocalTime.of(21, 15),
                StatoProiezione.SCHEDULED,
                venezia,
                chimera,
                salaGrande
        );
        proiezioneRepository.save(p3);

        Proiezione p4 = new Proiezione(
                LocalDate.of(2026, 10, 16),
                LocalTime.of(18, 0),
                StatoProiezione.SCHEDULED,
                roma,
                dune2,
                salaPasolini
        );
        proiezioneRepository.save(p4);

        Proiezione p5 = new Proiezione(
                LocalDate.of(2026, 10, 17),
                LocalTime.of(21, 0),
                StatoProiezione.SCHEDULED,
                roma,
                capitano,
                salaFellini
        );
        proiezioneRepository.save(p5);

        // 7. Recensioni
        Recensione r1 = new Recensione(
                "Un capolavoro assoluto del cinema contemporaneo",
                "Nolan firma un'opera monumentale sulla fragilità umana e il peso della scienza. Fotografia, sonoro e regia a livelli stratosferici.",
                5,
                oppenheimer,
                marioUser
        );
        recensioneRepository.save(r1);

        Recensione r2 = new Recensione(
                "Viaggio epico e toccante",
                "Garrone racconta l'odissea moderna con straordinaria delicatezza e autenticità visiva. Attori eccezionali.",
                5,
                capitano,
                marioUser
        );
        recensioneRepository.save(r2);

        Recensione r3 = new Recensione(
                "Poesia visiva e archeologia dell'anima",
                "Alice Rohrwacher conferma la sua cifra stilistica unica. Un film intriso di magia, mito e nostalgia.",
                4,
                chimera,
                giuliaUser
        );
        recensioneRepository.save(r3);

        Recensione r4 = new Recensione(
                "Spettacolo visivo monumentale",
                "Villeneuve orchestra un seguito incredibile, con un worldbuilding maestoso e scene d'azione memorabili.",
                5,
                dune2,
                giuliaUser
        );
        recensioneRepository.save(r4);

        System.out.println("--- Inizializzazione completata con successo! ---");
    }

    private void updateMediaToAuthentic() {
        // Aggiorna foto registi
        registaRepository.findAll().forEach(r -> {
            if ("Christopher".equalsIgnoreCase(r.getNome()) && "Nolan".equalsIgnoreCase(r.getCognome())) {
                r.setFoto("https://image.tmdb.org/t/p/w500/xuAIuYSmsUzKlUMBFGVZaWsY3DZ.jpg");
                registaRepository.save(r);
            } else if ("Denis".equalsIgnoreCase(r.getNome()) && "Villeneuve".equalsIgnoreCase(r.getCognome())) {
                r.setFoto("https://image.tmdb.org/t/p/w500/zdDx9Xs93UIrJFWYApYR28J8M6b.jpg");
                registaRepository.save(r);
            } else if ("Alice".equalsIgnoreCase(r.getNome()) && "Rohrwacher".equalsIgnoreCase(r.getCognome())) {
                r.setFoto("https://image.tmdb.org/t/p/w500/8xDpjRUr6hlS7aGh8W0DkNbfby7.jpg");
                registaRepository.save(r);
            } else if ("Matteo".equalsIgnoreCase(r.getNome()) && "Garrone".equalsIgnoreCase(r.getCognome())) {
                r.setFoto("https://image.tmdb.org/t/p/w500/bszKQINwwZKe9Ybwdaz8G5a6H7S.jpg");
                registaRepository.save(r);
            } else if ("Hayao".equalsIgnoreCase(r.getNome()) && "Miyazaki".equalsIgnoreCase(r.getCognome())) {
                r.setFoto("https://image.tmdb.org/t/p/w500/ouhjt9KugzhWtdEyBPipihB3ic8.jpg");
                registaRepository.save(r);
            }
        });

        // Aggiorna locandine film e assicura associazione registi
        Film oppenheimer = null;
        Film dune2 = null;
        Film chimera = null;
        Film capitano = null;
        Film airone = null;

        for (Film f : filmRepository.findAll()) {
            if ("Oppenheimer".equalsIgnoreCase(f.getTitolo())) {
                f.setLocandina("https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg");
                oppenheimer = f;
            } else if ("Dune - Parte Due".equalsIgnoreCase(f.getTitolo()) || f.getTitolo().toLowerCase().startsWith("dune")) {
                f.setLocandina("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg");
                dune2 = f;
            } else if ("La Chimera".equalsIgnoreCase(f.getTitolo())) {
                f.setLocandina("https://image.tmdb.org/t/p/w500/dV6SYHmmFLXQU7oKRfK4pOFCX6M.jpg");
                chimera = f;
            } else if ("Io Capitano".equalsIgnoreCase(f.getTitolo())) {
                f.setLocandina("https://image.tmdb.org/t/p/w500/kGlZFwUQI5gAUdySNFfqGIkAF9n.jpg");
                capitano = f;
            } else if ("Il ragazzo e l'airone".equalsIgnoreCase(f.getTitolo())) {
                f.setLocandina("https://image.tmdb.org/t/p/w500/f4oZTcfGrVTXKTWg157AwikXqmP.jpg");
                airone = f;
            }
            filmRepository.save(f);
        }

        // Assicura che i registi siano sempre associati correttamente
        for (Regista r : registaRepository.findAll()) {
            if ("Christopher".equalsIgnoreCase(r.getNome()) && oppenheimer != null && oppenheimer.getRegista() == null) {
                oppenheimer.setRegista(r);
                filmRepository.save(oppenheimer);
            } else if ("Denis".equalsIgnoreCase(r.getNome()) && dune2 != null && dune2.getRegista() == null) {
                dune2.setRegista(r);
                filmRepository.save(dune2);
            } else if ("Alice".equalsIgnoreCase(r.getNome()) && chimera != null && chimera.getRegista() == null) {
                chimera.setRegista(r);
                filmRepository.save(chimera);
            } else if ("Matteo".equalsIgnoreCase(r.getNome()) && capitano != null && capitano.getRegista() == null) {
                capitano.setRegista(r);
                filmRepository.save(capitano);
            } else if ("Hayao".equalsIgnoreCase(r.getNome()) && airone != null && airone.getRegista() == null) {
                airone.setRegista(r);
                filmRepository.save(airone);
            }
        }
    }
}
