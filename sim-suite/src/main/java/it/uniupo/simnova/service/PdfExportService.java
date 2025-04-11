package it.uniupo.simnova.service;

import it.uniupo.simnova.api.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
     * Margini e spaziatura per il layout del PDF.
     */
    private static final float MARGIN = 50;
    /**
     * Spaziatura tra le righe.
     */
    private static final float LEADING = 15;
    /**
     * Dimensioni dei font per i vari elementi del PDF.
     */
    private static final float TITLE_FONT_SIZE = 16;
    /**
     * Font size for the title.
     */
    private static final float HEADER_FONT_SIZE = 14;
    /**
     * Font size for the body text.
     */
    private static final float BODY_FONT_SIZE = 11;
    /**
     * Font size for the small text.
     */
    private static final float SMALL_FONT_SIZE = 9;
    /**
     * Dimensione del logo.
     */
    private static final float LOGO_WIDTH = 60;
    private static final float LOGO_HEIGHT = 60;

    private int pageNumber = 1;
    private static final Logger log = LoggerFactory.getLogger(PdfExportService.class);
    /**
     * Font size for the title.
     */
    private final ScenarioService scenarioService;

    // Aggiungi una variabile per tracciare la posizione Y corrente e la pagina attuale
    private float currentYPosition;
    private PDPageContentStream currentContentStream;
    private PDDocument document;
    private PDFont fontBold;
    private PDFont fontRegular;
    private PDImageXObject logo;

    /**
     * Costruttore del servizio PdfExportService.
     *
     * @param scenarioService il servizio per la gestione degli scenari
     */
    public PdfExportService(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    /**
     * Esporta uno scenario in formato PDF.
     *
     * @param scenarioId l'ID dello scenario da esportare
     * @return un array di byte contenente il PDF generato
     * @throws IOException se si verifica un errore durante la creazione del PDF
     */
    public byte[] exportScenarioToPdf(int scenarioId) throws IOException {
        document = null;
        currentContentStream = null;
        pageNumber = 1;

        try {
            document = new PDDocument();

            // Load fonts
            fontRegular = loadFont(document, "/fonts/LiberationSans-Regular.ttf");
            fontBold = loadFont(document, "/fonts/LiberationSans-Bold.ttf");

            // Load logo
            logo = loadLogo(document);

            // Initialize the first page
            initNewPage();

            // Get complete scenario data
            Scenario scenario = scenarioService.getScenarioById(scenarioId);
            log.info("Exporting scenario: {}", scenario.getTitolo());

            // Create document in sequential format
            createScenarioHeader(scenario);
            createPatientSection(scenarioId);
            createExamsSection(scenarioId);

            // Create timeline section if any
            String scenarioType = scenarioService.getScenarioType(scenarioId);
            if (scenarioType != null && (scenarioType.equals("Advanced Scenario") ||
                    scenarioType.equals("Patient Simulated Scenario"))) {
                createTimelineSection(scenario);
                log.info("Timeline section created");
            }

            if (scenarioType != null && scenarioType.equals("Patient Simulated Scenario")) {
                createSceneggiaturaSection(scenario);
            }

            // Safely close the last content stream
            if (currentContentStream != null) {
                try {
                    currentContentStream.close();
                    currentContentStream = null;
                } catch (Exception e) {
                    log.warn("Error closing final content stream: {}", e.getMessage());
                }
            }

            // Save document to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            log.info("PDF document saved");
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        } finally {
            // Make sure we close everything in finally block
            if (currentContentStream != null) {
                try {
                    currentContentStream.close();
                } catch (Exception e) {
                    log.warn("Error closing content stream in finally block: {}", e.getMessage());
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    log.warn("Error closing document in finally block: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Carica il logo da un file di risorse.
     *
     * @param document il documento PDF in cui caricare il logo
     * @return l'oggetto immagine del logo
     * @throws IOException se si verifica un errore durante il caricamento del logo
     */
    private PDImageXObject loadLogo(PDDocument document) throws IOException {
        try (InputStream logoStream = getClass().getResourceAsStream("/META-INF/resources/icons/LogoSimsuite.png")) {
            if (logoStream == null) {
                log.warn("Logo file not found: /META-INF/resources/icons/LogoSimsuite.png");
                return null;
            }

            // Converti l'InputStream in un array di byte
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = logoStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            // Crea l'immagine dall'array di byte
            return PDImageXObject.createFromByteArray(document, buffer.toByteArray(), "LogoSimsuite.png");
        }
    }

    /**
     * Inizializza una nuova pagina nel documento
     *
     * @throws IOException se si verifica un errore durante la creazione della pagina
     */
    private void initNewPage() throws IOException {
        // Safely close the content stream if it exists
        if (currentContentStream != null) {
            try {
                currentContentStream.close();
            } catch (Exception e) {
                log.warn("Error closing content stream: {}", e.getMessage());
                // Don't rethrow - we still want to create the new page
            }
        }

        // Create a new page
        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);

        // Create a new content stream
        currentContentStream = new PDPageContentStream(document, currentPage);

        // Add the logo in the first page only
        if (logo != null && pageNumber == 1) {
            // Position the logo at the top center
            float logoX = (PDRectangle.A4.getWidth() - LOGO_WIDTH) / 2;
            float logoY = PDRectangle.A4.getHeight() - MARGIN - LOGO_HEIGHT;
            currentContentStream.drawImage(logo, logoX, logoY, LOGO_WIDTH, LOGO_HEIGHT);

            // Update the Y position considering the space occupied by the logo
            currentYPosition = logoY - LEADING;
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
    private void checkForNewPage(float neededSpace) throws IOException {
        if (currentYPosition - neededSpace < MARGIN) {
            initNewPage();
        }
    }

    /**
     * Carica un font da un file di risorse.
     *
     * @param document il documento PDF in cui caricare il font
     * @param fontPath il percorso del file del font
     * @return il font caricato
     * @throws IOException se si verifica un errore durante il caricamento del font
     */
    private PDFont loadFont(PDDocument document, String fontPath) throws IOException {
        try (InputStream fontStream = getClass().getResourceAsStream(fontPath)) {
            if (fontStream == null) {
                throw new IOException("Font file not found: " + fontPath);
            }
            return PDType0Font.load(document, fontStream);
        }
    }

    /**
     * Crea l'intestazione dello scenario nel PDF.
     *
     * @param scenario lo scenario da esportare
     * @throws IOException se si verifica un errore durante la creazione dell'intestazione
     */
    private void createScenarioHeader(Scenario scenario) throws IOException {
        // Title
        drawCenteredText(fontBold, TITLE_FONT_SIZE, "Dettaglio Scenario");
        currentYPosition -= LEADING * 2;

        // Scenario Title
        drawCenteredText(fontBold, HEADER_FONT_SIZE, scenario.getTitolo());
        currentYPosition -= LEADING * 2;

        // Basic Info
        currentContentStream.setFont(fontRegular, BODY_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN, currentYPosition);
        currentContentStream.showText("ID Scenario: " + scenario.getId());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Paziente: " + scenario.getNomePaziente());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Patologia: " + scenario.getPatologia());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Durata: " + scenario.getTimerGenerale() + " minuti");
        currentContentStream.endText();
        currentYPosition -= LEADING * 5;

        // Description
        if (scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty()) {
            drawSection("Descrizione", scenario.getDescrizione());
        }

        // Briefing
        if (scenario.getBriefing() != null && !scenario.getBriefing().isEmpty()) {
            drawSection("Briefing", scenario.getBriefing());
        }

        //Patto d'aula
        if (scenario.getPattoAula() != null && !scenario.getPattoAula().isEmpty()) {
            drawSection("Patto d'Aula", scenario.getPattoAula());
        }

        //Azioni chiave
        if (scenario.getAzioneChiave() != null && !scenario.getAzioneChiave().isEmpty()) {
            drawSection("Azioni Chiave", scenario.getAzioneChiave());
        }

        //Obiettivi didattici
        if (scenario.getObiettivo() != null && !scenario.getObiettivo().isEmpty()) {
            drawSection("Obiettivi Didattici", scenario.getObiettivo());
        }

        //Materiale necessario
        if (scenario.getMateriale() != null && !scenario.getMateriale().isEmpty()) {
            drawSection("Materiale Necessario", scenario.getMateriale());
        }

        //Moulage
        if (scenario.getMoulage() != null && !scenario.getMoulage().isEmpty()) {
            drawSection("Moulage", scenario.getMoulage());
        }

        //Liquidi
        if (scenario.getLiquidi() != null && !scenario.getLiquidi().isEmpty()) {
            drawSection("Liquidi", scenario.getLiquidi());
        }

        log.info("Scenario header created");
    }

    /**
     * Crea la sezione dello stato del paziente nel PDF.
     *
     * @param scenarioId l'ID dello scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createPatientSection(Integer scenarioId) throws IOException {
        // Verifica spazio disponibile e crea pagina se necessario
        checkForNewPage(LEADING * 5);

        // Section title
        drawSection("Stato Paziente", "");

        PazienteT0 paziente = scenarioService.getPazienteT0ById(scenarioId);
        if (paziente != null) {
            // Vital parameters
            checkForNewPage(LEADING * 10); // Spazio per i parametri vitali
            drawSubsection("Parametri Vitali");

            String vitalParams = String.format(
                    "PA: %s mmHg\nFC: %d bpm\nRR: %d atti/min\nTemperatura: %.1f °C\nSpO2: %d%%\nEtCO2: %d mmHg\nMonitor: %s",
                    paziente.getPA(), paziente.getFC(), paziente.getRR(),
                    paziente.getT(), paziente.getSpO2(), paziente.getEtCO2(),
                    paziente.getMonitor()
            );

            drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, vitalParams);

            // Vascular access
            if (!paziente.getAccessiVenosi().isEmpty()) {
                checkForNewPage(LEADING * 3 + LEADING * paziente.getAccessiVenosi().size());
                drawSubsection("Accessi Venosi");
                for (Accesso accesso : paziente.getAccessiVenosi()) {
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20,
                            "• " + accesso.getTipologia() + " - " + accesso.getPosizione());
                }
            }

            if (!paziente.getAccessiArteriosi().isEmpty()) {
                checkForNewPage(LEADING * 3 + LEADING * paziente.getAccessiArteriosi().size());
                drawSubsection("Accessi Arteriosi");
                for (Accesso accesso : paziente.getAccessiArteriosi()) {
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20,
                            "• " + accesso.getTipologia() + " - " + accesso.getPosizione());
                }
            }
        }

        // Physical exam
        EsameFisico esame = scenarioService.getEsameFisicoById(scenarioId);
        if (esame != null && !esame.getSections().isEmpty()) {
            checkForNewPage(LEADING * 5);
            drawSubsection("Esame Fisico");

            Map<String, String> sections = esame.getSections();
            for (Map.Entry<String, String> entry : sections.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                    checkForNewPage(LEADING * 3);
                    drawWrappedText(fontBold, BODY_FONT_SIZE, MARGIN + 20, entry.getKey() + ":");
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 30, entry.getValue());
                }
            }
        }

        log.info("Patient section created");
    }

    /**
     * Crea la sezione degli esami e referti nel PDF.
     *
     * @param scenarioId l'ID dello scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createExamsSection(Integer scenarioId) throws IOException {
        List<EsameReferto> esami = scenarioService.getEsamiRefertiByScenarioId(scenarioId);
        if (esami == null || esami.isEmpty()) {
            return;
        }

        // Verifica spazio disponibile e crea pagina se necessario
        checkForNewPage(LEADING * 5);

        // Section title
        drawSection("Esami e Referti", "");

        for (EsameReferto esame : esami) {
            String examType = getExamType(esame);
            checkForNewPage(LEADING * 3);
            drawSubsection(examType);

            if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, esame.getRefertoTestuale());
            }

            if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                drawWrappedText(fontRegular, SMALL_FONT_SIZE, MARGIN + 20, "Allegato: " + esame.getMedia());
            }

            currentYPosition -= LEADING;
        }

        log.info("Exams section created");
    }

    private static String getExamType(EsameReferto esame) {
        String examType = esame.getTipo();
        if (examType != null) {
            examType = examType.replace('₂', '2');
            examType = examType.replace('₃', '3');
            examType = examType.replace('₄', '4');
            examType = examType.replace('₅', '5');
            examType = examType.replace('₆', '6');
            examType = examType.replace('₇', '7');
            examType = examType.replace('₈', '8');
            examType = examType.replace('⁻', '-');
            examType = examType.replace('⁺', '+');
            examType = examType.replace('¹', '1');
            examType = examType.replace('²', '2');
            examType = examType.replace('³', '3');
            examType = examType.replace('⁴', '4');
            examType = examType.replace('⁵', '5');
            examType = examType.replace('⁶', '6');
            examType = examType.replace('⁷', '7');
            examType = examType.replace('⁸', '8');
            examType = examType.replace('⁹', '9');
        } else examType = "";
        return examType;
    }

    /**
     * Crea la sezione timeline nel PDF.
     *
     * @param scenario lo scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createTimelineSection(Scenario scenario) throws IOException {
        List<Tempo> tempi = ScenarioService.getTempiByScenarioId(scenario.getId());
        if (tempi.isEmpty()) {
            return;
        }

        // Verifica spazio disponibile e crea pagina se necessario
        checkForNewPage(LEADING * 5);

        // Section title
        drawSection("Timeline", "");

        for (Tempo tempo : tempi) {
            // Verifica spazio disponibile per ogni tempo
            checkForNewPage(LEADING * 10); // Spazio minimo per un tempo

            // Time title - Tronca l'azione se è troppo lunga
            String azione = tempo.getAzione();
            if (azione != null && azione.length() > 50) {
                azione = azione.substring(0, 48) + "...";
            }

            String title = String.format("Tempo %d: %s (%.1f min)",
                    tempo.getIdTempo(),
                    azione,
                    tempo.getTimerTempo() / 3600.0
            );

            drawSubsection(title);

            // Vital parameters
            StringBuilder params = new StringBuilder(String.format(
                    "PA: %s mmHg\nFC: %d bpm\nRR: %d atti/min\nTemperatura: %.1f °C\nSpO2: %d%%\nEtCO2: %d mmHg",
                    tempo.getPA(), tempo.getFC(), tempo.getRR(),
                    tempo.getT(), tempo.getSpO2(), tempo.getEtCO2()
            ));

            List<ParametroAggiuntivo> parametriAggiuntivo = ScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenario.getId());

            if (!parametriAggiuntivo.isEmpty()) {
                for (ParametroAggiuntivo parametro : parametriAggiuntivo) {
                    params.append(String.format("\n%s: %s %s", parametro.getNome(), parametro.getValore(), parametro.getUnitaMisura()));
                }
            }

            drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, params.toString());

            // Additional details
            if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(fontBold, BODY_FONT_SIZE, MARGIN + 20, "Dettagli:");
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 30, tempo.getAltriDettagli());
            }

            if (tempo.getTSi() >= 0) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(fontBold, BODY_FONT_SIZE, MARGIN + 20, "TSI:");
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 30, String.valueOf(tempo.getTSi()));
            }

            if (tempo.getTNo() >= 0) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(fontBold, BODY_FONT_SIZE, MARGIN + 20, "TNO:");
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 30, String.valueOf(tempo.getTNo()));
            }

            // Aggiungi spazio tra gli elementi della timeline
            currentYPosition -= LEADING * 2;
        }
    }

    /**
     * Crea la sezione sceneggiatura nel PDF.
     *
     * @param scenario lo scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createSceneggiaturaSection(Scenario scenario) throws IOException {
        String sceneggiatura = scenarioService.getSceneggiatura(scenario.getId());
        if (sceneggiatura == null || sceneggiatura.isEmpty()) {
            return;
        }

        // Verifica spazio disponibile e crea pagina se necessario
        checkForNewPage(LEADING * 5);

        // Section title
        drawSection("Sceneggiatura", "");
        drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, sceneggiatura);

        log.info("Sceneggiatura section created");
    }

    /**
     * Disegna una sezione principale con titolo nel PDF.
     *
     * @param title   il titolo della sezione
     * @param content il contenuto della sezione
     * @throws IOException se si verifica un errore durante il disegno della sezione
     */
    private void drawSection(String title, String content) throws IOException {
        currentContentStream.setFont(fontBold, HEADER_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN, currentYPosition);
        currentContentStream.showText(title);
        currentContentStream.endText();
        currentYPosition -= LEADING * 1.5f;

        if (content != null && !content.isEmpty()) {
            drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 10, content);
            currentYPosition -= LEADING;
        }
    }

    /**
     * Disegna una sottosezione con titolo nel PDF.
     *
     * @param title il titolo della sottosezione
     * @throws IOException se si verifica un errore durante il disegno della sottosezione
     */
    private void drawSubsection(String title) throws IOException {
        currentContentStream.setFont(fontBold, BODY_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN + 10, currentYPosition);
        currentContentStream.showText(title);
        currentContentStream.endText();
        currentYPosition -= LEADING;

        drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, "");

    }

    /**
     * Disegna del testo avvolto in una posizione specificata.
     *
     * @param font     il font da utilizzare
     * @param fontSize la dimensione del font
     * @param x        la posizione X
     * @param text     il testo da disegnare
     * @throws IOException se si verifica un errore durante il disegno del testo
     */
    private void drawWrappedText(PDFont font, float fontSize, float x, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }

        String[] lines = text.split("\n");

        for (String line : lines) {
            // Controlla se abbiamo spazio per una nuova riga
            checkForNewPage(LEADING);

            // Set the font for the current content stream
            currentContentStream.setFont(font, fontSize);

            String[] words = line.split(" ");
            StringBuilder currentLine = new StringBuilder();

            currentContentStream.beginText();
            currentContentStream.newLineAtOffset(x, currentYPosition);

            for (String word : words) {
                String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
                float width = font.getStringWidth(testLine) / 1000 * fontSize;

                if (width > (PDRectangle.A4.getWidth() - 2 * MARGIN - 10)) {
                    currentContentStream.showText(currentLine.toString());
                    currentContentStream.endText();

                    currentYPosition -= LEADING;
                    // Controlla se abbiamo spazio per continuare sulla stessa pagina
                    checkForNewPage(LEADING);

                    // Important: Set the font again after potentially creating a new page
                    currentContentStream.setFont(font, fontSize);

                    currentContentStream.beginText();
                    currentContentStream.newLineAtOffset(x, currentYPosition);
                    currentLine = new StringBuilder(word);
                } else {
                    if (!currentLine.isEmpty()) currentLine.append(" ");
                    currentLine.append(word);
                }
            }

            if (!currentLine.isEmpty()) {
                currentContentStream.showText(currentLine.toString());
            }
            currentContentStream.endText();
            currentYPosition -= LEADING;
        }
    }

    /**
     * Disegna del testo centrato in una posizione specificata.
     *
     * @param font     il font da utilizzare
     * @param fontSize la dimensione del font
     * @param text     il testo da disegnare
     * @throws IOException se si verifica un errore durante il disegno del testo
     */
    private void drawCenteredText(PDFont font, float fontSize, String text) throws IOException {
        float titleWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (PDRectangle.A4.getWidth() - titleWidth) / 2;

        currentContentStream.setFont(font, fontSize);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(x, currentYPosition);
        currentContentStream.showText(text);
        currentContentStream.endText();
        currentYPosition -= LEADING;
    }
}