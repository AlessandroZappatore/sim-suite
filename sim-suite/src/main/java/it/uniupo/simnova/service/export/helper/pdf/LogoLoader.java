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

/**
 * Classe per il caricamento dei loghi per i PDF.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class LogoLoader {
    /**
     * Logger per la classe LogoLoader.
     */
    private static final Logger logger = LoggerFactory.getLogger(LogoLoader.class);
    /**
     * Percorso del logo di default.
     */
    private static final String LOGO_URL = "/META-INF/resources/icons/LogoSimsuite.png";
    /**
     * Nome del file del logo del centro.
     */
    private static final String CENTER_LOGO_FILENAME = "center_logo.png"; // Nome file standard per il logo del centro
    /**
     * Larghezza del logo del centro.
     */
    public static float centerLogoWidth;
    /**
     * Altezza del logo del centro.
     */
    public static float centerLogoHeight;

    /**
     * Carica il logo di SIMSUITE da aggiungere nel PDF.
     *
     * @param document Il documento PDF in cui caricare il logo.
     * @return L'oggetto PDImageXObject rappresentante il logo.
     * @throws IOException Se si verifica un errore durante il caricamento del logo.
     */
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

    /**
     * Carica il logo del centro da aggiungere nel PDF.
     *
     * @param document           Il documento PDF in cui caricare il logo.
     * @param fileStorageService Il servizio di storage per il caricamento del logo.
     * @return L'oggetto PDImageXObject rappresentante il logo del centro.
     */
    public static PDImageXObject loadCenterLogo(PDDocument document, FileStorageService fileStorageService) {
        try (InputStream logoStream = fileStorageService.readFile(CENTER_LOGO_FILENAME)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = logoStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            // Assicura che sia presente il logo del centro
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
            throw new RuntimeException("Logo del centro non trovato nella directory di upload");
        } catch (IOException e) {
            logger.error("Errore durante il caricamento del logo del centro da upload: {}", CENTER_LOGO_FILENAME, e);
            throw new RuntimeException("Errore durante il caricamento del logo del centro da upload", e);
        }
    }
}
