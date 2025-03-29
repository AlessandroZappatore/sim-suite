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

@Service
public class PdfExportService {

    // Layout constants
    private static final float MARGIN = 50;
    private static final float LEADING = 15;
    private static final float TITLE_FONT_SIZE = 16;
    private static final float HEADER_FONT_SIZE = 14;
    private static final float BODY_FONT_SIZE = 11;
    private static final float SMALL_FONT_SIZE = 9;

    private final ScenarioService scenarioService;

    public PdfExportService(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    public byte[] exportScenarioToPdf(int scenarioId) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // Load fonts
            PDFont fontRegular = loadFont(document, "/fonts/LiberationSans-Regular.ttf");
            PDFont fontBold = loadFont(document, "/fonts/LiberationSans-Bold.ttf");

            // Get complete scenario data
            Scenario scenario = scenarioService.getScenarioById(scenarioId);
            //AdvancedScenario advancedScenario = scenarioService.getAdvancedScenarioById(scenarioId);

            // Create cover page with scenario details
            createCoverPage(document, scenario, fontBold, fontRegular);

            // Create patient status page
            createPatientPage(document, scenarioId, fontBold, fontRegular);

            // Create exams page if any
            createExamsPage(document, scenarioId, fontBold, fontRegular);

            // Create timeline pages if any
            //if (advancedScenario != null) {
            //    createTimelinePages(document, advancedScenario, fontBold, fontRegular);
            //}

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private PDFont loadFont(PDDocument document, String fontPath) throws IOException {
        try (InputStream fontStream = getClass().getResourceAsStream(fontPath)) {
            if (fontStream == null) {
                throw new IOException("Font file not found: " + fontPath);
            }
            return PDType0Font.load(document, fontStream);
        }
    }

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
                yPosition = drawSection(contentStream, "Briefing",
                        scenario.getBriefing(),
                        fontBold, fontRegular, yPosition);
            }
        }
    }

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

    private void createTimelinePages(PDDocument document, AdvancedScenario scenario,
                                     PDFont fontBold, PDFont fontRegular) throws IOException {
        List<Tempo> tempi = scenario.getTempi();
        if (tempi == null || tempi.isEmpty()) {
            return;
        }

        for (Tempo tempo : tempi) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = PDRectangle.A4.getHeight() - MARGIN;

                // Time title
                String title = String.format("Timeline - Tempo %d: %s (%.1f min)",
                        tempo.getIdTempo(), tempo.getAzione(), tempo.getTimerTempo());
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
                    yPosition = drawWrappedText(contentStream, fontRegular, BODY_FONT_SIZE,
                            MARGIN + 20, yPosition - LEADING,
                            tempo.getAltriDettagli());
                }
            }
        }
    }

    // Helper methods for text drawing
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
                String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
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
                    if (currentLine.length() > 0) currentLine.append(" ");
                    currentLine.append(word);
                }
            }

            if (currentLine.length() > 0) {
                contentStream.showText(currentLine.toString());
            }
            contentStream.endText();
            currentY -= LEADING;
        }

        return currentY;
    }

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