package it.uniupo.simnova.views.support.detail;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import it.uniupo.simnova.api.model.ParametroAggiuntivo; // Import necessario
import it.uniupo.simnova.views.support.detail.VitalSignsDataProvider;

import java.util.List; // Import necessario
import java.util.concurrent.atomic.AtomicInteger; // Per ciclare i colori

public class MonitorSupport {

    // Lista di colori per i parametri aggiuntivi, se si vuole variare
    private static final List<String> ADDITIONAL_PARAM_COLORS = List.of(
            "var(--lumo-contrast-70pct)",
            "var(--lumo-shade-50pct)",
            "var(--lumo-tertiary-color)"
            // Aggiungere altri colori se necessario
    );


    public static Component createVitalSignsMonitor(VitalSignsDataProvider dataProvider) {
        Div monitorContainer = new Div();
        monitorContainer.setWidthFull();
        monitorContainer.getStyle()
                .set("background-color", "var(--lumo-shade-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("border", "2px solid var(--lumo-contrast-10pct)")
                .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
                .set("padding", "var(--lumo-space-m)")
                .set("max-width", "700px")
                .set("margin", "0 auto");

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
                .set("box-shadow", "0 0 5px var(--lumo-success-color)");
        statusLed.getElement().executeJs(
                "this.style.animation = 'pulse 2s infinite';" +
                        "if (!document.getElementById('led-style')) {" +
                        "  const style = document.createElement('style');" +
                        "  style.id = 'led-style';" +
                        "  style.textContent = '@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.6; } 100% { opacity: 1; } }';" +
                        "  document.head.appendChild(style);" +
                        "}"
        );
        monitorHeader.add(monitorTitle, statusLed);

        HorizontalLayout vitalSignsLayout = new HorizontalLayout();
        vitalSignsLayout.setWidthFull();
        vitalSignsLayout.setPadding(false);
        vitalSignsLayout.setSpacing(false);
        vitalSignsLayout.getStyle().set("flex-wrap", "wrap");
        vitalSignsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Parametri vitali standard
        if (dataProvider.getPA() != null && !dataProvider.getPA().isEmpty()) {
            vitalSignsLayout.add(createVitalSignBox("PA", dataProvider.getPA(), "mmHg", "var(--lumo-error-color)"));
        }
        if (dataProvider.getFC() != null) {
            vitalSignsLayout.add(createVitalSignBox("FC", String.valueOf(dataProvider.getFC()), "bpm", "var(--lumo-primary-color)"));
        }
        if (dataProvider.getT() != null && dataProvider.getT() > -50) {
            String temperaturaFormattata = String.format("%.1f", dataProvider.getT());
            vitalSignsLayout.add(createVitalSignBox("TC", temperaturaFormattata, "°C", "var(--lumo-success-color)"));
        }
        if (dataProvider.getRR() != null) {
            vitalSignsLayout.add(createVitalSignBox("RR", String.valueOf(dataProvider.getRR()), "rpm", "var(--lumo-tertiary-color)"));
        }
        if (dataProvider.getSpO2() != null) {
            vitalSignsLayout.add(createVitalSignBox("SpO₂", String.valueOf(dataProvider.getSpO2()), "%", "var(--lumo-contrast)"));
        }
        if (dataProvider.getFiO2() != null && dataProvider.getFiO2() != 0) {
            vitalSignsLayout.add(createVitalSignBox("FiO₂", String.valueOf(dataProvider.getFiO2()), "%", "var(--lumo-primary-color-50pct)"));
        }
        if (dataProvider.getLitriO2() != null && dataProvider.getLitriO2() != 0) {
            vitalSignsLayout.add(createVitalSignBox("Litri O₂", String.valueOf(dataProvider.getLitriO2()), "Litri/m", "var(--lumo-contrast-70pct)"));
        }
        if (dataProvider.getEtCO2() != null) {
            vitalSignsLayout.add(createVitalSignBox("EtCO₂", String.valueOf(dataProvider.getEtCO2()), "mmHg", "var(--lumo-warning-color)"));
        }

        // Parametri aggiuntivi
        List<ParametroAggiuntivo> additionalParams = dataProvider.getAdditionalParameters();
        if (additionalParams != null && !additionalParams.isEmpty()) {
            AtomicInteger colorIndex = new AtomicInteger(0); // Per ciclare i colori
            for (ParametroAggiuntivo param : additionalParams) {
                String label = param.getNome();
                String value = param.getValore();
                String unit = param.getUnitaMisura() != null ? param.getUnitaMisura() : "";
                String color = ADDITIONAL_PARAM_COLORS.get(colorIndex.getAndIncrement() % ADDITIONAL_PARAM_COLORS.size());
                vitalSignsLayout.add(createVitalSignBox(label, value, unit, color));
            }
        }

        monitorContainer.add(monitorHeader, vitalSignsLayout); // Aggiungo sempre vitalSignsLayout

        // Testo aggiuntivo del monitor (se presente) - messo dopo tutti i box parametri
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
            monitorContainer.add(textWrapper); // Aggiungi il testo aggiuntivo al container principale
        }
        return monitorContainer;
    }

    private static Div createVitalSignBox(String label, String value, String unit, String color) {
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
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");
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
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("display", "block")
                .set("font-size", "14px")
                .set("font-weight", "500")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "4px");
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("display", "block")
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("color", color)
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