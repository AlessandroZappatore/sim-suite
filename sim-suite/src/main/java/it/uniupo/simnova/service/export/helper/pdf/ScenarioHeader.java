package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.PdfExportService;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static it.uniupo.simnova.service.export.PdfExportService.FONTBOLD;
import static it.uniupo.simnova.service.export.PdfExportService.FONTREGULAR;

public class ScenarioHeader {
    private static final float TITLE_FONT_SIZE = 16;
    private static final float HEADER_FONT_SIZE = 14;
    private static final float LEADING = 14;
    private static final float BODY_FONT_SIZE = 11;
    private static final float MARGIN = 40;
    private static final Logger logger = LoggerFactory.getLogger(ScenarioHeader.class);


    public static void createScenarioHeader(Scenario scenario) throws IOException {

        if (PdfExportService.currentContentStream == null) {
            logger.error("PDPageContentStream is null before creating ScenarioHeader.");
            throw new IOException("PDF content stream not initialized.");
        }

        drawCenteredWrappedText(FONTBOLD, TITLE_FONT_SIZE, "Dettaglio Scenario");
        PdfExportService.currentYPosition -= LEADING * 2; // Space after title

        drawCenteredWrappedText(FONTBOLD, HEADER_FONT_SIZE, scenario.getTitolo());
        PdfExportService.currentYPosition -= LEADING * 2; // Space after scenario title

        drawTextWithWrapping(FONTREGULAR, "Autori: ", scenario.getAutori());
        drawTextWithWrapping(FONTREGULAR, "Target: ", scenario.getTarget());
        drawTextWithWrapping(FONTREGULAR, "Tipologia: ", scenario.getTipologia());
        drawTextWithWrapping(FONTREGULAR, "Paziente: ", scenario.getNomePaziente());
        drawTextWithWrapping(FONTREGULAR, "Patologia: ", scenario.getPatologia() != null && !scenario.getPatologia().isEmpty() ? scenario.getPatologia() : "-");
        drawTextWithWrapping(FONTREGULAR, "Durata: ", scenario.getTimerGenerale() > 0 ? scenario.getTimerGenerale() + " minuti" : "-");

        PdfExportService.currentYPosition -= LEADING;

        logger.info("Header creato");
    }

    private static void drawCenteredWrappedText(PDFont font, float fontSize, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }

        float pageWidth = PDRectangle.A4.getWidth();
        float maxWidth = pageWidth - 2 * MARGIN;
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float testWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (testWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        for (String line : lines) {
            checkForNewPage();
            float lineWidth = font.getStringWidth(line) / 1000 * fontSize;
            float xPosition = (pageWidth - lineWidth) / 2;

            PdfExportService.currentContentStream.setFont(font, fontSize);
            PdfExportService.currentContentStream.beginText();
            PdfExportService.currentContentStream.newLineAtOffset(xPosition, PdfExportService.currentYPosition);
            PdfExportService.currentContentStream.showText(line);
            PdfExportService.currentContentStream.endText();

            PdfExportService.currentYPosition -= LEADING;
        }
    }

    private static void drawTextWithWrapping(PDFont font, String label, String text) throws IOException {
        float pageWidth = PDRectangle.A4.getWidth();
        float labelWidth = FONTBOLD.getStringWidth(label) / 1000 * ScenarioHeader.BODY_FONT_SIZE;
        float textStartX = MARGIN + labelWidth;
        float textMaxWidth = pageWidth - textStartX - MARGIN;

        checkForNewPage();

        PdfExportService.currentContentStream.setFont(FONTBOLD, ScenarioHeader.BODY_FONT_SIZE);
        PdfExportService.currentContentStream.beginText();
        PdfExportService.currentContentStream.newLineAtOffset(MARGIN, PdfExportService.currentYPosition);
        PdfExportService.currentContentStream.showText(label);
        PdfExportService.currentContentStream.endText();

        if (text == null || text.isEmpty()) {
            text = "-";
        }

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float testWidth = font.getStringWidth(testLine) / 1000 * ScenarioHeader.BODY_FONT_SIZE;

            if (testWidth > textMaxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        boolean firstLine = true;
        for (String line : lines) {
            if (!firstLine) {
                checkForNewPage(); // Check for new page before subsequent lines
            }

            PdfExportService.currentContentStream.setFont(font, ScenarioHeader.BODY_FONT_SIZE);
            PdfExportService.currentContentStream.beginText();

            if (firstLine) {
                PdfExportService.currentContentStream.newLineAtOffset(textStartX, PdfExportService.currentYPosition);
                firstLine = false;
            } else {
                PdfExportService.currentContentStream.newLineAtOffset(MARGIN, PdfExportService.currentYPosition);
            }
            PdfExportService.currentContentStream.showText(line);
            PdfExportService.currentContentStream.endText();

            PdfExportService.currentYPosition -= LEADING;
        }
    }


    private static void checkForNewPage() throws IOException {
        if (PdfExportService.currentYPosition - LEADING < MARGIN) {
            PdfExportService.initNewPage();
            PdfExportService.currentYPosition = PDRectangle.A4.getHeight() - MARGIN;
        }
    }
}