package it.uniupo.simnova.service.export.helper.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.io.InputStream;

public class LoadFont {
    public static PDFont loadFont(PDDocument document, String fontPath) throws IOException {
        try (InputStream fontStream = LoadFont.class.getResourceAsStream(fontPath)) {
            if (fontStream == null) {
                throw new IOException("Font file not found: " + fontPath);
            }
            return PDType0Font.load(document, fontStream);
        }
    }
}
