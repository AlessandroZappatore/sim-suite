package it.uniupo.simnova.service;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import java.io.*;
import java.nio.file.*;
import java.util.List;

@Service
public class FileStorageService {
    private final Path rootLocation;

    public FileStorageService() {
        try {
            // Ottieni il percorso assoluto della directory resources
            File resourcesDir = ResourceUtils.getFile("classpath:META-INF/resources/Media");
            this.rootLocation = resourcesDir.toPath();

            // Crea la directory se non esiste (solo in sviluppo)
            if (!resourcesDir.exists()) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String storeFile(InputStream file, String filename) {
        try {
            if (file == null || filename == null || filename.isEmpty()) {
                return null;
            }

            // Sanitize filename
            String sanitizedFilename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
            Path destinationFile = this.rootLocation.resolve(sanitizedFilename)
                    .normalize().toAbsolutePath();

            // Verifica che il percorso sia valido
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory");
            }

            Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return sanitizedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    public void deleteFiles(List<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            return;
        }

        for (String filename : filenames) {
            try {
                deleteFile(filename);
            } catch (Exception e) {
                // Logga l'errore ma continua con gli altri file
                e.printStackTrace();
            }
        }
    }

    public boolean deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        try {
            Path filePath = this.rootLocation.resolve(filename).normalize().toAbsolutePath();

            // Verifica che il file sia dentro la directory consentita
            if (!filePath.getParent().equals(this.rootLocation.toAbsolutePath())) {
                return false;
            }

            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}