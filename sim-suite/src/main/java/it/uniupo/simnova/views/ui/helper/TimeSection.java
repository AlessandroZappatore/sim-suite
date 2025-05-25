package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.*;

import static it.uniupo.simnova.views.constant.AdditionParametersConst.ADDITIONAL_PARAMETERS;
import static it.uniupo.simnova.views.constant.AdditionParametersConst.CUSTOM_PARAMETER_KEY;
import static it.uniupo.simnova.views.constant.ColorsConst.BORDER_COLORS;

public class TimeSection {
    private static final Logger logger = LoggerFactory.getLogger(TimeSection.class);


    /**
     * Numero identificativo del tempo (0 per T0, 1 per T1, ...).
     */
    public final int timeNumber;
    /**
     * Layout principale verticale per questa sezione temporale.
     */
    public final VerticalLayout layout;
    /**
     * Selettore per impostare un timer associato a questo tempo.
     */
    public final TimePicker timerPicker;
    /**
     * Campo di testo per la Pressione Arteriosa (formato stringa es. "120/80").
     */
    public final TextField paField;
    /**
     * Campo numerico per la Frequenza Cardiaca (bpm).
     */
    public final NumberField fcField;
    /**
     * Campo numerico per la Frequenza Respiratoria (atti/min).
     */
    public final NumberField rrField;
    /**
     * Campo numerico per la Temperatura Corporea (°C).
     */
    public final NumberField tField;
    /**
     * Campo numerico per la Saturazione dell'Ossigeno (%).
     */
    public final NumberField spo2Field;

    public final NumberField fio2Field;

    public final NumberField litriO2Field;
    /**
     * Campo numerico per la Capnometria di fine espirazione (mmHg).
     */
    public final NumberField etco2Field;
    /**
     * Area di testo per descrivere l'azione richiesta per procedere.
     */
    public final TextArea actionDetailsArea;
    /**
     * Campo numerico per l'ID del tempo successivo se la condizione/azione è SI.
     */
    public final IntegerField timeIfYesField;
    /**
     * Campo numerico per l'ID del tempo successivo se la condizione/azione è NO.
     */
    public final IntegerField timeIfNoField;
    /**
     * Area di testo per eventuali dettagli aggiuntivi o note su questo tempo.
     */
    public final TextArea additionalDetailsArea;
    /**
     * Layout (FormLayout) che contiene i campi dei parametri medici base.
     */
    public final FormLayout medicalParamsForm;
    /**
     * Layout verticale che contiene i campi dei parametri aggiuntivi/personalizzati.
     */
    public final VerticalLayout customParamsContainer;
    /**
     * Mappa che associa la chiave di un parametro aggiuntivo (es. "PVC" o "CUSTOM_Glicemia") al suo campo {@link NumberField}.
     */
    public final Map<String, NumberField> customParameters = new HashMap<>();
    /**
     * Mappa che associa la chiave di un parametro aggiuntivo alla sua unità di misura (Stringa).
     * Usata per recuperare l'unità durante il salvataggio.
     */
    public final Map<String, String> customParameterUnits = new HashMap<>();
    /**
     * Mappa che associa la chiave di un parametro aggiuntivo al layout orizzontale (campo + pulsante rimuovi) che lo contiene.
     */
    public final Map<String, HorizontalLayout> customParameterLayouts = new HashMap<>();
    /**
     * Area di testo per il ruolo del genitore.
     */
    public final TextArea ruoloGenitoreArea;

    public TimeSection(int timeNumber, ScenarioService scenarioService, List<TimeSection> timeSections, VerticalLayout timeSectionsContainer, Integer scenarioId) {
        this.timeNumber = timeNumber;


        layout = new VerticalLayout();
        layout.addClassName(LumoUtility.Padding.MEDIUM);
        layout.addClassName(LumoUtility.Border.ALL);
        layout.addClassName(LumoUtility.BorderColor.CONTRAST_10);
        layout.addClassName(LumoUtility.BorderRadius.MEDIUM);
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.getStyle().set("border-left", "4px solid " + getBorderColor(timeNumber));


        Paragraph sectionTitle = new Paragraph("Tempo T" + timeNumber);
        sectionTitle.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.MEDIUM);


        HorizontalLayout timerLayout = FieldGenerator.createTimerPickerWithPresets(
                "Timer associato a T" + timeNumber + " (opzionale)"
        );
        timerPicker = (TimePicker) timerLayout.getComponentAt(0);


        medicalParamsForm = new FormLayout();
        medicalParamsForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        medicalParamsForm.setWidthFull();


        paField = FieldGenerator.createTextField("PA (Sist/Diast)", "es. 120/80", true);
        paField.setSuffixComponent(new Paragraph("mmHg"));
        paField.getStyle().set("max-width", "320px");

        fcField = FieldGenerator.createMedicalField("FC", "(es. 80)", true, "bpm");
        rrField = FieldGenerator.createMedicalField("FR", "(es. 16)", true, "atti/min");
        tField = FieldGenerator.createMedicalField("Temp.", "(es. 36.5)", true, "°C");
        spo2Field = FieldGenerator.createMedicalField("SpO₂", "(es. 98)", true, "%");
        fio2Field = FieldGenerator.createMedicalField("FiO₂", "(es. 21)", false, "%");
        litriO2Field = FieldGenerator.createMedicalField("Litri O₂", "(es. 5)", false, "L/min");
        etco2Field = FieldGenerator.createMedicalField("EtCO₂", "(es. 35)", true, "mmHg");


        medicalParamsForm.add(paField, fcField, rrField, tField, spo2Field, fio2Field, litriO2Field, etco2Field);
        medicalParamsForm.addClassName(LumoUtility.Margin.Bottom.MEDIUM);



        customParamsContainer = new VerticalLayout();
        customParamsContainer.setWidthFull();
        customParamsContainer.setPadding(false);
        customParamsContainer.setSpacing(false);


        Hr divider = new Hr();
        divider.addClassName(LumoUtility.Margin.Vertical.MEDIUM);


        Paragraph actionTitle = new Paragraph(timeNumber == 0 ?
                "DETTAGLI INIZIALI T0" : "AZIONE E TRANSIZIONI PER T" + timeNumber);
        actionTitle.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);

        actionDetailsArea = FieldGenerator.createTextArea(
                "Azione richiesta / Evento scatenante per procedere da T" + timeNumber,
                "Es. Somministrare farmaco X, Rilevare parametro Y, Domanda al paziente...",
                false
        );

        HorizontalLayout timeSelectionContainer = new HorizontalLayout();
        timeSelectionContainer.setWidthFull();
        timeSelectionContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);


        HorizontalLayout timeSelectionLayout = new HorizontalLayout();
        timeSelectionLayout.setWidthFull();
        timeSelectionLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        timeSelectionLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        timeSelectionLayout.setSpacing(true);

        timeIfYesField = FieldGenerator.createTimeNavigationField(
                "Se SI, vai a T:",
                "ID Tempo",
                true
        );

        timeIfNoField = FieldGenerator.createTimeNavigationField(
                "Se NO, vai a T:",
                "ID Tempo",
                true
        );


        timeSelectionLayout.add(timeIfYesField, timeIfNoField);
        timeSelectionContainer.add(timeSelectionLayout);
        timeSelectionContainer.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        additionalDetailsArea = FieldGenerator.createTextArea(
                "Altri dettagli / Note per T" + timeNumber + " (opzionale)",
                "Es. Note per il docente, trigger specifici, stato emotivo del paziente...",
                false
        );


        Button removeButton = new Button("Rimuovi T" + timeNumber, new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        removeButton.addClickListener(event -> {
            timeSections.remove(this);
            timeSectionsContainer.remove(layout);
            Notification.show("Tempo T" + timeNumber + " rimosso.", 2000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        ruoloGenitoreArea = FieldGenerator.createTextArea(
                "Ruolo Genitore (opzionale)",
                "Es. Genitore riferisce delle informazioni sul paziente...",
                false
        );


        layout.add(sectionTitle, timerLayout, medicalParamsForm, customParamsContainer, divider,
                actionTitle, actionDetailsArea, timeSelectionContainer,
                additionalDetailsArea);
        if (scenarioService.isPediatric(scenarioId)) {
            layout.add(ruoloGenitoreArea);
        }

        if (timeNumber > 0) {
            layout.add(removeButton);
            layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END, removeButton);
        }
    }

    /**
     * Restituisce il layout principale ({@link VerticalLayout}) di questa sezione temporale.
     *
     * @return il layout della sezione.
     */
    public VerticalLayout getLayout() {
        return layout;
    }

    /**
     * Restituisce il numero identificativo di questo tempo (0 per T0, 1 per T1, ...).
     *
     * @return il numero del tempo.
     */
    public int getTimeNumber() {
        return timeNumber;
    }


    /**
     * Restituisce il {@link FormLayout} che contiene i campi dei parametri medici base.
     * Utile per aggiungere dinamicamente il pulsante "Aggiungi Parametri".
     *
     * @return il FormLayout dei parametri medici.
     */
    public FormLayout getMedicalParamsForm() {
        return medicalParamsForm;
    }

    /**
     * Restituisce la mappa dei parametri aggiuntivi/personalizzati (chiave -> campo NumberField).
     *
     * @return la mappa dei campi dei parametri aggiuntivi.
     */
    public Map<String, NumberField> getCustomParameters() {
        return customParameters;
    }

    /**
     * Nasconde il pulsante "Rimuovi Tempo". Usato solitamente per la sezione T0.
     */
    public void hideRemoveButton() {

        layout.getChildren().filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getText().startsWith("Rimuovi T"))
                .findFirst()
                .ifPresent(button -> button.setVisible(false));
    }


    /**
     * Aggiunge un parametro (predefinito o personalizzato) alla sezione temporale.
     * Crea un campo numerico {@link NumberField} con un pulsante per rimuoverlo,
     * e memorizza l'unità di misura associata.
     *
     * @param key   la chiave identificativa del parametro (es. "PVC" o "CUSTOM_Nome_Parametro").
     * @param label l'etichetta completa da visualizzare per il campo (es. "Nome Parametro (unit)").
     * @param unit  l'unità di misura (stringa) da associare a questo parametro per il salvataggio.
     */
    public void addCustomParameter(String key, String label, String unit) {

        if (!customParameters.containsKey(key)) {

            NumberField field = FieldGenerator.createMedicalField(label, "", false, unit);


            customParameters.put(key, field);


            if (unit != null) {
                customParameterUnits.put(key, unit);
            }


            Button removeParamButton = new Button(new Icon(VaadinIcon.TRASH));
            removeParamButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
            removeParamButton.getElement().setAttribute("aria-label", "Rimuovi " + label);
            removeParamButton.setTooltipText("Rimuovi " + label);
            removeParamButton.addClassName(LumoUtility.Margin.Left.SMALL);


            HorizontalLayout paramLayout = new HorizontalLayout(field, removeParamButton);
            paramLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            paramLayout.setWidthFull();
            paramLayout.setFlexGrow(1, field);


            removeParamButton.addClickListener(e -> {
                HorizontalLayout layoutToRemove = customParameterLayouts.get(key);
                if (layoutToRemove != null) {
                    customParamsContainer.remove(layoutToRemove);
                    customParameters.remove(key);
                    customParameterLayouts.remove(key);
                    customParameterUnits.remove(key);
                }
            });


            customParameterLayouts.put(key, paramLayout);

            customParamsContainer.add(paramLayout);
        } else {
            logger.warn("Tentativo di aggiungere un parametro con chiave duplicata: {}", key);
        }
    }


    /**
     * Prepara i dati di questa sezione temporale per il salvataggio nel database.
     * Raccoglie i valori dai campi dell'interfaccia utente, recupera le unità di misura
     * memorizzate e li assembla in un oggetto {@link Tempo}.
     *
     * @return un oggetto {@link Tempo} pronto per essere salvato.
     */
    public Tempo prepareDataForSave() {

        LocalTime time = timerPicker.getValue();
        String pa = paField.getValue() != null ? paField.getValue().trim() : "";
        Integer fc = fcField.getValue() != null ? fcField.getValue().intValue() : null;
        Integer rr = rrField.getValue() != null ? rrField.getValue().intValue() : null;
        double rawT = tField.getValue() != null ? tField.getValue() : 0.0;
        double t = Math.round(rawT * 10.0) / 10.0;
        Integer spo2 = spo2Field.getValue() != null ? spo2Field.getValue().intValue() : null;
        Integer fiO2 = fio2Field.getValue() != null ? fio2Field.getValue().intValue() : null;
        Float litriO2 = litriO2Field.getValue() != null ? litriO2Field.getValue().floatValue() : null;
        Integer etco2 = etco2Field.getValue() != null ? etco2Field.getValue().intValue() : null;

        String actionDescription = actionDetailsArea.getValue() != null ? actionDetailsArea.getValue().trim() : "";
        int nextTimeIfYes = timeIfYesField.getValue() != null ? timeIfYesField.getValue() : 0;
        int nextTimeIfNo = timeIfNoField.getValue() != null ? timeIfNoField.getValue() : 0;
        String additionalDetails = additionalDetailsArea.getValue() != null ? additionalDetailsArea.getValue().trim() : "";
        long timerSeconds = (time != null) ? time.toSecondOfDay() : 0L;
        String ruoloGenitoreArea = this.ruoloGenitoreArea.getValue() != null ? this.ruoloGenitoreArea.getValue().trim() : "";

        List<ParametroAggiuntivo> additionalParamsList = new ArrayList<>();
        customParameters.forEach((key, field) -> {
            double value = field.getValue() != null ? field.getValue() : 0.0;
            String unit;
            String paramNameForDb;

            if (key.startsWith(CUSTOM_PARAMETER_KEY)) {

                paramNameForDb = key.substring(CUSTOM_PARAMETER_KEY.length() + 1).replace('_', ' ');
                unit = customParameterUnits.getOrDefault(key, "");
            } else {

                paramNameForDb = key;
                unit = customParameterUnits.get(key);
                if (unit == null) {
                    String fullLabel = ADDITIONAL_PARAMETERS.getOrDefault(key, "");
                    if (fullLabel.contains("(") && fullLabel.contains(")")) {
                        try {
                            unit = fullLabel.substring(fullLabel.indexOf("(") + 1, fullLabel.indexOf(")"));
                        } catch (IndexOutOfBoundsException e) {
                            unit = "";
                        }
                    } else {
                        unit = "";
                    }
                }
            }

            additionalParamsList.add(new ParametroAggiuntivo(
                    paramNameForDb,
                    value,
                    unit != null ? unit : ""
            ));
        });


        Tempo tempo = new Tempo(
                timeNumber,
                0,
                pa.isEmpty() ? null : pa,
                fc,
                rr,
                t,
                spo2,
                fiO2,
                litriO2,
                etco2,
                actionDescription,
                nextTimeIfYes,
                nextTimeIfNo,
                additionalDetails.isEmpty() ? null : additionalDetails,
                timerSeconds,
                ruoloGenitoreArea
        );

        tempo.setParametriAggiuntivi(additionalParamsList);
        return tempo;
    }


    /**
     * Crea un campo di testo per la Pressione Arteriosa (PA).
     *
     * @param value il valore iniziale da impostare (può essere null).
     */
    public void setPaValue(String value) {
        paField.setValue(value != null ? value : "");
        paField.setReadOnly(true);
        paField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore FC per T0 e rende il campo non modificabile.
     *
     * @param value il valore da impostare (può essere null).
     */
    public void setFcValue(Integer value) {
        fcField.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        fcField.setReadOnly(true);
        fcField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore RR per T0 e rende il campo non modificabile.
     *
     * @param value il valore da impostare (può essere null).
     */
    public void setRrValue(Integer value) {
        rrField.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        rrField.setReadOnly(true);
        rrField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore T per T0 e rende il campo non modificabile.
     *
     * @param value il valore da impostare (può essere null).
     */
    public void setTValue(Double value) {
        if (value != null) {
            double roundedValue = Math.round(value * 10.0) / 10.0;
            tField.setValue(roundedValue);
        } else {
            tField.setValue(null);
        }
        tField.setReadOnly(true);
        tField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore SpO2 per T0 e rende il campo non modificabile.
     *
     * @param value il valore da impostare (può essere null).
     */
    public void setSpo2Value(Integer value) {
        spo2Field.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        spo2Field.setReadOnly(true);
        spo2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    public void setFio2Value(Integer value) {
        fio2Field.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        fio2Field.setReadOnly(true);
        fio2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    public void setLitriO2Value(Float value) {
        litriO2Field.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        litriO2Field.setReadOnly(true);
        litriO2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore EtCO2 per T0 e rende il campo non modificabile.
     *
     * @param value il valore da impostare (può essere null).
     */
    public void setEtco2Value(Integer value) {
        etco2Field.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        etco2Field.setReadOnly(true);
        etco2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    private String getBorderColor(int timeNumber) {
        return BORDER_COLORS[(timeNumber) % BORDER_COLORS.length];
    }
}