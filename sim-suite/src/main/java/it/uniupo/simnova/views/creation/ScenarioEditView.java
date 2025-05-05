package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
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
import it.uniupo.simnova.api.model.EsameFisico;
import it.uniupo.simnova.api.model.PazienteT0;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe per la visualizzazione e modifica di uno scenario esistente.
 * <p>
 * Questa classe estende Composite e implementa HasUrlParameter per gestire i parametri dell'URL.
 *
 * @version 1.0
 * @author Alessandro Zappatore
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
                throw new NumberFormatException();
            }
            this.scenarioId = Integer.parseInt(parameter);
            // Controlla se lo scenario esiste
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }
            // Recupera lo scenario dal servizio
            scenario = scenarioService.getScenarioById(scenarioId);
            buildView();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
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
        /*TODO Sistemare caricamento infinito*/
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

        TextField editAuthorField = createTextField("AUTORE", "Inserisci il tuo nome");
        editAuthorField.setRequired(true);

        // Titolo scenario
        TextField scenarioTitle = createTextField("TITOLO SCENARIO", "Inserisci il titolo dello scenario");
        scenarioTitle.setValue(scenario.getTitolo());

        // Nome paziente
        TextField patientName = createTextField("NOME PAZIENTE", "Inserisci il nome del paziente");
        patientName.setValue(scenario.getNomePaziente());

        // Patologia
        TextField pathology = createTextField("PATOLOGIA/MALATTIA", "Inserisci la patologia");
        pathology.setValue(scenario.getPatologia());

        // Campo durata timer
        ComboBox<Integer> durationField = new ComboBox<>("DURATA SIMULAZIONE (minuti)");
        durationField.setItems(5, 10, 15, 20, 25, 30);
        durationField.setValue(10);
        durationField.setWidthFull();
        durationField.addClassName(LumoUtility.Margin.Top.LARGE);
        durationField.getStyle().set("max-width", "500px");
        durationField.setValue((int) scenario.getTimerGenerale());

        ComboBox<String> scenarioTypeSelect = new ComboBox<>("TIPO SCENARIO");
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
        VerticalLayout descriptionArea = createTinyMcePanel(
                "DESCRIZIONE SCENARIO",
                "Questa descrizione fornirà il contesto generale e le informazioni di background ai partecipanti.",
                scenario.getDescrizione()
        );

        // Briefing scenario
        VerticalLayout briefingArea = createTinyMcePanel(
                "BRIEFING",
                "Inserisci il testo da leggere ai discenti prima della simulazione...",
                scenario.getBriefing()
        );

        // Patto d'aula scenario
        VerticalLayout pattoAulaArea = createTinyMcePanel(
                "PATTO D'AULA / FAMILIARIZZAZIONE",
                "Inserisci il testo da mostrare nella sala...",
                scenario.getPattoAula()
        );

        // Azioni chiave scenario
        TextArea azioniChiaveArea = createTextArea(
                "AZIONI CHIAVE",
                "Inserisci le azioni da valutare durante il debriefing...",
                scenario.getAzioneChiave()
        );

        // Obiettivi didattici scenario
        VerticalLayout obiettiviArea = createTinyMcePanel(
                "OBIETTIVI DIDATTICI",
                "Inserisci gli obiettivi didattici dello scenario...",
                scenario.getObiettivo()
        );

        // Moulage scenario
        VerticalLayout moulageArea = createTinyMcePanel(
                "MOULAGE",
                "Descrivi il trucco da applicare al manichino/paziente simulato...",
                scenario.getMoulage()
        );

        // Liquidi e presidi scenario
        VerticalLayout liquidiArea = createTinyMcePanel(
                "LIQUIDI E FARMACI IN T0",
                "Indica quantità di liquidi e farmaci presenti all'inizio della simulazione...",
                scenario.getLiquidi()
        );

        // Aggiunta dei campi al layout delle informazioni generali
        informazioniGeneraliLayout.add(
                descriptionArea,
                briefingArea,
                pattoAulaArea,
                azioniChiaveArea,
                obiettiviArea,
                moulageArea,
                liquidiArea
        );

        // Layout per i parametri del paziente T0
        VerticalLayout parametriT0Layout = new VerticalLayout();
        parametriT0Layout.setPadding(false);
        parametriT0Layout.setSpacing(true);
        parametriT0Layout.setWidthFull();

        // Recupera i dati del paziente T0
        PazienteT0 parametriT0 = scenarioService.getPazienteT0ById(scenarioId);

        // Crea i componenti per visualizzare i dati
        TextField paField = new TextField("PA (mmHg)");
        paField.setValue(parametriT0 != null ? parametriT0.getPA() : "");
        paField.setWidthFull();

        TextField fcField = new TextField("FC (bpm)");
        fcField.setValue(parametriT0 != null && parametriT0.getFC() != 0 ? String.valueOf(parametriT0.getFC()) : "");
        fcField.setWidthFull();

        TextField rrField = new TextField("RR (atti/min)");
        rrField.setValue(parametriT0 != null && parametriT0.getRR() != 0 ? String.valueOf(parametriT0.getRR()) : "");
        rrField.setWidthFull();

        TextField tempField = new TextField("Temperatura (°C)");
        tempField.setValue(parametriT0 != null && parametriT0.getT() != 0 ? String.valueOf(parametriT0.getT()) : "");
        tempField.setWidthFull();

        TextField spo2Field = new TextField("SpO₂ (%)");
        spo2Field.setValue(parametriT0 != null && parametriT0.getSpO2() != 0 ? String.valueOf(parametriT0.getSpO2()) : "");
        spo2Field.setWidthFull();

        TextField fio2Field = new TextField("FiO₂ (%)");
        fio2Field.setValue(parametriT0 != null && parametriT0.getFiO2() != 0 ? String.valueOf(parametriT0.getFiO2()) : "");
        fio2Field.setWidthFull();

        TextField litriO2Field = new TextField("Litri O₂");
        litriO2Field.setValue(parametriT0 != null && parametriT0.getLitriO2() != 0 ? String.valueOf(parametriT0.getLitriO2()) : "");
        litriO2Field.setWidthFull();

        TextField etco2Field = new TextField("EtCO₂ (mmHg)");
        etco2Field.setValue(parametriT0 != null && parametriT0.getEtCO2() != 0 ? String.valueOf(parametriT0.getEtCO2()) : "");
        etco2Field.setWidthFull();

        // Area di testo per il monitoraggio
        TextArea monitorArea = new TextArea("Monitoraggio");
        monitorArea.setValue(parametriT0 != null && parametriT0.getMonitor() != null ? parametriT0.getMonitor() : "");
        monitorArea.setWidthFull();
        monitorArea.setMinHeight("100px");

        // Aggiunta dei campi al layout
        parametriT0Layout.add(
                paField, fcField, rrField, tempField, spo2Field, etco2Field, monitorArea
        );

        // Container per accessi venosi
        VerticalLayout venosiContainer = new VerticalLayout();
        venosiContainer.setWidthFull();
        venosiContainer.setSpacing(true);
        venosiContainer.setPadding(false);

        // Container per accessi arteriosi
        VerticalLayout arteriosiContainer = new VerticalLayout();
        arteriosiContainer.setWidthFull();
        arteriosiContainer.setSpacing(true);
        arteriosiContainer.setPadding(false);

        // Liste per memorizzare gli accessi
        List<AccessoComponent> venosiAccessi = new ArrayList<>();
        List<AccessoComponent> arteriosiAccessi = new ArrayList<>();

        // Titoli e pulsanti per gli accessi
        H4 venosiTitle = new H4("Accessi Venosi");
        venosiTitle.addClassName(LumoUtility.Margin.Top.MEDIUM);

        Button addVenosiButton = new Button("Aggiungi accesso venoso", new Icon(VaadinIcon.PLUS));
        addVenosiButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addVenosiButton.addClassName(LumoUtility.Margin.Top.SMALL);
        addVenosiButton.addClickListener(e -> {
            AccessoComponent accesso = new AccessoComponent("Venoso", venosiContainer, venosiAccessi);
            venosiAccessi.add(accesso);
            venosiContainer.add(accesso);
        });

        H4 arteriosiTitle = new H4("Accessi Arteriosi");
        arteriosiTitle.addClassName(LumoUtility.Margin.Top.MEDIUM);

        Button addArteriosiButton = new Button("Aggiungi accesso arterioso", new Icon(VaadinIcon.PLUS));
        addArteriosiButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addArteriosiButton.addClassName(LumoUtility.Margin.Top.SMALL);
        addArteriosiButton.addClickListener(e -> {
            AccessoComponent accesso = new AccessoComponent("Arterioso", arteriosiContainer, arteriosiAccessi);
            arteriosiAccessi.add(accesso);
            arteriosiContainer.add(accesso);
        });

        // Aggiunta dei componenti al layout
        parametriT0Layout.add(venosiTitle, venosiContainer, addVenosiButton,
                arteriosiTitle, arteriosiContainer, addArteriosiButton);

        // Carica accessi venosi esistenti
        if (parametriT0 != null && parametriT0.getAccessiVenosi() != null && !parametriT0.getAccessiVenosi().isEmpty()) {
            for (var accesso : parametriT0.getAccessiVenosi()) {
                AccessoComponent accessoComp = new AccessoComponent("Venoso", venosiContainer, venosiAccessi);
                accessoComp.setTipo(accesso.getTipologia());
                accessoComp.setPosizione(accesso.getPosizione());
                venosiAccessi.add(accessoComp);
                venosiContainer.add(accessoComp);
            }
        }

        // Carica accessi arteriosi esistenti
        if (parametriT0 != null && parametriT0.getAccessiArteriosi() != null && !parametriT0.getAccessiArteriosi().isEmpty()) {
            for (var accesso : parametriT0.getAccessiArteriosi()) {
                AccessoComponent accessoComp = new AccessoComponent("Arterioso", arteriosiContainer, arteriosiAccessi);
                accessoComp.setTipo(accesso.getTipologia());
                accessoComp.setPosizione(accesso.getPosizione());
                arteriosiAccessi.add(accessoComp);
                arteriosiContainer.add(accessoComp);
            }
        }

        //Inizio Esame Fisico
        VerticalLayout esameFisicoLayout = new VerticalLayout();
        esameFisicoLayout.setPadding(false);
        esameFisicoLayout.setSpacing(true);
        esameFisicoLayout.setWidthFull();

        // Recupera i dati dell'esame fisico
        EsameFisico esamiFisici = scenarioService.getEsameFisicoById(scenarioId);

        // Esame fisico generale
        VerticalLayout generaleArea = createTinyMcePanel(
                "GENERALE",
                "Inserisci i dettagli dell'esame generale...",
                esamiFisici != null ? esamiFisici.getSection("Generale") : ""
        );

        // Esame fisico pupille
        VerticalLayout pupilleArea = createTinyMcePanel(
                "PUPILLE",
                "Inserisci i dettagli dell'esame delle pupille...",
                esamiFisici != null ? esamiFisici.getSection("Pupille") : ""
        );

        // Esame fisico collo
        VerticalLayout colloArea = createTinyMcePanel(
                "COLLO",
                "Inserisci i dettagli dell'esame del collo...",
                esamiFisici != null ? esamiFisici.getSection("Collo") : ""
        );

        // Esame fisico torace
        VerticalLayout toraceArea = createTinyMcePanel(
                "TORACE",
                "Inserisci i dettagli dell'esame del torace...",
                esamiFisici != null ? esamiFisici.getSection("Torace") : ""
        );

        // Esame fisico cuore
        VerticalLayout cuoreArea = createTinyMcePanel(
                "CUORE",
                "Inserisci i dettagli dell'esame del cuore...",
                esamiFisici != null ? esamiFisici.getSection("Cuore") : ""
        );

        // Esame fisico addome
        VerticalLayout addomeArea = createTinyMcePanel(
                "ADDOME",
                "Inserisci i dettagli dell'esame dell'addome...",
                esamiFisici != null ? esamiFisici.getSection("Addome") : ""
        );

        // Esame fisico retto
        VerticalLayout rettoArea = createTinyMcePanel(
                "RETTO",
                "Inserisci i dettagli dell'esame rettale...",
                esamiFisici != null ? esamiFisici.getSection("Retto") : ""
        );

        // Esame fisico cute
        VerticalLayout cuteArea = createTinyMcePanel(
                "CUTE",
                "Inserisci i dettagli dell'esame della cute...",
                esamiFisici != null ? esamiFisici.getSection("Cute") : ""
        );

        // Esame fisico estremità
        VerticalLayout estremitaArea = createTinyMcePanel(
                "ESTREMITÀ",
                "Inserisci i dettagli dell'esame delle estremità...",
                esamiFisici != null ? esamiFisici.getSection("Estremità") : ""
        );

        // Esame fisico neurologico
        VerticalLayout neurologicoArea = createTinyMcePanel(
                "NEUROLOGICO",
                "Inserisci i dettagli dell'esame neurologico...",
                esamiFisici != null ? esamiFisici.getSection("Neurologico") : ""
        );

        // Esame fisico FAST
        VerticalLayout FASTArea = createTinyMcePanel(
                "FAST ECOGRAFIA",
                "Inserisci i dettagli dell'ecografia FAST...",
                esamiFisici != null ? esamiFisici.getSection("FAST") : ""
        );
        //Fine Esame Fisico

        // Aggiunta dei campi all'area dell'esame fisico
        esameFisicoLayout.add(
                generaleArea,
                pupilleArea,
                colloArea,
                toraceArea,
                cuoreArea,
                addomeArea,
                rettoArea,
                cuteArea,
                estremitaArea,
                neurologicoArea,
                FASTArea
        );

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
                informazioniGeneraliDetails,
                esamiRefertiDetails,
                parametriT0Details,
                esameFisicoDetails
        );

        if(!scenarioType.equals("Quick Scenario")){
            // Creazione tendina per i tempi
            Details tempiDetails = new Details("TIMELINE SIMULAZIONE", createTempiComponent());
            tempiDetails.setWidthFull();
            tempiDetails.addClassName(LumoUtility.Margin.Top.LARGE);
            tempiDetails.setOpened(false);

            contentLayout.add(tempiDetails);
        }

        // Aggiunta della tendina per la sceneggiatura solo se il tipo di scenario è "Patient Simulated Scenario"
        if (scenarioType.equals("Patient Simulated Scenario")) {
            TextArea sceneggiaturaArea = createTextArea(
                    "SCENEGGIATURA",
                    "Inserisci la sceneggiatura dello scenario...",
                    ScenarioService.getSceneggiatura(scenarioId)
            );
            VerticalLayout sceneggiaturaLayout = new VerticalLayout();
            sceneggiaturaLayout.setPadding(false);
            sceneggiaturaLayout.setSpacing(true);
            sceneggiaturaLayout.setWidthFull();

            sceneggiaturaLayout.add(sceneggiaturaArea);

            // Creazione tendina per la sceneggiatura
            Details sceneggiaturaDetails = new Details("SCENEGGIATURA", sceneggiaturaLayout);
            sceneggiaturaDetails.setWidthFull();
            sceneggiaturaDetails.addClassName(LumoUtility.Margin.Top.LARGE);
            sceneggiaturaDetails.setOpened(false);

            contentLayout.add(sceneggiaturaDetails);
        }

        mainLayout.add(customHeader, contentLayout);
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
     * Crea un'area di testo con etichetta e segnaposto.
     *
     * @param label       etichetta dell'area di testo
     * @param placeholder segnaposto dell'area di testo
     * @param value       valore iniziale dell'area di testo
     * @return area di testo creata
     */
    private TextArea createTextArea(String label, String placeholder, String value) {
        TextArea area = new TextArea(label);
        area.setPlaceholder(placeholder);
        area.setValue(value != null ? value : "");
        area.setWidthFull();
        area.setMinHeight("300px");
        area.getStyle().set("max-width", "100%");
        area.addClassName(LumoUtility.Margin.Top.LARGE);
        return area;
    }

    /**
     * Componente per rappresentare un accesso (venoso o arterioso).
     */
    private static class AccessoComponent extends HorizontalLayout {
        /**
         * Selettore per il tipo di accesso.
         */
        private final Select<String> tipoSelect;
        /**
         * Campo di testo per la posizione dell'accesso.
         */
        private final TextField posizioneField;
        /**
         * Container che contiene questo componente.
         */
        private final VerticalLayout container;
        /**
         * Lista di accessi a cui appartiene questo componente.
         */
        private final List<AccessoComponent> listaAccessi;

        /**
         * Costruttore del componente di accesso.
         *
         * @param tipo         tipo di accesso (Venoso o Arterioso)
         * @param container    container che contiene questo componente
         * @param listaAccessi lista dove viene memorizzato questo componente
         */
        public AccessoComponent(String tipo, VerticalLayout container, List<AccessoComponent> listaAccessi) {
            this.container = container;
            this.listaAccessi = listaAccessi;

            setWidthFull();
            setAlignItems(Alignment.BASELINE);
            setSpacing(true);

            tipoSelect = new Select<>();
            tipoSelect.setLabel("Tipo accesso " + tipo);
            if (tipo.equals("Venoso")) {
                tipoSelect.setItems("Periferico", "Centrale", "PICC", "Midline", "Altro");
            } else {
                tipoSelect.setItems("Radiale", "Femorale", "Omerale", "Altro");
            }
            tipoSelect.setWidth("200px");

            posizioneField = new TextField("Posizione");
            posizioneField.setWidthFull();

            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            removeButton.getStyle().set("margin-top", "auto");
            removeButton.addClickListener(e -> removeSelf());

            add(tipoSelect, posizioneField, removeButton);
        }

        /**
         * Imposta il tipo di accesso.
         *
         * @param tipo tipo di accesso
         */
        public void setTipo(String tipo) {
            tipoSelect.setValue(tipo);
        }

        /**
         * Imposta la posizione dell'accesso.
         *
         * @param posizione posizione dell'accesso
         */
        public void setPosizione(String posizione) {
            posizioneField.setValue(posizione);
        }

        /**
         * Rimuove questo componente dal container e dalla lista.
         */
        private void removeSelf() {
            container.remove(this);
            listaAccessi.remove(this);
        }
    }

    /**
     * Crea un componente per visualizzare i tempi della simulazione.
     *
     * @return il componente creato
     */
    private Component createTempiComponent() {
        // Crea un layout verticale per contenere i componenti della TempoView
        VerticalLayout tempiLayout = new VerticalLayout();
        tempiLayout.setPadding(false);
        tempiLayout.setSpacing(true);
        tempiLayout.setWidthFull();

        // Aggiungi un bottone per aprire la pagina completa se necessario
        Button openFullPageButton = new Button("Visualizza in pagina completa", new Icon(VaadinIcon.EXTERNAL_LINK));
        openFullPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFullPageButton.addClickListener(e -> UI.getCurrent().navigate("tempi/" + scenarioId + "/edit"));

        // Crea un iframe o embed della pagina TempoView
        IFrame tempiFrame = new IFrame("tempi/" + scenarioId + "/edit");
        tempiFrame.setHeight("600px");
        tempiFrame.setWidthFull();
        tempiLayout.add(openFullPageButton, tempiFrame);

        return tempiLayout;
    }

    /**
     * Crea un componente per visualizzare gli esami e i referti.
     *
     * @return il componente creato
     */
    private Component createEsamiRefertiComponent() {
        VerticalLayout esamiLayout = new VerticalLayout();
        esamiLayout.setPadding(false);
        esamiLayout.setSpacing(true);
        esamiLayout.setWidthFull();

        // Aggiungi un bottone per aprire la pagina completa se necessario
        Button openFullPageButton = new Button("Visualizza in pagina completa", new Icon(VaadinIcon.EXTERNAL_LINK));
        openFullPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFullPageButton.addClickListener(e -> UI.getCurrent().navigate("esamiReferti/" + scenarioId + "/edit"));

        // Crea un iframe o embed della pagina EsamiRefertiView
        IFrame esamiFrame = new IFrame("esamiReferti/" + scenarioId + "/edit");
        esamiFrame.setHeight("600px");
        esamiFrame.setWidthFull();
        esamiLayout.add(openFullPageButton, esamiFrame);

        return esamiLayout;
    }

    /**
     * Crea un pannello con titolo, sottotitolo e editor TinyMce.
     *
     * @param titolo     titolo del pannello
     * @param sottotitolo sottotitolo del pannello
     * @param valore     valore iniziale dell'editor
     * @return layout verticale contenente il pannello completo
     */
    private VerticalLayout createTinyMcePanel(String titolo, String sottotitolo, String valore) {
        H3 titleElement = new H3(titolo);
        Paragraph subtitleElement = new Paragraph(sottotitolo);

        TinyMce editor = new TinyMce();
        editor.setValue(valore != null ? valore : "");
        editor.setHeight("300px");

        VerticalLayout panelLayout = new VerticalLayout();
        panelLayout.add(titleElement, subtitleElement, editor);

        return panelLayout;
    }
}