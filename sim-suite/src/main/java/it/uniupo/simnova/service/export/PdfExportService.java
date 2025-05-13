package it.uniupo.simnova.service.export;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.helper.pdf.LogoLoader;
import it.uniupo.simnova.service.scenario.components.*;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static it.uniupo.simnova.service.export.helper.pdf.LoadFont.loadFont;
import static it.uniupo.simnova.service.export.helper.pdf.LogoLoader.loadCenterLogo;
import static it.uniupo.simnova.service.export.helper.pdf.LogoLoader.loadLogo;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioDescription.createScenarioDescription;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioExam.createExamsSection;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioHeader.createScenarioHeader;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioPatient.createPatientSection;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioSceneggiatura.createSceneggiaturaSection;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioTimeline.createTimelineSection;
import static it.uniupo.simnova.views.constant.PdfConstant.*;

/**
 * Servizio per l'esportazione di scenari in formato PDF.
 * <p>
 * Questo servizio utilizza Apache PDFBox per generare documenti PDF contenenti informazioni dettagliate sugli scenari.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class PdfExportService {
    /**
     * Logger per il servizio.
     */
    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);
    /**
     * Posizione Y corrente per il disegno del contenuto.
     */
    public static float currentYPosition;
    /**
     * Stream di contenuto corrente per la pagina PDF.
     */
    public static PDPageContentStream currentContentStream;
    /**
     * Font per il testo in grassetto.
     */
    public static PDFont FONTBOLD;
    /**
     * Font per il testo in grassetto e corsivo.
     */
    public static PDFont FONTBOLDITALIC;
    /**
     * Font per il testo normale.
     */
    public static PDFont FONTREGULAR;
    /**
     * Font per il testo in corsivo.
     */
    public static PDFont FONTITALIC;
    /**
     * Numero di pagina corrente.
     */
    private static int pageNumber = 1;
    /**
     * Documento PDF corrente.
     */
    private static PDDocument document;
    /**
     * Font per il testo in grassetto di piccole dimensioni.
     */
    private static PDImageXObject logo;
    /**
     * Logo del centro.
     */
    private static PDImageXObject centerLogo;
    private final FileStorageService fileStorageService;
    private final MaterialeService materialeService;
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    private final AzioneChiaveService azioneChiaveService;
    private final PazienteT0Service pazienteT0Service;
    private final EsameRefertoService esameRefertoService;
    private final EsameFisicoService esameFisicoService;
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    private final AdvancedScenarioService advancedScenarioService;

    /**
     * Costruttore del servizio PdfExportService.
     *
     * @param scenarioService il servizio per la gestione degli scenari
     */
    public PdfExportService(ScenarioService scenarioService,
                            FileStorageService fileStorageService,
                            MaterialeService materialeService,
                            PatientSimulatedScenarioService patientSimulatedScenarioService,
                            AzioneChiaveService azioneChiaveService,
                            PazienteT0Service pazienteT0Service,
                            EsameRefertoService esameRefertoService,
                            EsameFisicoService esameFisicoService, AdvancedScenarioService advancedScenarioService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.materialeService = materialeService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        this.azioneChiaveService = azioneChiaveService;
        this.pazienteT0Service = pazienteT0Service;
        this.esameRefertoService = esameRefertoService;
        this.esameFisicoService = esameFisicoService;
        this.advancedScenarioService = advancedScenarioService;
    }

    /**
     * Inizializza una nuova pagina nel documento
     *
     * @throws IOException se si verifica un errore durante la creazione della pagina
     */
    public static void initNewPage() throws IOException {

        if (currentContentStream != null) {
            try {
                currentContentStream.close();
            } catch (Exception e) {
                logger.warn("Errore nella chiusura dello stream del contenuto corrente: {}", e.getMessage());
            }
        }

        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);

        currentContentStream = new PDPageContentStream(document, currentPage);

        if (pageNumber == 1) {

            float simLogoWidth = 40; // Ridotto da 60
            float simLogoHeight = 40; // Ridotto da 60

            float centerLogoMaxWidth = 120; // Aumentato (dimensione massima)
            float centerLogoMaxHeight = 80; // Aumentato (dimensione massima)

            float centerLogoWidth;
            float centerLogoHeight = 0;

            float simLogoY = PDRectangle.A4.getHeight() - MARGIN - simLogoHeight;

            if (logo != null) {
                currentContentStream.drawImage(logo, MARGIN, simLogoY, simLogoWidth, simLogoHeight);
            }

            if (centerLogo != null) {

                float scale = Math.min(
                        centerLogoMaxWidth / LogoLoader.centerLogoWidth,
                        centerLogoMaxHeight / LogoLoader.centerLogoHeight
                );

                centerLogoWidth = LogoLoader.centerLogoWidth * scale;
                centerLogoHeight = LogoLoader.centerLogoHeight * scale;

                float centerLogoX = (PDRectangle.A4.getWidth() - centerLogoWidth) / 2;
                float centerLogoY = PDRectangle.A4.getHeight() - MARGIN - centerLogoHeight;

                currentContentStream.drawImage(centerLogo, centerLogoX, centerLogoY, centerLogoWidth, centerLogoHeight);
            }

            float lowestY = simLogoY;
            if (centerLogo != null) {
                lowestY = Math.min(lowestY, PDRectangle.A4.getHeight() - MARGIN - centerLogoHeight);
            }
            currentYPosition = lowestY - LEADING;
        } else {
            currentYPosition = PDRectangle.A4.getHeight() - MARGIN;
        }
        pageNumber++;
    }

    /**
     * Verifica se è necessario creare una nuova pagina in base allo spazio rimanente
     *
     * @param neededSpace lo spazio necessario per il contenuto successivo
     * @throws IOException se si verifica un errore durante la creazione della pagina
     */
    public static void checkForNewPage(float neededSpace) throws IOException {
        if (currentYPosition - neededSpace < MARGIN) {
            initNewPage();
        }
    }


    /**
     * Esporta uno scenario in formato PDF.
     *
     * @param scenarioId l'ID dello scenario da esportare
     * @return un array di byte contenente il PDF generato
     * @throws IOException se si verifica un errore durante la creazione del PDF
     */
    public byte[] exportScenarioToPdf(int scenarioId,
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
        document = null;
        currentContentStream = null;
        pageNumber = 1;

        try {
            document = new PDDocument();

            FONTREGULAR = loadFont(document, "/fonts/LiberationSans-Regular.ttf");
            FONTBOLD = loadFont(document, "/fonts/LiberationSans-Bold.ttf");
            FONTITALIC = loadFont(document, "/fonts/LiberationSans-Italic.ttf");
            FONTBOLDITALIC = loadFont(document, "/fonts/LiberationSans-BoldItalic.ttf");

            logo = loadLogo(document);
            centerLogo = loadCenterLogo(document, fileStorageService);

            initNewPage();

            Scenario scenario = scenarioService.getScenarioById(scenarioId);
            logger.info("Recuperato scenario: {}", scenario.getTitolo());

            createScenarioHeader(scenario);
            createScenarioDescription(scenario, desc, brief, infoGen, patto, azioni, obiettivi, moula, liqui, matNec, scenarioService, materialeService, azioneChiaveService);
            createPatientSection(scenarioId, param, acces, fisic, pazienteT0Service, esameFisicoService);
            createExamsSection(scenarioId, esam, esameRefertoService);

            String scenarioType = scenarioService.getScenarioType(scenarioId);
            if (scenarioType != null && (scenarioType.equals("Advanced Scenario") ||
                    scenarioType.equals("Patient Simulated Scenario")) && time) {
                createTimelineSection(scenario, advancedScenarioService,scenarioService);
                logger.info("Timeline creata");
            }

            if (scenarioType != null && scenarioType.equals("Patient Simulated Scenario")) {
                createSceneggiaturaSection(scenario, scen, patientSimulatedScenarioService);
            }

            if (currentContentStream != null) {
                try {
                    currentContentStream.close();
                    currentContentStream = null;
                } catch (Exception e) {
                    logger.warn("Errore nella chiusura dello stream corrente: {}", e.getMessage());
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            logger.info("PDF salvato con successo");
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Errore nella generazione del PDF", e);
            throw new IOException("Generazione PDF fallita: " + e.getMessage(), e);
        } finally {

            if (currentContentStream != null) {
                try {
                    currentContentStream.close();
                } catch (Exception e) {
                    logger.warn("Errore nella chiusura dello stream: {}", e.getMessage());
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    logger.warn("Errore nella chiusura del documento: {}", e.getMessage());
                }
            }
        }
    }
}