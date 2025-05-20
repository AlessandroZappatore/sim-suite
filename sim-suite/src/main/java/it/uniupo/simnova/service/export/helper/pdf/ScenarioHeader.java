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
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.*;

/**
 * Classe per la creazione dell'intestazione del PDF per gli scenari.
 *
 * @author Alessandro Zappatore
 * @version 1.2
 */
public class ScenarioHeader {
    /**
     * Logger per la class ScenarioHeader.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioHeader.class);

    /**
     * Crea l'intestazione del PDF per gli scenari.
     *
     * @param scenario Lo Scenario da cui creare l'intestazione.
     * @throws IOException Se si verifica un errore durante la scrittura nel PDF.
     */
    public static void createScenarioHeader(Scenario scenario) throws IOException {
        // Controlla se il contenuto del PDF è stato inizializzato
        if (PdfExportService.currentContentStream == null) {
            logger.error("PDPageContentStream is null before creating ScenarioHeader.");
            throw new IOException("PDF content stream not initialized.");
        }

        // Aggiunge il titolo dell'intestazione
        drawCenteredWrappedText(FONTBOLD, TITLE_FONT_SIZE, "Dettaglio Scenario");
        PdfExportService.currentYPosition -= LEADING * 2; // Aggiunge dello spazio dopo il titolo

        // Aggiunge il titolo dello scenario
        drawCenteredWrappedText(FONTBOLD, HEADER_FONT_SIZE, scenario.getTitolo());
        PdfExportService.currentYPosition -= LEADING * 2; // Aggiunge dello spazio dopo il titolo dello scenario

        // Aggiunge le informazioni dello scenario
        drawTextWithWrapping(FONTREGULAR, "Autori: ", scenario.getAutori());
        drawTextWithWrapping(FONTREGULAR, "Target: ", scenario.getTarget());
        drawTextWithWrapping(FONTREGULAR, "Tipologia: ", scenario.getTipologia());
        drawTextWithWrapping(FONTREGULAR, "Paziente: ", scenario.getNomePaziente());
        drawTextWithWrapping(FONTREGULAR, "Patologia: ", scenario.getPatologia() != null && !scenario.getPatologia().isEmpty() ? scenario.getPatologia() : "-");
        drawTextWithWrapping(FONTREGULAR, "Durata: ", scenario.getTimerGenerale() > 0 ? scenario.getTimerGenerale() + " minuti" : "-");

        // Aggiorna la posizione corrente
        PdfExportService.currentYPosition -= LEADING;

        logger.info("Header creato");
    }

    /**
     * Scrive del testo centrato e con eventuale a capo automatico nel PDF.
     * Il testo viene suddiviso in più righe se supera la larghezza massima consentita.
     * Ogni riga viene centrata orizzontalmente nella pagina.
     *
     * @param font     Il font da utilizzare per il testo.
     * @param fontSize La dimensione del font.
     * @param text     Il testo da aggiungere.
     * @throws IOException Se si verifica un errore durante la scrittura nel PDF.
     */
    private static void drawCenteredWrappedText(PDFont font, float fontSize, String text) throws IOException {
        // Se il testo è nullo o vuoto, non fa nulla
        if (text == null || text.isEmpty()) {
            return;
        }

        // Ottiene la larghezza della pagina e calcola la larghezza massima per il testo
        float pageWidth = PDRectangle.A4.getWidth();
        float maxWidth = pageWidth - 2 * MARGIN;

        // Lista delle righe risultanti dopo il wrapping
        List<String> lines = new ArrayList<>();
        // Divide il testo in parole
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        // Costruisce le righe rispettando la larghezza massima
        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float testWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            // Se la riga supera la larghezza massima, la aggiunge alla lista e ne inizia una nuova
            if (testWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            }
        }
        // Aggiunge l'ultima riga se presente
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        // Scrive ogni riga centrata
        for (String line : lines) {
            checkForNewPage(); // Verifica se serve andare a nuova pagina
            float lineWidth = font.getStringWidth(line) / 1000 * fontSize;
            float xPosition = (pageWidth - lineWidth) / 2;
            // Imposta font e posizione, poi scrive la riga
            PdfExportService.currentContentStream.setFont(font, fontSize);
            PdfExportService.currentContentStream.beginText();
            PdfExportService.currentContentStream.newLineAtOffset(xPosition, PdfExportService.currentYPosition);
            PdfExportService.currentContentStream.showText(line);
            PdfExportService.currentContentStream.endText();
            // Aggiorna la posizione verticale per la prossima riga
            PdfExportService.currentYPosition -= LEADING;
        }
    }

    /**
     * Scrive una coppia etichetta-valore con gestione dell'andata a capo automatica.
     * L'etichetta viene scritta in grassetto, il valore con il font specificato.
     * Se il testo supera la larghezza massima, viene suddiviso su più righe.
     *
     * @param font  Il font da utilizzare per il valore.
     * @param label L'etichetta da visualizzare (es. "Autori: ").
     * @param text  Il valore da visualizzare accanto all'etichetta.
     * @throws IOException Se si verifica un errore durante la scrittura nel PDF.
     */
    private static void drawTextWithWrapping(PDFont font, String label, String text) throws IOException {
        // Calcola la larghezza della pagina e la posizione di inizio del testo dopo l'etichetta
        float pageWidth = PDRectangle.A4.getWidth();
        float labelWidth = FONTBOLD.getStringWidth(label) / 1000 * BODY_FONT_SIZE;
        float textStartX = MARGIN + labelWidth;
        float textMaxWidth = pageWidth - textStartX - MARGIN;

        checkForNewPage(); // Verifica se serve andare a nuova pagina

        // Scrive l'etichetta in grassetto
        PdfExportService.currentContentStream.setFont(FONTBOLD, BODY_FONT_SIZE);
        PdfExportService.currentContentStream.beginText();
        PdfExportService.currentContentStream.newLineAtOffset(MARGIN, PdfExportService.currentYPosition);
        PdfExportService.currentContentStream.showText(label);
        PdfExportService.currentContentStream.endText();

        // Se il testo è nullo o vuoto, mostra un trattino
        if (text == null || text.isEmpty()) {
            text = "-";
        }

        // Suddivide il testo in righe rispettando la larghezza massima
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float testWidth = font.getStringWidth(testLine) / 1000 * BODY_FONT_SIZE;

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
                checkForNewPage(); // Verifica se serve andare a nuova pagina prima delle righe successive
            }

            PdfExportService.currentContentStream.setFont(font, BODY_FONT_SIZE);
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

    /**
     * Verifica se la posizione verticale corrente è sufficiente per scrivere una nuova riga.
     * Se non c'è spazio, crea una nuova pagina e riposiziona il cursore in alto.
     *
     * @throws IOException Se si verifica un errore durante la creazione della nuova pagina.
     */
    private static void checkForNewPage() throws IOException {
        if (PdfExportService.currentYPosition - LEADING < MARGIN) {
            PdfExportService.initNewPage();
            PdfExportService.currentYPosition = PDRectangle.A4.getHeight() - MARGIN;
        }
    }
}
