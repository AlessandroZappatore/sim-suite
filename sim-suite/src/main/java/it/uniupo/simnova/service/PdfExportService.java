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
    private static final float MARGIN = 40;
    /**
     * Spaziatura tra le righe.
     */
    private static final float LEADING = 14;
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
    private static final String CENTER_LOGO_FILENAME = "center_logo.png"; // Nome file standard per il logo del centro
    private final FileStorageService fileStorageService;
    private final MaterialeService materialeService;
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
     * Font per il testo in grassetto e corsivo.
     */
    private PDFont fontBoldItalic;
    /**
     * Font per il testo normale.
     */
    private PDFont fontRegular;
    /**
     * Font per il testo in corsivo.
     */
    private PDFont fontItalic;
    /**
     * Font per il testo in grassetto di piccole dimensioni.
     */
    private PDImageXObject logo;
    /**
     * Logo del centro.
     */
    private PDImageXObject centerLogo;
    /**
     * Larghezza originale del logo del centro.
     */
    private float centerLogoWidth;
    /**
     * Altezza originale del logo del centro.
     */
    private float centerLogoHeight;

    /**
     * Costruttore del servizio PdfExportService.
     *
     * @param scenarioService il servizio per la gestione degli scenari
     */
    public PdfExportService(ScenarioService scenarioService, FileStorageService fileStorageService, MaterialeService materialeService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.materialeService = materialeService;
    }

    /**
     * Esporta uno scenario in formato PDF.
     *
     * @param scenarioId l'ID dello scenario da esportare
     * @return un array di byte contenente il PDF generato
     * @throws IOException se si verifica un errore durante la creazione del PDF
     */
    public byte[] exportScenarioToPdf(int scenarioId,
                                      boolean desc,
                                      boolean brief,
                                      boolean infoGen,
                                      boolean patto,
                                      boolean azioni,
                                      boolean obiettivi,
                                      boolean moula,
                                      boolean liqui,
                                      boolean matNec,
                                      boolean param,
                                      boolean acces,
                                      boolean fisic,
                                      boolean esam,
                                      boolean time,
                                      boolean scen) throws IOException {
        document = null;
        currentContentStream = null;
        pageNumber = 1;

        try {
            document = new PDDocument();

            // Carica i font
            fontRegular = loadFont(document, "/fonts/LiberationSans-Regular.ttf");
            fontBold = loadFont(document, "/fonts/LiberationSans-Bold.ttf");
            fontItalic = loadFont(document, "/fonts/LiberationSans-Italic.ttf");
            fontBoldItalic = loadFont(document, "/fonts/LiberationSans-BoldItalic.ttf");

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
            createScenarioDescription(scenario, desc, brief, infoGen, patto, azioni, obiettivi, moula, liqui, matNec);
            createPatientSection(scenarioId, param, acces, fisic);
            createExamsSection(scenarioId, esam);

            // Controlla il tipo di scenario e crea le sezioni appropriate
            String scenarioType = scenarioService.getScenarioType(scenarioId);
            if (scenarioType != null && (scenarioType.equals("Advanced Scenario") ||
                    scenarioType.equals("Patient Simulated Scenario")) && time) {
                createTimelineSection(scenario);
                logger.info("Timeline creata");
            }

            // Crea la sezione della sceneggiatura
            if (scenarioType != null && scenarioType.equals("Patient Simulated Scenario")) {
                createSceneggiaturaSection(scenario, scen);
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
            // Dimensioni ridotte per il logo SimSuite
            float simLogoWidth = 40; // Ridotto da 60
            float simLogoHeight = 40; // Ridotto da 60

            // Dimensioni più grandi per il logo del centro
            float centerLogoMaxWidth = 120; // Aumentato (dimensione massima)
            float centerLogoMaxHeight = 80; // Aumentato (dimensione massima)

            float centerLogoWidth;
            float centerLogoHeight = 0;

            // Calcola la posizione iniziale per il logo SimSuite (spostato a sinistra)
            float simLogoY = PDRectangle.A4.getHeight() - MARGIN - simLogoHeight;

            // Disegna il logo SimSuite se disponibile
            if (logo != null) {
                currentContentStream.drawImage(logo, MARGIN, simLogoY, simLogoWidth, simLogoHeight);
            }

            // Calcola le dimensioni per il logo del centro mantenendo le proporzioni
            if (centerLogo != null) {
                // Calcola il fattore di scala per mantenere le proporzioni
                float scale = Math.min(
                        centerLogoMaxWidth / this.centerLogoWidth,
                        centerLogoMaxHeight / this.centerLogoHeight
                );

                // Applica la scala mantenendo le proporzioni
                centerLogoWidth = this.centerLogoWidth * scale;
                centerLogoHeight = this.centerLogoHeight * scale;

                // Calcola la posizione per centrare il logo del centro nella pagina
                float centerLogoX = (PDRectangle.A4.getWidth() - centerLogoWidth) / 2;
                float centerLogoY = PDRectangle.A4.getHeight() - MARGIN - centerLogoHeight;

                // Disegna il logo del centro
                currentContentStream.drawImage(centerLogo, centerLogoX, centerLogoY, centerLogoWidth, centerLogoHeight);
            }

            // Aggiorna la posizione Y corrente
            float lowestY = simLogoY;
            if (centerLogo != null) {
                lowestY = Math.min(lowestY, PDRectangle.A4.getHeight() - MARGIN - centerLogoHeight);
            }
            currentYPosition = lowestY - LEADING;
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
        currentContentStream.showText("Autori: " + scenario.getAutori());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Target: " + scenario.getTarget());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Tipologia: " + scenario.getTipologia());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Paziente: " + scenario.getNomePaziente());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Patologia: " + scenario.getPatologia());
        currentContentStream.newLineAtOffset(0, -LEADING);
        currentContentStream.showText("Durata: " + scenario.getTimerGenerale() + " minuti");
        currentContentStream.endText();
        currentYPosition -= LEADING * 7;

        logger.info("Header creato");
    }

    private void createScenarioDescription(Scenario scenario,
                                           boolean desc,
                                           boolean brief,
                                           boolean infoGen,
                                           boolean patto,
                                           boolean azioni,
                                           boolean obiettivi,
                                           boolean moula,
                                           boolean liqui,
                                           boolean matNec) throws IOException {
        // Descrizione
        if (scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty() && desc) {
            drawSection("Descrizione", scenario.getDescrizione());
        }

        // Briefing
        if (scenario.getBriefing() != null && !scenario.getBriefing().isEmpty() && brief) {
            drawSection("Briefing", scenario.getBriefing());
        }

        if (scenarioService.isPediatric(scenario.getId()) && scenario.getInfoGenitore() != null && !scenario.getInfoGenitore().isEmpty() && infoGen) {
            drawSection("Informazioni dai genitori", scenario.getInfoGenitore());
        }

        // Patto d'aula
        if (scenario.getPattoAula() != null && !scenario.getPattoAula().isEmpty() && patto) {
            drawSection("Patto d'Aula", scenario.getPattoAula());
        }

        // Azioni chiave
        if (scenario.getAzioneChiave() != null && !scenario.getAzioneChiave().isEmpty() && azioni) {
            drawSection("Azioni Chiave", scenario.getAzioneChiave());
        }

        // Obiettivi didattici
        if (scenario.getObiettivo() != null && !scenario.getObiettivo().isEmpty() && obiettivi) {
            drawSection("Obiettivi Didattici", scenario.getObiettivo());
        }

        // Moulage
        if (scenario.getMoulage() != null && !scenario.getMoulage().isEmpty() && moula) {
            drawSection("Moulage", scenario.getMoulage());
        }

        // Liquidi
        if (scenario.getLiquidi() != null && !scenario.getLiquidi().isEmpty() && liqui) {
            drawSection("Liquidi e dosi farmaci", scenario.getLiquidi());
        }

        //Materiale necessario
        if (materialeService.toStringAllMaterialsByScenarioId(scenario.getId()) != null && !materialeService.toStringAllMaterialsByScenarioId(scenario.getId()).isEmpty() && matNec) {
            drawSection("Materiale necessario", materialeService.toStringAllMaterialsByScenarioId(scenario.getId()));
        }

    }

    /**
     * Crea la sezione dello stato del paziente nel PDF.
     *
     * @param scenarioId l'ID dello scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createPatientSection(Integer scenarioId,
                                      boolean param,
                                      boolean acces,
                                      boolean fisic) throws IOException {
        if(!param && !acces && !fisic) {
            return; // Se tutte le sezioni sono false, non fare nulla
        }

        // Stima lo spazio solo per il titolo della sezione principale
        checkForNewPage(LEADING * 3); // Spazio stimato per drawSection
        // Disegna il titolo della sezione principale
        drawSection("Stato Paziente", "");

        // Recupera i dati del paziente al tempo T0
        PazienteT0 paziente = scenarioService.getPazienteT0ById(scenarioId);
        if (paziente != null) {
            // --- Parametri Vitali (Riga per Riga) ---
            // Controlla lo spazio prima di disegnare il sottotitolo
            if (param) {
                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Parametri Vitali");

                // Stampa PA
                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, String.format("PA: %s mmHg", paziente.getPA()));

                // Stampa FC
                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, String.format("FC: %d bpm", paziente.getFC()));

                // Stampa RR
                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, String.format("RR: %d atti/min", paziente.getRR()));

                // Stampa Temperatura
                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, String.format("Temperatura: %.1f °C", paziente.getT()));

                // Stampa SpO2
                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, String.format("SpO2: %d%%", paziente.getSpO2()));

                // Stampa FiO2 (condizionale)
                // Assumendo che getFiO2() ritorni un valore numerico (es. Float/double/int)
                if (paziente.getFiO2() > 0) {
                    checkForNewPage(LEADING * 2); // Spazio per una riga
                    // Formattare come percentuale se è una frazione, o direttamente se è già percentuale
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, String.format("FiO2: %d%%", paziente.getFiO2())); // Esempio: formatta come intero %
                }

                // Stampa LitriO2 (condizionale)
                // Assumendo che getLitriO2() ritorni un valore numerico (es. Float/double/int)
                if (paziente.getLitriO2() > 0) {
                    checkForNewPage(LEADING * 2); // Spazio per una riga
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, String.format("Litri O2: %.1f L/min", paziente.getLitriO2())); // Esempio: formatta con un decimale L/min
                }

                // Stampa EtCO2
                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, String.format("EtCO2: %d mmHg", paziente.getEtCO2()));

                // Stampa Monitor
                if (paziente.getMonitor() != null && !paziente.getMonitor().isEmpty()) {
                    checkForNewPage(LEADING * 2); // Spazio per una riga
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20, String.format("Monitor: %s", paziente.getMonitor()));
                }
                currentYPosition -= LEADING; // Aggiungi un piccolo spazio dopo il blocco dei parametri vitali
            }
            // --- Accessi Venosi ---
            List<Accesso> accessiVenosi = paziente.getAccessiVenosi();
            if (accessiVenosi != null && !accessiVenosi.isEmpty() && acces) {
                // Controlla lo spazio prima di disegnare il sottotitolo
                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Accessi Venosi");
                // Itera sugli accessi venosi
                for (Accesso accesso : accessiVenosi) {
                    // Controlla lo spazio prima di disegnare ogni riga di accesso
                    checkForNewPage(LEADING * 2); // Spazio stimato per una riga
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20,
                            "• " + accesso.getTipologia() + " - " + accesso.getPosizione() + " (" + accesso.getLato() + ") - " + accesso.getMisura() + "G");
                }
                currentYPosition -= LEADING; // Aggiungi spazio dopo la sezione
            }

            // --- Accessi Arteriosi ---
            List<Accesso> accessiArteriosi = paziente.getAccessiArteriosi();
            if (accessiArteriosi != null && !accessiArteriosi.isEmpty() && acces) {
                // Controlla lo spazio prima di disegnare il sottotitolo
                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Accessi Arteriosi");
                // Itera sugli accessi arteriosi
                for (Accesso accesso : accessiArteriosi) {
                    // Controlla lo spazio prima di disegnare ogni riga di accesso
                    checkForNewPage(LEADING * 2); // Spazio stimato per una riga
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 20,
                            "• " + accesso.getTipologia() + " - " + accesso.getPosizione() + " (" + accesso.getLato() + ") - " + accesso.getMisura() + "G");
                }
                currentYPosition -= LEADING; // Aggiungi spazio dopo la sezione
            }
        }

        // --- Esame Fisico ---
        EsameFisico esame = scenarioService.getEsameFisicoById(scenarioId);
        if (esame != null && esame.getSections() != null && !esame.getSections().isEmpty() && fisic) {
            // Controlla lo spazio prima di disegnare il sottotitolo
            checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
            drawSubsection("Esame Fisico");

            Map<String, String> sections = esame.getSections();
            // Itera sulle sezioni dell'esame fisico (es. Cute, Torace, etc.)
            for (Map.Entry<String, String> entry : sections.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                // Disegna solo se la chiave e il valore sono validi e il valore non è vuoto/blank
                if (key != null && !key.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                    checkForNewPage(LEADING * 4);

                    // Disegna la chiave (es. "Cute:") in grassetto
                    drawWrappedText(fontBold, BODY_FONT_SIZE, MARGIN + 20, key + ":");

                    // Usa un offset maggiore (40px in più rispetto al testo della chiave)
                    renderHtmlWithFormatting(value, MARGIN + 40);

                    currentYPosition -= LEADING; // Aggiungi piccolo spazio tra le entry
                }
            }
            currentYPosition -= LEADING; // Aggiungi spazio dopo la sezione
        }

        logger.info("Patient section creata con page break granulare e vitali riga per riga.");
    }

    /**
     * Crea la sezione degli esami e referti nel PDF.
     *
     * @param scenarioId l'ID dello scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createExamsSection(Integer scenarioId,
                                    boolean esam) throws IOException {
        List<EsameReferto> esami = scenarioService.getEsamiRefertiByScenarioId(scenarioId);
        if (esami == null || esami.isEmpty() || !esam) {
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
        return replaceSubscriptCharacters(examType);
    }

    /**
     * Crea la sezione timeline nel PDF.
     *
     * @param scenario lo scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createTimelineSection(Scenario scenario) throws IOException {
        // Recupera i tempi associati allo scenario
        List<Tempo> tempi = scenarioService.getTempiByScenarioId(scenario.getId());
        if (tempi.isEmpty()) {
            // Se non ci sono tempi, non fare nulla
            return;
        }

        // --- Inizio: Controllo Spazio Iniziale ---
        // Stima lo spazio minimo per il titolo della sezione E il titolo del primo tempo
        float spazioTitoloSezione = LEADING * 3; // Spazio stimato per drawSection("Timeline", "")
        float spazioTitoloPrimoTempo = LEADING * 3; // Spazio stimato per drawSubsection del primo tempo
        checkForNewPage(spazioTitoloSezione + spazioTitoloPrimoTempo);
        // --- Fine: Controllo Spazio Iniziale ---

        // Disegna il titolo della sezione principale "Timeline"
        drawSection("Timeline", "");

        // Itera su ogni tempo nella lista
        for (int i = 0; i < tempi.size(); i++) {
            Tempo tempo = tempi.get(i);

            // --- Inizio: Disegno Contenuto del Tempo (Riga per Riga) ---

            // Formatta il titolo per questo tempo
            String title = String.format("Tempo %d (%.1f min)",
                    tempo.getIdTempo(),
                    tempo.getTimerTempo() / 60.0 // Converte i secondi in minuti
            );

            // Controlla spazio e disegna il sottotitolo per questo tempo
            checkForNewPage(LEADING * 3); // Spazio per sottotitolo
            drawSubsection(title);

            // --- Parametri Vitali (Riga per Riga) ---
            float paramsIndent = MARGIN + 20; // Indentazione per i parametri

            // Stampa PA
            checkForNewPage(LEADING * 2);
            drawWrappedText(fontRegular, BODY_FONT_SIZE, paramsIndent, String.format("PA: %s mmHg", tempo.getPA()));

            // Stampa FC
            checkForNewPage(LEADING * 2);
            drawWrappedText(fontRegular, BODY_FONT_SIZE, paramsIndent, String.format("FC: %d bpm", tempo.getFC()));

            // Stampa RR
            checkForNewPage(LEADING * 2);
            drawWrappedText(fontRegular, BODY_FONT_SIZE, paramsIndent, String.format("RR: %d atti/min", tempo.getRR()));

            // Stampa Temperatura
            checkForNewPage(LEADING * 2);
            drawWrappedText(fontRegular, BODY_FONT_SIZE, paramsIndent, String.format("Temperatura: %.1f °C", tempo.getT()));

            // Stampa SpO2
            checkForNewPage(LEADING * 2);
            drawWrappedText(fontRegular, BODY_FONT_SIZE, paramsIndent, String.format("SpO2: %d%%", tempo.getSpO2()));

            // Stampa FiO2 (condizionale)
            // Assumendo che getFiO2() ritorni un tipo numerico (Integer, Float, etc.) o null
            Number fio2 = tempo.getFiO2(); // Usa Number per gestire diversi tipi numerici
            if (fio2 != null && fio2.doubleValue() > 0) {
                checkForNewPage(LEADING * 2);
                drawWrappedText(fontRegular, BODY_FONT_SIZE, paramsIndent, String.format("FiO2: %.0f%%", fio2.doubleValue())); // Formatta come intero %
            }

            // Stampa LitriO2 (condizionale)
            // Assumendo che getLitriO2() ritorni un tipo numerico o null
            Number litriO2 = tempo.getLitriO2();
            if (litriO2 != null && litriO2.doubleValue() > 0) {
                checkForNewPage(LEADING * 2);
                drawWrappedText(fontRegular, BODY_FONT_SIZE, paramsIndent, String.format("Litri O2: %.1f L/min", litriO2.doubleValue())); // Formatta con un decimale L/min
            }

            // Stampa EtCO2
            checkForNewPage(LEADING * 2);
            drawWrappedText(fontRegular, BODY_FONT_SIZE, paramsIndent, String.format("EtCO2: %d mmHg", tempo.getEtCO2()));

            // --- Parametri Aggiuntivi ---
            List<ParametroAggiuntivo> parametriAggiuntivo = ScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenario.getId());
            if (!parametriAggiuntivo.isEmpty()) {
                for (ParametroAggiuntivo parametro : parametriAggiuntivo) {
                    checkForNewPage(LEADING * 2); // Spazio per ogni parametro aggiuntivo
                    String parametroNome = replaceSubscriptCharacters(parametro.getNome());
                    String parametroUnita = replaceSubscriptCharacters(parametro.getUnitaMisura());
                    drawWrappedText(fontRegular, BODY_FONT_SIZE, paramsIndent,
                            String.format("%s: %s %s", parametroNome, parametro.getValore(), parametroUnita));
                }
            }
            currentYPosition -= LEADING; // Piccolo spazio dopo i parametri

            // --- Dettagli Aggiuntivi ---
            float detailsLabelIndent = MARGIN + 20;
            float detailsTextIndent = MARGIN + 30;

            if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                checkForNewPage(LEADING * 3); // Spazio per etichetta "Dettagli:"
                drawWrappedText(fontBold, BODY_FONT_SIZE, detailsLabelIndent, "Dettagli:");
                checkForNewPage(LEADING * 2); // Spazio minimo per il testo (drawWrappedText gestirà il resto)
                drawWrappedText(fontRegular, BODY_FONT_SIZE, detailsTextIndent, tempo.getAltriDettagli());
                currentYPosition -= LEADING / 2; // Piccolo spazio dopo i dettagli
            }

            // --- Dettagli Pediatrici (Condizionale) ---
            if (scenarioService.isPediatric(scenario.getId()) && tempo.getRuoloGenitore() != null && !tempo.getRuoloGenitore().isEmpty()) {
                checkForNewPage(LEADING * 3); // Spazio per etichetta "Ruolo del genitore:"
                drawWrappedText(fontBold, BODY_FONT_SIZE, detailsLabelIndent, "Ruolo del genitore:");
                checkForNewPage(LEADING * 2); // Spazio minimo per il testo
                drawWrappedText(fontRegular, BODY_FONT_SIZE, detailsTextIndent, tempo.getRuoloGenitore());
                currentYPosition -= LEADING / 2; // Piccolo spazio dopo i dettagli pediatrici
            }

            // --- Azioni ---
            String azione = tempo.getAzione();
            if (azione != null && !azione.isEmpty()) {
                checkForNewPage(LEADING * 3); // Spazio per etichetta Azioni
                drawWrappedText(fontBold, BODY_FONT_SIZE, detailsLabelIndent, "Azioni da svolgere per passare a → T" + tempo.getTSi() + ":");
                checkForNewPage(LEADING * 2); // Spazio minimo per il testo delle azioni
                drawWrappedText(fontRegular, BODY_FONT_SIZE, detailsTextIndent, azione);
                currentYPosition -= LEADING / 2; // Piccolo spazio dopo le azioni
            }

            // --- Condizione TNo ---
            if (tempo.getTNo() >= 0) {
                checkForNewPage(LEADING * 3); // Spazio per etichetta TNo
                drawWrappedText(fontBold, BODY_FONT_SIZE, detailsLabelIndent, "Se non vengono svolte le azioni passare a → T" + tempo.getTNo());
                currentYPosition -= LEADING / 2; // Piccolo spazio dopo TNo
            }
            // --- Fine: Disegno Contenuto del Tempo ---

            // --- Inizio: Aggiunta Spazio Verticale tra Tempi ---
            // Aggiungi uno spazio verticale dopo aver disegnato l'elemento Tempo,
            // tranne che per l'ultimo elemento.
            if (i < tempi.size() - 1) {
                checkForNewPage(LEADING * 2); // Controlla se c'è spazio per il padding
                currentYPosition -= LEADING; // Spazio tra gli elementi della timeline
            }
            // --- Fine: Aggiunta Spazio Verticale ---
        }
    }

    /**
     * Crea la sezione sceneggiatura nel PDF.
     *
     * @param scenario lo scenario
     * @throws IOException se si verifica un errore durante la creazione della sezione
     */
    private void createSceneggiaturaSection(Scenario scenario, boolean scen) throws IOException {
        String sceneggiatura = ScenarioService.getSceneggiatura(scenario.getId());
        if (sceneggiatura == null || sceneggiatura.isEmpty() || !scen) {
            return;
        }

        // Verifica spazio disponibile e crea pagina se necessario
        checkForNewPage(LEADING * 5);

        // Titolo sezione
        drawSection("Sceneggiatura", "");
        renderHtmlWithFormatting(sceneggiatura, MARGIN + 20);
        currentYPosition -= LEADING;

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
            if (title.equals("Descrizione")
                    || title.equals("Briefing")
                    || title.equals("Informazioni dai genitori")
                    || title.equals("Patto d'Aula")
                    || title.equals("Obiettivi Didattici")
                    || title.equals("Moulage")
                    || title.equals("Liquidi e dosi farmaci")
            ) {
                // Analizziamo l'HTML per mantenere grassetto e corsivo
                renderHtmlWithFormatting(content, MARGIN + 20);
                currentYPosition -= LEADING / 2;
            } else {
                drawWrappedText(fontRegular, BODY_FONT_SIZE, MARGIN + 10, content);
            }
            currentYPosition -= LEADING;
        }
    }

    private void renderHtmlWithFormatting(String htmlContent, float xOffset) throws IOException {
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
        int charCount = 0;

        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(xOffset, currentYPosition);

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
            font = fontBoldItalic;
        } else if (isBold) {
            font = fontBold;
        } else if (isItalic) {
            font = fontItalic;
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

    private static String replaceSubscriptCharacters(String text) {
        if (text == null) return null;

        return text.replace('₁', '1')
                .replace('₂', '2')
                .replace('₃', '3')
                .replace('₄', '4')
                .replace('₅', '5')
                .replace('₆', '6')
                .replace('₇', '7')
                .replace('₈', '8')
                .replace('₉', '9')
                .replace('₀', '0')
                .replace('⁰', '0')
                .replace('¹', '1')
                .replace('²', '2')
                .replace('³', '3')
                .replace('⁴', '4')
                .replace('⁵', '5')
                .replace('⁶', '6')
                .replace('⁷', '7')
                .replace('⁸', '8')
                .replace('⁹', '9')
                .replace('⁻', '-')
                .replace('⁺', '+');
    }
}