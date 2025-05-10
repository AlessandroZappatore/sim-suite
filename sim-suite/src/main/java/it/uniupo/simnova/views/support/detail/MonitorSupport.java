package it.uniupo.simnova.views.support.detail;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import it.uniupo.simnova.api.model.ParametroAggiuntivo;
import it.uniupo.simnova.service.PresidiService;

import java.util.List;
import java.util.Objects;
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
    private static String formatDisplayValue(Number number, String format, String nullDisplay) {
        if (number == null) return nullDisplay;
        if (number instanceof Double || number instanceof Float) {
            return String.format(format, number.doubleValue());
        }
        return String.valueOf(number);
    }
    private static String formatDisplayValue(Number number, String nullDisplay) {
        if (number == null) return nullDisplay;
        return String.valueOf(number);
    }


    public static Component createVitalSignsMonitor(VitalSignsDataProvider dataProvider, Integer scenarioId) {
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

        if (currentUI != null && currentUI.getPage() != null){
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
                    "var(--lumo-primary-color)", null, null, null, null, null));
        } else {
            vitalSignsLayout.add(createVitalSignBox("PA", NULL_DISPLAY_VALUE, "mmHg",
                    "var(--lumo-secondary-text-color)", null, null, null, null, null));
        }
        // FC
        final Double FC_CRITICAL_LOW = 40.0; final Double FC_CRITICAL_HIGH = 130.0;
        final Double FC_WARNING_LOW = 50.0; final Double FC_WARNING_HIGH = 110.0;
        vitalSignsLayout.add(createVitalSignBox("FC",
                formatDisplayValue(dataProvider.getFC(), NULL_DISPLAY_VALUE), "bpm",
                "var(--lumo-primary-color)", toDouble(dataProvider.getFC()),
                FC_CRITICAL_LOW, FC_CRITICAL_HIGH, FC_WARNING_LOW, FC_WARNING_HIGH));
        // T
        if (dataProvider.getT() != null && dataProvider.getT() > -50) {
            final double MIN_CRITICAL_TEMP = 35.0; final double MAX_CRITICAL_TEMP = 39.0;
            final double MIN_WARNING_TEMP = 36.0; final double MAX_WARNING_TEMP = 37.5;
            vitalSignsLayout.add(createVitalSignBox("T",
                    formatDisplayValue(dataProvider.getT(), "%.1f", NULL_DISPLAY_VALUE), "°C",
                    "var(--lumo-success-color)", toDouble(dataProvider.getT()),
                    MIN_CRITICAL_TEMP, MAX_CRITICAL_TEMP, MIN_WARNING_TEMP, MAX_WARNING_TEMP));
        } else {
            vitalSignsLayout.add(createVitalSignBox("T", NULL_DISPLAY_VALUE, "°C",
                    "var(--lumo-secondary-text-color)", null,
                    null, null, null, null));
        }
        // RR
        final Double RR_CRITICAL_LOW = 10.0; final Double RR_CRITICAL_HIGH = 30.0;
        final Double RR_WARNING_LOW = 12.0; final Double RR_WARNING_HIGH = 25.0;
        vitalSignsLayout.add(createVitalSignBox("RR",
                formatDisplayValue(dataProvider.getRR(),NULL_DISPLAY_VALUE), "rpm",
                "var(--lumo-tertiary-color)", toDouble(dataProvider.getRR()),
                RR_CRITICAL_LOW, RR_CRITICAL_HIGH, RR_WARNING_LOW, RR_WARNING_HIGH));
        // SpO2
        final Double SPO2_CRITICAL_LOW = 90.0;
        final Double SPO2_WARNING_LOW = 94.0;
        vitalSignsLayout.add(createVitalSignBox("SpO₂",
                formatDisplayValue(dataProvider.getSpO2(),NULL_DISPLAY_VALUE), "%",
                "var(--lumo-contrast)", toDouble(dataProvider.getSpO2()),
                SPO2_CRITICAL_LOW, null, SPO2_WARNING_LOW, null));
        // FiO2
        if (dataProvider.getFiO2() != null && dataProvider.getFiO2() != 0) {
            vitalSignsLayout.add(createVitalSignBox("FiO₂",
                    formatDisplayValue(dataProvider.getFiO2(),NULL_DISPLAY_VALUE), "%",
                    "var(--lumo-primary-color-50pct)", toDouble(dataProvider.getFiO2()),
                    null, null, null, null));
        }
        // Litri O2
        if (dataProvider.getLitriO2() != null && dataProvider.getLitriO2() != 0) {
            vitalSignsLayout.add(createVitalSignBox("Litri O₂",
                    formatDisplayValue(dataProvider.getLitriO2(),NULL_DISPLAY_VALUE), "Litri/m",
                    "var(--lumo-contrast-70pct)", toDouble(dataProvider.getLitriO2()),
                    null, null, null, null));
        }
        // EtCO2
        final Double ETCO2_CRITICAL_LOW = 25.0; final Double ETCO2_CRITICAL_HIGH = 60.0;
        final Double ETCO2_WARNING_LOW = 35.0; final Double ETCO2_WARNING_HIGH = 45.0;
        vitalSignsLayout.add(createVitalSignBox("EtCO₂",
                formatDisplayValue(dataProvider.getEtCO2(),NULL_DISPLAY_VALUE), "mmHg",
                "var(--lumo-warning-color)", toDouble(dataProvider.getEtCO2()),
                ETCO2_CRITICAL_LOW, ETCO2_CRITICAL_HIGH, ETCO2_WARNING_LOW, ETCO2_WARNING_HIGH));
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
                        null, null, null, null, null));
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
            monitorTextHeader.add(monitorIcon, monitorTextTitle);

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

            Div textWrapper = new Div(monitorTextContainer);
            textWrapper.setWidthFull();
            textWrapper.getStyle().set("padding-left", "var(--lumo-space-xs)")
                    .set("padding-right", "var(--lumo-space-xs)");
            monitorContainer.add(textWrapper);
        }

        // --- NUOVA SEZIONE: PRESIDI UTILIZZATI ---
        List<String> presidiList = PresidiService.getPresidiByScenarioId(scenarioId); // Ottieni i presidi dal data provider
        if (presidiList != null && !presidiList.isEmpty()) {
            Div presidiOuterContainer = new Div(); // Contenitore esterno per allineamento padding con "Monitoraggio"
            presidiOuterContainer.setWidthFull();
            presidiOuterContainer.getStyle()
                    .set("padding-left", "var(--lumo-space-xs)")
                    .set("padding-right", "var(--lumo-space-xs)");

            Div presidiInnerContainer = new Div(); // Contenitore interno per lo stile del box
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

            Icon presidiIcon = new Icon(VaadinIcon.TOOLS); // Icona per i presidi
            presidiIcon.getStyle()
                    .set("color", "var(--lumo-tertiary-color)")
                    .set("margin-right", "var(--lumo-space-xs)");

            H4 presidiTitle = new H4("Presidi Utilizzati");
            presidiTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "500")
                    .set("font-size", "var(--lumo-font-size-m)")
                    .set("color", "var(--lumo-tertiary-text-color)");
            presidiHeader.add(presidiIcon, presidiTitle);

            Div presidiItemsDiv = new Div(); // Contenitore per gli elementi della lista dei presidi
            presidiItemsDiv.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column") // Lista verticale
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("padding-left", "var(--lumo-space-xs)"); // Leggero indent per gli item

            for (String presidio : presidiList) {
                Span presidioSpan = new Span(presidio);
                presidioSpan.getStyle()
                        .set("font-family", "var(--lumo-font-family)") // Usa il font di default Lumo
                        .set("color", "var(--lumo-body-text-color)")
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("line-height", "1.5")
                        .set("margin-bottom", "var(--lumo-space-xxs)"); // Piccola spaziatura tra gli item
                presidiItemsDiv.add(presidioSpan);
            }

            presidiInnerContainer.add(presidiHeader, presidiItemsDiv);
            presidiOuterContainer.add(presidiInnerContainer);
            monitorContainer.add(presidiOuterContainer);
        }
        // --- FINE NUOVA SEZIONE: PRESIDI UTILIZZATI ---

        return monitorContainer;
    }

    private static Div createVitalSignBox(String label, String displayValue, String unit, String defaultNormalColor,
                                          Double numericValue,
                                          Double criticalLowThreshold, Double criticalHighThreshold,
                                          Double warningLowThreshold, Double warningHighThreshold) {
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
            boolean isCritical = false;
            if (criticalLowThreshold != null && numericValue < criticalLowThreshold) isCritical = true;
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

        valueSpan.getStyle()
                .set("display", "block")
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("color", finalValueColor)
                .set("line-height", "1.2");

        Span unitSpan = new Span(unit);
        unitSpan.getStyle()
                .set("display", "block")
                .set("font-size", "12px")
                .set("color", "var(--lumo-tertiary-text-color)");

        box.add(labelSpan, valueSpan, unitSpan);
        return box;
    }
}