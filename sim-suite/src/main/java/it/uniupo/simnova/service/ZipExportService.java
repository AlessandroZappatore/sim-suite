package it.uniupo.simnova.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Servizio per l'esportazione di scenari in formato ZIP.
 * <p>
 * Questo servizio gestisce la creazione di file ZIP contenenti il JSON dello scenario
 * e gli allegati multimediali associati.
 * </p>
 */
@Service
public class ZipExportService {
    private static final Logger logger = LoggerFactory.getLogger(ZipExportService.class);

    private final FileStorageService fileStorageService;
    private final ScenarioService scenarioService;

    @Autowired
    public ZipExportService(FileStorageService fileStorageService, ScenarioService scenarioService) {
        this.fileStorageService = fileStorageService;
        this.scenarioService = scenarioService;
    }

    /**
     * Crea un file ZIP contenente lo scenario in formato JSON e gli allegati multimediali.
     *
     * @param scenarioId ID dello scenario da esportare
     * @return Array di byte contenente il file ZIP
     * @throws IOException in caso di errori durante la creazione del ZIP
     */
    public byte[] exportScenarioToZip(Integer scenarioId) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            // Aggiungi il file JSON allo ZIP
            byte[] jsonBytes = JSONExportService.exportScenarioToJSON(scenarioId);
            ZipEntry jsonEntry = new ZipEntry("scenario.json");
            zipOut.putNextEntry(jsonEntry);
            zipOut.write(jsonBytes);
            zipOut.closeEntry();

            // Ottieni le immagini associate allo scenario e aggiungile allo ZIP
            List<String> mediaFiles = scenarioService.getScenarioMediaFiles(scenarioId);
            if (mediaFiles != null && !mediaFiles.isEmpty()) {
                // Crea una cartella "esami" per le immagini
                zipOut.putNextEntry(new ZipEntry("esami/"));
                zipOut.closeEntry();

                for (String filename : mediaFiles) {
                    try {
                        Path imagePath = Paths.get(fileStorageService.getMediaDirectory().toString(), filename);
                        if (Files.exists(imagePath)) {
                            byte[] imageBytes = Files.readAllBytes(imagePath);
                            ZipEntry imageEntry = new ZipEntry("esami/" + filename);
                            zipOut.putNextEntry(imageEntry);
                            zipOut.write(imageBytes);
                            zipOut.closeEntry();
                        }
                    } catch (IOException e) {
                        logger.error("Errore nell'aggiungere l'immagine {} allo ZIP", filename, e);
                    }
                }
            }

            zipOut.finish();
            zipOut.flush();
            return baos.toByteArray();
        }
    }
}