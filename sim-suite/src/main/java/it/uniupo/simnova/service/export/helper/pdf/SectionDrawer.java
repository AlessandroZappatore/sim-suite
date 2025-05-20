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
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.*;

/**
 * Classe di utilità per la generazione di varie sezioni.
 * Permette di disegnare sezioni e sottosezioni nel PDF, gestendo il testo formattato e il wrapping.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class SectionDrawer {

    /**
     * Disegna una sezione con un titolo e un contenuto.
     * Gestisce la formattazione del titolo e il rendering del contenuto, scegliendo se interpretare HTML o testo semplice.
     *
     * @param title   Il titolo della sezione.
     * @param content Il contenuto della sezione.
     * @throws IOException In caso di errore nella scrittura del PDF.
     */
    public static void drawSection(String title, String content) throws IOException {
        // Imposta il font e la dimensione per il titolo della sezione
        currentContentStream.setFont(FONTBOLD, HEADER_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN, PdfExportService.currentYPosition);
        currentContentStream.showText(title);
        currentContentStream.endText();
        // Aggiorna la posizione verticale dopo il titolo
        PdfExportService.currentYPosition -= LEADING * 1.5f;

        // Se il contenuto non è vuoto, lo disegna
        if (content != null && !content.isEmpty()) {

            // Se il titolo corrisponde a una delle sezioni che richiedono formattazione HTML, usa il rendering HTML
            if (title.equals("Descrizione")
                    || title.equals("Briefing")
                    || title.equals("Informazioni dai genitori")
                    || title.equals("Patto d'Aula")
                    || title.equals("Obiettivi Didattici")
                    || title.equals("Moulage")
                    || title.equals("Liquidi e dosi farmaci")
            ) {
                // Rendering HTML con formattazione
                renderHtmlWithFormatting(content, MARGIN + 20);
                PdfExportService.currentYPosition -= LEADING / 2;
            } else {
                // Rendering testo semplice con wrapping
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 10, content);
            }
            // Spazio dopo il contenuto della sezione
            PdfExportService.currentYPosition -= LEADING;
        }
    }

    /**
     * Esegue il rendering di testo HTML con formattazione (grassetto, corsivo) e wrapping automatico.
     * Gestisce il parsing del testo e la suddivisione in linee, applicando i font corretti.
     *
     * @param htmlContent Contenuto HTML da renderizzare.
     * @param xOffset     Offset orizzontale per l'inizio della riga.
     * @throws IOException In caso di errore nella scrittura del PDF.
     */
    public static void renderHtmlWithFormatting(String htmlContent, float xOffset) throws IOException {
        // Parsing HTML ed estrazione del testo semplice
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
        String plainText = doc.text();

        // Seleziona gli elementi in grassetto e corsivo
        Elements boldElements = doc.select("strong, b");
        Elements italicElements = doc.select("em, i");

        // Mappa per intervalli di testo in grassetto e corsivo
        Map<Integer, Integer> boldRanges = new HashMap<>();
        Map<Integer, Integer> italicRanges = new HashMap<>();

        // Calcola gli intervalli di grassetto
        for (org.jsoup.nodes.Element element : boldElements) {
            String text = element.text();
            int start = plainText.indexOf(text);
            if (start >= 0) {
                boldRanges.put(start, start + text.length());
            }
        }

        // Calcola gli intervalli di corsivo
        for (Element element : italicElements) {
            String text = element.text();
            int start = plainText.indexOf(text);
            if (start >= 0) {
                italicRanges.put(start, start + text.length());
            }
        }

        // Suddivide il testo in parole per il wrapping
        String[] words = plainText.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int charCount = 0;

        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(xOffset, currentYPosition);

        // Ciclo sulle parole per costruire le righe con wrapping
        for (String word : words) {
            String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
            float width = FONTREGULAR.getStringWidth(testLine) / 1000 * BODY_FONT_SIZE;

            // Se la riga supera la larghezza massima, la stampa e va a capo
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

        // Stampa l'ultima riga se presente
        if (!currentLine.isEmpty()) {
            drawFormattedLine(currentLine.toString(), charCount - currentLine.length(), boldRanges, italicRanges);
        }
        currentContentStream.endText();
    }

    /**
     * Disegna una riga di testo applicando la formattazione (grassetto/corsivo) sui relativi intervalli.
     *
     * @param line         Riga di testo da stampare.
     * @param startPos     Posizione iniziale globale della riga nel testo originale.
     * @param boldRanges   Intervalli di grassetto.
     * @param italicRanges Intervalli di corsivo.
     * @throws IOException In caso di errore nella scrittura del PDF.
     */
    private static void drawFormattedLine(String line, int startPos, Map<Integer, Integer> boldRanges,
                                          Map<Integer, Integer> italicRanges) throws IOException {

        StringBuilder currentPart = new StringBuilder();

        // Ciclo sui caratteri della riga per applicare la formattazione
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            int globalPos = startPos + i;

            boolean isBold = isBold(globalPos, boldRanges);
            boolean isItalic = isItalic(globalPos, italicRanges);

            currentPart.append(c);

            // Quando cambia la formattazione o si arriva alla fine, stampa la parte corrente
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

    /**
     * Restituisce il font PDF corretto in base a grassetto/corsivo.
     *
     * @param isBold   True se grassetto.
     * @param isItalic True se corsivo.
     * @return Il font PDF corrispondente.
     */
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

    /**
     * Verifica se la posizione specificata rientra in un intervallo di grassetto.
     *
     * @param position   Posizione da verificare.
     * @param boldRanges Intervalli di grassetto.
     * @return True se la posizione è in grassetto.
     */
    private static boolean isBold(int position, Map<Integer, Integer> boldRanges) {
        for (Map.Entry<Integer, Integer> range : boldRanges.entrySet()) {
            if (position >= range.getKey() && position < range.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se la posizione specificata rientra in un intervallo di corsivo.
     *
     * @param position     Posizione da verificare.
     * @param italicRanges Intervalli di corsivo.
     * @return True se la posizione è in corsivo.
     */
    private static boolean isItalic(int position, Map<Integer, Integer> italicRanges) {
        for (Map.Entry<Integer, Integer> range : italicRanges.entrySet()) {
            if (position >= range.getKey() && position < range.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Disegna testo con wrapping automatico, andando a capo quando necessario.
     * Gestisce anche eventuali ritorni a capo espliciti nel testo.
     *
     * @param font     Font da utilizzare.
     * @param fontSize Dimensione del font.
     * @param x        Offset orizzontale.
     * @param text     Testo da stampare.
     * @throws IOException In caso di errore nella scrittura del PDF.
     */
    public static void drawWrappedText(PDFont font, float fontSize, float x, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }

        // Suddivide il testo in righe esplicite
        String[] lines = text.split("\n");

        for (String line : lines) {

            // Verifica se serve andare a nuova pagina
            checkForNewPage(LEADING);

            currentContentStream.setFont(font, fontSize);

            // Suddivide la riga in parole per il wrapping
            String[] words = line.split(" ");
            StringBuilder currentLine = new StringBuilder();

            currentContentStream.beginText();
            currentContentStream.newLineAtOffset(x, currentYPosition);

            // Ciclo sulle parole per costruire la riga
            for (String word : words) {
                String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
                float width = font.getStringWidth(testLine) / 1000 * fontSize;

                // Se la riga supera la larghezza massima, la stampa e va a capo
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

            // Stampa l'ultima riga della linea
            if (!currentLine.isEmpty()) {
                currentContentStream.showText(currentLine.toString());
            }
            currentContentStream.endText();
            currentYPosition -= LEADING;
        }
    }

    /**
     * Disegna una sottosezione con titolo, utilizzando un font più piccolo e indentazione.
     * Inserisce anche una riga vuota dopo il titolo.
     *
     * @param title Titolo della sottosezione.
     * @throws IOException In caso di errore nella scrittura del PDF.
     */
    public static void drawSubsection(String title) throws IOException {
        // Imposta font e posizione per il titolo della sottosezione
        currentContentStream.setFont(FONTBOLD, BODY_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN + 10, currentYPosition);
        currentContentStream.showText(title);
        currentContentStream.endText();
        currentYPosition -= LEADING;

        // Inserisce una riga vuota dopo il titolo della sottosezione
        drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, "");

    }
}
