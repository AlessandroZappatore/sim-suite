package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.service.storage.FileStorageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class LogoLoader {
    private static final Logger logger = LoggerFactory.getLogger(LogoLoader.class);
    private static final String LOGO_URL = "/META-INF/resources/icons/LogoSimsuite.png";
    private static final String CENTER_LOGO_FILENAME = "center_logo.png"; // Nome file standard per il logo del centro

    public static float centerLogoWidth;
    public static float centerLogoHeight;

    public static PDImageXObject loadLogo(PDDocument document) throws IOException {
        try (InputStream logoStream = LogoLoader.class.getResourceAsStream(LOGO_URL)) {
            if (logoStream == null) {
                logger.warn("File del logo non trovato: {}", LOGO_URL);
                return null;
            }

            // Converte l'InputStream in un array di byte
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = logoStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            // Crea l'immagine dall'array di byte
            return PDImageXObject.createFromByteArray(document, buffer.toByteArray(), CENTER_LOGO_FILENAME);
        }
    }

    public static PDImageXObject loadCenterLogo(PDDocument document, FileStorageService fileStorageService) {
        try (InputStream logoStream = fileStorageService.readFile(CENTER_LOGO_FILENAME)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = logoStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            if (buffer.size() == 0) {
                logger.warn("Il file del logo del centro è vuoto: {}", CENTER_LOGO_FILENAME);
                return null;
            }

            // Crea l'immagine dall'array di byte
            PDImageXObject logoImage = PDImageXObject.createFromByteArray(document, buffer.toByteArray(), CENTER_LOGO_FILENAME);

            // Memorizza le dimensioni originali per poterle usare nel metodo initNewPage
            centerLogoWidth = logoImage.getWidth();
            centerLogoHeight = logoImage.getHeight();

            return logoImage;

        } catch (FileNotFoundException e) {
            logger.info("Logo del centro non trovato nella directory di upload: {}", CENTER_LOGO_FILENAME);
            return null; // Il logo non esiste, gestito come caso normale.
        } catch (IOException e) {
            logger.error("Errore durante il caricamento del logo del centro da upload: {}", CENTER_LOGO_FILENAME, e);
            return null; // Errore durante la lettura o creazione dell'immagine
        }
    }
}
