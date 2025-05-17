package it.uniupo.simnova.views.creation.scenario;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox; // Import Checkbox
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
// Import necessari per il nuovo AccessoComponent
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.operations.ScenarioUpdateService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.components.PresidiService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import it.uniupo.simnova.views.ui.helper.AccessoComponent;
import it.uniupo.simnova.views.ui.helper.InfoSupport;
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
@Route(value = "modificaScenario")
public class ScenarioEditView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    private final FileStorageService fileStorageService;
    private final PresidiService presidiService;
    private final ScenarioUpdateService scenarioUpdateService;
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    private final AzioneChiaveService azioneChiaveService;
    private final EsameFisicoService esameFisicoService;
    private final PazienteT0Service pazienteT0Service;
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

    private VerticalLayout venosiContainer;
    private VerticalLayout arteriosiContainer;
    private List<AccessoComponent> venosiAccessi = new ArrayList<>();
    private List<AccessoComponent> arteriosiAccessi = new ArrayList<>();
    private Button addVenosiButton;
    private Button addArteriosiButton;

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
    MultiSelectComboBox<String> presidiField;

    /**
     * Costruttore della classe ScenarioEditView.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public ScenarioEditView(ScenarioService scenarioService,
                            FileStorageService fileStorageService,
                            PresidiService presidiService,
                            ScenarioUpdateService scenarioUpdateService,
                            PatientSimulatedScenarioService patientSimulatedScenarioService,
                            AzioneChiaveService azioneChiaveService,
                            EsameFisicoService esameFisicoService,
                            PazienteT0Service pazienteT0Service) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.presidiService = presidiService;
        this.scenarioUpdateService = scenarioUpdateService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        this.azioneChiaveService = azioneChiaveService;
        this.esameFisicoService = esameFisicoService;
        this.pazienteT0Service = pazienteT0Service;
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
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.addClickListener(e -> UI.getCurrent().navigate("scenari/" + scenarioId));

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Modifica Scenario",
                "Modifica i dettagli dello scenario selezionato",
                VaadinIcon.EDIT.create(),
                "var(--lumo-primary-color)"
        );
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        // Layout principale per il contenuto
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Recupera il tipo di scenario
        String scenarioType = scenarioService.getScenarioType(scenarioId);
        Span scenarioTypeSpan = InfoSupport.createInfoBadge("TIPO SCENARIO", scenarioType, "var(--lumo-primary-color)");

        editAuthorField = FieldGenerator.createTextField(
                "AUTORI DELLO SCENARIO (aggiungi il tuo nome se non presente)",
                "Inserisci il tuo nome",
                true
        );
        editAuthorField.setValue(scenario.getAutori());

        // Titolo scenario
        scenarioTitle = FieldGenerator.createTextField(
                "TITOLO SCENARIO",
                "Inserisci il titolo dello scenario",
                false
        );
        scenarioTitle.setValue(scenario.getTitolo());

        // Nome paziente
        patientName = FieldGenerator.createTextField(
                "NOME PAZIENTE",
                "Inserisci il nome del paziente",
                false
        );
        patientName.setValue(scenario.getNomePaziente());

        // Patologia
        pathology = FieldGenerator.createTextField(
                "PATOLOGIA",
                "Inserisci la patologia del paziente",
                false
        );
        pathology.setValue(scenario.getPatologia());

        // Campo durata timer
        List<Integer> timerOptions = Arrays.asList(5, 10, 15, 20, 25, 30);
        durationField = FieldGenerator.createComboBox(
                "DURATA SIMULAZIONE (minuti)",
                timerOptions,
                scenario.getTimerGenerale() > 0 ? (int) scenario.getTimerGenerale() : 10,
                false
        );

        List<String> scenarioTypes = Arrays.asList("Adulto", "Pediatrico", "Neonatale", "Prematuro");
        scenarioTypeSelect = FieldGenerator.createComboBox(
                "TIPO SCENARIO",
                scenarioTypes,
                scenario.getTipologia() != null ? scenario.getTipologia() : "Adulto",
                false
        );

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
                azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId)
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
                createIFrame("Modifica Materiale Necessario in pagina dedicata", "materialeNecessario/"),
                moulageArea,
                liquidiArea
        );

        // Layout per i parametri del paziente T0
        VerticalLayout parametriT0Layout = new VerticalLayout();
        parametriT0Layout.setPadding(false); // Rimuovi padding se vuoi allineare con le checkbox
        parametriT0Layout.setSpacing(true);
        parametriT0Layout.setWidthFull();

        // Recupera i dati del paziente T0 (Optional per sicurezza)
        PazienteT0 parametriT0 = pazienteT0Service.getPazienteT0ById(scenarioId);

        // Crea i componenti per visualizzare i dati
        paField = FieldGenerator.createTextField(
                "PA (mmHg)",
                "Inserisci la pressione arteriosa",
                false
        );
        paField.setValue(parametriT0 != null ? Objects.requireNonNullElse(parametriT0.getPA(), "") : "");

        fcField = FieldGenerator.createTextField(
                "FC (bpm)",
                "Inserisci la frequenza cardiaca",
                false
        );
        fcField.setValue(parametriT0 != null && parametriT0.getFC() != 0 ? String.valueOf(parametriT0.getFC()) : "");

        rrField = FieldGenerator.createTextField(
                "RR (atti/min)",
                "Inserisci la frequenza respiratoria",
                false
        );
        rrField.setValue(parametriT0 != null && parametriT0.getRR() != 0 ? String.valueOf(parametriT0.getRR()) : "");

        tempField = FieldGenerator.createTextField(
                "Temperatura (°C)",
                "Inserisci la temperatura corporea",
                false
        );
        tempField.setValue(parametriT0 != null ? String.format("%.1f", parametriT0.getT()) : "");

        spo2Field = FieldGenerator.createTextField(
                "SpO₂ (%)",
                "Inserisci la saturazione di ossigeno",
                false
        );
        spo2Field.setValue(parametriT0 != null && parametriT0.getSpO2() != 0 ? String.valueOf(parametriT0.getSpO2()) : "");

        fio2Field = FieldGenerator.createTextField(
                "FiO₂ (%)",
                "Inserisci la frazione inspiratoria di ossigeno",
                false
        );
        fio2Field.setValue(parametriT0 != null && parametriT0.getFiO2() != 0 ? String.valueOf(parametriT0.getFiO2()) : "");

        litriO2Field = FieldGenerator.createTextField(
                "Litri O₂",
                "Inserisci i litri di ossigeno",
                false
        );
        litriO2Field.setValue(parametriT0 != null && parametriT0.getLitriO2() != 0 ? String.valueOf(parametriT0.getLitriO2()) : "");

        etco2Field = FieldGenerator.createTextField(
                "EtCO₂ (mmHg)",
                "Inserisci la pressione parziale di CO₂",
                false
        );
        etco2Field.setValue(parametriT0 != null && parametriT0.getEtCO2() != 0 ? String.valueOf(parametriT0.getEtCO2()) : "");

        // Area di testo per il monitoraggio
        monitorArea = FieldGenerator.createTextArea(
                "MONITORAGGIO",
                "Inserisci i dettagli del monitoraggio...",
                false
        );
        monitorArea.setValue(parametriT0 != null ? Objects.requireNonNullElse(parametriT0.getMonitor(), "") : "");

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

        List<String> presidiList = PresidiService.getAllPresidi();
        presidiField = FieldGenerator.createMultiSelectComboBox(
                "Presidi",
                presidiList,
                false
        );
        List<String> selectedPresidi = PresidiService.getPresidiByScenarioId(scenarioId);
        presidiField.setValue(selectedPresidi);


        // Aggiunta dei campi e della nuova gestione accessi al layout parametriT0Layout
        parametriT0Layout.add(
                paField, fcField, rrField, tempField, spo2Field, fio2Field, litriO2Field, etco2Field, monitorArea, // Campi esistenti + Fio2/Litri
                venosiCheckbox, venosiContainer, addVenosiButton,        // Nuova gestione venosi
                arteriosiCheckbox, arteriosiContainer, addArteriosiButton,
                presidiField
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


        //Inizio Esame Fisico
        VerticalLayout esameFisicoLayout = new VerticalLayout();
        esameFisicoLayout.setPadding(false);
        esameFisicoLayout.setSpacing(true);
        esameFisicoLayout.setWidthFull();

        // Recupera i dati dell'esame fisico (Optional per sicurezza)
        EsameFisico esamiFisici = esameFisicoService.getEsameFisicoById(scenarioId);

        // Esame fisico generale
        generaleArea = createTinyMcePanel(
                "GENERALE",
                "Inserisci i dettagli dell'esame generale...",
                esamiFisici != null ? esamiFisici.getSection("Generale") : ""
        );

        // ... (altri pannelli esame fisico come prima) ...
        pupilleArea = createTinyMcePanel(
                "PUPILLE",
                "Dimensione, reattività, simmetria",
                esamiFisici != null ? esamiFisici.getSection("Pupille") : ""
        );

        colloArea = createTinyMcePanel(
                "COLLO",
                "Esame del collo, tiroide, linfonodi",
                esamiFisici != null ? esamiFisici.getSection("Collo") : ""
        );

        toraceArea = createTinyMcePanel(
                "TORACE",
                "Ispezione, palpazione, percussione, auscultazione",
                esamiFisici != null ? esamiFisici.getSection("Torace") : ""
        );

        cuoreArea = createTinyMcePanel(
                "CUORE",
                "Frequenza, ritmo, soffi",
                esamiFisici != null ? esamiFisici.getSection("Cuore") : ""
        );

        addomeArea = createTinyMcePanel(
                "ADDOME",
                "Ispezione, palpazione, dolorabilità, organomegalie",
                esamiFisici != null ? esamiFisici.getSection("Addome") : ""
        );

        rettoArea = createTinyMcePanel(
                "RETTO",
                "Esame rettale se indicato",
                esamiFisici != null ? esamiFisici.getSection("Retto") : ""
        );

        cuteArea = createTinyMcePanel(
                "CUTE",
                "Colorito, turgore, lesioni",
                esamiFisici != null ? esamiFisici.getSection("Cute") : ""
        );

        estremitaArea = createTinyMcePanel(
                "ESTREMITÀ",
                "Edemi, pulsazioni periferiche",
                esamiFisici != null ? esamiFisici.getSection("Estremità") : ""
        );

        neurologicoArea = createTinyMcePanel(
                "NEUROLOGICO",
                "Stato mentale, nervi cranici, forza, sensibilità",
                esamiFisici != null ? esamiFisici.getSection("Neurologico") : ""
        );

        FASTArea = createTinyMcePanel(
                "FAST ECOGRAFIA",
                "Focused Assessment with Sonography for Trauma",
                esamiFisici != null ? esamiFisici.getSection("FAST") : ""
        );

        // Aggiunta dei campi all'area dell'esame fisico
        esameFisicoLayout.add(
                generaleArea, pupilleArea, colloArea, toraceArea, cuoreArea, addomeArea,
                rettoArea, cuteArea, estremitaArea, neurologicoArea, FASTArea
        );
        //Fine Esame Fisico

        Details targetDetails = createDetails("TARGET E LEARNING GROUPS", createIFrame("Modifica Target in pagina dedicata", "target/"));

        // Creazione tendina per l'esame fisico
        Details esameFisicoDetails = createDetails("ESAME FISICO", esameFisicoLayout);

        // Creazione tendina per le informazioni generali
        Details informazioniGeneraliDetails = createDetails("INFORMAZIONI GENERALI", informazioniGeneraliLayout);

        // Creazione tendina per i parametri del paziente T0
        Details parametriT0Details = createDetails("PARAMETRI T0", parametriT0Layout);

        // Creazione tendina per gli esami e i referti
        Details esamiRefertiDetails = createDetails("ESAMI E REFERTI", createIFrame("Modifica Esami e Referti in pagina dedicata", "esamiReferti/"));

        // Aggiunta dei componenti al layout principale
        contentLayout.add(
                headerSection,
                scenarioTypeSpan,
                editAuthorField,
                scenarioTitle,
                patientName,
                pathology,
                durationField,
                scenarioTypeSelect,
                targetDetails,
                informazioniGeneraliDetails,
                esamiRefertiDetails,
                parametriT0Details,
                esameFisicoDetails
        );

        // Gestione Quick Scenario (nessuna timeline)
        if (!scenarioType.equals("Quick Scenario")) {
            Details tempiDetails = createDetails("TIMELINE", createIFrame("Modifica Timeline in pagina dedicata", "tempi/"));
            contentLayout.add(tempiDetails);
        }

        // Aggiunta della tendina per la sceneggiatura
        if (scenarioType.equals("Patient Simulated Scenario")) {
            String sceneggiatura = patientSimulatedScenarioService.getSceneggiatura(scenarioId);
            VerticalLayout sceneggiaturaArea = createTinyMcePanel(
                    "SCENEGGIATURA",
                    "Inserisci la sceneggiatura dello scenario...",
                    sceneggiatura
            );
            VerticalLayout sceneggiaturaLayout = new VerticalLayout(sceneggiaturaArea); // Layout semplice
            sceneggiaturaLayout.setPadding(false);
            sceneggiaturaLayout.setSpacing(false); // Rimosso spacing extra
            sceneggiaturaLayout.setWidthFull();

            Details sceneggiaturaDetails = createDetails("SCENEGGIATURA", sceneggiaturaLayout);
            contentLayout.add(sceneggiaturaDetails);
        }

        Button saveButton = getSaveButton();

        contentLayout.add(saveButton);

        Button scrollToTopButton = StyleApp.getScrollButton();
        Button scrollDownButton = StyleApp.getScrollDownButton();
        VerticalLayout scrollButtonLayout = new VerticalLayout(scrollToTopButton, scrollDownButton);

        mainLayout.add(scrollButtonLayout);

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(null);
        mainLayout.add(customHeader, contentLayout, footerLayout);
    }

    private Details createDetails(String title, Component content) {
        Details details = new Details(title, content);
        details.setWidthFull();
        details.addClassName(LumoUtility.Margin.Top.LARGE);
        details.setOpened(false);
        StyleApp.styleDetailsSummary(details); // Applica lo stile al summary
        return details;
    }

    private Button getSaveButton() {
        Button saveButton = StyleApp.getSaveEditButton();

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
        return saveButton;
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

        presidiService.savePresidi(scenarioId, presidiField.getValue());

        // Salva le modifiche
        scenarioUpdateService.updateScenario(scenarioId, updatedFields, updatedSections,
                esameFisico, updatedSectionsT0, venosiDaSalvare, arteriosiDaSalvare);
    }

    private Map<String, String> getUpdatedSectionsT0() {
        Map<String, String> updatedSectionsT0 = new HashMap<>();
        updatedSectionsT0.put("PA", paField != null ? paField.getValue() : "");
        updatedSectionsT0.put("FC", fcField != null ? fcField.getValue() : "");
        updatedSectionsT0.put("RR", rrField != null ? rrField.getValue() : "");

        // Gestione speciale per la temperatura: sostituisci la virgola con il punto
        String tempValue = tempField != null ? tempField.getValue() : "";
        updatedSectionsT0.put("Temperatura", tempValue.replace(',', '.'));

        updatedSectionsT0.put("SpO2", spo2Field != null ? spo2Field.getValue() : "");
        updatedSectionsT0.put("FiO2", fio2Field != null ? fio2Field.getValue() : "");
        updatedSectionsT0.put("LitriO2", litriO2Field != null ? litriO2Field.getValue() : "");
        updatedSectionsT0.put("EtCO2", etco2Field != null ? etco2Field.getValue() : "");
        updatedSectionsT0.put("Monitoraggio", monitorArea != null ? monitorArea.getValue() : "");
        return updatedSectionsT0;
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
            accessoComp = new AccessoComponent(tipo);
            venosiAccessi.add(accessoComp);
            venosiContainer.add(accessoComp);
        } else { // Arterioso
            // Passa il container e la lista d'istanza corretti
            accessoComp = new AccessoComponent(tipo);
            arteriosiAccessi.add(accessoComp);
            arteriosiContainer.add(accessoComp);
        }
        return accessoComp;
    }

    private VerticalLayout createAzioniChiavePanel(List<String> azioniChiavePreesistenti) {
        // List to keep track of the TextFields for the actions within this panel instance
        List<TextField> actionFieldsLocal = new ArrayList<>();

        // Main layout for the panel
        VerticalLayout panelLayout = new VerticalLayout();
        panelLayout.setWidthFull();
        panelLayout.setPadding(false);
        panelLayout.setSpacing(true); // Spacing between title, subtitle, container, and add button

        // Title and subtitle
        H3 titleElement = new H3("AZIONI CHIAVE");
        Paragraph subtitleElement = new Paragraph("Inserisci le azioni da valutare durante il debriefing.");
        titleElement.getStyle().set("margin-bottom", "0"); // Adjust spacing if needed
        subtitleElement.getStyle().set("margin-top", "0");

        // Container for the dynamic action TextFields
        VerticalLayout actionFieldsContainer = new VerticalLayout();
        actionFieldsContainer.setWidthFull();
        actionFieldsContainer.setPadding(false);
        actionFieldsContainer.setSpacing(true); // Spacing between individual action fields

        // Button to add a new action field
        Button addButton = new Button("Aggiungi azione chiave", new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.getStyle().set("align-self", "start"); // Align button to the start if container is centered
        addButton.addClickListener(e -> {
            // Call the helper method to add a new, empty action field
            addActionFieldUI(actionFieldsContainer, actionFieldsLocal, "");
        });

        // Load existing actions if the provided list is not null or empty
        if (azioniChiavePreesistenti != null && !azioniChiavePreesistenti.isEmpty()) {
            for (String actionName : azioniChiavePreesistenti) {
                if (actionName != null && !actionName.trim().isEmpty()) {
                    // Call the helper method to add a field pre-filled with the existing action name
                    addActionFieldUI(actionFieldsContainer, actionFieldsLocal, actionName.trim());
                }
            }
        }

        // If, after attempting to load, there are still no action fields, add one empty field to start with
        if (actionFieldsLocal.isEmpty()) {
            addActionFieldUI(actionFieldsContainer, actionFieldsLocal, "");
        }

        panelLayout.add(titleElement, subtitleElement, actionFieldsContainer, addButton);
        return panelLayout;
    }

    /**
     * Helper method to create and add a new action field (TextField and Remove Button) to the UI.
     * This method would typically be part of the same class or a utility class.
     *
     * @param container    The VerticalLayout container where the action field will be added.
     * @param fieldList    The list tracking all action TextFields.
     * @param initialValue The initial value for the TextField (can be empty).
     */
    private void addActionFieldUI(VerticalLayout container, List<TextField> fieldList, String initialValue) {
        HorizontalLayout fieldRowLayout = new HorizontalLayout();
        fieldRowLayout.setWidthFull();
        fieldRowLayout.setSpacing(true);
        fieldRowLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        TextField actionField = FieldGenerator.createTextField(
                "Azione chiave",
                "Inserisci l'azione chiave da valutare",
                false
        );
        actionField.setValue(initialValue != null ? initialValue : "");

        Button removeButton = StyleApp.getButton(
                "Rimuovi",
                VaadinIcon.TRASH,
                ButtonVariant.LUMO_ERROR,
                "var(--lumo-error-color)"
        );
        removeButton.addClickListener(e -> {
            container.remove(fieldRowLayout); // Remove the whole row from the container
            fieldList.remove(actionField);    // Remove the TextField from the tracking list
        });

        fieldRowLayout.addAndExpand(actionField); // TextField takes up available space
        fieldRowLayout.add(removeButton);

        container.add(fieldRowLayout);
        fieldList.add(actionField); // Add the new TextField to the tracking list
    }

    private Component createIFrame(String label, String url) {
        VerticalLayout iframeLayout = new VerticalLayout();
        iframeLayout.setPadding(false);
        iframeLayout.setSpacing(true);
        iframeLayout.setWidthFull();

        Button openFullPageButton = new Button(label, new Icon(VaadinIcon.EXTERNAL_LINK));
        openFullPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFullPageButton.addClickListener(e -> UI.getCurrent().navigate(url + scenarioId + "/edit"));
        IFrame iframe = new IFrame(url + scenarioId + "/edit");

        iframe.setHeight("600px");
        iframe.setWidthFull();
        iframe.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)"); // Bordo per chiarezza

        iframeLayout.add(openFullPageButton, iframe);
        return iframeLayout;
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

        TinyMce editor = TinyEditor.getEditor();
        editor.setValue(valore);

        VerticalLayout panelLayout = new VerticalLayout(titleElement, subtitleElement, editor);
        panelLayout.setPadding(false); // Rimuovi padding per compattezza
        panelLayout.setSpacing(true); // Mantieni spaziatura interna
        panelLayout.setWidthFull(); // Occupa tutta la larghezza disponibile

        return panelLayout;
    }

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