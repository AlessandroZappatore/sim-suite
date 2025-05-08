package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox; // Import Checkbox
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
// Import necessari per il nuovo AccessoComponent
import it.uniupo.simnova.api.model.Accesso;
import it.uniupo.simnova.api.model.EsameFisico;
import it.uniupo.simnova.api.model.PazienteT0;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import java.util.*;
// Import Optional


/**
 * Classe per la visualizzazione e modifica di uno scenario esistente.
 * <p>
 * Questa classe estende Composite e implementa HasUrlParameter per gestire i parametri dell'URL.
 *
 * @author Alessandro Zappatore
 * @version 1.1 // Versione incrementata
 */
@PageTitle("Modifica Scenario")
@Route(value = "modificaScenario", layout = MainLayout.class)
public class ScenarioEditView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * ID dello scenario da modificare.
     */
    private Integer scenarioId;
    /**
     * Scenario da modificare.
     */
    private Scenario scenario;
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioEditView.class);

    // --- Variabili d'istanza per la gestione accessi (come in PazienteT0View ma non statiche) ---
    private VerticalLayout venosiContainer;
    private VerticalLayout arteriosiContainer;
    private List<AccessoComponent> venosiAccessi = new ArrayList<>();
    private List<AccessoComponent> arteriosiAccessi = new ArrayList<>();
    private Button addVenosiButton;
    private Button addArteriosiButton;
    // --- Fine variabili gestione accessi ---

    TextField editAuthorField;
    TextField scenarioTitle;
    TextField patientName;
    TextField pathology;
    ComboBox<Integer> durationField;
    ComboBox<String> scenarioTypeSelect;
    VerticalLayout descriptionArea = new VerticalLayout();
    VerticalLayout briefingArea = new VerticalLayout();
    VerticalLayout pattoAulaArea = new VerticalLayout();
    VerticalLayout obiettiviArea = new VerticalLayout();
    VerticalLayout moulageArea = new VerticalLayout();
    VerticalLayout liquidiArea = new VerticalLayout();
    VerticalLayout generaleArea = new VerticalLayout();
    VerticalLayout pupilleArea = new VerticalLayout();
    VerticalLayout colloArea = new VerticalLayout();
    VerticalLayout toraceArea = new VerticalLayout();
    VerticalLayout cuoreArea = new VerticalLayout();
    VerticalLayout addomeArea = new VerticalLayout();
    VerticalLayout rettoArea = new VerticalLayout();
    VerticalLayout cuteArea = new VerticalLayout();
    VerticalLayout estremitaArea = new VerticalLayout();
    VerticalLayout neurologicoArea = new VerticalLayout();
    VerticalLayout FASTArea = new VerticalLayout();
    VerticalLayout azioniChiaveArea = new VerticalLayout();
    VerticalLayout InfoGenitoriArea = new VerticalLayout();
    TextArea monitorArea = new TextArea();
    TextField paField;
    TextField fcField;
    TextField rrField;
    TextField tempField;
    TextField spo2Field;
    TextField fio2Field;
    TextField litriO2Field;
    TextField etco2Field;
    TextField presidiSelect;


    /**
     * Costruttore della classe ScenarioEditView.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public ScenarioEditView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    /**
     * Metodo per impostare il parametro dell'URL.
     *
     * @param event     evento di navigazione
     * @param parameter parametro dell'URL
     */
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException("ID Scenario mancante");
            }
            this.scenarioId = Integer.parseInt(parameter);
            // Controlla se lo scenario esiste
            if (scenarioId <= 0) {
                throw new NumberFormatException("ID Scenario deve essere positivo");
            }
            scenario = scenarioService.getScenarioById(scenarioId);
            // Resetta le liste accessi a ogni caricamento parametro
            venosiAccessi = new ArrayList<>();
            arteriosiAccessi = new ArrayList<>();
            buildView();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido o scenario non esistente: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido: " + parameter);
        } catch (NotFoundException e) {
            logger.error(e.getMessage());
            event.rerouteToError(NotFoundException.class, e.getMessage());
        }
    }


    /**
     * Costruisce la vista per la modifica dello scenario.
     */
    private void buildView() {
        VerticalLayout mainLayout = getContent();
        mainLayout.removeAll();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);


        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");
        backButton.addClickListener(e -> UI.getCurrent().navigate("scenari/" + scenarioId));

        H2 pageTitle = new H2("Modifica Scenario");
        pageTitle.getStyle().set("margin", "0 auto").set("padding", "0 1rem");
        HorizontalLayout customHeader = new HorizontalLayout(backButton, pageTitle);
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.expand(pageTitle);

        // Layout principale per il contenuto
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("800px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        // Recupera il tipo di scenario
        String scenarioType = scenarioService.getScenarioType(scenarioId);
        TextField scenarioTypeField = createTextField("TIPO SCENARIO", "Inserisci il tipo di scenario");
        scenarioTypeField.setValue(scenarioType);
        scenarioTypeField.setReadOnly(true);

        editAuthorField = createTextField("AUTORE", "Inserisci il tuo nome");
        // editAuthorField.setValue(scenario.getAutore()); // Assumendo ci sia un getter
        editAuthorField.setRequired(true); // Mantenuto required, ma potrebbe essere pre-popolato

        // Titolo scenario
        scenarioTitle = createTextField("TITOLO SCENARIO", "Inserisci il titolo dello scenario");
        scenarioTitle.setValue(scenario.getTitolo());

        // Nome paziente
        patientName = createTextField("NOME PAZIENTE", "Inserisci il nome del paziente");
        patientName.setValue(scenario.getNomePaziente());

        // Patologia
        pathology = createTextField("PATOLOGIA/MALATTIA", "Inserisci la patologia");
        pathology.setValue(scenario.getPatologia());

        // Campo durata timer
        durationField = new ComboBox<>("DURATA SIMULAZIONE (minuti)");
        durationField.setItems(5, 10, 15, 20, 25, 30);
        // Imposta il valore esistente, con fallback a 10 se non valido o zero
        durationField.setValue(scenario.getTimerGenerale() > 0 ? (int) scenario.getTimerGenerale() : 10);
        durationField.setWidthFull();
        durationField.addClassName(LumoUtility.Margin.Top.LARGE);
        durationField.getStyle().set("max-width", "500px");

        scenarioTypeSelect = new ComboBox<>("TIPO SCENARIO");
        scenarioTypeSelect.setItems("Adulto", "Pediatrico", "Neonatale", "Prematuro");
        scenarioTypeSelect.setValue(scenario.getTipologia());
        scenarioTypeSelect.setWidthFull();
        scenarioTypeSelect.addClassName(LumoUtility.Margin.Top.LARGE);
        scenarioTypeSelect.getStyle().set("max-width", "500px");

        // Layout per le informazioni generali
        VerticalLayout informazioniGeneraliLayout = new VerticalLayout();
        informazioniGeneraliLayout.setWidth("100%");
        informazioniGeneraliLayout.setMaxWidth("800px");
        informazioniGeneraliLayout.setPadding(true);
        informazioniGeneraliLayout.setSpacing(false);
        informazioniGeneraliLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        informazioniGeneraliLayout.getStyle().set("margin", "0 auto").set("flex-grow", "1");


        // Descrizione scenario
        descriptionArea = createTinyMcePanel(
                "DESCRIZIONE SCENARIO",
                "Questa descrizione fornirà il contesto generale e le informazioni di background ai partecipanti.",
                scenario.getDescrizione()
        );

        // Briefing scenario
        briefingArea = createTinyMcePanel(
                "BRIEFING",
                "Inserisci il testo da leggere ai discenti prima della simulazione...",
                scenario.getBriefing()
        );

        InfoGenitoriArea = new VerticalLayout();
        if (scenarioTypeSelect.getValue() != null && scenarioTypeSelect.getValue().equals("Pediatrico")) {
            InfoGenitoriArea = createTinyMcePanel(
                    "INFORMAZIONI DEI GENITORI",
                    "Inserisci le informazioni fornite dai genitori...",
                    scenario.getInfoGenitore()
            );
        }

        // Patto d'aula scenario
        pattoAulaArea = createTinyMcePanel(
                "PATTO D'AULA / FAMILIARIZZAZIONE",
                "Inserisci il testo da mostrare nella sala...",
                scenario.getPattoAula()
        );

        // Azioni chiave scenario
        azioniChiaveArea = createAzioniChiavePanel(
                scenario.getAzione_chiave_raw()
        );

        // Obiettivi didattici scenario
        obiettiviArea = createTinyMcePanel(
                "OBIETTIVI DIDATTICI",
                "Inserisci gli obiettivi didattici dello scenario...",
                scenario.getObiettivo()
        );

        // Moulage scenario
        moulageArea = createTinyMcePanel(
                "MOULAGE",
                "Descrivi il trucco da applicare al manichino/paziente simulato...",
                scenario.getMoulage()
        );

        // Liquidi e presidi scenario
        liquidiArea = createTinyMcePanel(
                "LIQUIDI E FARMACI IN T0",
                "Indica quantità di liquidi e farmaci presenti all'inizio della simulazione...",
                scenario.getLiquidi()
        );

        // Aggiunta dei campi al layout delle informazioni generali
        informazioniGeneraliLayout.add(
                descriptionArea,
                briefingArea
        );

        if (scenarioTypeSelect.getValue() != null && scenarioTypeSelect.getValue().equals("Pediatrico")) {
            informazioniGeneraliLayout.add(InfoGenitoriArea);
        }
        informazioniGeneraliLayout.add(
                pattoAulaArea,
                azioniChiaveArea,
                obiettiviArea,
                createMaterialeNecessarioComponent(),
                moulageArea,
                liquidiArea
        );

        // Layout per i parametri del paziente T0
        VerticalLayout parametriT0Layout = new VerticalLayout();
        parametriT0Layout.setPadding(false); // Rimuovi padding se vuoi allineare con le checkbox
        parametriT0Layout.setSpacing(true);
        parametriT0Layout.setWidthFull();

        // Recupera i dati del paziente T0 (Optional per sicurezza)
        PazienteT0 parametriT0 = scenarioService.getPazienteT0ById(scenarioId);

        // Crea i componenti per visualizzare i dati
        paField = new TextField("PA (mmHg)");
        paField.setValue(parametriT0 != null ? Objects.requireNonNullElse(parametriT0.getPA(), "") : "");
        paField.setWidthFull();

        fcField = new TextField("FC (bpm)"); // TextField perché i NumberField di PazienteT0View non sono qui
        fcField.setValue(parametriT0 != null && parametriT0.getFC() != 0 ? String.valueOf(parametriT0.getFC()) : "");
        fcField.setWidthFull();

        rrField = new TextField("RR (atti/min)");
        rrField.setValue(parametriT0 != null && parametriT0.getRR() != 0 ? String.valueOf(parametriT0.getRR()) : "");
        rrField.setWidthFull();

        tempField = new TextField("Temperatura (°C)");
        tempField.setValue(parametriT0 != null ? String.valueOf(parametriT0.getT()) : ""); // Controllo Null per Double
        tempField.setWidthFull();

        spo2Field = new TextField("SpO₂ (%)");
        spo2Field.setValue(parametriT0 != null && parametriT0.getSpO2() != 0 ? String.valueOf(parametriT0.getSpO2()) : "");
        spo2Field.setWidthFull();

        fio2Field = new TextField("FiO₂ (%)");
        fio2Field.setValue(parametriT0 != null && parametriT0.getFiO2() != 0 ? String.valueOf(parametriT0.getFiO2()) : "");
        fio2Field.setWidthFull();

        litriO2Field = new TextField("Litri O₂");
        litriO2Field.setValue(parametriT0 != null && parametriT0.getLitriO2() != 0 ? String.valueOf(parametriT0.getLitriO2()) : "");
        litriO2Field.setWidthFull();

        etco2Field = new TextField("EtCO₂ (mmHg)");
        etco2Field.setValue(parametriT0 != null && parametriT0.getEtCO2() != 0 ? String.valueOf(parametriT0.getEtCO2()) : "");
        etco2Field.setWidthFull();

        // Area di testo per il monitoraggio
        monitorArea = new TextArea("Monitoraggio");
        monitorArea.setValue(parametriT0 != null ? Objects.requireNonNullElse(parametriT0.getMonitor(), "") : "");
        monitorArea.setWidthFull();
        monitorArea.setMinHeight("100px");
        monitorArea.addClassName(LumoUtility.Margin.Top.LARGE); // Aggiunto margine

        // === INIZIO GESTIONE ACCESSI NUOVA ===

        // Container per accessi venosi
        venosiContainer = new VerticalLayout();
        venosiContainer.setWidthFull();
        venosiContainer.setSpacing(true);
        venosiContainer.setPadding(false);
        venosiContainer.setVisible(false); // Inizialmente nascosto

        // Container per accessi arteriosi
        arteriosiContainer = new VerticalLayout();
        arteriosiContainer.setWidthFull();
        arteriosiContainer.setSpacing(true);
        arteriosiContainer.setPadding(false);
        arteriosiContainer.setVisible(false); // Inizialmente nascosto

        // Pulsanti Aggiungi
        addVenosiButton = new Button("Aggiungi accesso venoso", new Icon(VaadinIcon.PLUS));
        addVenosiButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addVenosiButton.addClassName(LumoUtility.Margin.Top.SMALL);
        addVenosiButton.setVisible(false); // Inizialmente nascosto
        addVenosiButton.addClickListener(e -> addAccesso("Venoso"));

        addArteriosiButton = new Button("Aggiungi accesso arterioso", new Icon(VaadinIcon.PLUS));
        addArteriosiButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addArteriosiButton.addClassName(LumoUtility.Margin.Top.SMALL);
        addArteriosiButton.setVisible(false); // Inizialmente nascosto
        addArteriosiButton.addClickListener(e -> addAccesso("Arterioso"));

        // Checkbox per mostrare/nascondere
        Checkbox venosiCheckbox = new Checkbox("Accessi venosi");
        venosiCheckbox.addClassName(LumoUtility.Margin.Top.MEDIUM);
        venosiCheckbox.addValueChangeListener(e -> {
            boolean isVisible = e.getValue();
            venosiContainer.setVisible(isVisible);
            addVenosiButton.setVisible(isVisible);
            // Non rimuoviamo gli elementi quando si deseleziona in modifica
        });

        Checkbox arteriosiCheckbox = new Checkbox("Accessi arteriosi");
        arteriosiCheckbox.addClassName(LumoUtility.Margin.Top.MEDIUM);
        arteriosiCheckbox.addValueChangeListener(e -> {
            boolean isVisible = e.getValue();
            arteriosiContainer.setVisible(isVisible);
            addArteriosiButton.setVisible(isVisible);
            // Non rimuoviamo gli elementi quando si deseleziona in modifica
        });

        // Presidi
        ComboBox<String> presidiSelect;
        presidiSelect = new ComboBox<>("Presidi");
        presidiSelect.setItems("Nessuno", "Catetere vescicale", "Sonda nasogastrica", "Altro");
        presidiSelect.setPlaceholder("Seleziona un presidio");
        presidiSelect.setWidthFull();


        // Aggiunta dei campi e della nuova gestione accessi al layout parametriT0Layout
        parametriT0Layout.add(
                paField, fcField, rrField, tempField, spo2Field, fio2Field, litriO2Field, etco2Field, monitorArea, // Campi esistenti + Fio2/Litri
                venosiCheckbox, venosiContainer, addVenosiButton,        // Nuova gestione venosi
                arteriosiCheckbox, arteriosiContainer, addArteriosiButton,
                presidiSelect// Nuova gestione arteriosi
        );

        // Carica accessi esistenti usando la nuova logica
        boolean venosiPresenti = false;
        if (parametriT0 != null && parametriT0.getAccessiVenosi() != null && !parametriT0.getAccessiVenosi().isEmpty()) {
            for (var accesso : parametriT0.getAccessiVenosi()) {
                AccessoComponent accessoComp = addAccesso("Venoso"); // Aggiunge e ottiene il componente
                accessoComp.setAccessoData(accesso); // Imposta tutti i dati
                venosiPresenti = true;
            }
        }
        // Imposta stato iniziale checkbox venosi
        venosiCheckbox.setValue(venosiPresenti);
        venosiContainer.setVisible(venosiPresenti);
        addVenosiButton.setVisible(venosiPresenti);


        boolean arteriosiPresenti = false;
        if (parametriT0 != null && parametriT0.getAccessiArteriosi() != null && !parametriT0.getAccessiArteriosi().isEmpty()) {
            for (var accesso : parametriT0.getAccessiArteriosi()) {
                AccessoComponent accessoComp = addAccesso("Arterioso"); // Aggiunge e ottiene il componente
                accessoComp.setAccessoData(accesso); // Imposta tutti i dati
                arteriosiPresenti = true;
            }
        }
        // Imposta stato iniziale checkbox arteriosi
        arteriosiCheckbox.setValue(arteriosiPresenti);
        arteriosiContainer.setVisible(arteriosiPresenti);
        addArteriosiButton.setVisible(arteriosiPresenti);


        // === FINE GESTIONE ACCESSI NUOVA ===


        //Inizio Esame Fisico
        VerticalLayout esameFisicoLayout = new VerticalLayout();
        esameFisicoLayout.setPadding(false);
        esameFisicoLayout.setSpacing(true);
        esameFisicoLayout.setWidthFull();

        // Recupera i dati dell'esame fisico (Optional per sicurezza)
        EsameFisico esamiFisici = scenarioService.getEsameFisicoById(scenarioId);

        // Esame fisico generale
        generaleArea = createTinyMcePanel(
                "GENERALE",
                "Inserisci i dettagli dell'esame generale...",
                esamiFisici.getSection("Generale")
        );

        // ... (altri pannelli esame fisico come prima) ...
        pupilleArea = createTinyMcePanel(
                "PUPILLE",
                "Dimensione, reattività, simmetria",
                esamiFisici.getSection("Pupille")
        );

        colloArea = createTinyMcePanel(
                "COLLO",
                "Esame del collo, tiroide, linfonodi",
                esamiFisici.getSection("Collo")
        );

        toraceArea = createTinyMcePanel(
                "TORACE",
                "Ispezione, palpazione, percussione, auscultazione",
                esamiFisici.getSection("Torace")
        );

        cuoreArea = createTinyMcePanel(
                "CUORE",
                "Frequenza, ritmo, soffi",
                esamiFisici.getSection("Cuore")
        );

        addomeArea = createTinyMcePanel(
                "ADDOME",
                "Ispezione, palpazione, dolorabilità, organomegalie",
                esamiFisici.getSection("Addome")
        );

        rettoArea = createTinyMcePanel(
                "RETTO",
                "Esame rettale se indicato",
                esamiFisici.getSection("Retto")
        );

        cuteArea = createTinyMcePanel(
                "CUTE",
                "Colorito, turgore, lesioni",
                esamiFisici.getSection("Cute")
        );

        estremitaArea = createTinyMcePanel(
                "ESTREMITÀ",
                "Edemi, pulsazioni periferiche",
                esamiFisici.getSection("Estremità")
        );

        neurologicoArea = createTinyMcePanel(
                "NEUROLOGICO",
                "Stato mentale, nervi cranici, forza, sensibilità",
                esamiFisici.getSection("Neurologico")
        );

        FASTArea = createTinyMcePanel(
                "FAST ECOGRAFIA",
                "Focused Assessment with Sonography for Trauma",
                esamiFisici.getSection("FAST")
        );

        // Aggiunta dei campi all'area dell'esame fisico
        esameFisicoLayout.add(
                generaleArea, pupilleArea, colloArea, toraceArea, cuoreArea, addomeArea,
                rettoArea, cuteArea, estremitaArea, neurologicoArea, FASTArea
        );
        //Fine Esame Fisico

        Details targetDetails = new Details("TARGET E LEARNING GROUPS", createTargetComponent());
        targetDetails.setWidthFull();
        targetDetails.addClassName(LumoUtility.Margin.Top.LARGE);
        targetDetails.setOpened(false);

        // Creazione tendina per l'esame fisico
        Details esameFisicoDetails = new Details("ESAME FISICO", esameFisicoLayout);
        esameFisicoDetails.setWidthFull();
        esameFisicoDetails.addClassName(LumoUtility.Margin.Top.LARGE);
        esameFisicoDetails.setOpened(false);

        // Creazione tendina per le informazioni generali
        Details informazioniGeneraliDetails = new Details("INFORMAZIONI GENERALI", informazioniGeneraliLayout);
        informazioniGeneraliDetails.setWidthFull();
        informazioniGeneraliDetails.addClassName(LumoUtility.Margin.Top.LARGE);
        informazioniGeneraliDetails.setOpened(false);

        // Creazione tendina per i parametri del paziente T0
        Details parametriT0Details = new Details("PARAMETRI PAZIENTE T0", parametriT0Layout);
        parametriT0Details.setWidthFull();
        parametriT0Details.addClassName(LumoUtility.Margin.Top.LARGE);
        parametriT0Details.setOpened(false);

        // Creazione tendina per gli esami e i referti
        Details esamiRefertiDetails = new Details("ESAMI E REFERTI", createEsamiRefertiComponent());
        esamiRefertiDetails.setWidthFull();
        esamiRefertiDetails.addClassName(LumoUtility.Margin.Top.LARGE);
        esamiRefertiDetails.setOpened(false);


        // Aggiunta dei componenti al layout principale
        contentLayout.add(
                editAuthorField,
                scenarioTypeField,
                scenarioTitle,
                patientName,
                pathology,
                durationField,
                scenarioTypeSelect,
                targetDetails,
                informazioniGeneraliDetails,
                esamiRefertiDetails,
                parametriT0Details, // Contiene già la nuova gestione accessi
                esameFisicoDetails
        );

        // Gestione Quick Scenario (nessuna timeline)
        if (!scenarioType.equals("Quick Scenario")) {
            Details tempiDetails = new Details("TIMELINE SIMULAZIONE", createTempiComponent());
            tempiDetails.setWidthFull();
            tempiDetails.addClassName(LumoUtility.Margin.Top.LARGE);
            tempiDetails.setOpened(false);
            contentLayout.add(tempiDetails);
        }

        // Aggiunta della tendina per la sceneggiatura
        if (scenarioType.equals("Patient Simulated Scenario")) {
            String sceneggiatura = ScenarioService.getSceneggiatura(scenarioId);
            VerticalLayout sceneggiaturaArea = createTinyMcePanel(
                    "SCENEGGIATURA",
                    "Inserisci la sceneggiatura dello scenario...",
                    sceneggiatura
            );
            VerticalLayout sceneggiaturaLayout = new VerticalLayout(sceneggiaturaArea); // Layout semplice
            sceneggiaturaLayout.setPadding(false);
            sceneggiaturaLayout.setSpacing(false); // Rimosso spacing extra
            sceneggiaturaLayout.setWidthFull();

            Details sceneggiaturaDetails = new Details("SCENEGGIATURA", sceneggiaturaLayout);
            sceneggiaturaDetails.setWidthFull();
            sceneggiaturaDetails.addClassName(LumoUtility.Margin.Top.LARGE);
            sceneggiaturaDetails.setOpened(false);
            contentLayout.add(sceneggiaturaDetails);
        }

        // Aggiungi un pulsante Salva (DA IMPLEMENTARE LA LOGICA)
        Button saveButton = new Button("Salva Modifiche", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle().set("margin-top", LumoUtility.Margin.Top.XLARGE);
        saveButton.addClickListener(e -> {
            // Creazione di una finestra di dialogo per confermare il salvataggio
            Dialog confirmDialog = new Dialog();
            confirmDialog.setCloseOnEsc(false);
            confirmDialog.setCloseOnOutsideClick(false);

            // Titolo e contenuto della finestra di dialogo
            H3 dialogTitle = new H3("Conferma salvataggio");

            VerticalLayout dialogContent = new VerticalLayout();
            dialogContent.setPadding(true);
            dialogContent.setSpacing(true);

            // Messaggio di conferma con elenco puntato delle sezioni da controllare
            Html warningMessage = new Html("<div>Hai salvato le modifiche nelle sezioni seguenti?<ul>" +
                    "<li><strong>Target e Learning Groups</strong></li>" +
                    "<li><strong>Esami e Referti</strong></li>" +
                    "<li><strong>Materiale Necessario</strong></li>" +
                    "<li><strong>Timeline</strong> (se applicabile)</li>" +
                    "</ul>Queste sezioni richiedono un salvataggio separato nelle rispettive pagine dedicate.</div>");

            // Pulsanti di conferma
            Button confirmButton = new Button("Sì, ho salvato tutto", event -> {
                // Procedi con il salvataggio (codice esistente)
                salvaScenario();
                confirmDialog.close();
                UI.getCurrent().navigate("scenari/" + scenarioId); // Torna alla vista dettagli
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            Button cancelButton = new Button("Annulla", event -> confirmDialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button proceedButton = new Button("No, salva comunque", event -> {
                // Procedi comunque con il salvataggio
                salvaScenario();
                confirmDialog.close();
                UI.getCurrent().navigate("scenari/" + scenarioId);
            });
            proceedButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, proceedButton, confirmButton);
            buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            buttonLayout.setWidthFull();

            // Assemblaggio della finestra di dialogo
            dialogContent.add(dialogTitle, warningMessage, buttonLayout);
            confirmDialog.add(dialogContent);

            confirmDialog.open();
        });


        contentLayout.add(saveButton); // Aggiungi il pulsante Salva alla fine del contentLayout

        mainLayout.add(customHeader, contentLayout);
    }

    private void salvaScenario() {
        String autore = editAuthorField.getValue();
        String titolo = scenarioTitle.getValue();
        String nomePaziente = patientName.getValue();
        String patologia = pathology.getValue();
        String tipoPaziente = scenarioTypeSelect.getValue();
        String timer = durationField.getValue() != null ? String.valueOf(durationField.getValue()) : "0";

        Map<String, String> updatedFields = new HashMap<>();
        updatedFields.put("Autore", autore);
        updatedFields.put("Titolo", titolo);
        updatedFields.put("NomePaziente", nomePaziente);
        updatedFields.put("Patologia", patologia);
        updatedFields.put("TipologiaPaziente", tipoPaziente);
        updatedFields.put("Timer", timer);

        // Controllo di esistenza del tipo di scenario per determinare se è pediatrico
        boolean isPediatric = scenarioTypeSelect.getValue() != null &&
                scenarioTypeSelect.getValue().equals("Pediatrico");

        // Recupero dei valori dalle aree di testo con controlli null-safe
        // Recupero dei valori dalle aree di testo con controlli null-safe più rigorosi
        String descrizione = descriptionArea != null ? getTinyMceValue(descriptionArea) : "";
        String briefing = briefingArea != null ? getTinyMceValue(briefingArea) : "";
        String infoGenitore = isPediatric && InfoGenitoriArea != null ? getTinyMceValue(InfoGenitoriArea) : null;
        String pattoAula = pattoAulaArea != null ? getTinyMceValue(pattoAulaArea) : "";
        String obiettivi = obiettiviArea != null ? getTinyMceValue(obiettiviArea) : "";
        String moulage = moulageArea != null ? getTinyMceValue(moulageArea) : "";
        String liquidi = liquidiArea != null ? getTinyMceValue(liquidiArea) : "";

        Map<String, String> updatedSections = new HashMap<>();
        updatedSections.put("Descrizione", descrizione);
        updatedSections.put("Briefing", briefing);
        if (isPediatric) {
            updatedSections.put("InfoGenitore", infoGenitore);
        }
        updatedSections.put("PattoAula", pattoAula);
        updatedSections.put("Obiettivi", obiettivi);
        updatedSections.put("Moulage", moulage);
        updatedSections.put("Liquidi", liquidi);
        updatedSections.put("AzioniChiave", getAzioniChiaveValue(azioniChiaveArea));

        System.out.println("DEBUG" + getTinyMceValue(generaleArea));
        EsameFisico esameFisico = new EsameFisico(scenarioId,
                generaleArea != null ? getTinyMceValue(generaleArea) : "",
                pupilleArea != null ? getTinyMceValue(pupilleArea) : "",
                colloArea != null ? getTinyMceValue(colloArea) : "",
                toraceArea != null ? getTinyMceValue(toraceArea) : "",
                cuoreArea != null ? getTinyMceValue(cuoreArea) : "",
                addomeArea != null ? getTinyMceValue(addomeArea) : "",
                rettoArea != null ? getTinyMceValue(rettoArea) : "",
                cuteArea != null ? getTinyMceValue(cuteArea) : "",
                estremitaArea != null ? getTinyMceValue(estremitaArea) : "",
                neurologicoArea != null ? getTinyMceValue(neurologicoArea) : "",
                FASTArea != null ? getTinyMceValue(FASTArea) : ""
        );

        // Ottieni i parametri T0 aggiornati
        Map<String, String> updatedSectionsT0 = getUpdatedSectionsT0();

        // Raccogli gli accessi
        List<Accesso> venosiDaSalvare = new ArrayList<>();
        for (AccessoComponent comp : venosiAccessi) {
            venosiDaSalvare.add(comp.getAccesso());
        }

        List<Accesso> arteriosiDaSalvare = new ArrayList<>();
        for (AccessoComponent comp : arteriosiAccessi) {
            arteriosiDaSalvare.add(comp.getAccesso());
        }

        // Salva le modifiche
        scenarioService.updateScenario(scenarioId, updatedFields, updatedSections,
                esameFisico, updatedSectionsT0, venosiDaSalvare, arteriosiDaSalvare);
    }

    private Map<String, String> getUpdatedSectionsT0() {
        Map<String, String> updatedSectionsT0 = new HashMap<>();
        updatedSectionsT0.put("PA", paField != null ? paField.getValue() : "");
        updatedSectionsT0.put("FC", fcField != null ? fcField.getValue() : "");
        updatedSectionsT0.put("RR", rrField != null ? rrField.getValue() : "");
        updatedSectionsT0.put("Temperatura", tempField != null ? tempField.getValue() : "");
        updatedSectionsT0.put("SpO2", spo2Field != null ? spo2Field.getValue() : "");
        updatedSectionsT0.put("FiO2", fio2Field != null ? fio2Field.getValue() : "");
        updatedSectionsT0.put("LitriO2", litriO2Field != null ? litriO2Field.getValue() : "");
        updatedSectionsT0.put("EtCO2", etco2Field != null ? etco2Field.getValue() : "");
        updatedSectionsT0.put("Monitoraggio", monitorArea != null ? monitorArea.getValue() : "");
        updatedSectionsT0.put("Presidi", presidiSelect != null ? presidiSelect.getValue() : "");
        return updatedSectionsT0;
    }

    /**
     * Crea un campo di testo con etichetta e segnaposto.
     *
     * @param label       etichetta del campo
     * @param placeholder segnaposto del campo
     * @return campo di testo creato
     */
    private TextField createTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.getStyle().set("max-width", "500px");
        field.addClassName(LumoUtility.Margin.Top.LARGE);
        return field;
    }

    /**
     * Aggiunge un nuovo componente AccessoComponent (venoso o arterioso) al layout
     * e alla lista corrispondente. Questo metodo viene chiamato sia quando si clicca
     * il pulsante "Aggiungi" sia durante il caricamento iniziale dei dati.
     *
     * @param tipo "Venoso" o "Arterioso"
     * @return Il componente AccessoComponent creato e aggiunto.
     */
    private AccessoComponent addAccesso(String tipo) {
        AccessoComponent accessoComp;
        if ("Venoso".equals(tipo)) {
            // Passa il container e la lista d'istanza corretti
            accessoComp = new AccessoComponent(tipo, venosiContainer, venosiAccessi);
            venosiAccessi.add(accessoComp);
            venosiContainer.add(accessoComp);
        } else { // Arterioso
            // Passa il container e la lista d'istanza corretti
            accessoComp = new AccessoComponent(tipo, arteriosiContainer, arteriosiAccessi);
            arteriosiAccessi.add(accessoComp);
            arteriosiContainer.add(accessoComp);
        }
        return accessoComp;
    }


    /**
     * Crea un pannello per la gestione delle azioni chiave con funzionalità di aggiunta/rimozione dinamica.
     *
     * @param valore valore iniziale delle azioni chiave (stringa separata da punto e virgola)
     * @return layout verticale contenente il pannello completo
     */
    private VerticalLayout createAzioniChiavePanel(String valore) {
        // Lista per tenere traccia dei campi delle azioni chiave
        List<TextField> actionFields = new ArrayList<>();

        // Layout principale del pannello
        VerticalLayout panelLayout = new VerticalLayout();
        panelLayout.setWidthFull();
        panelLayout.setPadding(false);
        panelLayout.setSpacing(true);

        // Titolo e sottotitolo
        H3 titleElement = new H3("AZIONI CHIAVE");
        Paragraph subtitleElement = new Paragraph("Inserisci le azioni da valutare durante il debriefing...");

        // Container per i campi delle azioni chiave
        VerticalLayout actionFieldsContainer = new VerticalLayout();
        actionFieldsContainer.setWidthFull();
        actionFieldsContainer.setPadding(false);
        actionFieldsContainer.setSpacing(true);

        // Funzione per aggiungere un nuovo campo azione
        Button addButton = new Button("Aggiungi azione chiave", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addActionField(actionFieldsContainer, actionFields)); // Estratta logica

        // Carica le azioni esistenti se presenti
        if (valore != null && !valore.isEmpty()) {
            String[] actions = valore.split(";");
            for (String action : actions) {
                if (action != null && !action.trim().isEmpty()) { // Aggiunto controllo null
                    addActionField(actionFieldsContainer, actionFields, action.trim()); // Usa metodo helper
                }
            }
        }

        // Se non ci sono azioni DOPO il caricamento, aggiungiamo un campo vuoto
        if (actionFields.isEmpty()) {
            addActionField(actionFieldsContainer, actionFields);
        }

        panelLayout.add(titleElement, subtitleElement, actionFieldsContainer, addButton);
        return panelLayout;
    }

    /**
     * Ottiene tutte le azioni chiave dai campi di input, separate da punto e virgola.
     *
     * @param panel Il layout verticale contenente i campi delle azioni chiave
     * @return Una stringa con tutte le azioni separate da punto e virgola
     */
    private String getAzioniChiaveValue(VerticalLayout panel) {
        if (panel == null) {
            return "";
        }

        List<String> azioni = new ArrayList<>();

        // Prendi il VerticalLayout che contiene i campi azioni
        // (dovrebbe essere il terzo elemento nel panel, dopo titleElement e subtitleElement)
        panel.getChildren().skip(2).findFirst()
                .filter(child -> child instanceof VerticalLayout)
                .ifPresent(container -> container.getChildren()
                        .filter(child -> child instanceof HorizontalLayout)
                        .forEach(horizontalLayout -> horizontalLayout.getChildren()
                                .filter(field -> field instanceof TextField)
                                .map(field -> ((TextField) field).getValue())
                                .filter(value -> value != null && !value.trim().isEmpty())
                                .forEach(value -> azioni.add(value.trim()))));

        // Unisci le azioni con il punto e virgola
        return String.join(";", azioni);
    }


    /**
     * Metodo helper per aggiungere un campo azione (con o senza valore iniziale).
     *
     * @param container    Il layout dove aggiungere il campo.
     * @param fieldsList   La lista dove tracciare il TextField.
     * @param initialValue Il valore iniziale (può essere null o vuoto).
     */
    private void addActionField(VerticalLayout container, List<TextField> fieldsList, String... initialValue) {
        HorizontalLayout fieldLayout = new HorizontalLayout();
        fieldLayout.setWidthFull();
        fieldLayout.setSpacing(true);
        fieldLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        TextField actionField = new TextField();
        actionField.setWidthFull();
        actionField.setPlaceholder("Inserisci un'azione chiave...");
        if (initialValue.length > 0 && initialValue[0] != null) {
            actionField.setValue(initialValue[0]);
        }
        fieldsList.add(actionField); // Aggiungi alla lista per tracciamento

        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        removeButton.addClickListener(event -> {
            fieldsList.remove(actionField); // Rimuovi dalla lista
            container.remove(fieldLayout); // Rimuovi dal layout
        });

        fieldLayout.add(actionField, removeButton);
        fieldLayout.expand(actionField); // Assicura che il textfield prenda lo spazio
        container.add(fieldLayout);
    }


    // --- NUOVA CLASSE AccessoComponent (basata su PazienteT0View ma adattata) ---

    /**
     * Componente per la gestione degli accessi venosi e arteriosi.
     * Contiene campi per tipo, posizione, lato e misura.
     */
    private static class AccessoComponent extends HorizontalLayout {
        private final Select<String> tipoSelect;
        private final TextField posizioneField;
        private final ComboBox<String> latoSelect;
        private final ComboBox<Integer> misuraSelect;
        private final Accesso accesso; // Oggetto dati
        private final VerticalLayout container; // Contenitore parent (passato)
        private final List<AccessoComponent> listaAccessi; // Lista parent (passata)

        /**
         * Costruttore del componente di accesso.
         *
         * @param tipo         Il tipo di accesso ("Venoso" o "Arterioso").
         * @param container    Il layout VerticalLayout che conterrà questo componente.
         * @param listaAccessi La lista List<AccessoComponent> a cui aggiungere/rimuovere questo componente.
         */
        public AccessoComponent(String tipo, VerticalLayout container, List<AccessoComponent> listaAccessi) {
            // "Venoso" o "Arterioso"
            this.container = container;
            this.listaAccessi = listaAccessi;
            this.accesso = new Accesso(0, "", "", "", 0); // Inizializza oggetto dati vuoto

            setWidthFull();
            setAlignItems(Alignment.BASELINE); // Allinea alla base per estetica migliore
            setSpacing(true);


            tipoSelect = new Select<>();
            tipoSelect.setLabel("Tipo accesso " + tipo);
            if ("Venoso".equals(tipo)) {
                tipoSelect.setItems(
                        "Periferico", "Centrale", "CVC a breve termine", "CVC tunnellizzato",
                        "PICC", "Midline", "Intraosseo", "PORT", "Dialysis catheter", "Altro"
                );
            } else { // Arterioso
                tipoSelect.setItems(
                        "Radiale", "Femorale", "Omerale", "Brachiale", "Ascellare", "Pedidia", "Altro"
                );
            }
            tipoSelect.setWidth("200px"); // Larghezza fissa per coerenza
            tipoSelect.addValueChangeListener(e -> accesso.setTipologia(e.getValue()));


            posizioneField = new TextField("Posizione");
            //posizioneField.setWidthFull(); // Lasciamo che si adatti? O diamo larghezza?
            posizioneField.addValueChangeListener(e -> accesso.setPosizione(e.getValue()));

            latoSelect = new ComboBox<>("Lato");
            latoSelect.setItems("DX", "SX");
            latoSelect.setWidth("100px");
            latoSelect.addValueChangeListener(e -> accesso.setLato(e.getValue()));

            misuraSelect = new ComboBox<>("Misura (G)"); // Abbreviato Gauge
            misuraSelect.setItems(14, 16, 18, 20, 22, 24, 26); // Gauge comuni
            misuraSelect.setAllowCustomValue(true); // Permette altri numeri
            misuraSelect.setWidth("100px");
            misuraSelect.addValueChangeListener(e -> accesso.setMisura(e.getValue()));
            misuraSelect.addCustomValueSetListener(e -> {
                try {
                    int customValue = Integer.parseInt(e.getDetail());
                    List<Integer> items = new ArrayList<>(misuraSelect.getListDataView().getItems().toList());
                    if (!items.contains(customValue)) {
                        items.add(customValue);
                        items.sort(Integer::compareTo); // Mantieni ordinato
                        misuraSelect.setItems(items);
                    }
                    misuraSelect.setValue(customValue); // Imposta il valore custom
                } catch (NumberFormatException ex) {
                    // Ignora se non è un numero valido
                }
            });


            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            removeButton.getStyle().set("margin-top", "auto"); // Allinea verticalmente il bottone
            removeButton.addClickListener(e -> removeSelf());

            // Aggiungi i componenti al layout orizzontale
            add(tipoSelect, posizioneField, latoSelect, misuraSelect, removeButton);
            expand(posizioneField); // Fai espandere il campo posizione
        }

        /**
         * Rimuove questo componente dal suo contenitore e dalla lista di tracciamento.
         */
        private void removeSelf() {
            if (container != null) {
                container.remove(this);
            }
            if (listaAccessi != null) {
                listaAccessi.remove(this);
            }
        }

        /**
         * Restituisce l'oggetto Accesso con i dati correnti del componente.
         *
         * @return l'oggetto Accesso.
         */
        public Accesso getAccesso() {
            // Assicurati che l'oggetto accesso sia aggiornato (anche se i listener dovrebbero averlo fatto)
            accesso.setTipologia(tipoSelect.getValue());
            accesso.setPosizione(posizioneField.getValue());
            accesso.setLato(latoSelect.getValue());
            accesso.setMisura(misuraSelect.getValue() != null ? misuraSelect.getValue() : 0); // Gestisci null per misura
            return accesso;
        }

        /**
         * Imposta i valori dei campi del componente basandosi su un oggetto Accesso esistente.
         *
         * @param accesso L'oggetto Accesso da cui caricare i dati.
         */
        public void setAccessoData(Accesso accesso) {
            if (accesso == null) return;

            // Imposta i valori nei componenti UI
            tipoSelect.setValue(accesso.getTipologia());
            posizioneField.setValue(accesso.getPosizione());
            latoSelect.setValue(accesso.getLato());

            // Gestione misura: assicurati che il valore esista nella ComboBox
            Integer misura = accesso.getMisura();
            if (misura != null && misura != 0) {
                List<Integer> items = new ArrayList<>(misuraSelect.getListDataView().getItems().toList());
                if (!items.contains(misura)) {
                    items.add(misura);
                    items.sort(Integer::compareTo);
                    misuraSelect.setItems(items); // Aggiungi se non presente
                }
                misuraSelect.setValue(misura);
            } else {
                misuraSelect.clear(); // Pulisci se misura è 0 o null
            }


            // Aggiorna anche l'oggetto interno 'accesso' di questo componente
            this.accesso.setId(accesso.getId()); // Mantieni l'ID se presente
            this.accesso.setTipologia(accesso.getTipologia());
            this.accesso.setPosizione(accesso.getPosizione());
            this.accesso.setLato(accesso.getLato());
            this.accesso.setMisura(accesso.getMisura());
        }
    }
    // --- FINE NUOVA CLASSE AccessoComponent ---


    private Component createTargetComponent() {
        VerticalLayout targetLayout = new VerticalLayout();
        targetLayout.setPadding(false);
        targetLayout.setSpacing(true);
        targetLayout.setWidthFull();

        Button openFullPageButton = new Button("Modifica Target in pagina dedicata", new Icon(VaadinIcon.EXTERNAL_LINK));
        openFullPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFullPageButton.addClickListener(e -> UI.getCurrent().navigate("target/" + scenarioId + "/edit"));

        IFrame targetFrame = new IFrame("target/" + scenarioId + "/edit");
        targetFrame.setHeight("600px");
        targetFrame.setWidthFull();
        targetFrame.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");

        targetLayout.add(openFullPageButton, targetFrame);
        return targetLayout;
    }

    /**
     * Crea un componente per visualizzare i tempi della simulazione (tramite IFrame).
     *
     * @return il componente creato
     */
    private Component createTempiComponent() {
        VerticalLayout tempiLayout = new VerticalLayout();
        tempiLayout.setPadding(false);
        tempiLayout.setSpacing(true);
        tempiLayout.setWidthFull();

        Button openFullPageButton = new Button("Modifica Timeline in pagina dedicata", new Icon(VaadinIcon.EXTERNAL_LINK));
        openFullPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFullPageButton.addClickListener(e -> UI.getCurrent().navigate("tempi/" + scenarioId + "/edit"));

        IFrame tempiFrame = new IFrame("tempi/" + scenarioId + "/edit");
        tempiFrame.setHeight("600px"); // Altezza fissa potrebbe essere un problema
        tempiFrame.setWidthFull();
        tempiFrame.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)"); // Bordo per chiarezza

        tempiLayout.add(openFullPageButton, tempiFrame);
        return tempiLayout;
    }

    /**
     * Crea un componente per visualizzare il materiale necessario (tramite IFrame).
     *
     * @return il componente creato
     */
    private Component createMaterialeNecessarioComponent() {
        VerticalLayout materialeLayout = new VerticalLayout();
        materialeLayout.setPadding(false);
        materialeLayout.setSpacing(true);
        materialeLayout.setWidthFull();

        Button openFullPageButton = new Button("Modifica Materiale in pagina dedicata", new Icon(VaadinIcon.EXTERNAL_LINK));
        openFullPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFullPageButton.addClickListener(e -> UI.getCurrent().navigate("materialeNecessario/" + scenarioId + "/edit"));

        IFrame materialeFrame = new IFrame("materialeNecessario/" + scenarioId + "/edit");
        materialeFrame.setHeight("600px");
        materialeFrame.setWidthFull();
        materialeFrame.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");

        materialeLayout.add(openFullPageButton, materialeFrame);
        return materialeLayout;
    }

    /**
     * Crea un componente per visualizzare gli esami e i referti (tramite IFrame).
     *
     * @return il componente creato
     */
    private Component createEsamiRefertiComponent() {
        VerticalLayout esamiLayout = new VerticalLayout();
        esamiLayout.setPadding(false);
        esamiLayout.setSpacing(true);
        esamiLayout.setWidthFull();

        Button openFullPageButton = new Button("Modifica Esami/Referti in pagina dedicata", new Icon(VaadinIcon.EXTERNAL_LINK));
        openFullPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFullPageButton.addClickListener(e -> UI.getCurrent().navigate("esamiReferti/" + scenarioId + "/edit"));

        IFrame esamiFrame = new IFrame("esamiReferti/" + scenarioId + "/edit");
        esamiFrame.setHeight("600px");
        esamiFrame.setWidthFull();
        esamiFrame.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");

        esamiLayout.add(openFullPageButton, esamiFrame);
        return esamiLayout;
    }

    /**
     * Crea un pannello con titolo, sottotitolo ed editor TinyMce.
     *
     * @param titolo      titolo del pannello
     * @param sottotitolo sottotitolo del pannello
     * @param valore      valore iniziale dell'editor
     * @return layout verticale contenente il pannello completo
     */
    private VerticalLayout createTinyMcePanel(String titolo, String sottotitolo, String valore) {
        H3 titleElement = new H3(titolo);
        titleElement.addClassName(LumoUtility.Margin.Bottom.NONE); // Riduci margini titolo
        Paragraph subtitleElement = new Paragraph(sottotitolo);
        subtitleElement.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL, LumoUtility.Margin.Top.XSMALL); // Stile sottotitolo

        TinyMce editor = new TinyMce();
        editor.setValue(Objects.requireNonNullElse(valore, "")); // Gestione null per valore iniziale
        editor.setHeight("250px");
        editor.setWidthFull();

        VerticalLayout panelLayout = new VerticalLayout(titleElement, subtitleElement, editor);
        panelLayout.setPadding(false); // Rimuovi padding per compattezza
        panelLayout.setSpacing(true); // Mantieni spaziatura interna
        panelLayout.setWidthFull(); // Occupa tutta la larghezza disponibile

        return panelLayout;
    }

    // Ottieni il valore dall'editor TinyMce nel layout
    // Ottieni il valore dall'editor TinyMce nel layout
    private String getTinyMceValue(VerticalLayout panel) {
        // Verifica che il panel non sia nullo
        if (panel == null) {
            return ""; // Ritorna stringa vuota se il pannello è nullo
        }

        // L'editor è il terzo componente nel layout (indice 2)
        Optional<Component> component = panel.getChildren()
                .filter(c -> c instanceof TinyMce)
                .findFirst();

        if (component.isPresent()) {
            TinyMce editor = (TinyMce) component.get();
            return editor.getValue();
        }

        // Restituisci stringa vuota se l'editor non viene trovato
        return "";
    }
}