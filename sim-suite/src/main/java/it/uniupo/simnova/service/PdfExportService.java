package it.uniupo.simnova.service;

import it.uniupo.simnova.api.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * Font size for the title.
     */
    private final ScenarioService scenarioService;

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
        try (PDDocument document = new PDDocument()) {
            // Load fonts
            PDFont fontRegular = loadFont(document, "/fonts/LiberationSans-Regular.ttf");
            PDFont fontBold = loadFont(document, "/fonts/LiberationSans-Bold.ttf");

            // Get complete scenario data
            Scenario scenario = scenarioService.getScenarioById(scenarioId);

            // Create cover page with scenario details
            createCoverPage(document, scenario, fontBold, fontRegular);

            // Create patient status page
            createPatientPage(document, scenarioId, fontBold, fontRegular);

            // Create exams page if any
            createExamsPage(document, scenarioId, fontBold, fontRegular);

            // Create timeline pages if any
            String scenarioType = scenarioService.getScenarioType(scenarioId);
            if (scenarioType != null && scenarioType.equals("Advanced Scenario") || Objects.equals(scenarioType, "Patient Simulated Scenario")) {
                createTimelinePages(document, scenario, fontBold, fontRegular);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
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
     * Crea la pagina di copertura del PDF con i dettagli dello scenario.
     *
     * @param document    il documento PDF
     * @param scenario    lo scenario da esportare
     * @param fontBold    il font in grassetto
     * @param fontRegular il font normale
     * @throws IOException se si verifica un errore durante la creazione della pagina
     */
    private void createCoverPage(PDDocument document, Scenario scenario,
                                 PDFont fontBold, PDFont fontRegular) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float yPosition = PDRectangle.A4.getHeight() - MARGIN;

            // Title
            drawCenteredText(contentStream, fontBold, TITLE_FONT_SIZE,
                    "Dettaglio Scenario", yPosition);
            yPosition -= LEADING * 3;

            // Scenario Title
            drawCenteredText(contentStream, fontBold, HEADER_FONT_SIZE,
                    scenario.getTitolo(), yPosition);
            yPosition -= LEADING * 2;

            // Basic Info
            contentStream.setFont(fontRegular, BODY_FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("ID Scenario: " + scenario.getId());
            contentStream.newLineAtOffset(0, -LEADING);
            contentStream.showText("Paziente: " + scenario.getNomePaziente());
            contentStream.newLineAtOffset(0, -LEADING);
            contentStream.showText("Patologia: " + scenario.getPatologia());
            contentStream.newLineAtOffset(0, -LEADING);
            contentStream.showText("Durata: " + scenario.getTimerGenerale() + " minuti");
            contentStream.endText();
            yPosition -= LEADING * 9;

            // Description
            if (scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty()) {
                yPosition = drawSection(contentStream, "Descrizione",
                        scenario.getDescrizione(),
                        fontBold, fontRegular, yPosition);
            }

            // Briefing
            if (scenario.getBriefing() != null && !scenario.getBriefing().isEmpty()) {
                drawSection(contentStream, "Briefing",
                        scenario.getBriefing(),
                        fontBold, fontRegular, yPosition);
            }
        }
    }

    /**
     * Crea la pagina dello stato del paziente nel PDF.
     *
     * @param document    il documento PDF
     * @param scenarioId  l'ID dello scenario
     * @param fontBold    il font in grassetto
     * @param fontRegular il font normale
     * @throws IOException se si verifica un errore durante la creazione della pagina
     */
    private void createPatientPage(PDDocument document, Integer scenarioId,
                                   PDFont fontBold, PDFont fontRegular) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float yPosition = PDRectangle.A4.getHeight() - MARGIN;

            // Page title
            drawCenteredText(contentStream, fontBold, TITLE_FONT_SIZE,
                    "Stato Paziente", yPosition);
            yPosition -= LEADING * 3;

            PazienteT0 paziente = scenarioService.getPazienteT0ById(scenarioId);
            if (paziente != null) {
                // Vital parameters
                yPosition = drawSection(contentStream, "Parametri Vitali", "",
                        fontBold, fontRegular, yPosition);

                String vitalParams = String.format(
                        "PA: %s mmHg\nFC: %d bpm\nRR: %d atti/min\nTemperatura: %.1f °C\nSpO2: %d%%\nEtCO2: %d mmHg\nMonitor: %s",
                        paziente.getPA(), paziente.getFC(), paziente.getRR(),
                        paziente.getT(), paziente.getSpO2(), paziente.getEtCO2(),
                        paziente.getMonitor()
                );

                yPosition = drawWrappedText(contentStream, fontRegular, BODY_FONT_SIZE,
                        MARGIN + 20, yPosition - LEADING,
                        vitalParams);

                // Vascular access
                if (!paziente.getAccessiVenosi().isEmpty()) {
                    yPosition = drawSection(contentStream, "Accessi Venosi", "",
                            fontBold, fontRegular, yPosition);
                    for (Accesso accesso : paziente.getAccessiVenosi()) {
                        yPosition = drawWrappedText(contentStream, fontRegular, BODY_FONT_SIZE,
                                MARGIN + 20, yPosition - LEADING,
                                "• " + accesso.getTipologia() + " - " + accesso.getPosizione());
                    }
                }

                if (!paziente.getAccessiArteriosi().isEmpty()) {
                    yPosition = drawSection(contentStream, "Accessi Arteriosi", "",
                            fontBold, fontRegular, yPosition);
                    for (Accesso accesso : paziente.getAccessiArteriosi()) {
                        yPosition = drawWrappedText(contentStream, fontRegular, BODY_FONT_SIZE,
                                MARGIN + 20, yPosition - LEADING,
                                "• " + accesso.getTipologia() + " - " + accesso.getPosizione());
                    }
                }
            }

            // Physical exam
            EsameFisico esame = scenarioService.getEsameFisicoById(scenarioId);
            if (esame != null && !esame.getSections().isEmpty()) {
                yPosition = drawSection(contentStream, "Esame Fisico", "",
                        fontBold, fontRegular, yPosition);

                Map<String, String> sections = esame.getSections();
                for (Map.Entry<String, String> entry : sections.entrySet()) {
                    if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                        yPosition = drawWrappedText(contentStream, fontBold, BODY_FONT_SIZE,
                                MARGIN + 20, yPosition - LEADING,
                                entry.getKey() + ":");

                        yPosition = drawWrappedText(contentStream, fontRegular, BODY_FONT_SIZE,
                                MARGIN + 30, yPosition - LEADING,
                                entry.getValue());
                    }
                }
            }
        }
    }

    /**
     * Crea la pagina degli esami e referti nel PDF.
     *
     * @param document    il documento PDF
     * @param scenarioId  l'ID dello scenario
     * @param fontBold    il font in grassetto
     * @param fontRegular il font normale
     * @throws IOException se si verifica un errore durante la creazione della pagina
     */
    private void createExamsPage(PDDocument document, Integer scenarioId,
                                 PDFont fontBold, PDFont fontRegular) throws IOException {
        List<EsameReferto> esami = scenarioService.getEsamiRefertiByScenarioId(scenarioId);
        if (esami == null || esami.isEmpty()) {
            return;
        }

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float yPosition = PDRectangle.A4.getHeight() - MARGIN;

            // Page title
            drawCenteredText(contentStream, fontBold, TITLE_FONT_SIZE,
                    "Esami e Referti", yPosition);
            yPosition -= LEADING * 3;

            for (EsameReferto esame : esami) {
                yPosition = drawSection(contentStream, esame.getTipo(), "",
                        fontBold, fontRegular, yPosition);

                if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                    yPosition = drawWrappedText(contentStream, fontRegular, BODY_FONT_SIZE,
                            MARGIN + 20, yPosition - LEADING,
                            esame.getRefertoTestuale());
                }

                if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                    yPosition = drawWrappedText(contentStream, fontRegular, SMALL_FONT_SIZE,
                            MARGIN + 20, yPosition - LEADING,
                            "Allegato: " + esame.getMedia());
                }

                yPosition -= LEADING;
            }
        }
    }

    /**
     * Crea le pagine della timeline nel PDF.
     *
     * @param document    il documento PDF
     * @param scenario    lo scenario avanzato
     * @param fontBold    il font in grassetto
     * @param fontRegular il font normale
     * @throws IOException se si verifica un errore durante la creazione delle pagine
     */
    private void createTimelinePages(PDDocument document, Scenario scenario,
                                     PDFont fontBold, PDFont fontRegular) throws IOException {
        List<Tempo> tempi = ScenarioService.getTempiByScenarioId(scenario.getId());
        if (tempi.isEmpty()) {
            return;
        }

        for (Tempo tempo : tempi) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = PDRectangle.A4.getHeight() - MARGIN;

                // Time title
                String title = String.format("Timeline - Tempo %d: %s (%.1f min)",
                    tempo.getIdTempo(),
                    tempo.getAzione(),
                    tempo.getTimerTempo() / 60.0
                );
                drawCenteredText(contentStream, fontBold, TITLE_FONT_SIZE, title, yPosition);
                yPosition -= LEADING * 3;

                // Vital parameters
                String params = String.format(
                        "PA: %s mmHg\nFC: %d bpm\nRR: %d atti/min\nTemperatura: %.1f °C\nSpO2: %d%%\nEtCO2: %d mmHg",
                        tempo.getPA(), tempo.getFC(), tempo.getRR(),
                        tempo.getT(), tempo.getSpO2(), tempo.getEtCO2()
                );

                yPosition = drawWrappedText(contentStream, fontRegular, BODY_FONT_SIZE,
                        MARGIN, yPosition - LEADING, params);

                // Additional details
                if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                    yPosition = drawSection(contentStream, "Dettagli", "",
                            fontBold, fontRegular, yPosition);
                    drawWrappedText(contentStream, fontRegular, BODY_FONT_SIZE,
                            MARGIN + 20, yPosition - LEADING,
                            tempo.getAltriDettagli());
                }
            }
        }
    }

    /**
     * Disegna una sezione con titolo e contenuto nel PDF.
     *
     * @param contentStream il flusso di contenuto della pagina
     * @param title         il titolo della sezione
     * @param content       il contenuto della sezione
     * @param fontBold      il font in grassetto
     * @param fontRegular   il font normale
     * @param yPosition     la posizione Y corrente
     * @return la nuova posizione Y dopo aver disegnato la sezione
     * @throws IOException se si verifica un errore durante il disegno della sezione
     */
    private float drawSection(PDPageContentStream contentStream, String title, String content,
                              PDFont fontBold, PDFont fontRegular,
                              float yPosition) throws IOException {
        contentStream.setFont(fontBold, HEADER_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();

        if (content != null && !content.isEmpty()) {
            return drawWrappedText(contentStream, fontRegular, BODY_FONT_SIZE,
                    MARGIN + 10, yPosition - LEADING, content);
        }
        return yPosition - LEADING;
    }

    /**
     * Disegna del testo avvolto in una posizione specificata.
     *
     * @param contentStream il flusso di contenuto della pagina
     * @param font          il font da utilizzare
     * @param fontSize      la dimensione del font
     * @param x             la posizione X
     * @param y             la posizione Y
     * @param text          il testo da disegnare
     * @return la nuova posizione Y dopo aver disegnato il testo
     * @throws IOException se si verifica un errore durante il disegno del testo
     */
    private float drawWrappedText(PDPageContentStream contentStream, PDFont font,
                                  float fontSize, float x, float y, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return y;
        }

        contentStream.setFont(font, fontSize);
        String[] lines = text.split("\n");
        float currentY = y;

        for (String line : lines) {
            String[] words = line.split(" ");
            StringBuilder currentLine = new StringBuilder();

            contentStream.beginText();
            contentStream.newLineAtOffset(x, currentY);

            for (String word : words) {
                String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
                float width = font.getStringWidth(testLine) / 1000 * fontSize;

                if (width > (PDRectangle.A4.getWidth() - 2 * MARGIN - 10)) {
                    contentStream.showText(currentLine.toString());
                    contentStream.endText();

                    currentY -= LEADING;
                    if (currentY < MARGIN) {
                        return currentY; // Stop if we reach the bottom margin
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(x, currentY);
                    currentLine = new StringBuilder(word);
                } else {
                    if (!currentLine.isEmpty()) currentLine.append(" ");
                    currentLine.append(word);
                }
            }

            if (!currentLine.isEmpty()) {
                contentStream.showText(currentLine.toString());
            }
            contentStream.endText();
            currentY -= LEADING;
        }

        return currentY;
    }

    /**
     * Disegna del testo centrato in una posizione specificata.
     *
     * @param contentStream il flusso di contenuto della pagina
     * @param font          il font da utilizzare
     * @param fontSize      la dimensione del font
     * @param text          il testo da disegnare
     * @param y             la posizione Y
     * @throws IOException se si verifica un errore durante il disegno del testo
     */
    private void drawCenteredText(PDPageContentStream contentStream, PDFont font,
                                  float fontSize, String text, float y) throws IOException {
        float titleWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (PDRectangle.A4.getWidth() - titleWidth) / 2;

        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }
}