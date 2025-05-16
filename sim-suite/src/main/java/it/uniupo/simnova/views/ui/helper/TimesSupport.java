package it.uniupo.simnova.views.ui.helper;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;


import java.util.Comparator;
import java.util.List;

public class TimesSupport {

    // Colori per i bordi laterali delle sezioni
    private static final String AZIONE_BORDER_COLOR = "var(--lumo-primary-color)";
    private static final String DETTAGLI_BORDER_COLOR = "var(--lumo-success-color)";
    private static final String RUOLO_BORDER_COLOR = "var(--lumo-warning-color)"; // o var(--lumo-tertiary-color)

    private static Div createStyledSectionContainer(String borderColor) {
        Div sectionContainer = new Div();
        sectionContainer.getStyle()
                .set("width", "100%")
                .set("max-width", "650px")
                .set("margin-top", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-l)")
                .set("padding-left", "calc(var(--lumo-space-l) + 3px)") // Spazio per il bordo
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-left", "3px solid " + borderColor) // Bordo colorato a sinistra
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background-color", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("box-sizing", "border-box");
        return sectionContainer;
    }

    private static HorizontalLayout createSectionTitle(Icon icon, String titleText) {
        icon.getStyle()
                .set("color", "var(--lumo-primary-color)") // Potrebbe essere il borderColor per coerenza
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("margin-right", "var(--lumo-space-m)");

        H4 title = new H4(titleText);
        title.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        title.getStyle().set("font-weight", "600");

        HorizontalLayout titleLayout = new HorizontalLayout(icon, title);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.getStyle().set("margin-bottom", "var(--lumo-space-s)");
        return titleLayout;
    }

    private static Span createTransitionTag(String prefix, String text, String baseColorVariable) {
        Span tag = new Span(prefix + " → " + text);
        tag.getStyle()
                .set("background-color", "var(" + baseColorVariable + "-10pct)")
                .set("color", "var(" + baseColorVariable + ")")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600")
                .set("border", "1px solid var(" + baseColorVariable + "-20pct)");
        return tag;
    }

    public static VerticalLayout createTimelineContent(List<Tempo> tempi, int scenarioId, AdvancedScenarioService advancedScenarioService) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();

        if (tempi.isEmpty()) {
            layout.add(new Paragraph("Nessun tempo definito per questo scenario"));
            return layout;
        }

        tempi.sort(Comparator.comparingInt(Tempo::getIdTempo));

        for (Tempo tempo : tempi) {
            Div timeCard = new Div();
            timeCard.addClassName("time-card");
            timeCard.getStyle()
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("box-shadow", "var(--lumo-box-shadow-xs)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("margin-bottom", "var(--lumo-space-l)")
                    .set("transition", "box-shadow 0.3s ease-in-out")
                    .set("width", "90%")
                    .set("max-width", "900px");

            timeCard.getElement().executeJs(
                    "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                            "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
            );

            H3 timeTitle = new H3(String.format("T%d - %s",
                    tempo.getIdTempo(),
                    formatTime(tempo.getTimerTempo())));
            timeTitle.addClassName(LumoUtility.Margin.Top.NONE);
            timeTitle.getStyle().set("text-align", "center").set("color", "var(--lumo-primary-text-color)");

            List<ParametroAggiuntivo> parametriAggiuntivi =
                    advancedScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenarioId);

            VitalSignsDataProvider tempoDataProvider = new TempoVitalSignsAdapter(tempo, parametriAggiuntivi);
            Component vitalSignsMonitorComponent = MonitorSupport.createVitalSignsMonitor(tempoDataProvider, scenarioId, false);
            Div monitorWrapper = new Div(vitalSignsMonitorComponent);
            monitorWrapper.getStyle().set("display", "flex").set("justify-content", "center").set("margin-bottom", "var(--lumo-space-m)");

            VerticalLayout detailsAndActionsContainer = new VerticalLayout();
            detailsAndActionsContainer.setPadding(false);
            detailsAndActionsContainer.setSpacing(false);
            detailsAndActionsContainer.setWidthFull();
            detailsAndActionsContainer.setAlignItems(FlexComponent.Alignment.CENTER);

            // --- Azione Section (con transizioni incluse) ---
            if (tempo.getAzione() != null && !tempo.getAzione().isEmpty()) {
                Div azioneSection = createStyledSectionContainer(AZIONE_BORDER_COLOR);
                azioneSection.add(createSectionTitle(VaadinIcon.PLAY_CIRCLE_O.create(), "Azione"));

                Paragraph azioneParagraph = new Paragraph(tempo.getAzione());
                azioneParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
                azioneParagraph.getStyle().set("white-space", "pre-wrap").set("line-height", "var(--lumo-line-height-l)");
                azioneSection.add(azioneParagraph);

                // --- Transitions (ora dentro Azione Section) ---
                if (tempo.getTSi() > 0 || tempo.getTNo() > 0 || (tempo.getTSi() == 0 && tempo.getTNo() == 0)) { // Mostra sempre la sezione transizioni se c'è un'azione
                    Hr transitionSeparator = new Hr();
                    transitionSeparator.getStyle()
                            .set("margin-top", "var(--lumo-space-m)")
                            .set("margin-bottom", "var(--lumo-space-s)");
                    azioneSection.add(transitionSeparator);

                    HorizontalLayout transitionsLayout = new HorizontalLayout();
                    transitionsLayout.setSpacing(true);
                    transitionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Allinea a sinistra dentro la card azione
                    transitionsLayout.setWidthFull(); // Occupa la larghezza per il justify

                    boolean hasSiTransition = tempo.getTSi() >= 0;
                    boolean hasNoTransition = tempo.getTNo() > 0;

                    if (hasSiTransition) {
                        transitionsLayout.add(createTransitionTag("Se SI", "T" + tempo.getTSi(), "--lumo-success-color"));
                    }

                    if (hasNoTransition) {
                        transitionsLayout.add(createTransitionTag("Se NO", "T" + tempo.getTNo(), "--lumo-error-color"));
                    } else if (!hasSiTransition) { // Se non c'è né TSi né TNo esplicito, ma TNo è 0 (default)
                        transitionsLayout.add(createTransitionTag("Se NO", "Fine simulazione", "--lumo-error-color"));
                    } else if (tempo.getTNo() == 0) { // Se c'è TSi ma TNo è 0 (default)
                        transitionsLayout.add(createTransitionTag("Se NO", "Fine simulazione", "--lumo-error-color"));
                    }


                    // Se non ci sono transizioni esplicite (TSi e TNo sono 0), mostriamo "Fine Simulazione"
                    // Questo caso è coperto dalla logica sopra se TNo è 0.
                    // Potremmo voler essere più espliciti se l'azione è l'ultima.
                    // La logica attuale: se TSi > 0 e TNo == 0, mostra "Fine simulazione" per il NO.
                    // Se TSi == 0 e TNo > 0, mostra solo il NO.
                    // Se TSi == 0 e TNo == 0, mostra "Fine simulazione" per il NO.

                    if (transitionsLayout.getComponentCount() > 0) {
                        azioneSection.add(transitionsLayout);
                    }
                }
                detailsAndActionsContainer.add(azioneSection);
            }


            // --- Altri Dettagli Section ---
            if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                Div dettagliSection = createStyledSectionContainer(DETTAGLI_BORDER_COLOR);
                dettagliSection.add(createSectionTitle(VaadinIcon.INFO_CIRCLE_O.create(), "Dettagli Aggiuntivi"));
                Paragraph dettagliParagraph = new Paragraph(tempo.getAltriDettagli());
                dettagliParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
                dettagliParagraph.getStyle().set("white-space", "pre-wrap").set("line-height", "var(--lumo-line-height-l)");
                dettagliSection.add(dettagliParagraph);
                detailsAndActionsContainer.add(dettagliSection);
            }

            // --- Ruolo Genitore Section ---
            if (tempo.getRuoloGenitore() != null && !tempo.getRuoloGenitore().isEmpty()) {
                Div ruoloSection = createStyledSectionContainer(RUOLO_BORDER_COLOR);
                ruoloSection.add(createSectionTitle(FontAwesome.Solid.CHILD_REACHING.create(), "Ruolo Genitore"));
                Paragraph ruoloParagraph = new Paragraph(tempo.getRuoloGenitore());
                ruoloParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
                ruoloParagraph.getStyle().set("white-space", "pre-wrap").set("line-height", "var(--lumo-line-height-l)");
                ruoloSection.add(ruoloParagraph);
                detailsAndActionsContainer.add(ruoloSection);
            }

            timeCard.add(
                    timeTitle,
                    monitorWrapper,
                    new Hr(), // Separatore tra monitor e dettagli/azioni
                    detailsAndActionsContainer
            );

            VerticalLayout cardWrapper = new VerticalLayout(timeCard);
            cardWrapper.setWidthFull();
            cardWrapper.setAlignItems(FlexComponent.Alignment.CENTER);
            cardWrapper.setPadding(false);
            layout.add(cardWrapper);
        }
        return layout;
    }

    private static String formatTime(float totalSeconds) {
        int minutes = (int) totalSeconds / 60;
        int remainingSeconds = (int) totalSeconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    // Adattatore da Tempo a VitalSignsDataProvider
    private record TempoVitalSignsAdapter(Tempo tempo,
                                          List<ParametroAggiuntivo> additionalParameters) implements VitalSignsDataProvider {
        @Override
        public String getPA() {
            return tempo.getPA();
        }

        @Override
        public Integer getFC() {
            return tempo.getFC();
        }

        // Assumendo che VitalSignsDataProvider.getT() sia Float e tempo.getT() sia double
        @Override
        public Double getT() {
            return tempo.getT();
        }

        @Override
        public Integer getRR() {
            return tempo.getRR();
        }

        @Override
        public Integer getSpO2() {
            return tempo.getSpO2();
        }

        @Override
        public Integer getFiO2() {
            return tempo.getFiO2();
        }

        @Override
        public Float getLitriO2() {
            return tempo.getLitriO2();
        }

        @Override
        public Integer getEtCO2() {
            return tempo.getEtCO2();
        }

        @Override
        public String getAdditionalMonitorText() {
            return null;
        }

        @Override
        public List<ParametroAggiuntivo> getAdditionalParameters() {
            return additionalParameters != null ? additionalParameters : List.of();
        }
    }
}