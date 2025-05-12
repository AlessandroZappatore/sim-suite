package it.uniupo.simnova.service.storage;

import it.uniupo.simnova.service.scenario.ScenarioService;
import jakarta.annotation.PostConstruct; // Import corretto per @PostConstruct
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path rootLocation;

    /**
     * Costruttore che inizializza il servizio con il percorso della directory
     * specificato in application.properties.
     *
     * @param mediaDir Il percorso della directory di archiviazione, iniettato da Spring.
     */
    public FileStorageService(@Value("${storage.media-dir}") String mediaDir) {
        // Risolve il percorso assoluto e normalizza per sicurezza
        this.rootLocation = Paths.get(mediaDir).toAbsolutePath().normalize();
        logger.info("Percorso di archiviazione configurato: {}", this.rootLocation);
    }

    /**
     * Metodo eseguito dopo l'inizializzazione del bean per creare la directory
     * di archiviazione se non esiste.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            logger.info("Directory di archiviazione creata (o già esistente): {}", rootLocation);
        } catch (IOException e) {
            logger.error("Impossibile creare la directory di archiviazione {}", rootLocation, e);
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * Salva un file nella directory di archiviazione.
     * Il nome del file viene sanitizzato e viene aggiunto l'ID dello scenario.
     *
     * @param file     InputStream del file da salvare.
     * @param filename Nome originale del file.
     * @return Il nome del file sanitizzato e salvato, o null in caso di input non valido.
     * @throws RuntimeException Se si verifica un errore durante il salvataggio.
     */
    public String storeFile(InputStream file, String filename) {
        try {
            if (file == null || filename == null || filename.isBlank()) {
                logger.warn("Input non valido per storeFile: file, nome file o idScenario mancanti.");
                return null; // Ritorna null per input non validi
            }
            String sanitizedFilename = getSanitizedFilename(filename);
            // Risolve il percorso completo del file di destinazione
            Path destinationFile = this.rootLocation.resolve(sanitizedFilename).normalize();

            // Controllo di sicurezza: verifica che il file sia salvato DENTRO la rootLocation
            if (!destinationFile.getParent().equals(this.rootLocation)) {
                logger.error("Tentativo di memorizzare il file fuori dalla directory consentita: {}", destinationFile);
                throw new RuntimeException("Cannot store file outside current directory");
            }

            // Copia il file, sostituendo quello esistente se presente
            Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File memorizzato con successo: {}", sanitizedFilename);
            return sanitizedFilename; // Ritorna il nome del file come salvato
        } catch (IOException e) {
            logger.error("Errore durante la memorizzazione del file {} (sanitized: {})", filename, getSanitizedFilename(filename), e);
            // Rilancia come RuntimeException per segnalare l'errore al chiamante
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    /**
     * Genera un nome file sicuro aggiungendo l'ID dello scenario e rimuovendo caratteri non validi.
     *
     * @param filename Nome originale del file.
     * @return Nome del file sanitizzato.
     */
    private static String getSanitizedFilename(String filename) {
        String extension = "";
        String baseName = filename;
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex >= 0) { // Usa >= 0 per gestire file che iniziano con '.'
            extension = filename.substring(lastDotIndex); // Include il '.'
            baseName = filename.substring(0, lastDotIndex);
        }
        // Pulisce il nome base e l'estensione separatamente se necessario
        String sanitizedBaseName = baseName.replaceAll("[^a-zA-Z0-9_-]", "_");
        String sanitizedExtension = extension.replaceAll("[^a-zA-Z0-9.]", ""); // Permette solo lettere, numeri e '.' nell'estensione

        // Costruisce il nuovo nome file
        String newFilename = sanitizedBaseName + "_" + sanitizedExtension;

        // Ulteriore sanitizzazione generale (potrebbe essere ridondante ma sicura)
        return newFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Elimina una lista di file dalla directory di archiviazione.
     *
     * @param filenames Lista dei nomi dei file da eliminare.
     */
    public void deleteFiles(List<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            logger.warn("Lista di file da eliminare vuota o nulla.");
            return;
        }
        for (String filename : filenames) {
            // Chiama deleteFile per ogni file nella lista
            deleteFile(filename);
        }
    }

    /**
     * Elimina un singolo file dalla directory di archiviazione.
     * Verifica prima che il file non sia utilizzato da altri scenari.
     *
     * @param filename Nome del file da eliminare.
     */
    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) {
            logger.warn("Nome file non valido per l'eliminazione.");
            return;
        }

        // Verifica se il file è utilizzato in altri scenari
        if (ScenarioService.isFileInUse(filename)) {
            logger.info("File {} non eliminato perché è utilizzato in altri scenari", filename);
            return;
        }

        try {
            Path filePath = this.rootLocation.resolve(filename).normalize();

            // Controllo di sicurezza: verifica che il file sia DENTRO la rootLocation
            if (!filePath.getParent().equals(this.rootLocation)) {
                logger.error("Tentativo di eliminare il file fuori dalla directory consentita: {}", filePath);
                return; // Non procedere se il percorso è sospetto
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                logger.info("File eliminato con successo: {}", filename);
            } else {
                logger.warn("File non trovato per l'eliminazione: {}", filename);
            }
        } catch (IOException e) {
            // Logga l'errore ma non rilancia eccezioni per non bloccare eliminazioni multiple
            logger.error("Errore durante l'eliminazione del file {}", filename, e);
        }
    }

    /**
     * Restituisce il percorso assoluto e normalizzato della directory di archiviazione.
     *
     * @return Oggetto Path che rappresenta la directory di archiviazione.
     */
    public Path getMediaDirectory() {
        return rootLocation;
    }

    /**
     * Controlla se un file esiste nella directory di archiviazione.
     *
     * @param centerLogoFilename Nome del file da controllare.
     * @return true se il file esiste, false altrimenti.
     */
    public boolean fileExists(String centerLogoFilename) {
        Path filePath = rootLocation.resolve(centerLogoFilename);
        return Files.exists(filePath);
    }

    public void store(InputStream inputStream, String centerLogoFilename) {
        try {
            Path destinationFile = rootLocation.resolve(centerLogoFilename).normalize();
            // Controllo di sicurezza: verifica che il file sia salvato DENTRO la rootLocation
            if (!destinationFile.getParent().equals(rootLocation)) {
                logger.error("Tentativo di memorizzare il file fuori dalla directory consentita: {}", destinationFile);
                throw new RuntimeException("Cannot store file outside current directory");
            }
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File memorizzato con successo: {}", centerLogoFilename);
        } catch (IOException e) {
            logger.error("Errore durante la memorizzazione del file {}", centerLogoFilename, e);
            throw new RuntimeException("Failed to store file " + centerLogoFilename, e);
        }
    }

    public InputStream readFile(String centerLogoFilename) {
        try {
            Path filePath = rootLocation.resolve(centerLogoFilename).normalize();
            // Controllo di sicurezza: verifica che il file sia DENTRO la rootLocation
            if (!filePath.getParent().equals(rootLocation)) {
                logger.error("Tentativo di leggere il file fuori dalla directory consentita: {}", filePath);
                throw new RuntimeException("Cannot read file outside current directory");
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            logger.error("Errore durante la lettura del file {}", centerLogoFilename, e);
            throw new RuntimeException("Failed to read file " + centerLogoFilename, e);
        }
    }

    public ArrayList<String> getAllFiles() {
        ArrayList<String> files = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootLocation)) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    String filename = path.getFileName().toString();
                    // Esclude center_logo.png dalla lista dei file
                    if (!filename.equals("center_logo.png")) {
                        files.add(filename);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Errore durante la lettura dei file nella directory {}", rootLocation, e);
        }
        return files;
    }
}