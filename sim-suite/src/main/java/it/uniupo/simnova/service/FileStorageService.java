package it.uniupo.simnova.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * Servizio per la gestione dello storage dei file.
 * <p>
 * Fornisce metodi per memorizzare, eliminare e gestire file all'interno di una directory specificata.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class FileStorageService {
    /**
     * Logger per la registrazione delle operazioni del servizio.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    /**
     * Directory di root per lo storage dei file.
     */
    private final Path rootLocation;

    /**
     * Costruttore che inizializza la directory di root per lo storage dei file.
     */
    public FileStorageService() {
        try {
            // Ottieni il percorso assoluto della directory resources
            File resourcesDir = ResourceUtils.getFile("classpath:META-INF/resources/Media");
            this.rootLocation = resourcesDir.toPath();

            // Crea la directory se non esiste (solo in sviluppo)
            if (!resourcesDir.exists()) {
                Files.createDirectories(rootLocation);
                logger.info("Directory di storage creata: {}", rootLocation);
            }
        } catch (IOException e) {
            logger.error("Impossibile inizializzare lo storage", e);
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    /**
     * Memorizza un file nella directory di root.
     *
     * @param file     l'InputStream del file da memorizzare
     * @param filename il nome del file
     * @return il nome del file memorizzato
     */
    public String storeFile(InputStream file, String filename, Integer idScenario) {
        try {
            if (file == null || filename == null || filename.isEmpty() || idScenario == null) {
                logger.warn("File, nome file o idScenario non valido");
                return null;
            }

            // Estrai estensione e nome del file
            String sanitizedFilename = getSanitizedFilename(filename, idScenario);
            Path destinationFile = this.rootLocation.resolve(sanitizedFilename)
                    .normalize().toAbsolutePath();

            // Verifica che il percorso sia valido
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                logger.error("Tentativo di memorizzare il file fuori dalla directory corrente");
                throw new RuntimeException("Cannot store file outside current directory");
            }

            Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File memorizzato con successo: {}", sanitizedFilename);
            return sanitizedFilename;
        } catch (IOException e) {
            logger.error("Errore durante la memorizzazione del file {}", filename, e);
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    private static String getSanitizedFilename(String filename, Integer idScenario) {
        String extension = "";
        String baseName = filename;
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = filename.substring(lastDotIndex);
            baseName = filename.substring(0, lastDotIndex);
        }

        // Crea il nuovo nome del file con il formato nomefile_idScenario.estensione
        String newFilename = baseName + "_" + idScenario + extension;

        // Sanitize filename
        return newFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    /**
     * Elimina una lista di file dalla directory di root.
     *
     * @param filenames la lista dei nomi dei file da eliminare
     */
    public void deleteFiles(List<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            logger.warn("Lista di file da eliminare vuota o nulla");
            return;
        }

        for (String filename : filenames) {
            try {
                deleteFile(filename);
            } catch (Exception e) {
                logger.error("Errore durante l'eliminazione del file {}", filename, e);
            }
        }
    }

    /**
     * Elimina un file dalla directory di root.
     *
     * @param filename il nome del file da eliminare
     */
    public void deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            logger.warn("Nome file non valido per l'eliminazione");
            return;
        }

        try {
            Path filePath = this.rootLocation.resolve(filename).normalize().toAbsolutePath();

            // Verifica che il file sia dentro la directory consentita
            if (!filePath.getParent().equals(this.rootLocation.toAbsolutePath())) {
                logger.error("Tentativo di eliminare il file fuori dalla directory corrente");
                return;
            }

            Files.deleteIfExists(filePath);
            logger.info("File eliminato con successo: {}", filename);
        } catch (IOException e) {
            logger.error("Errore durante l'eliminazione del file {}", filename, e);
        }
    }

    public Object getMediaDirectory() {
        try {
            return ResourceUtils.getFile("classpath:META-INF/resources/Media");
        } catch (FileNotFoundException e) {
            logger.error("Impossibile trovare la directory dei media", e);
            return null;
        }
    }
}