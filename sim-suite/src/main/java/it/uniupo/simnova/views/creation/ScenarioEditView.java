package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.EsameFisico;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Modifica Scenario")
@Route(value = "modificaScenario", layout = MainLayout.class)
public class ScenarioEditView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private Scenario scenario;
    private static final Logger logger = LoggerFactory.getLogger(ScenarioEditView.class);


    public ScenarioEditView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException();
            }
            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }
            // Carica ora lo Scenario
            scenario = scenarioService.getScenarioById(scenarioId);
            buildView();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
        }
    }

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

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("800px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        String scenarioType = scenarioService.getScenarioType(scenarioId);
        TextField scenarioTypeField = createTextField("TIPO SCENARIO", "Inserisci il tipo di scenario");
        scenarioTypeField.setValue(scenarioType);
        scenarioTypeField.setReadOnly(true);
        // Campi del form
        TextField scenarioTitle = createTextField("TITOLO SCENARIO", "Inserisci il titolo dello scenario");
        scenarioTitle.setValue(scenario.getTitolo());
        TextField patientName = createTextField("NOME PAZIENTE", "Inserisci il nome del paziente");
        patientName.setValue(scenario.getNomePaziente());
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

        VerticalLayout informazioniGeneraliLayout = new VerticalLayout();
        informazioniGeneraliLayout.setWidth("100%");
        informazioniGeneraliLayout.setMaxWidth("800px");
        informazioniGeneraliLayout.setPadding(true);
        informazioniGeneraliLayout.setSpacing(false);
        informazioniGeneraliLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        informazioniGeneraliLayout.getStyle().set("margin", "0 auto").set("flex-grow", "1");

        TextArea descriptionArea = createTextArea(
                "DESCRIZIONE SCENARIO",
                "Inserisci una descrizione dettagliata dello scenario...",
                scenario.getDescrizione()
        );

        TextArea briefingArea = createTextArea(
                "BRIEFING",
                "Inserisci il testo da leggere ai discenti prima della simulazione...",
                scenario.getBriefing()
        );

        TextArea pattoAulaArea = createTextArea(
                "PATTO D'AULA / FAMILIARIZZAZIONE",
                "Inserisci il testo da mostrare nella sala...",
                scenario.getPattoAula()
        );

        TextArea azioniChiaveArea = createTextArea(
                "AZIONI CHIAVE",
                "Inserisci le azioni da valutare durante il debriefing...",
                scenario.getAzioneChiave()
        );

        TextArea obiettiviArea = createTextArea(
                "OBIETTIVI DIDATTICI",
                "Inserisci gli obiettivi didattici dello scenario...",
                scenario.getObiettivo()
        );

        TextArea materialeArea = createTextArea(
                "MATERIALE NECESSARIO",
                "Elenca il materiale necessario per l'allestimento della sala...",
                scenario.getMateriale()
        );


        TextArea moulageArea = createTextArea(
                "MOULAGE",
                "Descrivi il trucco da applicare al manichino/paziente simulato...",
                scenario.getMoulage()
        );

        TextArea liquidiArea = createTextArea(
                "LIQUIDI E PRESIDI IN T0",
                "Indica quantità di liquidi e presidi presenti all'inizio della simulazione...",
                scenario.getLiquidi()
        );

        informazioniGeneraliLayout.add(
                descriptionArea,
                briefingArea,
                pattoAulaArea,
                azioniChiaveArea,
                obiettiviArea,
                materialeArea,
                moulageArea,
                liquidiArea
        );

        // Aggiungi una nuova sezione per Parametri Paziente T0 in una tendina
        VerticalLayout parametriT0Layout = new VerticalLayout();
        parametriT0Layout.setPadding(false);
        parametriT0Layout.setSpacing(true);
        parametriT0Layout.setWidthFull();

        // Recupera i dati del paziente T0
        var parametriT0 = scenarioService.getPazienteT0ById(scenarioId);

        if (parametriT0 != null) {
            // Crea i componenti per visualizzare i dati
            TextField paField = new TextField("PA (mmHg)");
            paField.setValue(parametriT0.getPA());
            paField.setWidthFull();

            TextField fcField = new TextField("FC (bpm)");
            fcField.setValue(String.valueOf(parametriT0.getFC()));
            fcField.setWidthFull();

            TextField rrField = new TextField("RR (atti/min)");
            rrField.setValue(String.valueOf(parametriT0.getRR()));
            rrField.setWidthFull();

            TextField tempField = new TextField("Temperatura (°C)");
            tempField.setValue(String.valueOf(parametriT0.getT()));
            tempField.setWidthFull();

            TextField spo2Field = new TextField("SpO₂ (%)");
            spo2Field.setValue(String.valueOf(parametriT0.getSpO2()));
            spo2Field.setWidthFull();

            TextField etco2Field = new TextField("EtCO₂ (mmHg)");
            etco2Field.setValue(String.valueOf(parametriT0.getEtCO2()));
            etco2Field.setWidthFull();

            // Area di testo per il monitoraggio
            TextArea monitorArea = new TextArea("Monitoraggio");
            monitorArea.setValue(parametriT0.getMonitor() != null ? parametriT0.getMonitor() : "");
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

            // Aggiungi i componenti al layout
            parametriT0Layout.add(venosiTitle, venosiContainer, addVenosiButton,
                    arteriosiTitle, arteriosiContainer, addArteriosiButton);

            // Carica accessi venosi esistenti
            if (parametriT0.getAccessiVenosi() != null && !parametriT0.getAccessiVenosi().isEmpty()) {
                for (var accesso : parametriT0.getAccessiVenosi()) {
                    AccessoComponent accessoComp = new AccessoComponent("Venoso", venosiContainer, venosiAccessi);
                    accessoComp.setTipo(accesso.getTipologia());
                    accessoComp.setPosizione(accesso.getPosizione());
                    venosiAccessi.add(accessoComp);
                    venosiContainer.add(accessoComp);
                }
            }

            // Carica accessi arteriosi esistenti
            if (parametriT0.getAccessiArteriosi() != null && !parametriT0.getAccessiArteriosi().isEmpty()) {
                for (var accesso : parametriT0.getAccessiArteriosi()) {
                    AccessoComponent accessoComp = new AccessoComponent("Arterioso", arteriosiContainer, arteriosiAccessi);
                    accessoComp.setTipo(accesso.getTipologia());
                    accessoComp.setPosizione(accesso.getPosizione());
                    arteriosiAccessi.add(accessoComp);
                    arteriosiContainer.add(accessoComp);
                }
            }
        } else {
            // Se non ci sono dati, mostra un messaggio
            Paragraph noDataMessage = new Paragraph("Nessun parametro paziente T0 disponibile");
            noDataMessage.addClassName(LumoUtility.TextColor.SECONDARY);
            parametriT0Layout.add(noDataMessage);
        }

        //Inizio Esame Fisico
        VerticalLayout esameFisicoLayout = new VerticalLayout();
        esameFisicoLayout.setPadding(false);
        esameFisicoLayout.setSpacing(true);
        esameFisicoLayout.setWidthFull();

        EsameFisico esamiFisici = scenarioService.getEsameFisicoById(scenarioId);
        TextArea generaleArea = createTextArea(
                "GENERALE",
                "Inserisci i dettagli dell'esame generale...",
                esamiFisici.getSection("Generale")
        );

        TextArea pupilleArea = createTextArea(
                "PUPILLE",
                "Inserisci i dettagli dell'esame delle pupille...",
                esamiFisici.getSection("Pupille")
        );

        TextArea colloArea = createTextArea(
                "COLLO",
                "Inserisci i dettagli dell'esame del collo...",
                esamiFisici.getSection("Collo")
        );

        TextArea toraceArea = createTextArea(
                "TORACE",
                "Inserisci i dettagli dell'esame del torace...",
                esamiFisici.getSection("Torace")
        );

        TextArea cuoreArea = createTextArea(
                "CUORE",
                "Inserisci i dettagli dell'esame del cuore...",
                esamiFisici.getSection("Cuore")
        );

        TextArea addomeArea = createTextArea(
                "ADDOME",
                "Inserisci i dettagli dell'esame dell'addome...",
                esamiFisici.getSection("Addome")
        );

        TextArea rettoArea = createTextArea(
                "RETTO",
                "Inserisci i dettagli dell'esame rettale...",
                esamiFisici.getSection("Retto")
        );

        TextArea cuteArea = createTextArea(
                "CUTE",
                "Inserisci i dettagli dell'esame della cute...",
                esamiFisici.getSection("Cute")
        );

        TextArea estremitaArea = createTextArea(
                "ESTREMITÀ",
                "Inserisci i dettagli dell'esame delle estremità...",
                esamiFisici.getSection("Estremità")
        );

        TextArea neurologicoArea = createTextArea(
                "NEUROLOGICO",
                "Inserisci i dettagli dell'esame neurologico...",
                esamiFisici.getSection("Neurologico")
        );

        TextArea FASTArea = createTextArea(
                "FAST ECOGRAFIA",
                "Inserisci i dettagli dell'ecografia FAST...",
                esamiFisici.getSection("FAST")
        );
        //Fine Esame Fisico

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

        Details esameFisicoDetails = new Details("ESAME FISICO", esameFisicoLayout);
        esameFisicoDetails.setWidthFull();
        esameFisicoDetails.addClassName(LumoUtility.Margin.Top.LARGE);
        esameFisicoDetails.setOpened(false);

        Details informazioniGeneraliDetails = new Details("INFORMAZIONI GENERALI", informazioniGeneraliLayout);
        informazioniGeneraliDetails.setWidthFull();
        informazioniGeneraliDetails.addClassName(LumoUtility.Margin.Top.LARGE);
        informazioniGeneraliDetails.setOpened(false);

        Details parametriT0Details = new Details("PARAMETRI PAZIENTE T0", parametriT0Layout);
        parametriT0Details.setWidthFull();
        parametriT0Details.addClassName(LumoUtility.Margin.Top.LARGE);
        parametriT0Details.setOpened(false);

        Details tempiDetails = new Details("TIMELINE SIMULAZIONE", createTempiComponent());
        tempiDetails.setWidthFull();
        tempiDetails.addClassName(LumoUtility.Margin.Top.LARGE);
        tempiDetails.setOpened(false);

        Details esamiRefertiDetails = new Details("ESAMI E REFERTI", createEsamiRefertiComponent());
        esamiRefertiDetails.setWidthFull();
        esamiRefertiDetails.addClassName(LumoUtility.Margin.Top.LARGE);
        esamiRefertiDetails.setOpened(false);



        contentLayout.add(
                scenarioTypeField,
                scenarioTitle,
                patientName,
                pathology,
                durationField,
                informazioniGeneraliDetails,
                esamiRefertiDetails,
                parametriT0Details,
                esameFisicoDetails,
                tempiDetails
        );

        if (scenarioType.equals("Patient Simulated Scenario")) {
            TextArea sceneggiaturaArea = createTextArea(
                    "SCENEGGIATURA",
                    "Inserisci la sceneggiatura dello scenario...",
                    scenarioService.getSceneggiatura(scenarioId)
            );
            VerticalLayout sceneggiaturaLayout = new VerticalLayout();
            sceneggiaturaLayout.setPadding(false);
            sceneggiaturaLayout.setSpacing(true);
            sceneggiaturaLayout.setWidthFull();

            sceneggiaturaLayout.add(sceneggiaturaArea);

            Details sceneggiaturaDetails = new Details("SCENEGGIATURA", sceneggiaturaLayout);
            sceneggiaturaDetails.setWidthFull();
            sceneggiaturaDetails.addClassName(LumoUtility.Margin.Top.LARGE);
            sceneggiaturaDetails.setOpened(false);

            contentLayout.add(sceneggiaturaDetails);
        }

        mainLayout.add(customHeader, contentLayout);
    }

    private TextField createTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.getStyle().set("max-width", "500px");
        field.addClassName(LumoUtility.Margin.Top.LARGE);
        return field;
    }

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
     * Componente per la gestione degli accessi venosi e arteriosi.
     */
    private static class AccessoComponent extends HorizontalLayout {
        private final Select<String> tipoSelect;
        private final TextField posizioneField;
        private final VerticalLayout container;
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

        /**
         * Ottiene i dati dell'accesso.
         *
         * @return dati dell'accesso
         */
        public AccessoData getData() {
            return new AccessoData(
                    tipoSelect.getValue(),
                    posizioneField.getValue()
            );
        }
    }

    /**
     * Record per rappresentare i dati di un accesso.
     */
    public record AccessoData(String tipo, String posizione) {
    }

    private Component createTempiComponent() {
        // Crea un layout verticale per contenere i componenti della TempoView
        VerticalLayout tempiLayout = new VerticalLayout();
        tempiLayout.setPadding(false);
        tempiLayout.setSpacing(true);
        tempiLayout.setWidthFull();

        // Aggiungi un bottone per aprire la pagina completa se necessario
        Button openFullPageButton = new Button("Visualizza in pagina completa", new Icon(VaadinIcon.EXTERNAL_LINK));
        openFullPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFullPageButton.addClickListener(e -> UI.getCurrent().navigate("tempi/" + scenarioId+"?mode=edit"));

        // Crea un iframe o embed della pagina TempoView
        IFrame tempiFrame = new IFrame("tempi/" + scenarioId+"?mode=edit");
        tempiFrame.setHeight("600px");
        tempiFrame.setWidthFull();
        tempiLayout.add(openFullPageButton, tempiFrame);

        return tempiLayout;
    }

    private Component createEsamiRefertiComponent(){
        VerticalLayout esamiLayout = new VerticalLayout();
        esamiLayout.setPadding(false);
        esamiLayout.setSpacing(true);
        esamiLayout.setWidthFull();

        // Aggiungi un bottone per aprire la pagina completa se necessario
        Button openFullPageButton = new Button("Visualizza in pagina completa", new Icon(VaadinIcon.EXTERNAL_LINK));
        openFullPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFullPageButton.addClickListener(e -> UI.getCurrent().navigate("esamiReferti/" + scenarioId+"?mode=edit"));

        // Crea un iframe o embed della pagina EsamiRefertiView
        IFrame esamiFrame = new IFrame("esamiReferti/" + scenarioId+"?mode=edit");
        esamiFrame.setHeight("600px");
        esamiFrame.setWidthFull();
        esamiLayout.add(openFullPageButton, esamiFrame);

        return esamiLayout;
    }
}