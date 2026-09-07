package it.uniroma3.siw.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Caricatore automatico del file .env per l'ambiente Spring Boot.
 * Cerca il file .env nella directory corrente di lavoro, nella root del progetto o nella cartella del modulo.
 * Inietta le variabili lette all'inizio delle PropertySource di Spring, permettendo
 * ad application.properties e ad @Value("${...}") di accedere ai valori definiti in .env.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File envFile = findDotenvFile();
        if (envFile == null || !envFile.exists() || !envFile.isFile()) {
            return;
        }

        Map<String, Object> dotenvMap = loadEnvFile(envFile);
        if (!dotenvMap.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, dotenvMap));
            System.out.println("[SIW Cinema] File .env caricato con successo da: " + envFile.getAbsolutePath() +
                    " (" + dotenvMap.size() + " variabili caricate)");
        }
    }

    private File findDotenvFile() {
        String userDir = System.getProperty("user.dir");
        String[] potentialPaths = {
                userDir + File.separator + ".env",
                userDir + File.separator + "siw" + File.separator + ".env",
                userDir + File.separator + ".." + File.separator + ".env"
        };

        for (String path : potentialPaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                return f;
            }
        }
        return null;
    }

    private Map<String, Object> loadEnvFile(File file) {
        Map<String, Object> envMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Ignora linee vuote o commenti
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalIndex = line.indexOf('=');
                if (equalIndex > 0) {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();

                    // Rimuovi eventuali apici singoli o doppi attorno al valore
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        if (value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                    }

                    envMap.put(key, value);
                }
            }
        } catch (Exception e) {
            System.err.println("[SIW Cinema] Errore durante la lettura del file .env: " + e.getMessage());
        }
        return envMap;
    }
}
