package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.ParametroAggiuntivo;
import it.uniupo.simnova.api.model.PazienteT0;
import it.uniupo.simnova.api.model.Tempo;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Tempo")
@Route("tempo")
@Menu(order = 14)
public class TempoView extends Composite<VerticalLayout> implements HasUrlParameter<Integer> {

    private final VerticalLayout timeSectionsContainer;
    private final List<TimeSection> timeSections = new ArrayList<>();
    private int timeCount = 1;
    private final Button nextButton;
    private int scenarioId;
    private final ScenarioService scenarioService;
    public static final String CUSTOM_PARAMETER_KEY = "CUSTOM";
    public static final Map<String, String> ADDITIONAL_PARAMETERS = new LinkedHashMap<>();

    static {
        // Cardiologia / Monitor Multiparametrico
        ADDITIONAL_PARAMETERS.put("PVC", "Pressione Venosa Centrale (cmH₂O)");
        ADDITIONAL_PARAMETERS.put("QTc", "QT/QTc (ms)");
        ADDITIONAL_PARAMETERS.put("ST", "Segmento ST (mV)");
        ADDITIONAL_PARAMETERS.put("SI", "Indice di Shock (FC/PA sistolica)");

        // Pneumologia / Ventilazione
        ADDITIONAL_PARAMETERS.put("PIP", "Pressione Inspiratoria Positiva (cmH₂O)");
        ADDITIONAL_PARAMETERS.put("VT", "Volume Corrente (mL/kg)");
        ADDITIONAL_PARAMETERS.put("COMP", "Compliance Polmonare (mL/cmH₂O)");
        ADDITIONAL_PARAMETERS.put("RAW", "Resistenza Vie Aeree (cmH₂O/L/s)");
        ADDITIONAL_PARAMETERS.put("RSBI", "Indice di Tobin (atti/min/L)");

        // Neurologia / Neuro Monitoraggio
        ADDITIONAL_PARAMETERS.put("GCS", "Scala di Glasgow (3-15)");
        ADDITIONAL_PARAMETERS.put("ICP", "Pressione Intracranica (mmHg)");
        ADDITIONAL_PARAMETERS.put("PRx", "Indice di Pressione Cerebrale");
        ADDITIONAL_PARAMETERS.put("BIS", "Bispectral Index (0-100)");
        ADDITIONAL_PARAMETERS.put("TOF", "Train of Four (%)");

        // Emodinamica / Terapia Intensiva
        ADDITIONAL_PARAMETERS.put("CO", "Gittata Cardiaca (L/min)");
        ADDITIONAL_PARAMETERS.put("CI", "Indice Cardiaco (L/min/m²)");
        ADDITIONAL_PARAMETERS.put("PCWP", "Pressione Capillare Polmonare (mmHg)");
        ADDITIONAL_PARAMETERS.put("SvO2", "Saturazione Venosa Mista (%)");
        ADDITIONAL_PARAMETERS.put("SVR", "Resistenza Vascolare Sistemica (dyn·s·cm⁻⁵)");

        // Metabolismo / Elettroliti
        ADDITIONAL_PARAMETERS.put("GLY", "Glicemia (mg/dL)");
        ADDITIONAL_PARAMETERS.put("LAC", "Lattati (mmol/L)");
        ADDITIONAL_PARAMETERS.put("NA", "Sodio (mmol/L)");
        ADDITIONAL_PARAMETERS.put("K", "Potassio (mmol/L)");
        ADDITIONAL_PARAMETERS.put("CA", "Calcio ionizzato (mmol/L)");

        // Nefrologia / Diuresi
        ADDITIONAL_PARAMETERS.put("UO", "Diuresi oraria (mL/h)");
        ADDITIONAL_PARAMETERS.put("CR", "Creatinina (mg/dL)");
        ADDITIONAL_PARAMETERS.put("BUN", "Azotemia (mg/dL)");

        // Infettivologia / Stato Infettivo
        ADDITIONAL_PARAMETERS.put("WBC", "Globuli Bianchi (10³/μL)");
        ADDITIONAL_PARAMETERS.put("qSOFA", "qSOFA (0-4)");

        // Coagulazione / Ematologia
        ADDITIONAL_PARAMETERS.put("INR", "INR");
        ADDITIONAL_PARAMETERS.put("PTT", "PTT (sec)");
        ADDITIONAL_PARAMETERS.put("PLT", "Piastrine (10³/μL)");

        //Altri Monitoraggi Specializzati
        ADDITIONAL_PARAMETERS.put("pCO₂ cutanea", "pCO₂ cutanea (mmHg)");
        ADDITIONAL_PARAMETERS.put("NIRS", "Ossimetria cerebrale (%)");
    }

    public TempoView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER con pulsante indietro e titolo
        AppHeader header = new AppHeader();

        // Pulsante indietro con RouterLink
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("descrizione/" + scenarioId)));

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);
        customHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        // Titolo della pagina
        H2 pageTitle = new H2("TEMPO");
        pageTitle.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.LARGE,
                LumoUtility.Margin.Horizontal.AUTO
        );
        pageTitle.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("width", "100%");

        // Istruzioni
        Paragraph instructionText = new Paragraph(
                "Definisci i tempi dello scenario. Per ogni tempo (T0, T1, T2, ecc.) specifica i parametri " +
                        "e le transizioni possibili. T0 rappresenta lo stato iniziale.");
        instructionText.setWidth("100%");
        instructionText.getStyle().set("font-size", "var(--lumo-font-size-m)");
        instructionText.addClassName(LumoUtility.Margin.Bottom.LARGE);

        // Container per le sezioni dei tempi
        timeSectionsContainer = new VerticalLayout();
        timeSectionsContainer.setWidthFull();
        timeSectionsContainer.setSpacing(true);

        // Pulsante per aggiungere nuovi tempi
        Button addTimeButton = new Button("Aggiungi Tempo", new Icon(VaadinIcon.PLUS));
        addTimeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTimeButton.addClassName(LumoUtility.Margin.Top.MEDIUM);
        addTimeButton.addClickListener(event -> addTimeSection(timeCount++));

        contentLayout.add(pageTitle, instructionText, timeSectionsContainer, addTimeButton);

        // 3. FOOTER
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px");
        nextButton.addClickListener(e -> saveAllTimeSections());

        Paragraph credits = new Paragraph("Sviluppato e creato da Alessandro Zappatore");
        credits.addClassName(LumoUtility.TextColor.SECONDARY);
        credits.addClassName(LumoUtility.FontSize.XSMALL);
        credits.getStyle().set("margin", "0");

        footerLayout.add(credits, nextButton);

        // Aggiunta dei layout principali
        mainLayout.add(customHeader, contentLayout, footerLayout);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer parameter) {
        if (parameter != null) {
            this.scenarioId = parameter;
            System.out.println("Scenario ID ricevuto: " + scenarioId);
            loadInitialData();
        } else {
            Notification.show("ID scenario mancante", 3000, Notification.Position.MIDDLE);
            event.getUI().navigate("creation");
        }
    }

    private void addTimeSection(int timeNumber) {
        TimeSection timeSection = new TimeSection(timeNumber);
        timeSections.add(timeSection);
        timeSectionsContainer.add(timeSection.getLayout());

        if (timeNumber == 0) {
            timeSection.hideRemoveButton();
        }

        // Aggiungi pulsante per parametri aggiuntivi a tutte le sezioni (incluso T0)
        Button addParamsButton = new Button("Aggiungi Parametri", new Icon(VaadinIcon.PLUS));
        addParamsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addParamsButton.addClassName(LumoUtility.Margin.Top.SMALL);
        addParamsButton.addClickListener(e -> showAdditionalParamsDialog(timeSection));

        // Aggiungi il pulsante al form layout
        timeSection.getMedicalParamsForm().add(addParamsButton);
    }

    private void showAdditionalParamsDialog(TimeSection timeSection) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleziona Parametri Aggiuntivi");

        // Pulsante per aggiungere parametro personalizzato
        Button addCustomParamButton = new Button("Nuovo Parametro Personalizzato",
                new Icon(VaadinIcon.PLUS_CIRCLE));
        addCustomParamButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addCustomParamButton.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        addCustomParamButton.addClickListener(e -> {
            dialog.close();
            showCustomParamDialog(timeSection);
        });

        // Ottieni i parametri già selezionati
        Set<String> alreadySelected = timeSection.getCustomParameters().keySet();

        // Filtra i parametri disponibili
        List<String> availableParams = ADDITIONAL_PARAMETERS.entrySet().stream()
                .filter(entry -> !alreadySelected.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        CheckboxGroup<String> paramsSelector = new CheckboxGroup<>();
        paramsSelector.setItems(availableParams);
        paramsSelector.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        paramsSelector.setWidthFull();

        Button confirmButton = new Button("Conferma", e -> {
            paramsSelector.getSelectedItems().forEach(param -> {
                String paramKey = ADDITIONAL_PARAMETERS.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(param))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse("");

                if (!paramKey.isEmpty()) {
                    timeSection.addCustomParameter(paramKey, param);
                }
            });
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogContent = new VerticalLayout(
                addCustomParamButton,
                new Paragraph("Seleziona dai parametri predefiniti:"),
                paramsSelector
        );
        dialog.add(dialogContent, buttons);
        dialog.open();
    }

    private void showCustomParamDialog(TimeSection timeSection) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aggiungi Parametro Personalizzato");

        TextField nameField = new TextField("Nome parametro");
        nameField.setWidthFull();

        TextField unitField = new TextField("Unità di misura");
        unitField.setWidthFull();

        NumberField valueField = new NumberField("Valore");
        valueField.setWidthFull();

        Button saveButton = new Button("Salva", e -> {
            String paramName = nameField.getValue();
            String unit = unitField.getValue();

            if (paramName == null || paramName.isEmpty()) {
                Notification.show("Inserisci un nome per il parametro",
                        3000, Notification.Position.MIDDLE);
                return;
            }

            // Crea una chiave univoca per il parametro personalizzato
            String paramKey = CUSTOM_PARAMETER_KEY + "_" + System.currentTimeMillis();
            String fullLabel = paramName + (unit.isEmpty() ? "" : " (" + unit + ")");

            // Aggiungi alla mappa dei parametri (opzionale, se vuoi tener traccia)
            ADDITIONAL_PARAMETERS.put(paramKey, fullLabel);

            // Aggiungi il parametro alla sezione
            timeSection.addCustomParameter(paramKey, fullLabel);

            // Imposta il valore se fornito
            if (valueField.getValue() != null) {
                timeSection.getCustomParameters().get(paramKey).setValue(valueField.getValue());
            }

            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogContent = new VerticalLayout(
                new Paragraph("Definisci un nuovo parametro:"),
                nameField,
                unitField,
                valueField
        );
        dialog.add(dialogContent, buttons);
        dialog.open();
    }

    private void saveAllTimeSections() {
        try {
            List<ScenarioService.TempoData> allTempiData = new ArrayList<>();

            for (TimeSection section : timeSections) {
                ScenarioService.TempoData tempoData = section.prepareDataForSave();
                allTempiData.add(tempoData);
                System.out.println("Preparato tempo T" + tempoData.idTempo() + ": " + tempoData);
            }

            // Salva tutti i tempi nel database
            boolean success = scenarioService.saveTempi(scenarioId, allTempiData);

            if (success) {
                Notification.show("Tempi salvati con successo!", 3000, Notification.Position.MIDDLE);
                System.out.println("Tempi salvati con successo per scenario " + scenarioId);
                switch (scenarioService.getScenarioType(scenarioId)) {
                    case "Quick Scenario":
                        Notification.show("Tipo di scenario errato", 3000, Notification.Position.MIDDLE);
                        System.err.println("Tipo di scenario non riconosciuto per ID " + scenarioId);
                        break;
                    case "Advanced Scenario":
                        nextButton.getUI().ifPresent(ui -> ui.navigate("scenari/" + scenarioId));
                        break;
                    case "Patient Simulated Scenario":
                        nextButton.getUI().ifPresent(ui -> ui.navigate("sceneggiatura/" + scenarioId));
                        break;
                    default:
                        Notification.show("Tipo di scenario non riconosciuto", 3000, Notification.Position.MIDDLE);
                        System.err.println("Tipo di scenario non riconosciuto per ID " + scenarioId);
                        break;
                }
            } else {
                Notification.show("Errore durante il salvataggio dei tempi", 5000, Notification.Position.MIDDLE);
                System.err.println("Errore durante il salvataggio dei tempi per scenario " + scenarioId);
            }
        } catch (Exception e) {
            Notification.show("Errore durante il salvataggio: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE);
            System.err.println("Errore durante il salvataggio dei tempi:");
            e.printStackTrace();
        }
    }

    private NumberField createMedicalField(String label, String unit) {
        NumberField field = new NumberField(label);
        field.setSuffixComponent(new Paragraph(unit));
        field.setWidthFull();
        field.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return field;
    }

    private TextField createTextField() {
        TextField field = new TextField("PA (mmHg)");
        field.setSuffixComponent(new Paragraph("mmHg"));
        field.setWidthFull();
        field.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return field;
    }

    private void loadInitialData() {
        try {
            PazienteT0 pazienteT0 = scenarioService.getPazienteT0ById(scenarioId);
            if (pazienteT0 != null) {
                // Aggiungi la sezione iniziale per T0 solo se non esiste già
                if (timeSections.isEmpty()) {
                    addTimeSection(0);
                }

                TimeSection t0Section = timeSections.getFirst();

                // Imposta i valori nei campi di T0
                t0Section.setPaValue(pazienteT0.getPA());
                t0Section.setFcValue(pazienteT0.getFC());
                t0Section.setRrValue(pazienteT0.getRR());
                t0Section.setTValue(pazienteT0.getT());
                t0Section.setSpo2Value(pazienteT0.getSpO2());
                t0Section.setEtco2Value(pazienteT0.getEtCO2());

                // Carica anche i tempi esistenti se presenti
                loadExistingTimes();

                Notification.show("I parametri di T0 sono precompilati e non modificabili",
                        3000, Notification.Position.BOTTOM_START);
            }
        } catch (Exception e) {
            Notification.show("Errore nel caricamento dei dati iniziali: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    private void loadExistingTimes() {
        List<Tempo> existingTempi = scenarioService.getTempiByScenarioId(scenarioId);
        if (existingTempi != null && !existingTempi.isEmpty()) {
            // Rimuovi T0 se già caricato da loadInitialData
            if (!timeSections.isEmpty() && timeSections.getFirst().timeNumber == 0) {
                timeSections.removeFirst();
                timeSectionsContainer.removeAll();
            }

            for (Tempo tempo : existingTempi) {
                // Aggiungi solo tempi successivi a T0
                if (tempo.getIdTempo() > 0 || timeSections.isEmpty()) {
                    addTimeSection(tempo.getIdTempo());
                    TimeSection section = timeSections.getLast();

                    // Imposta i valori base
                    section.paField.setValue(tempo.getPA());
                    section.fcField.setValue((double) tempo.getFC());
                    section.rrField.setValue((double) tempo.getRR());
                    section.tField.setValue((double) tempo.getT());
                    section.spo2Field.setValue((double) tempo.getSpO2());
                    section.etco2Field.setValue((double) tempo.getEtCO2());

                    // Imposta i campi di azione
                    section.actionDetailsArea.setValue(tempo.getAzione());
                    section.timeIfYesField.setValue(tempo.getTSi());
                    section.timeIfNoField.setValue(tempo.getTNo());
                    section.additionalDetailsArea.setValue(tempo.getAltriDettagli());

                    // Imposta il timer
                    if (tempo.getTimerTempo() > 0) {
                        section.timerPicker.setValue(LocalTime.ofSecondOfDay(tempo.getTimerTempo()));
                    }

                    // Carica i parametri aggiuntivi
                    loadAdditionalParameters(section, tempo.getIdTempo());
                }
            }
        }
    }

    private void loadAdditionalParameters(TimeSection section, int tempoId) {
        List<ParametroAggiuntivo> params = scenarioService.getParametriAggiuntiviById(tempoId, scenarioId);
        if (params != null && !params.isEmpty()) {
            for (ParametroAggiuntivo param : params) {
                String paramKey = ADDITIONAL_PARAMETERS.entrySet().stream()
                        .filter(e -> e.getKey().equals(param.getNome()) ||
                                e.getValue().contains(param.getNome()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(CUSTOM_PARAMETER_KEY + "_" + param.getNome());

                String label = param.getNome();
                if (param.getUnitaMisura() != null && !param.getUnitaMisura().isEmpty()) {
                    label += " (" + param.getUnitaMisura() + ")";
                }

                section.addCustomParameter(paramKey, label);
                section.customParameters.get(paramKey).setValue(Double.parseDouble(param.getValore()));
            }
        }
    }


    private class TimeSection {
        private final int timeNumber;
        private final VerticalLayout layout;
        private final TimePicker timerPicker;
        private final TextField paField;
        private final NumberField fcField;
        private final NumberField rrField;
        private final NumberField tField;
        private final NumberField spo2Field;
        private final NumberField etco2Field;
        private final TextArea actionDetailsArea;
        private final IntegerField timeIfYesField;
        private final IntegerField timeIfNoField;
        private final TextArea additionalDetailsArea;
        private final Button removeButton;
        private final FormLayout medicalParamsForm;
        private final VerticalLayout customParamsContainer;
        private final Map<String, NumberField> customParameters = new HashMap<>();
        private final Map<String, HorizontalLayout> customParameterLayouts = new HashMap<>();

        public TimeSection(int timeNumber) {
            this.timeNumber = timeNumber;

            layout = new VerticalLayout();
            layout.addClassName(LumoUtility.Padding.MEDIUM);
            layout.addClassName(LumoUtility.Border.ALL);
            layout.addClassName(LumoUtility.BorderColor.CONTRAST_10);
            layout.addClassName(LumoUtility.BorderRadius.MEDIUM);
            layout.setPadding(true);
            layout.setSpacing(false);

            Paragraph sectionTitle = new Paragraph("Tempo T" + timeNumber);
            sectionTitle.addClassName(LumoUtility.FontWeight.BOLD);
            sectionTitle.addClassName(LumoUtility.Margin.Bottom.NONE);

            timerPicker = new TimePicker("Timer");
            timerPicker.setStep(Duration.ofMinutes(1));
            timerPicker.setValue(LocalTime.parse("00:00"));
            timerPicker.setWidthFull();

            medicalParamsForm = new FormLayout();
            medicalParamsForm.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 2),
                    new FormLayout.ResponsiveStep("500px", 3)
            );
            medicalParamsForm.setWidthFull();

            paField = createTextField();
            fcField = createMedicalField("FC (bpm)", "battiti/min");
            rrField = createMedicalField("FR (rpm)", "respiri/min");
            tField = createMedicalField("Temperatura (°C)", "°C");
            spo2Field = createMedicalField("SpO₂ (%)", "%");
            etco2Field = createMedicalField("EtCO₂ (mmHg)", "mmHg");

            medicalParamsForm.add(paField, fcField, rrField, tField, spo2Field, etco2Field);
            medicalParamsForm.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            // Container per parametri aggiuntivi
            customParamsContainer = new VerticalLayout();
            customParamsContainer.setWidthFull();
            customParamsContainer.setPadding(false);
            customParamsContainer.setSpacing(false);
            medicalParamsForm.add(customParamsContainer);

            Hr divider = new Hr();
            divider.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

            Paragraph actionTitle = new Paragraph(timeNumber == 0 ?
                    "AZIONE INIZIALE T0" : "AZIONE CORRENTE: T" + timeNumber);
            actionTitle.getStyle()
                    .set("font-weight", "bold")
                    .set("text-align", "center")
                    .set("width", "100%");
            actionTitle.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            actionDetailsArea = new TextArea("Azione da svolgere per passare al prossimo Tn");
            actionDetailsArea.setWidthFull();
            actionDetailsArea.setMinHeight("100px");
            actionDetailsArea.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            HorizontalLayout timeSelectionContainer = new HorizontalLayout();
            timeSelectionContainer.setWidthFull();
            timeSelectionContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

            FormLayout timeSelectionForm = new FormLayout();
            timeSelectionForm.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 2)
            );
            timeSelectionForm.setWidth("auto");

            timeIfYesField = new IntegerField(timeNumber == 0 ?
                    "Prossimo tempo se SI" : "Tempo se SI (Tn)");
            timeIfYesField.setMin(0);
            timeIfYesField.setWidth("120px");

            timeIfNoField = new IntegerField(timeNumber == 0 ?
                    "Prossimo tempo se NO" : "Tempo se NO (Tm)");
            timeIfNoField.setMin(0);
            timeIfNoField.setWidth("120px");

            timeSelectionForm.add(timeIfYesField, timeIfNoField);
            timeSelectionContainer.add(timeSelectionForm);
            timeSelectionContainer.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            additionalDetailsArea = new TextArea("Eventuali altri dettagli");
            additionalDetailsArea.setWidthFull();
            additionalDetailsArea.setMinHeight("150px");
            additionalDetailsArea.addClassName(LumoUtility.Margin.Bottom.LARGE);

            removeButton = new Button("Rimuovi Tempo", new Icon(VaadinIcon.TRASH));
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            removeButton.addClickListener(event -> {
                timeSections.remove(this);
                timeSectionsContainer.remove(layout);
            });

            layout.add(sectionTitle, timerPicker, medicalParamsForm, divider,
                    actionTitle, actionDetailsArea, timeSelectionContainer,
                    additionalDetailsArea, removeButton);
        }

        public VerticalLayout getLayout() {
            return layout;
        }

        public FormLayout getMedicalParamsForm() {
            return medicalParamsForm;
        }

        public Map<String, NumberField> getCustomParameters() {
            return customParameters;
        }

        public void hideRemoveButton() {
            removeButton.setVisible(false);
        }

        public void addCustomParameter(String key, String label) {
            if (!customParameters.containsKey(key)) {
                NumberField field = createMedicalField(label, getUnitFromLabel(label));
                customParameters.put(key, field);

                Button removeParamButton = new Button(new Icon(VaadinIcon.TRASH));
                removeParamButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                removeParamButton.addClassName(LumoUtility.Margin.Left.SMALL);
                removeParamButton.addClickListener(e -> {
                    HorizontalLayout layoutToRemove = customParameterLayouts.get(key);
                    if (layoutToRemove != null) {
                        customParamsContainer.remove(layoutToRemove);
                        customParameters.remove(key);
                        customParameterLayouts.remove(key);
                    }
                });

                HorizontalLayout paramLayout = new HorizontalLayout(field, removeParamButton);
                paramLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
                paramLayout.setWidthFull();

                customParameterLayouts.put(key, paramLayout);
                customParamsContainer.add(paramLayout);
            }
        }

        private String getUnitFromLabel(String label) {
            if (label.contains("(") && label.contains(")")) {
                return label.substring(label.indexOf("(") + 1, label.indexOf(")"));
            }
            return "";
        }

        public ScenarioService.TempoData prepareDataForSave() {
            LocalTime time = timerPicker.getValue();
            String pa = paField.getValue() != null ? paField.getValue() : "0/0";
            double fc = fcField.getValue() != null ? fcField.getValue() : 0;
            double rr = rrField.getValue() != null ? rrField.getValue() : 0;
            double t = tField.getValue() != null ? tField.getValue() : 0;
            double spo2 = spo2Field.getValue() != null ? spo2Field.getValue() : 0;
            double etco2 = etco2Field.getValue() != null ? etco2Field.getValue() : 0;

            String actionDescription = actionDetailsArea.getValue();
            int nextTimeIfYes = timeIfYesField.getValue() != null ? timeIfYesField.getValue() : 0;
            int nextTimeIfNo = timeIfNoField.getValue() != null ? timeIfNoField.getValue() : 0;
            String additionalDetails = additionalDetailsArea.getValue();

            // Raccogli parametri aggiuntivi con unità di misura
            Map<String, Double> additionalParams = new HashMap<>();
            customParameters.forEach((key, field) -> {
                double value = field.getValue() != null ? field.getValue() : 0;
                additionalParams.put(key, value);
            });

            return new ScenarioService.TempoData(
                    timeNumber,
                    pa,
                    fc,
                    rr,
                    t,
                    spo2,
                    etco2,
                    actionDescription,
                    nextTimeIfYes,
                    nextTimeIfNo,
                    additionalDetails,
                    time != null ? time.toSecondOfDay() : 0,
                    additionalParams
            );
        }

        // Metodi per impostare i valori dei campi
        public void setPaValue(String value) {
            paField.setValue(value);
            paField.setReadOnly(true);
            paField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }

        public void setFcValue(int value) {
            fcField.setValue((double) value);
            fcField.setReadOnly(true);
            fcField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }

        public void setRrValue(int value) {
            rrField.setValue((double) value);
            rrField.setReadOnly(true);
            rrField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }

        public void setTValue(double value) {
            tField.setValue(value);
            tField.setReadOnly(true);
            tField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }

        public void setSpo2Value(int value) {
            spo2Field.setValue((double) value);
            spo2Field.setReadOnly(true);
            spo2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }

        public void setEtco2Value(int value) {
            etco2Field.setValue((double) value);
            etco2Field.setReadOnly(true);
            etco2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }
    }
}