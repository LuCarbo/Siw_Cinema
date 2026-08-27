package it.uniroma3.siw;

import it.uniroma3.siw.model.*;
import it.uniroma3.siw.repository.FestivalRepository;
import it.uniroma3.siw.repository.FilmRepository;
import it.uniroma3.siw.repository.ProiezioneRepository;
import it.uniroma3.siw.repository.SalaRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SpringBootTest
@Transactional
public class DataAccessPerformanceAnalysisTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private ProiezioneRepository proiezioneRepository;

    private Long sampleFestivalId;

    @BeforeEach
    void setUpDataset() {
        Festival festival = new Festival("Festival Sperimentale", 2026, "Roma",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), "Dataset per analisi prestazioni");
        festival = festivalRepository.save(festival);
        this.sampleFestivalId = festival.getId();

        Sala sala = new Sala("Sala Grande", "Via Nazionale 10", 300);
        sala = salaRepository.save(sala);

        for (int i = 1; i <= 10; i++) {
            Film film = new Film("Film Test " + i, 2024, 100 + i, "Drammatico", "Italia");
            film = filmRepository.save(film);
            festival.addFilm(film);

            Proiezione p = new Proiezione(LocalDate.of(2026, 10, 2), LocalTime.of(10 + i, 0), StatoProiezione.SCHEDULED, festival, film, sala);
            proiezioneRepository.save(p);
        }
        festivalRepository.save(festival);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Analisi Sperimentale: Confronto Strategia LAZY (N+1) vs JOIN FETCH")
    void compareLazyVsJoinFetchPerformance() {
        Session session = entityManager.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);

        System.out.println("\n" + "=".repeat(65));
        System.out.println("=== ANALISI SPERIMENTALE DELL'ACCESSO AI DATI (JPA / HIBERNATE) ===");
        System.out.println("=".repeat(65));
        System.out.println("Caso d'uso: Caricamento proiezioni con dati di Festival, Film e Sala\n");

        // -------------------------------------------------------------
        // STRATEGIA 1: Accesso Standard LAZY (Senza JOIN FETCH -> N+1)
        // -------------------------------------------------------------
        entityManager.clear();
        stats.clear();

        long startTimeLazy = System.currentTimeMillis();

        // 1 Query per recuperare le proiezioni (le associazioni sono caricate on-demand)
        List<Proiezione> proiezioniLazy = entityManager.createQuery(
                "SELECT p FROM Proiezione p WHERE p.festival.id = :festId", Proiezione.class)
                .setParameter("festId", sampleFestivalId)
                .getResultList();

        // Itera e accede alle proprietà delle entità collegate
        int loadedEntitiesLazy = 0;
        for (Proiezione p : proiezioniLazy) {
            String festNome = p.getFestival().getNome();
            String filmTitolo = p.getFilm().getTitolo();
            String salaNome = p.getSala().getNome();
            if (festNome != null && filmTitolo != null && salaNome != null) {
                loadedEntitiesLazy++;
            }
        }

        long timeLazy = System.currentTimeMillis() - startTimeLazy;
        long queriesLazy = stats.getPrepareStatementCount();

        System.out.println("--- Strategia 1: Accesso Standard LAZY (Problema N+1 Query) ---");
        System.out.println("Proiezioni caricate: " + loadedEntitiesLazy);
        System.out.println("Query SQL eseguite:  " + queriesLazy + "  (1 query iniziale + query separate per ogni entità collegata)");
        System.out.println("Tempo complessivo:   " + timeLazy + " ms\n");

        // -------------------------------------------------------------
        // STRATEGIA 2: Accesso Ottimizzato con JOIN FETCH (1 singola Query)
        // -------------------------------------------------------------
        entityManager.clear();
        stats.clear();

        long startTimeJoinFetch = System.currentTimeMillis();

        // 1 Singola Query ottimizzata con JOIN FETCH
        List<Proiezione> proiezioniFetch = entityManager.createQuery(
                "SELECT p FROM Proiezione p " +
                "JOIN FETCH p.festival " +
                "JOIN FETCH p.film " +
                "JOIN FETCH p.sala " +
                "WHERE p.festival.id = :festId", Proiezione.class)
                .setParameter("festId", sampleFestivalId)
                .getResultList();

        // Itera e accede alle proprietà (già caricate in memoria nella prima query)
        int loadedEntitiesFetch = 0;
        for (Proiezione p : proiezioniFetch) {
            String festNome = p.getFestival().getNome();
            String filmTitolo = p.getFilm().getTitolo();
            String salaNome = p.getSala().getNome();
            if (festNome != null && filmTitolo != null && salaNome != null) {
                loadedEntitiesFetch++;
            }
        }

        long timeJoinFetch = System.currentTimeMillis() - startTimeJoinFetch;
        long queriesJoinFetch = stats.getPrepareStatementCount();

        System.out.println("--- Strategia 2: Accesso Ottimizzato con JOIN FETCH ---");
        System.out.println("Proiezioni caricate: " + loadedEntitiesFetch);
        System.out.println("Query SQL eseguite:  " + queriesJoinFetch + "  (1 singola query SQL con JOIN)");
        System.out.println("Tempo complessivo:   " + timeJoinFetch + " ms\n");

        System.out.println("--- CONCLUSIONE & CONFRONTO ---");
        System.out.println("Risparmio Query:    " + (queriesLazy - queriesJoinFetch) + " query SQL risparmiate");
        System.out.println("Fattore Efficienza: Da " + queriesLazy + " query a " + queriesJoinFetch + " query singola");
        System.out.println("=".repeat(65) + "\n");
    }
}
