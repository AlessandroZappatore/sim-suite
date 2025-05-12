package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.service.export.PdfExportService;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static it.uniupo.simnova.service.export.PdfExportService.*;
import static it.uniupo.simnova.views.constant.PdfConstant.*;

public class SectionDrawer {

    public static void drawSection(String title, String content) throws IOException {
        currentContentStream.setFont(FONTBOLD, HEADER_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN, PdfExportService.currentYPosition);
        currentContentStream.showText(title);
        currentContentStream.endText();
        PdfExportService.currentYPosition -= LEADING * 1.5f;

        if (content != null && !content.isEmpty()) {

            if (title.equals("Descrizione")
                    || title.equals("Briefing")
                    || title.equals("Informazioni dai genitori")
                    || title.equals("Patto d'Aula")
                    || title.equals("Obiettivi Didattici")
                    || title.equals("Moulage")
                    || title.equals("Liquidi e dosi farmaci")
            ) {

                renderHtmlWithFormatting(content, MARGIN + 20);
                PdfExportService.currentYPosition -= LEADING / 2;
            } else {
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 10, content);
            }
            PdfExportService.currentYPosition -= LEADING;
        }
    }

    public static void renderHtmlWithFormatting(String htmlContent, float xOffset) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
        String plainText = doc.text();

        Elements boldElements = doc.select("strong, b");
        Elements italicElements = doc.select("em, i");

        Map<Integer, Integer> boldRanges = new HashMap<>();
        Map<Integer, Integer> italicRanges = new HashMap<>();

        for (org.jsoup.nodes.Element element : boldElements) {
            String text = element.text();
            int start = plainText.indexOf(text);
            if (start >= 0) {
                boldRanges.put(start, start + text.length());
            }
        }

        for (Element element : italicElements) {
            String text = element.text();
            int start = plainText.indexOf(text);
            if (start >= 0) {
                italicRanges.put(start, start + text.length());
            }
        }

        String[] words = plainText.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int charCount = 0;

        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(xOffset, currentYPosition);

        for (String word : words) {
            String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
            float width = FONTREGULAR.getStringWidth(testLine) / 1000 * BODY_FONT_SIZE;

            if (width > (PDRectangle.A4.getWidth() - 2 * MARGIN - 10)) {

                drawFormattedLine(currentLine.toString(), charCount - currentLine.length(), boldRanges, italicRanges);

                currentContentStream.endText();
                currentYPosition -= LEADING;
                checkForNewPage(LEADING);

                currentContentStream.beginText();
                currentContentStream.newLineAtOffset((float) 60.0, currentYPosition);
                currentLine = new StringBuilder(word);
                charCount += 1; // Per lo spazio
            } else {
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                    charCount++;
                }
                currentLine.append(word);
            }
            charCount += word.length();
        }

        if (!currentLine.isEmpty()) {
            drawFormattedLine(currentLine.toString(), charCount - currentLine.length(), boldRanges, italicRanges);
        }
        currentContentStream.endText();
    }

    private static void drawFormattedLine(String line, int startPos, Map<Integer, Integer> boldRanges,
                                          Map<Integer, Integer> italicRanges) throws IOException {

        StringBuilder currentPart = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            int globalPos = startPos + i;

            boolean isBold = isBold(globalPos, boldRanges);
            boolean isItalic = isItalic(globalPos, italicRanges);

            currentPart.append(c);

            if (i == line.length() - 1 ||
                    isBold(globalPos + 1, boldRanges) != isBold ||
                    isItalic(globalPos + 1, italicRanges) != isItalic) {

                PDFont font = getPdFont(isBold, isItalic);

                currentContentStream.setFont(font, BODY_FONT_SIZE);
                font.getStringWidth(currentPart.toString());
                currentContentStream.showText(currentPart.toString());

                currentPart = new StringBuilder();
            }
        }
    }


    private static PDFont getPdFont(boolean isBold, boolean isItalic) {
        PDFont font = FONTREGULAR;
        if (isBold && isItalic) {
            font = FONTBOLDITALIC;
        } else if (isBold) {
            font = FONTBOLD;
        } else if (isItalic) {
            font = FONTITALIC;
        }
        return font;
    }

    private static boolean isBold(int position, Map<Integer, Integer> boldRanges) {
        for (Map.Entry<Integer, Integer> range : boldRanges.entrySet()) {
            if (position >= range.getKey() && position < range.getValue()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isItalic(int position, Map<Integer, Integer> italicRanges) {
        for (Map.Entry<Integer, Integer> range : italicRanges.entrySet()) {
            if (position >= range.getKey() && position < range.getValue()) {
                return true;
            }
        }
        return false;
    }

    public static void drawWrappedText(PDFont font, float fontSize, float x, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }

        String[] lines = text.split("\n");

        for (String line : lines) {

            checkForNewPage(LEADING);

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

                    checkForNewPage(LEADING);

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

    public static void drawSubsection(String title) throws IOException {
        currentContentStream.setFont(FONTBOLD, BODY_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN + 10, currentYPosition);
        currentContentStream.showText(title);
        currentContentStream.endText();
        currentYPosition -= LEADING;

        drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, "");

    }
}
