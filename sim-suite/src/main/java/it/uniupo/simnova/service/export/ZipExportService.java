package it.uniupo.simnova.service.export;

import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
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
 * Servizio per l'esportazione di uno scenario in un file ZIP.
 * Contiene il file JSON dello scenario e gli allegati multimediali.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class ZipExportService {
    /**
     * Logger per la registrazione delle informazioni e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(ZipExportService.class);
    /**
     * Servizio per la gestione dei file.
     */
    private final FileStorageService fileStorageService;
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;

    private final PdfExportService pdfExportService;

    /**
     * Costruttore del servizio ZipExportService.
     *
     * @param fileStorageService servizio per la gestione dei file
     * @param scenarioService    servizio per la gestione degli scenari
     */
    @Autowired
    public ZipExportService(FileStorageService fileStorageService, ScenarioService scenarioService, PdfExportService pdfExportService) {
        this.fileStorageService = fileStorageService;
        this.scenarioService = scenarioService;
        this.pdfExportService = pdfExportService;
    }

    /**
     * Esporta uno scenario in un file ZIP.
     *
     * @param scenarioId ID dello scenario da esportare
     * @return byte[] contenente il file ZIP
     * @throws IOException in caso di errore durante la scrittura del file ZIP
     */
    public byte[] exportScenarioToZip(Integer scenarioId) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            // Aggiunge il file JSON dello scenario allo ZIP
            byte[] jsonBytes = JSONExportService.exportScenarioToJSON(scenarioId);
            ZipEntry jsonEntry = new ZipEntry("scenario.json");
            zipOut.putNextEntry(jsonEntry);
            zipOut.write(jsonBytes);
            zipOut.closeEntry();

            // Aggiunge gli allegati multimediali allo ZIP
            List<String> mediaFiles = scenarioService.getScenarioMediaFiles(scenarioId);
            if (mediaFiles != null && !mediaFiles.isEmpty()) {
                // Crea una directory per gli allegati multimediali
                zipOut.putNextEntry(new ZipEntry("esami/"));
                zipOut.closeEntry();

                // Aggiunge ogni file multimediale allo ZIP
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

    public byte[] exportScenarioPdfToZip(Integer scenarioId,
                                         boolean desc,
                                         boolean brief,
                                         boolean infoGen,
                                         boolean patto,
                                         boolean azioni,
                                         boolean obiettivi,
                                         boolean moula,
                                         boolean liqui,
                                         boolean matNec,
                                         boolean param,
                                         boolean acces,
                                         boolean fisic,
                                         boolean esam,
                                         boolean time,
                                         boolean scen) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            // Aggiunge il file PDF dello scenario allo ZIP
            byte[] pdfBytes = pdfExportService.exportScenarioToPdf(scenarioId, desc, brief, infoGen, patto, azioni, obiettivi, moula, liqui, matNec, param, acces, fisic, esam, time, scen);
            ZipEntry pdfEntry = new ZipEntry("scenario.pdf");
            zipOut.putNextEntry(pdfEntry);
            zipOut.write(pdfBytes);
            zipOut.closeEntry();

            List<String> mediaFiles = scenarioService.getScenarioMediaFiles(scenarioId);
            if (mediaFiles != null && !mediaFiles.isEmpty()) {
                // Crea una directory per gli allegati multimediali
                zipOut.putNextEntry(new ZipEntry("esami/"));
                zipOut.closeEntry();

                // Aggiunge ogni file multimediale allo ZIP
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