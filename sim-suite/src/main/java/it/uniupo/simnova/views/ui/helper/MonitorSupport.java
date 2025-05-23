package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.components.PresidiService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.views.common.utils.StyleApp;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitorSupport {

    // Lista di colori per i parametri aggiuntivi
    private static final List<String> ADDITIONAL_PARAM_COLORS = List.of(
            "var(--lumo-contrast-70pct)",
            "var(--lumo-shade-50pct)",
            "var(--lumo-tertiary-color)"
    );

    // CSS per l'animazione di lampeggiamento
    private static final String FLASH_ANIMATION_CSS =
            "@keyframes flash-outline-anim {" +
                    "  0% { outline: 2px solid transparent; outline-offset: 0px; }" +
                    "  50% { outline: 3px solid var(--lumo-error-color); outline-offset: 2px; }" +
                    "  100% { outline: 2px solid transparent; outline-offset: 0px; }" +
                    "} " +
                    ".flash-alert-box {" +
                    "  animation: flash-outline-anim 1.2s infinite;" +
                    "  border-color: var(--lumo-error-color) !important;" + // Mantieni il bordo rosso
                    "}";

    // Flag per assicurare che il CSS sia iniettato una sola volta per pagina/UI
    private static final String FLASH_STYLE_ID = "global-flash-alert-style";

    // Metodo helper per convertire a Double, gestendo null
    private static Double toDouble(Number number) {
        return number == null ? null : number.doubleValue();
    }

    // Metodo helper per formattare il display value, gestendo null
    private static String formatDisplayValue(Number number, @SuppressWarnings("SameParameterValue") String format) {
        if (number == null) return "-";
        if (number instanceof Double || number instanceof Float) {
            return String.format(format, number.doubleValue());
        }
        return String.valueOf(number);
    }

    private static String formatDisplayValue(Number number) {
        if (number == null) return "-";
        return String.valueOf(number);
    }


    public static Component createVitalSignsMonitor(VitalSignsDataProvider dataProvider,
                                                    Integer scenarioId,
                                                    boolean isT0,
                                                    PresidiService presidiService,
                                                    PazienteT0Service pazienteT0Service,
                                                    AdvancedScenarioService advancedScenarioService,
                                                    Integer tempoId) {
        UI currentUI = UI.getCurrent();
        if (currentUI != null && currentUI.getPage() != null) {
            currentUI.getPage().executeJs(
                    "if (!document.getElementById('" + FLASH_STYLE_ID + "')) {" +
                            "  const style = document.createElement('style');" +
                            "  style.id = '" + FLASH_STYLE_ID + "';" +
                            "  style.textContent = '" + FLASH_ANIMATION_CSS.replace("'", "\\'") + "';" +
                            "  document.head.appendChild(style);" +
                            "}"
            );
        }

        Div monitorContainer = new Div();
        monitorContainer.setWidthFull();
        monitorContainer.getStyle()
                .set("background-color", "var(--lumo-shade-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("border", "2px solid var(--lumo-contrast-10pct)")
                .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
                .set("padding", "var(--lumo-space-m)")
                .set("max-width", "700px")
                .set("margin", "0 auto")
                .set("box-sizing", "border-box");

        HorizontalLayout monitorHeader = new HorizontalLayout();
        monitorHeader.setWidthFull();
        monitorHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        monitorHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        monitorHeader.setPadding(false);
        monitorHeader.setSpacing(true);

        H3 monitorTitle = new H3("Parametri Vitali");
        monitorTitle.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-primary-color)")
                .set("font-weight", "600");

        Div statusLed = new Div();
        statusLed.getStyle()
                .set("width", "12px")
                .set("height", "12px")
                .set("background-color", "var(--lumo-success-color)")
                .set("border-radius", "50%")
                .set("box-shadow", "0 0 5px var(--lumo-success-color)")
                .set("box-sizing", "border-box");

        if (currentUI != null && currentUI.getPage() != null) {
            statusLed.getElement().executeJs(
                    "this.style.animation = 'pulse 2s infinite';" +
                            "if (!document.getElementById('led-style')) {" +
                            "  const style = document.createElement('style');" +
                            "  style.id = 'led-style';" +
                            "  style.textContent = '@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.6; } 100% { opacity: 1; } }';" +
                            "  document.head.appendChild(style);" +
                            "}"
            );
        }
        monitorHeader.add(monitorTitle, statusLed);

        HorizontalLayout vitalSignsLayout = new HorizontalLayout();
        vitalSignsLayout.setWidthFull();
        vitalSignsLayout.setPadding(false);
        vitalSignsLayout.setSpacing(false);
        vitalSignsLayout.getStyle().set("flex-wrap", "wrap");
        vitalSignsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        final String NULL_DISPLAY_VALUE = "-";

        // PA
        if (dataProvider.getPA() != null && !dataProvider.getPA().isEmpty()) {
            vitalSignsLayout.add(createVitalSignBox("PA", dataProvider.getPA(), "mmHg",
                    "var(--lumo-primary-color)", null, null, null, null, null, advancedScenarioService, scenarioId, tempoId));
        } else {
            vitalSignsLayout.add(createVitalSignBox("PA", NULL_DISPLAY_VALUE, "mmHg",
                    "var(--lumo-secondary-text-color)", null, null, null, null, null, advancedScenarioService, scenarioId, tempoId));
        }
        // FC
        final Double FC_CRITICAL_LOW = 40.0;
        final Double FC_CRITICAL_HIGH = 130.0;
        final Double FC_WARNING_LOW = 50.0;
        final Double FC_WARNING_HIGH = 110.0;
        vitalSignsLayout.add(createVitalSignBox("FC",
                formatDisplayValue(dataProvider.getFC()), "bpm",
                "var(--lumo-primary-color)", toDouble(dataProvider.getFC()),
                FC_CRITICAL_LOW, FC_CRITICAL_HIGH, FC_WARNING_LOW, FC_WARNING_HIGH, advancedScenarioService, scenarioId, tempoId));
        // T
        if (dataProvider.getT() != null && dataProvider.getT() > -50) {
            final double MIN_CRITICAL_TEMP = 35.0;
            final double MAX_CRITICAL_TEMP = 39.0;
            final double MIN_WARNING_TEMP = 36.0;
            final double MAX_WARNING_TEMP = 37.5;
            vitalSignsLayout.add(createVitalSignBox("T",
                    formatDisplayValue(dataProvider.getT(), "%.1f"), "°C",
                    "var(--lumo-success-color)", toDouble(dataProvider.getT()),
                    MIN_CRITICAL_TEMP, MAX_CRITICAL_TEMP, MIN_WARNING_TEMP, MAX_WARNING_TEMP, advancedScenarioService, scenarioId, tempoId));
        } else {
            vitalSignsLayout.add(createVitalSignBox("T", NULL_DISPLAY_VALUE, "°C",
                    "var(--lumo-secondary-text-color)", null,
                    null, null, null, null, advancedScenarioService, scenarioId, tempoId));
        }
        // RR
        final Double RR_CRITICAL_LOW = 10.0;
        final Double RR_CRITICAL_HIGH = 30.0;
        final Double RR_WARNING_LOW = 12.0;
        final Double RR_WARNING_HIGH = 25.0;
        vitalSignsLayout.add(createVitalSignBox("RR",
                formatDisplayValue(dataProvider.getRR()), "rpm",
                "var(--lumo-tertiary-color)", toDouble(dataProvider.getRR()),
                RR_CRITICAL_LOW, RR_CRITICAL_HIGH, RR_WARNING_LOW, RR_WARNING_HIGH, advancedScenarioService, scenarioId, tempoId));
        // SpO2
        final Double SPO2_CRITICAL_LOW = 90.0;
        final Double SPO2_WARNING_LOW = 94.0;
        vitalSignsLayout.add(createVitalSignBox("SpO₂",
                formatDisplayValue(dataProvider.getSpO2()), "%",
                "var(--lumo-contrast)", toDouble(dataProvider.getSpO2()),
                SPO2_CRITICAL_LOW, null, SPO2_WARNING_LOW, null, advancedScenarioService, scenarioId, tempoId));

        // FiO2
        vitalSignsLayout.add(createVitalSignBox("FiO₂",
                formatDisplayValue(dataProvider.getFiO2()), "%",
                "var(--lumo-primary-color-50pct)", toDouble(dataProvider.getFiO2()),
                null, null, null, null, advancedScenarioService, scenarioId, tempoId));

        // Litri O2
        vitalSignsLayout.add(createVitalSignBox("Litri O₂",
                formatDisplayValue(dataProvider.getLitriO2()), "Litri/m",
                "var(--lumo-contrast-70pct)", toDouble(dataProvider.getLitriO2()),
                null, null, null, null, advancedScenarioService, scenarioId, tempoId));

        // EtCO2
        final Double ETCO2_CRITICAL_LOW = 25.0;
        final Double ETCO2_CRITICAL_HIGH = 60.0;
        final Double ETCO2_WARNING_LOW = 35.0;
        final Double ETCO2_WARNING_HIGH = 45.0;
        vitalSignsLayout.add(createVitalSignBox("EtCO₂",
                formatDisplayValue(dataProvider.getEtCO2()), "mmHg",
                "var(--lumo-warning-color)", toDouble(dataProvider.getEtCO2()),
                ETCO2_CRITICAL_LOW, ETCO2_CRITICAL_HIGH, ETCO2_WARNING_LOW, ETCO2_WARNING_HIGH, advancedScenarioService, scenarioId, tempoId));
        // Parametri aggiuntivi
        List<ParametroAggiuntivo> additionalParams = dataProvider.getAdditionalParameters();
        if (additionalParams != null && !additionalParams.isEmpty()) {
            AtomicInteger colorIndex = new AtomicInteger(0);
            for (ParametroAggiuntivo param : additionalParams) {
                String label = param.getNome();
                String value = param.getValore() != null ? param.getValore() : NULL_DISPLAY_VALUE;
                String unit = param.getUnitaMisura() != null ? param.getUnitaMisura() : "";
                String color = ADDITIONAL_PARAM_COLORS.get(colorIndex.getAndIncrement() % ADDITIONAL_PARAM_COLORS.size());
                vitalSignsLayout.add(createVitalSignBox(label, value, unit, color,
                        null, null, null, null, null, advancedScenarioService, scenarioId, tempoId));
            }
        }
        monitorContainer.add(monitorHeader, vitalSignsLayout);

        // Sezione Monitoraggio (Testo aggiuntivo)
        String additionalText = dataProvider.getAdditionalMonitorText();
        if (additionalText != null && !additionalText.isEmpty()) {
            Div monitorTextContainer = new Div();
            monitorTextContainer.setWidthFull();
            monitorTextContainer.getStyle()
                    .set("margin-top", "var(--lumo-space-m)")
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("padding", "var(--lumo-space-s)")
                    .set("box-shadow", "inset 0 1px 3px rgba(0, 0, 0, 0.1)")
                    .set("box-sizing", "border-box");

            HorizontalLayout monitorTextHeader = new HorizontalLayout();
            monitorTextHeader.setWidthFull();
            monitorTextHeader.setAlignItems(FlexComponent.Alignment.CENTER);
            monitorTextHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            Icon monitorIcon = new Icon(VaadinIcon.LAPTOP);
            monitorIcon.getStyle()
                    .set("color", "var(--lumo-tertiary-color)")
                    .set("margin-right", "var(--lumo-space-xs)");

            H4 monitorTextTitle = new H4("Monitoraggio");
            monitorTextTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "500")
                    .set("font-size", "var(--lumo-font-size-m)")
                    .set("color", "var(--lumo-tertiary-text-color)");

            Button editMonitorButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color");
            editMonitorButton.setTooltipText("Modifica il monitoraggio");
            HorizontalLayout titleAndIconMonitor = new HorizontalLayout(monitorIcon, monitorTextTitle);
            titleAndIconMonitor.setAlignItems(FlexComponent.Alignment.CENTER);
            titleAndIconMonitor.setSpacing(true);
            monitorTextHeader.add(titleAndIconMonitor, editMonitorButton);

            Span monitorText = new Span(additionalText);
            monitorText.getStyle()
                    .set("display", "block")
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("white-space", "pre-wrap")
                    .set("font-family", "var(--lumo-font-family-monospace)")
                    .set("color", "var(--lumo-body-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("line-height", "1.5");
            monitorTextContainer.add(monitorTextHeader, monitorText);

            // TextArea per modifica
            TextArea monitorTextArea = new TextArea();
            monitorTextArea.setWidthFull();
            monitorTextArea.setVisible(false);
            monitorTextArea.setValue(additionalText);
            Button saveMonitorButton = new Button("Salva");
            saveMonitorButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            Button cancelMonitorButton = new Button("Annulla");
            cancelMonitorButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            HorizontalLayout monitorActions = new HorizontalLayout(saveMonitorButton, cancelMonitorButton);
            monitorActions.setVisible(false);
            monitorTextContainer.add(monitorTextArea, monitorActions);

            editMonitorButton.addClickListener(e -> {
                monitorText.setVisible(false);
                monitorTextArea.setValue(monitorText.getText());
                monitorTextArea.setVisible(true);
                monitorActions.setVisible(true);
                editMonitorButton.setVisible(false);
            });
            saveMonitorButton.addClickListener(e -> {
                String newText = monitorTextArea.getValue();
                monitorText.setText(newText);
                pazienteT0Service.saveMonitor(scenarioId, newText);
                monitorText.setVisible(true);
                monitorTextArea.setVisible(false);
                monitorActions.setVisible(false);
                editMonitorButton.setVisible(true);
                Notification.show("Monitoraggio aggiornato.", 3000, Notification.Position.BOTTOM_CENTER);
            });
            cancelMonitorButton.addClickListener(e -> {
                monitorTextArea.setVisible(false);
                monitorActions.setVisible(false);
                monitorText.setVisible(true);
                editMonitorButton.setVisible(true);
            });

            Div textWrapper = new Div(monitorTextContainer);
            textWrapper.setWidthFull();
            textWrapper.getStyle().set("padding-left", "var(--lumo-space-xs)")
                    .set("padding-right", "var(--lumo-space-xs)");
            monitorContainer.add(textWrapper);
        }

        List<String> presidiList = PresidiService.getPresidiByScenarioId(scenarioId);
        if (isT0) {
            Div presidiOuterContainer = new Div();
            presidiOuterContainer.setWidthFull();
            presidiOuterContainer.getStyle()
                    .set("padding-left", "var(--lumo-space-xs)")
                    .set("padding-right", "var(--lumo-space-xs)");

            Div presidiInnerContainer = new Div();
            presidiInnerContainer.setWidthFull();
            presidiInnerContainer.getStyle()
                    .set("margin-top", "var(--lumo-space-m)")
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("padding", "var(--lumo-space-s)")
                    .set("box-shadow", "inset 0 1px 3px rgba(0, 0, 0, 0.1)")
                    .set("box-sizing", "border-box");

            HorizontalLayout presidiHeader = new HorizontalLayout();
            presidiHeader.setWidthFull();
            presidiHeader.setAlignItems(FlexComponent.Alignment.CENTER);
            presidiHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            Icon presidiIcon = new Icon(VaadinIcon.TOOLS);
            presidiIcon.getStyle()
                    .set("color", "var(--lumo-tertiary-color)")
                    .set("margin-right", "var(--lumo-space-xs)");

            H4 presidiTitle = new H4("Presidi Utilizzati");
            presidiTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "500")
                    .set("font-size", "var(--lumo-font-size-m)")
                    .set("color", "var(--lumo-tertiary-text-color)");

            Button editPresidiButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color");
            editPresidiButton.setTooltipText("Modifica i presidi");
            HorizontalLayout titleAndIconPresidi = new HorizontalLayout(presidiIcon, presidiTitle);
            titleAndIconPresidi.setAlignItems(FlexComponent.Alignment.CENTER);
            titleAndIconPresidi.setSpacing(true);
            presidiHeader.add(titleAndIconPresidi, editPresidiButton);

            Div presidiItemsDiv = new Div();
            presidiItemsDiv.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("padding-left", "var(--lumo-space-xs)");
            for (String presidio : presidiList) {
                HorizontalLayout itemLayout = new HorizontalLayout();
                itemLayout.setSpacing(false);
                itemLayout.setPadding(false);
                itemLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                itemLayout.getStyle()
                        .set("margin-bottom", "var(--lumo-space-xxs)");
                Span bulletPoint = new Span("•");
                bulletPoint.getStyle()
                        .set("color", "var(--lumo-tertiary-color)")
                        .set("font-size", "var(--lumo-font-size-m)")
                        .set("line-height", "1")
                        .set("margin-right", "var(--lumo-space-xs)");
                Span presidioSpan = new Span(presidio);
                presidioSpan.getStyle()
                        .set("font-family", "var(--lumo-font-family)")
                        .set("color", "var(--lumo-body-text-color)")
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("line-height", "1.5");
                itemLayout.add(bulletPoint, presidioSpan);
                presidiItemsDiv.add(itemLayout);
            }
            presidiInnerContainer.add(presidiHeader, presidiItemsDiv);

            // MultiSelectComboBox per modifica
            List<String> allPresidi = PresidiService.getAllPresidi();
            MultiSelectComboBox<String> presidiComboBox = new MultiSelectComboBox<>();
            presidiComboBox.setItems(allPresidi);
            presidiComboBox.setWidthFull();
            presidiComboBox.setVisible(false);
            presidiComboBox.setValue(Set.copyOf(presidiList));
            Button savePresidiButton = new Button("Salva");
            savePresidiButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            Button cancelPresidiButton = new Button("Annulla");
            cancelPresidiButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            HorizontalLayout presidiActions = new HorizontalLayout(savePresidiButton, cancelPresidiButton);
            presidiActions.setVisible(false);
            presidiInnerContainer.add(presidiComboBox, presidiActions);

            editPresidiButton.addClickListener(e -> {
                presidiItemsDiv.setVisible(false);
                presidiComboBox.setVisible(true);
                presidiActions.setVisible(true);
                editPresidiButton.setVisible(false);
            });
            savePresidiButton.addClickListener(e -> {
                Set<String> newPresidi = presidiComboBox.getValue();
                presidiItemsDiv.removeAll();
                for (String presidio : newPresidi) {
                    HorizontalLayout itemLayout = new HorizontalLayout();
                    itemLayout.setSpacing(false);
                    itemLayout.setPadding(false);
                    itemLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                    itemLayout.getStyle().set("margin-bottom", "var(--lumo-space-xxs)");
                    Span bulletPoint = new Span("•");
                    bulletPoint.getStyle().set("color", "var(--lumo-tertiary-color)").set("font-size", "var(--lumo-font-size-m)").set("line-height", "1").set("margin-right", "var(--lumo-space-xs)");
                    Span presidioSpan = new Span(presidio);
                    presidioSpan.getStyle().set("font-family", "var(--lumo-font-family)").set("color", "var(--lumo-body-text-color)").set("font-size", "var(--lumo-font-size-s)").set("line-height", "1.5");
                    itemLayout.add(bulletPoint, presidioSpan);
                    presidiItemsDiv.add(itemLayout);
                }
                presidiService.savePresidi(scenarioId, newPresidi);
                presidiItemsDiv.setVisible(true);
                presidiComboBox.setVisible(false);
                presidiActions.setVisible(false);
                editPresidiButton.setVisible(true);
                Notification.show("Presidi aggiornati.", 3000, Notification.Position.BOTTOM_CENTER);
            });
            cancelPresidiButton.addClickListener(e -> {
                presidiComboBox.setVisible(false);
                presidiActions.setVisible(false);
                presidiItemsDiv.setVisible(true);
                editPresidiButton.setVisible(true);
            });

            presidiOuterContainer.add(presidiInnerContainer);
            monitorContainer.add(presidiOuterContainer);
        }
        return monitorContainer;
    }

    private static Div createVitalSignBox(String label, String displayValue, String unit, String defaultNormalColor,
                                          Double numericValue,
                                          Double criticalLowThreshold, Double criticalHighThreshold,
                                          Double warningLowThreshold, Double warningHighThreshold,
                                          AdvancedScenarioService advancedScenarioService, Integer scenarioId, Integer tempoId) {
        Div box = new Div();
        box.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("margin", "var(--lumo-space-xs)")
                .set("text-align", "center")
                .set("min-width", "130px")
                .set("flex-grow", "1")
                .set("flex-basis", "130px")
                .set("background-color", "var(--lumo-base-color)")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease, border-color 0.3s ease");

        UI currentUI = UI.getCurrent();
        if (currentUI != null && currentUI.getPage() != null) {
            box.getElement().executeJs(
                    "this.addEventListener('mouseover', function() {" +
                            "  this.style.transform = 'translateY(-2px)';" +
                            "  this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';" +
                            "});" +
                            "this.addEventListener('mouseout', function() {" +
                            "  this.style.transform = 'translateY(0)';" +
                            "  this.style.boxShadow = '0 2px 4px rgba(0,0,0,0.05)';" +
                            "});"
            );
        }

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("display", "block")
                .set("font-size", "14px")
                .set("font-weight", "500")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "4px");

        Span valueSpan = new Span(Objects.requireNonNullElse(displayValue, "-"));
        String finalValueColor = defaultNormalColor;

        if (numericValue != null) {
            boolean isCritical = criticalLowThreshold != null && numericValue < criticalLowThreshold;
            if (!isCritical && criticalHighThreshold != null && numericValue > criticalHighThreshold) isCritical = true;

            boolean isWarning = false;
            if (!isCritical) {
                if (warningLowThreshold != null && numericValue < warningLowThreshold) isWarning = true;
                if (!isWarning && warningHighThreshold != null && numericValue > warningHighThreshold) isWarning = true;
            }

            if (isCritical) {
                box.addClassName("flash-alert-box");
                finalValueColor = "var(--lumo-error-color)";
            } else if (isWarning) {
                finalValueColor = "var(--lumo-warning-color)";
                box.getStyle().set("border-color", "var(--lumo-warning-color-50pct)");
            }
        }

        Span unitSpan = new Span(unit);
        // Caratterizzazione "spenta" per FiO₂ e Litri O₂ se valore 0
        if (("FiO₂".equals(label) || "Litri O₂".equals(label)) && numericValue != null && numericValue == 0.0) {
            box.getStyle()
                .set("background-color", "#f3f3f3")
                .set("border", "1.5px dashed #bbb");
            valueSpan.getStyle().set("color", "#bbb");
            unitSpan.getStyle().set("color", "#bbb");
            labelSpan.getStyle().set("color", "#bbb");
            valueSpan.setText("--");
        }
        valueSpan.getStyle()
                .set("display", "block")
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("color", finalValueColor)
                .set("line-height", "1.2");

        unitSpan.getStyle()
                .set("display", "block")
                .set("font-size", "12px")
                .set("color", "var(--lumo-tertiary-text-color)");

        // Elementi per la modifica
        TextField valueEditField = new TextField();
        valueEditField.setVisible(false);
        valueEditField.setWidthFull();
        valueEditField.getStyle().set("margin-bottom", "var(--lumo-space-xs)");

        Button editButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editButton.setTooltipText("Modifica " + label);
        editButton.getStyle().set("margin-left", "auto").set("align-self", "flex-start");


        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancelButton = new Button("Annulla");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout editActions = new HorizontalLayout(saveButton, cancelButton);
        editActions.setVisible(false);
        editActions.setSpacing(true);
        editActions.getStyle().set("margin-top", "var(--lumo-space-xs)");

        // Layout per label e pulsante modifica
        HorizontalLayout labelAndEditButtonLayout = new HorizontalLayout(labelSpan, editButton);
        labelAndEditButtonLayout.setWidthFull();
        labelAndEditButtonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        labelAndEditButtonLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        labelAndEditButtonLayout.setSpacing(false);


        box.add(labelAndEditButtonLayout, valueSpan, valueEditField, unitSpan, editActions);

        editButton.addClickListener(e -> {
            valueSpan.setVisible(false);
            unitSpan.setVisible(false); // Nasconde anche l'unità durante la modifica del valore puro
            valueEditField.setValue(valueSpan.getText()); // Pre-popola con il valore attuale
            valueEditField.setVisible(true);
            editActions.setVisible(true);
            editButton.setVisible(false);
            labelAndEditButtonLayout.remove(editButton); // Rimuove temporaneamente per evitare disallineamenti
        });

        saveButton.addClickListener(e -> {
            String newValue = valueEditField.getValue();
            valueSpan.setText(newValue);
            advancedScenarioService.saveVitalSign(scenarioId, tempoId, label, newValue);
            // Ricalcolo stato e stile
            Double newNumericValue = null;
            try {
                newNumericValue = Double.parseDouble(newValue.replace(",", "."));
            } catch (Exception ex) {
                // Non numerico, nessun controllo
            }
            // Reset stile
            box.getClassNames().remove("flash-alert-box");
            box.getStyle().remove("border-color");
            String newColor = defaultNormalColor;
            if (newNumericValue != null) {
                boolean isCritical = criticalLowThreshold != null && newNumericValue < criticalLowThreshold;
                if (!isCritical && criticalHighThreshold != null && newNumericValue > criticalHighThreshold)
                    isCritical = true;
                boolean isWarning = false;
                if (!isCritical) {
                    if (warningLowThreshold != null && newNumericValue < warningLowThreshold) isWarning = true;
                    if (!isWarning && warningHighThreshold != null && newNumericValue > warningHighThreshold)
                        isWarning = true;
                }
                if (isCritical) {
                    box.addClassName("flash-alert-box");
                    newColor = "var(--lumo-error-color)";
                } else if (isWarning) {
                    newColor = "var(--lumo-warning-color)";
                    box.getStyle().set("border-color", "var(--lumo-warning-color-50pct)");
                }
            }
            valueSpan.getStyle().set("color", newColor);
            valueSpan.setVisible(true);
            unitSpan.setVisible(true);
            valueEditField.setVisible(false);
            editActions.setVisible(false);
            labelAndEditButtonLayout.add(editButton);
            editButton.setVisible(true);
            Notification.show(label+" aggiornata.", 3000, Notification.Position.BOTTOM_CENTER);
        });

        cancelButton.addClickListener(e -> {
            valueSpan.setVisible(true);
            unitSpan.setVisible(true);
            valueEditField.setVisible(false);
            editActions.setVisible(false);
            labelAndEditButtonLayout.add(editButton); // Riaggiunge il pulsante edit
            editButton.setVisible(true);
        });
        return box;
    }
}

