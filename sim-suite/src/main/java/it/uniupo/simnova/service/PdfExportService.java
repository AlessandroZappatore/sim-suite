package it.uniupo.simnova.service;

import it.uniupo.simnova.api.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
     * Dimensione del font per l'intestazione.
     */
    private static final float HEADER_FONT_SIZE = 14;
    /**
     * Dimensione del font per il corpo del testo.
     */
    private static final float BODY_FONT_SIZE = 11;
    /**
     * Dimensione del font per il testo di piccole dimensioni.
     */
    private static final float SMALL_FONT_SIZE = 9;
    /**
     * Larghezza del logo.
     */
    private static final float LOGO_WIDTH = 60;
    /**
     * Altezza del logo.
     */
    private static final float LOGO_HEIGHT = 60;
    private static final String CENTER_LOGO_FILENAME = "center_logo.png"; // Nome file standard per il logo del centro
    private final FileStorageService fileStorageService;
    /**
     * Numero di pagina corrente.
     */
    private int pageNumber = 1;
    /**
     * Logger per il servizio.
     */
    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Posizione Y corrente per il disegno del contenuto.
     */
    private float currentYPosition;
    /**
     * Stream di contenuto corrente per la pagina PDF.
     */
    private PDPageContentStream currentContentStream;
    /**
     * Documento PDF corrente.
     */
    private PDDocument document;
    /**
     * Font per il testo in grassetto.
     */
    private PDFont fontBold;
    /**
     * Font per il testo normale.
     */
    private PDFont fontRegular;
    private PDFont fontItalic;
    /**
     * Font per il testo in grassetto di piccole dimensioni.
     */
    private PDImageXObject logo;

    private PDImageXObject centerLogo;

    /**
     * Costruttore del servizio PdfExportService.
     *
     * @param scenarioService il servizio per la gestione degli scenari
     */
    public PdfExportService(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Esporta uno scenario in formato PDF.
     *
     * @param scenarioId l'ID dello scenario da esportare
     * @return un array di byte contenente il PDF generato
     * @throws IOException se si verifica un errore durante la creazione del PDF
     */
    public byte[] exportScenarioToPdf(int scenarioId) throws IOException {
        document = null;
        currentContentStream = null;
        pageNumber = 1;

        try {
            document = new PDDocument();

            // Carica i font
            fontRegular = loadFont(document, "/fonts/LiberationSans-Regular.ttf");
            fontBold = loadFont(document, "/fonts/LiberationSans-Bold.ttf");
            fontItalic = loadFont(document, "/fonts/LiberationSans-Italic.ttf");
            // Carica il logo
            logo = loadLogo(document);
            centerLogo = loadCenterLogo(document);

            // Inizializza la prima pagina
            initNewPage();

            // Recupera lo scenario
            Scenario scenario = scenarioService.getScenarioById(scenarioId);
            logger.info("Recuperato scenario: {}", scenario.getTitolo());

            // Crea l'intestazione dello scenario
            createScenarioHeader(scenario);
            createPatientSection(scenarioId);
            createExamsSection(scenarioId);

            // Controlla il tipo di scenario e crea le sezioni appropriate
            String scenarioType = scenarioService.getScenarioType(scenarioId);
            if (scenarioType != null && (scenarioType.equals("Advanced Scenario") ||
                    scenarioType.equals("Patient Simulated Scenario"))) {
                createTimelineSection(scenario);
                logger.info("Timeline creata");
            }

            // Crea la sezione della sceneggiatura
            if (scenarioType != null && scenarioType.equals("Patient Simulated Scenario")) {
                createSceneggiaturaSection(scenario);
            }

            // Chiude il contenuto corrente
            if (currentContentStream != null) {
                try {
                    currentContentStream.close();
                    currentContentStream = null;
                } catch (Exception e) {
                    logger.warn("Errore nella chiusura dello stream corrente: {}", e.getMessage());
                }
            }

            // Salva il documento PDF in un array di byte
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            logger.info("PDF salvato con successo");
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Errore nella generazione del PDF", e);
            throw new IOException("Generazione PDF fallita: " + e.getMessage(), e);
        } finally {
            // Chiude il documento e lo stream di contenuto
            if (currentContentStream != null) {
                try {
                    currentContentStream.close();
                } catch (Exception e) {
                    logger.warn("Errore nella chiusura dello stream: {}", e.getMessage());
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    logger.warn("Errore nella chiusura del documento: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Carica il logo da un file di risorse.
     *
     * @param document il documento PDF in cui caricare il logo
     * @return l'oggetto immagine del logo
     * @throws IOException se si verifica un errore durante il caricamento del logo
     */
    private PDImageXObject loadLogo(PDDocument document) throws IOException {
        try (InputStream logoStream = getClass().getResourceAsStream("/META-INF/resources/icons/LogoSimsuite.png")) {
            if (logoStream == null) {
                logger.warn("File del logo non trovato: /META-INF/resources/icons/LogoSimsuite.png");
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
            return PDImageXObject.createFromByteArray(document, buffer.toByteArray(), "LogoSimsuite.png");
        }
    }

    private PDImageXObject loadCenterLogo(PDDocument document) {
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
            return PDImageXObject.createFromByteArray(document, buffer.toByteArray(), CENTER_LOGO_FILENAME);

        } catch (FileNotFoundException e) {
            logger.info("Logo del centro non trovato nella directory di upload: {}", CENTER_LOGO_FILENAME);
            return null; // Il logo non esiste, gestito come caso normale.
        } catch (IOException e) {
            logger.error("Errore durante il caricamento del logo del centro da upload: {}", CENTER_LOGO_FILENAME, e);
            return null; // Errore durante la lettura o creazione dell'immagine
        }
    }

    /**
     * Inizializza una nuova pagina nel documento
     *
     * @throws IOException se si verifica un errore durante la creazione della pagina
     */
    private void initNewPage() throws IOException {
        // Chiude lo stream di contenuto corrente se esiste
        if (currentContentStream != null) {
            try {
                currentContentStream.close();
            } catch (Exception e) {
                logger.warn("Errore nella chiusura dello stream del contenuto corrente: {}", e.getMessage());
            }
        }

        // Crea una nuova pagina
        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);

        // Creazione di un nuovo stream di contenuto
        currentContentStream = new PDPageContentStream(document, currentPage);

        // Aggiunge il logo se è la prima pagina
        if (pageNumber == 1) {
            float totalWidth = 0;
            int logoCount = 0;

            // Calcola la larghezza totale necessaria
            if (logo != null) {
                totalWidth += LOGO_WIDTH;
                logoCount++;
            }
            if (centerLogo != null) {
                totalWidth += LOGO_WIDTH;
                logoCount++;
            }

            // Aggiungi spaziatura tra i loghi se ce ne sono due
            float spacing = logoCount > 1 ? 20 : 0;
            totalWidth += spacing;

            // Calcola la posizione iniziale X (per centrare l'insieme dei loghi)
            float startX = (PDRectangle.A4.getWidth() - totalWidth) / 2;
            float logoY = PDRectangle.A4.getHeight() - MARGIN - LOGO_HEIGHT;

            // Disegna il primo logo se disponibile
            if (logo != null) {
                currentContentStream.drawImage(logo, startX, logoY, LOGO_WIDTH, LOGO_HEIGHT);
                startX += LOGO_WIDTH + spacing;
            }

            // Disegna il logo del centro se disponibile
            if (centerLogo != null) {
                currentContentStream.drawImage(centerLogo, startX, logoY, LOGO_WIDTH, LOGO_HEIGHT);
            }

            // Aggiorna la posizione Y corrente
            currentYPosition = logoY - LEADING;
        } else {
            currentYPosition = PDRectangle.A4.getHeight() - MARGIN;
        }
        pageNumber++;
    }

    /**
     * Verifica se è necessario creare una nuova pagina in base allo spazio rimanente
     *
     * @param neededSpace lo spazio necessario per il contenuto successivo
     * @throws IOException se si verifica un errore durante la creazione della pagina
     */
    private void checkForNewPage(float neededSpace) throws IOException {
        if (currentYPosition - neededSpace < MARGIN) {
            initNewPage();
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
     * Crea l'intestazione dello scenario nel PDF.
     *
     * @param scenario lo scenario da esportare
     * @throws IOException se si verifica un errore durante la creazione dell'intestazione
     */
    private void createScenarioHeader(Scenario scenario) throws IOException {
        // Titolo del PDF
        drawCenteredText(fontBold, TITLE_FONT_SIZE, "Dettaglio Scenario");
        currentYPosition -= LEADING * 2;

        // Titolo dello scenario
        drawCenteredText(fontBold, HEADER_FONT_SIZE, scenario.getTitolo());
        currentYPosition -= LEADING * 2;

        // Informazioni generali
        currentContentStream.setFont(fontRegular, BODY_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN, currentYPosition);
        currentContentStream.showText("Paziente: " + scenario.getNomePaziente());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Patologia: " + scenario.getPatologia());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Durata: " + scenario.getTimerGenerale() + " minuti");
        currentContentStream.endText();
        currentYPosition -= LEADING * 5;

        // Descrizione
        if (scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty()) {
            drawSection("Descrizione", scenario.getDescrizione());
        }

        // Briefing
        if (scenario.getBriefing() != null && !scenario.getBriefing().isEmpty()) {
            drawSection("Briefing", scenario.getBriefing());
        }

        // Patto d'aula
        if (scenario.getPattoAula() != null && !scenario.getPattoAula().isEmpty()) {
            drawSection("Patto d'Aula", scenario.getPattoAula());
        }

        // Azioni chiave
        if (scenario.getAzioneChiave() != null && !scenario.getAzioneChiave().isEmpty()) {
            drawSection("Azioni Chiave", scenario.getAzioneChiave());
        }

        // Obiettivi didattici
        if (scenario.getObiettivo() != null && !scenario.getObiettivo().isEmpty()) {
            drawSection("Obiettivi Didattici", scenario.getObiettivo());
        }

        // Moulage
        if (scenario.getMoulage() != null && !scenario.getMoulage().isEmpty()) {
            drawSection("Moulage", scenario.getMoulage());
        }

        // Liquidi
        if (scenario.getLiquidi() != null && !scenario.getLiquidi().isEmpty()) {
            drawSection("Liquidi", scenario.getLiquidi());
        }

        logger.info("Header creato");
    }

    /**
     * Crea la sezione dello stato del paziente nel PDF.
     *
     * @param scenarioId l'ID dello scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createPatientSection(Integer scenarioId) throws IOException {
        // Verifica spazio disponibile e crea pagina se necessario
        checkForNewPage(LEADING * 5);

        // Titolo sezione
        drawSection("Stato Paziente", "");

        PazienteT0 paziente = scenarioService.getPazienteT0ById(scenarioId);
        if (paziente != null) {
            // Parametri vitali
            checkForNewPage(LEADING * 10);
            drawSubsection("Parametri Vitali");

            String vitalParams = String.format(
                    "PA: %s mmHg\nFC: %d bpm\nRR: %d atti/min\nTemperatura: %.1f °C\nSpO2: %d%%\nEtCO2: %d mmHg\nMonitor: %s",
                    paziente.getPA(), paziente.getFC(), paziente.getRR(),
                    paziente.getT(), paziente.getSpO2(), paziente.getEtCO2(),
                    paziente.getMonitor()
            );

            drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, vitalParams);

            // Accessi venosi
            if (!paziente.getAccessiVenosi().isEmpty()) {
                checkForNewPage(LEADING * 3 + LEADING * paziente.getAccessiVenosi().size());
                drawSubsection("Accessi Venosi");
                for (Accesso accesso : paziente.getAccessiVenosi()) {
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20,
                            "• " + accesso.getTipologia() + " - " + accesso.getPosizione());
                }
            }

            // Accessi arteriosi
            if (!paziente.getAccessiArteriosi().isEmpty()) {
                checkForNewPage(LEADING * 3 + LEADING * paziente.getAccessiArteriosi().size());
                drawSubsection("Accessi Arteriosi");
                for (Accesso accesso : paziente.getAccessiArteriosi()) {
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20,
                            "• " + accesso.getTipologia() + " - " + accesso.getPosizione());
                }
            }
        }

        // Esame fisico
        EsameFisico esame = scenarioService.getEsameFisicoById(scenarioId);
        if (esame != null && !esame.getSections().isEmpty()) {
            checkForNewPage(LEADING * 5);
            drawSubsection("Esame Fisico");

            Map<String, String> sections = esame.getSections();
            for (Map.Entry<String, String> entry : sections.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                    checkForNewPage(LEADING * 3);
                    drawWrappedText(fontBold, BODY_FONT_SIZE, MARGIN + 20, entry.getKey() + ":");
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 30, entry.getValue());
                }
            }
            currentYPosition -= LEADING;
        }


        logger.info("Patient section creata");
    }

    /**
     * Crea la sezione degli esami e referti nel PDF.
     *
     * @param scenarioId l'ID dello scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createExamsSection(Integer scenarioId) throws IOException {
        List<EsameReferto> esami = scenarioService.getEsamiRefertiByScenarioId(scenarioId);
        if (esami == null || esami.isEmpty()) {
            return;
        }

        // Verifica spazio disponibile e crea pagina se necessario
        checkForNewPage(LEADING * 5);

        // Titolo sezione
        drawSection("Esami e Referti", "");


        for (EsameReferto esame : esami) {
            String examType = getExamType(esame);
            checkForNewPage(LEADING * 3);
            // Aggiunge il tipo dell'esame
            drawSubsection(examType);
            // Aggiunge il referto
            if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, "Referto: " + esame.getRefertoTestuale());
            }
            // Aggiunge il nome del media allegato
            if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                drawWrappedText(fontRegular, SMALL_FONT_SIZE, MARGIN + 20, "Allegato: " + esame.getMedia());
            }

            currentYPosition -= LEADING;
        }

        logger.info("Exams section created");
    }

    /**
     * Ottiene il tipo di esame e sostituisce i caratteri speciali con i numeri corrispondenti.
     *
     * @param esame l'oggetto EsameReferto
     * @return il tipo di esame con i caratteri speciali sostituiti
     */
    private static String getExamType(EsameReferto esame) {
        String examType = esame.getTipo();
        // Sostituisce i caratteri speciali con i numeri corrispondenti
        if (examType != null) {
            examType = examType.replace('₀', '0');
            examType = examType.replace('₁', '1');
            examType = examType.replace('₂', '2');
            examType = examType.replace('₃', '3');
            examType = examType.replace('₄', '4');
            examType = examType.replace('₅', '5');
            examType = examType.replace('₆', '6');
            examType = examType.replace('₇', '7');
            examType = examType.replace('₈', '8');
            examType = examType.replace('⁻', '-');
            examType = examType.replace('⁺', '+');
            examType = examType.replace('⁰', '0');
            examType = examType.replace('¹', '1');
            examType = examType.replace('²', '2');
            examType = examType.replace('³', '3');
            examType = examType.replace('⁴', '4');
            examType = examType.replace('⁵', '5');
            examType = examType.replace('⁶', '6');
            examType = examType.replace('⁷', '7');
            examType = examType.replace('⁸', '8');
            examType = examType.replace('⁹', '9');
        } else examType = "";
        return examType;
    }

    /**
     * Crea la sezione timeline nel PDF.
     *
     * @param scenario lo scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createTimelineSection(Scenario scenario) throws IOException {
        List<Tempo> tempi = ScenarioService.getTempiByScenarioId(scenario.getId());
        if (tempi.isEmpty()) {
            return;
        }

        // Verifica spazio disponibile e crea pagina se necessario
        checkForNewPage(LEADING * 5);

        // Titolo sezione
        drawSection("Timeline", "");

        for (Tempo tempo : tempi) {
            // Verifica spazio disponibile per ogni tempo
            checkForNewPage(LEADING * 10);


            String title = String.format("Tempo %d (%.1f min)",
                    tempo.getIdTempo(),
                    tempo.getTimerTempo() / 60.0 // Converte i secondi in minuti
            );

            drawSubsection(title);

            // Parametri vitali
            StringBuilder params = new StringBuilder(String.format(
                    "PA: %s mmHg\nFC: %d bpm\nRR: %d atti/min\nTemperatura: %.1f °C\nSpO2: %d%%\nEtCO2: %d mmHg",
                    tempo.getPA(), tempo.getFC(), tempo.getRR(),
                    tempo.getT(), tempo.getSpO2(), tempo.getEtCO2()
            ));

            List<ParametroAggiuntivo> parametriAggiuntivo = ScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenario.getId());

            // Aggiungi parametri aggiuntivi
            if (!parametriAggiuntivo.isEmpty()) {
                for (ParametroAggiuntivo parametro : parametriAggiuntivo) {
                    params.append(String.format("\n%s: %s %s", parametro.getNome(), parametro.getValore(), parametro.getUnitaMisura()));
                }
            }

            drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, params.toString());

            // Dettagli aggiuntivi
            if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(fontBold, BODY_FONT_SIZE, MARGIN + 20, "Dettagli:");
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 30, tempo.getAltriDettagli());
            }

            String azione = tempo.getAzione();
            if (azione != null && !azione.isEmpty()) {
                drawWrappedText(fontBold, BODY_FONT_SIZE, MARGIN + 20, "Azioni da svolgere per passare a T" + tempo.getTSi() + ":");
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 30, azione);
            }

            // Tempo se non avviene l'azione
            if (tempo.getTNo() >= 0) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(fontBold, BODY_FONT_SIZE, MARGIN + 20, "Se non vengono svolte le azioni passare a T" + tempo.getTNo());
            }

            // Aggiungi spazio tra gli elementi della timeline
            currentYPosition -= LEADING * 2;
        }
    }

    /**
     * Crea la sezione sceneggiatura nel PDF.
     *
     * @param scenario lo scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createSceneggiaturaSection(Scenario scenario) throws IOException {
        String sceneggiatura = ScenarioService.getSceneggiatura(scenario.getId());
        if (sceneggiatura == null || sceneggiatura.isEmpty()) {
            return;
        }

        // Verifica spazio disponibile e crea pagina se necessario
        checkForNewPage(LEADING * 5);

        // Titolo sezione
        drawSection("Sceneggiatura", "");
        drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, sceneggiatura);

        logger.info("Sceneggiatura section created");
    }

    /**
     * Disegna una sezione principale con titolo nel PDF.
     *
     * @param title   il titolo della sezione
     * @param content il contenuto della sezione
     * @throws IOException se si verifica un errore durante il disegno della sezione
     */
    private void drawSection(String title, String content) throws IOException {
        currentContentStream.setFont(fontBold, HEADER_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN, currentYPosition);
        currentContentStream.showText(title);
        currentContentStream.endText();
        currentYPosition -= LEADING * 1.5f;

        if (content != null && !content.isEmpty()) {
            // Gestione speciale per contenuti che potrebbero contenere HTML
            if (title.equals("Descrizione") || title.equals("Briefing") || title.equals("Patto d'Aula")) {
                // Analizziamo l'HTML per mantenere grassetto e corsivo
                renderHtmlWithFormatting(content);
            } else {
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 10, content);
            }
            currentYPosition -= LEADING;
        }
    }

    private void renderHtmlWithFormatting(String htmlContent) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
        String plainText = doc.text();

        // Estrai tutti i tag strong (grassetto) e (corsivo)
        Elements boldElements = doc.select("strong, b");
        Elements italicElements = doc.select("em, i");

        // Crea mappe per ricordare quali porzioni di testo sono formattate
        Map<Integer, Integer> boldRanges = new HashMap<>();
        Map<Integer, Integer> italicRanges = new HashMap<>();

        // Processa grassetto
        for (org.jsoup.nodes.Element element : boldElements) {
            String text = element.text();
            int start = plainText.indexOf(text);
            if (start >= 0) {
                boldRanges.put(start, start + text.length());
            }
        }

        // Processa corsivo
        for (Element element : italicElements) {
            String text = element.text();
            int start = plainText.indexOf(text);
            if (start >= 0) {
                italicRanges.put(start, start + text.length());
            }
        }

        // Dividi il testo in parole
        String[] words = plainText.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int charCount = 0;  // Tiene traccia della posizione dei caratteri

        currentContentStream.beginText();
        currentContentStream.newLineAtOffset((float) 60.0, currentYPosition);

        for (String word : words) {
            String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
            float width = fontRegular.getStringWidth(testLine) / 1000 * BODY_FONT_SIZE;

            if (width > (PDRectangle.A4.getWidth() - 2 * MARGIN - 10)) {
                // Disegna la linea corrente con la formattazione appropriata
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

        // Disegna l'ultima linea
        if (!currentLine.isEmpty()) {
            drawFormattedLine(currentLine.toString(), charCount - currentLine.length(), boldRanges, italicRanges);
        }
        currentContentStream.endText();
    }

    private void drawFormattedLine(String line, int startPos, Map<Integer, Integer> boldRanges,
                                   Map<Integer, Integer> italicRanges) throws IOException {
        // Suddividi la linea in parti in base alla formattazione
        StringBuilder currentPart = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            int globalPos = startPos + i;

            // Determina la formattazione per il carattere corrente
            boolean isBold = isBold(globalPos, boldRanges);
            boolean isItalic = isItalic(globalPos, italicRanges);

            // Aggiungi il carattere alla parte corrente
            currentPart.append(c);

            // Se il carattere successivo ha una formattazione diversa, o siamo alla fine
            if (i == line.length() - 1 ||
                    isBold(globalPos + 1, boldRanges) != isBold ||
                    isItalic(globalPos + 1, italicRanges) != isItalic) {

                // Scegli il font appropriato in base alla formattazione
                PDFont font = getPdFont(isBold, isItalic);

                // Disegna questa parte del testo
                currentContentStream.setFont(font, BODY_FONT_SIZE);
                font.getStringWidth(currentPart.toString());
                currentContentStream.showText(currentPart.toString());

                // Prepara per la prossima parte
                currentPart = new StringBuilder();
            }
        }
    }

    private PDFont getPdFont(boolean isBold, boolean isItalic) {
        PDFont font = fontRegular;
        if (isBold && isItalic) {
            // Se hai un font per grassetto+corsivo, usalo qui
            font = fontBold; // Usa grassetto come fallback
        } else if (isBold) {
            font = fontBold;
        } else if (isItalic) {
            font = fontItalic;
            // Se hai un font per corsivo, usalo qui
            // Per ora useremo il regular come fallback
        }
        return font;
    }

    private boolean isBold(int position, Map<Integer, Integer> boldRanges) {
        for (Map.Entry<Integer, Integer> range : boldRanges.entrySet()) {
            if (position >= range.getKey() && position < range.getValue()) {
                return true;
            }
        }
        return false;
    }

    private boolean isItalic(int position, Map<Integer, Integer> italicRanges) {
        for (Map.Entry<Integer, Integer> range : italicRanges.entrySet()) {
            if (position >= range.getKey() && position < range.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Disegna una sottosezione con titolo nel PDF.
     *
     * @param title il titolo della sottosezione
     * @throws IOException se si verifica un errore durante il disegno della sottosezione
     */
    private void drawSubsection(String title) throws IOException {
        currentContentStream.setFont(fontBold, BODY_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN + 10, currentYPosition);
        currentContentStream.showText(title);
        currentContentStream.endText();
        currentYPosition -= LEADING;

        drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, "");

    }

    /**
     * Disegna del testo avvolto in una posizione specificata.
     *
     * @param font     il font da utilizzare
     * @param fontSize la dimensione del font
     * @param x        la posizione X
     * @param text     il testo da disegnare
     * @throws IOException se si verifica un errore durante il disegno del testo
     */
    private void drawWrappedText(PDFont font, float fontSize, float x, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }

        String[] lines = text.split("\n");

        for (String line : lines) {
            // Controlla se abbiamo spazio per una nuova riga
            checkForNewPage(LEADING);

            // Imposta il font e la dimensione
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
                    // Controlla se abbiamo spazio per continuare sulla stessa pagina
                    checkForNewPage(LEADING);

                    // Imposta il font e la dimensione
                    currentContentStream.setFont(font, fontSize);

                    currentContentStream.beginText();
                    currentContentStream.newLineAtOffset(x, currentYPosition);
                    currentLine = new StringBuilder(word);
                } else {
                    if (!currentLine.isEmpty()) currentLine.append(" ");
                    currentLine.append(word);
                }
            }

            // Disegna l'ultima riga
            if (!currentLine.isEmpty()) {
                currentContentStream.showText(currentLine.toString());
            }
            currentContentStream.endText();
            currentYPosition -= LEADING;
        }
    }

    /**
     * Disegna del testo centrato in una posizione specificata.
     *
     * @param font     il font da utilizzare
     * @param fontSize la dimensione del font
     * @param text     il testo da disegnare
     * @throws IOException se si verifica un errore durante il disegno del testo
     */
    private void drawCenteredText(PDFont font, float fontSize, String text) throws IOException {
        float titleWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (PDRectangle.A4.getWidth() - titleWidth) / 2;

        currentContentStream.setFont(font, fontSize);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(x, currentYPosition);
        currentContentStream.showText(text);
        currentContentStream.endText();
        currentYPosition -= LEADING;
    }
}