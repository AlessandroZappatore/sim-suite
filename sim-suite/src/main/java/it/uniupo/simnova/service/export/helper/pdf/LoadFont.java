package it.uniupo.simnova.service.export.helper.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classe di supporto per caricare i font per i PDF.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class LoadFont {
    /**
     * Carica un font TrueType da un file.
     *
     * @param document il documento PDF in cui caricare il font
     * @param fontPath il percorso del file del font
     * @return il font caricato
     * @throws IOException se si verifica un errore durante il caricamento del font
     */
    public static PDFont loadFont(PDDocument document, String fontPath) throws IOException {
        try (InputStream fontStream = LoadFont.class.getResourceAsStream(fontPath)) {
            if (fontStream == null) {
                throw new IOException("Font file not found: " + fontPath);
            }
            return PDType0Font.load(document, fontStream);
        }
    }
}
