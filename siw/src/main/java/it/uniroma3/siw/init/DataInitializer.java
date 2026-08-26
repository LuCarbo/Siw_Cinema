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
            return; // Dati già inizializzati
        }

        System.out.println("--- Inizializzazione Dati SIW Cinema ---");

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

        // 2. Registi
        Regista nolan = new Regista("Christopher", "Nolan", LocalDate.of(1970, 7, 30), "Britannica");
        nolan.setFoto("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80");
        registaRepository.save(nolan);

        Regista villeneuve = new Regista("Denis", "Villeneuve", LocalDate.of(1967, 10, 3), "Canadese");
        villeneuve.setFoto("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80");
        registaRepository.save(villeneuve);

        Regista rohrwacher = new Regista("Alice", "Rohrwacher", LocalDate.of(1981, 12, 29), "Italiana");
        rohrwacher.setFoto("https://images.unsplash.com/photo-1580489944761-15a19d654956?w=500&auto=format&fit=crop&q=80");
        registaRepository.save(rohrwacher);

        Regista garrone = new Regista("Matteo", "Garrone", LocalDate.of(1968, 10, 15), "Italiana");
        garrone.setFoto("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80");
        registaRepository.save(garrone);

        Regista miyazaki = new Regista("Hayao", "Miyazaki", LocalDate.of(1941, 1, 5), "Giapponese");
        miyazaki.setFoto("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=500&auto=format&fit=crop&q=80");
        registaRepository.save(miyazaki);

        // 3. Film
        Film oppenheimer = new Film("Oppenheimer", 2023, 180, "Biografico / Storico", "Stati Uniti");
        oppenheimer.setLocandina("https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80");
        oppenheimer.setRegista(nolan);
        filmRepository.save(oppenheimer);

        Film dune2 = new Film("Dune - Parte Due", 2024, 166, "Fantascienza / Avventura", "Stati Uniti");
        dune2.setLocandina("https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80");
        dune2.setRegista(villeneuve);
        filmRepository.save(dune2);

        Film chimera = new Film("La Chimera", 2023, 130, "Drammatico / Avventura", "Italia");
        chimera.setLocandina("https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600&auto=format&fit=crop&q=80");
        chimera.setRegista(rohrwacher);
        filmRepository.save(chimera);

        Film capitano = new Film("Io Capitano", 2023, 121, "Drammatico", "Italia");
        capitano.setLocandina("https://images.unsplash.com/photo-1518676590629-3dcbd9c5a5c9?w=600&auto=format&fit=crop&q=80");
        capitano.setRegista(garrone);
        filmRepository.save(capitano);

        Film airone = new Film("Il ragazzo e l'airone", 2023, 124, "Animazione / Fantasy", "Giappone");
        airone.setLocandina("https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80");
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

        // 5. Festival
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
}
