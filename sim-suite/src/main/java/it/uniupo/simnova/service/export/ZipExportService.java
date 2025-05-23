package it.uniupo.simnova.service.export;

import it.uniupo.simnova.service.scenario.helper.MediaHelper;
import it.uniupo.simnova.service.storage.FileStorageService;
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
     * Servizio per l'esportazione in PDF.
     */
    private final PdfExportService pdfExportService;
    /**
     * Servizio per l'esportazione in JSON.
     */
    private final JSONExportService jsonExportService;

    /**
     * Costruttore del servizio ZipExportService.
     *
     * @param fileStorageService servizio per la gestione dei file
     */
    @Autowired
    public ZipExportService(FileStorageService fileStorageService, PdfExportService pdfExportService, JSONExportService jsonExportService) {
        this.fileStorageService = fileStorageService;
        this.pdfExportService = pdfExportService;
        this.jsonExportService = jsonExportService;
    }

    /**
     * Esporta uno scenario in un file ZIP, includendo il file JSON dello scenario e tutti i file multimediali associati.
     *
     * @param scenarioId ID dello scenario da esportare
     * @return Array di byte rappresentante il file ZIP generato
     * @throws IOException in caso di errore durante la scrittura del file ZIP
     */
    public byte[] exportScenarioToZip(Integer scenarioId) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            // Aggiunge il file JSON dello scenario allo ZIP
            byte[] jsonBytes = jsonExportService.exportScenarioToJSON(scenarioId);
            ZipEntry jsonEntry = new ZipEntry("scenario.json");
            zipOut.putNextEntry(jsonEntry);
            zipOut.write(jsonBytes);
            zipOut.closeEntry();

            // Recupera la lista dei file multimediali associati allo scenario
            List<String> mediaFiles = MediaHelper.getMediaFilesForScenario(scenarioId);
            if (!mediaFiles.isEmpty()) {
                // Crea una directory per gli allegati multimediali all'interno dello ZIP
                zipOut.putNextEntry(new ZipEntry("esami/"));
                zipOut.closeEntry();

                // Per ogni file multimediale trovato
                for (String filename : mediaFiles) {
                    try {
                        // Costruisce il percorso assoluto del file
                        Path imagePath = Paths.get(fileStorageService.getMediaDirectory().toString(), filename);
                        // Verifica che il file esista fisicamente
                        if (Files.exists(imagePath)) {
                            // Legge il contenuto del file e lo aggiunge allo ZIP
                            byte[] imageBytes = Files.readAllBytes(imagePath);
                            ZipEntry imageEntry = new ZipEntry("esami/" + filename);
                            zipOut.putNextEntry(imageEntry);
                            zipOut.write(imageBytes);
                            zipOut.closeEntry();
                        }
                    } catch (IOException e) {
                        // Logga eventuali errori nell'aggiunta dei file multimediali
                        logger.error("Errore nell'aggiungere l'immagine {} allo ZIP", filename, e);
                    }
                }
            }
            // Finalizza e svuota lo stream ZIP
            zipOut.finish();
            zipOut.flush();
            // Restituisce il contenuto dello ZIP come array di byte
            return baos.toByteArray();
        }
    }

    /**
     * Esporta uno scenario in un file ZIP, includendo il file PDF dello scenario e tutti i file multimediali associati.
     *
     * @param scenarioId ID dello scenario da esportare
     * @param desc       flag per includere la descrizione
     * @param brief      flag per includere il brief
     * @param infoGen    flag per includere le informazioni generali
     * @param patto      flag per includere il patto
     * @param azioni     flag per includere le azioni chiave
     * @param obiettivi  flag per includere gli obiettivi
     * @param moula      flag per includere la sezione moula
     * @param liqui      flag per includere la sezione liquidi
     * @param matNec     flag per includere i materiali necessari
     * @param param      flag per includere i parametri
     * @param acces      flag per includere gli accessi
     * @param fisic      flag per includere l'esame fisico
     * @param esam       flag per includere gli esami
     * @param time       flag per includere la timeline
     * @param scen       flag per includere la sceneggiatura
     * @return Array di byte rappresentante il file ZIP generato
     * @throws IOException in caso di errore durante la scrittura del file ZIP
     */
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

            // Recupera la lista dei file multimediali associati allo scenario
            List<String> mediaFiles = MediaHelper.getMediaFilesForScenario(scenarioId);
            if (!mediaFiles.isEmpty()) {
                // Crea una directory per gli allegati multimediali all'interno dello ZIP
                zipOut.putNextEntry(new ZipEntry("esami/"));
                zipOut.closeEntry();

                // Per ogni file multimediale trovato
                for (String filename : mediaFiles) {
                    try {
                        // Costruisce il percorso assoluto del file
                        Path imagePath = Paths.get(fileStorageService.getMediaDirectory().toString(), filename);
                        // Verifica che il file esista fisicamente
                        if (Files.exists(imagePath)) {
                            // Legge il contenuto del file e lo aggiunge allo ZIP
                            byte[] imageBytes = Files.readAllBytes(imagePath);
                            ZipEntry imageEntry = new ZipEntry("esami/" + filename);
                            zipOut.putNextEntry(imageEntry);
                            zipOut.write(imageBytes);
                            zipOut.closeEntry();
                        }
                    } catch (IOException e) {
                        // Logga eventuali errori nell'aggiunta dei file multimediali
                        logger.error("Errore nell'aggiungere l'immagine {} allo ZIP", filename, e);
                    }
                }
            }
            // Finalizza e svuota lo stream ZIP
            zipOut.finish();
            zipOut.flush();
            // Restituisce il contenuto dello ZIP come array di byte
            return baos.toByteArray();
        }
    }
}
