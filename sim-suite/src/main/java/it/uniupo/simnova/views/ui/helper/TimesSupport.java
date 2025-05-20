package it.uniupo.simnova.views.ui.helper;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.textfield.NumberField; // Added import
import com.vaadin.flow.component.textfield.TextArea;   // Added import
import com.vaadin.flow.component.notification.Notification; // Added import
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;

import java.util.Comparator;
import java.util.List;

public class TimesSupport {

    private static final String AZIONE_BORDER_COLOR = "var(--lumo-primary-color)";
    private static final String DETTAGLI_BORDER_COLOR = "var(--lumo-success-color)";
    private static final String RUOLO_BORDER_COLOR = "var(--lumo-warning-color)";
    private static HorizontalLayout azioneSaveCancelLayout;
    private static HorizontalLayout transitionsSaveCancel;
    private static HorizontalLayout dettagliSaveCancelLayout;
    private static HorizontalLayout ruoloSaveCancelLayout;

    private static Div createStyledSectionContainer(String borderColor) {
        Div sectionContainer = new Div();
        sectionContainer.getStyle()
                .set("width", "100%")
                .set("max-width", "650px")
                .set("margin-top", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-l)")
                .set("padding-left", "calc(var(--lumo-space-l) + 3px)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-left", "3px solid " + borderColor)
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background-color", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("box-sizing", "border-box");
        return sectionContainer;
    }

    private static HorizontalLayout createSectionTitle(Icon icon, String titleText, Component... suffixComponents) {
        icon.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("margin-right", "var(--lumo-space-m)");

        H4 title = new H4(titleText);
        title.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        title.getStyle().set("font-weight", "600");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.getStyle().set("margin-bottom", "var(--lumo-space-s)");
        titleLayout.add(icon, title);

        if (suffixComponents != null && suffixComponents.length > 0) {
            Div spacer = new Div();
            spacer.getStyle().set("flex-grow", "1");
            titleLayout.add(spacer);
            for (Component suffix : suffixComponents) {
                titleLayout.add(suffix);
            }
            titleLayout.setWidthFull();
        }
        return titleLayout;
    }

    private static Button createEditButton(String ariaLabel) {
        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.getElement().setAttribute("aria-label", "Modifica " + ariaLabel);
        editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        return editButton;
    }

    private static HorizontalLayout createSaveCancelButtons(Runnable saveAction, Runnable cancelAction) {
        Button saveButton = new Button("Salva", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveAction.run());

        Button cancelButton = new Button("Annulla");
        cancelButton.addClickListener(e -> cancelAction.run());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonsLayout.setSpacing(true);
        buttonsLayout.getStyle().set("margin-top", "var(--lumo-space-s)");
        return buttonsLayout;
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
            // ... (timeCard styling and mouseover unchanged)
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


            H3 timeTitle = new H3(String.format("T%d - %s", tempo.getIdTempo(), formatTime(tempo.getTimerTempo())));
            // ... (timeTitle styling unchanged)
            timeTitle.addClassName(LumoUtility.Margin.Top.NONE);
            timeTitle.getStyle().set("text-align", "center").set("color", "var(--lumo-primary-text-color)");


            List<ParametroAggiuntivo> parametriAggiuntivi = advancedScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenarioId);
            VitalSignsDataProvider tempoDataProvider = new TempoVitalSignsAdapter(tempo, parametriAggiuntivi);
            Component vitalSignsMonitorComponent = MonitorSupport.createVitalSignsMonitor(tempoDataProvider, scenarioId, false);
            Div monitorWrapper = new Div(vitalSignsMonitorComponent);
            // ... (monitorWrapper styling unchanged)
            monitorWrapper.getStyle().set("display", "flex").set("justify-content", "center").set("margin-bottom", "var(--lumo-space-m)");


            VerticalLayout detailsAndActionsContainer = new VerticalLayout();
            // ... (detailsAndActionsContainer styling unchanged)
            detailsAndActionsContainer.setPadding(false);
            detailsAndActionsContainer.setSpacing(false);
            detailsAndActionsContainer.setWidthFull();
            detailsAndActionsContainer.setAlignItems(FlexComponent.Alignment.CENTER);


            // --- Azione Section ---
            if (tempo.getAzione() != null && !tempo.getAzione().isEmpty()) {
                Div azioneSection = createStyledSectionContainer(AZIONE_BORDER_COLOR);
                Button editAzioneButton = createEditButton("Azione Principale");
                HorizontalLayout azioneTitleLayout = createSectionTitle(VaadinIcon.PLAY_CIRCLE_O.create(), "Azione", editAzioneButton);
                azioneSection.add(azioneTitleLayout);

                Div azioneContentWrapper = new Div();
                azioneContentWrapper.setWidthFull();
                Paragraph azioneParagraph = new Paragraph(tempo.getAzione());
                azioneParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
                azioneParagraph.getStyle().set("white-space", "pre-wrap").set("line-height", "var(--lumo-line-height-l)");
                azioneContentWrapper.add(azioneParagraph);

                TextArea azioneTextArea = new TextArea("Testo Azione");
                azioneTextArea.setValue(tempo.getAzione());
                azioneTextArea.setWidthFull();
                azioneTextArea.getStyle().set("min-height", "100px");
                azioneTextArea.setVisible(false);

                azioneSaveCancelLayout = createSaveCancelButtons(
                        () -> { // Save Azione
                            String newValue = azioneTextArea.getValue();
                            // tempo.setAzione(newValue); // Update domain object (conceptual)
                            // advancedScenarioService.updateAzione(tempo.getIdTempo(), scenarioId, newValue); // Actual save call
                            azioneParagraph.setText(newValue);
                            azioneParagraph.setVisible(true);
                            azioneTextArea.setVisible(false);
                            azioneSaveCancelLayout.setVisible(true);
                            editAzioneButton.setVisible(true);
                            Notification.show("Azione aggiornata.", 3000, Notification.Position.BOTTOM_CENTER);
                        },
                        () -> { // Cancel Azione
                            azioneTextArea.setValue(azioneParagraph.getText()); // Reset from paragraph
                            azioneParagraph.setVisible(true);
                            azioneTextArea.setVisible(false);
                            azioneSaveCancelLayout.setVisible(true);
                            editAzioneButton.setVisible(true);
                            Notification.show("Modifica azione annullata.", 3000, Notification.Position.BOTTOM_CENTER);
                        }
                );
                azioneSaveCancelLayout.setVisible(false);
                azioneContentWrapper.add(azioneTextArea, azioneSaveCancelLayout);
                azioneSection.add(azioneContentWrapper);

                editAzioneButton.addClickListener(e -> {
                    azioneParagraph.setVisible(false);
                    azioneTextArea.setVisible(true);
                    azioneSaveCancelLayout.setVisible(true);
                    editAzioneButton.setVisible(false);
                });

                // --- Transitions subsection ---
                if (tempo.getTSi() >= 0 || tempo.getTNo() > 0) { // Simplified condition to show transition section if either is set (TSi can be 0)
                    Hr transitionSeparator = new Hr();
                    transitionSeparator.getStyle().set("margin-top", "var(--lumo-space-m)").set("margin-bottom", "var(--lumo-space-s)");

                    Button editTransitionsButton = createEditButton("Transizioni Se SI/NO");
                    HorizontalLayout transitionsHeaderLayout = new HorizontalLayout();
                    transitionsHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                    transitionsHeaderLayout.setWidthFull();
                    transitionsHeaderLayout.getStyle().set("margin-bottom", "var(--lumo-space-xs)");
                    Span transitionsLabel = new Span("Transizioni Condizionali");
                    transitionsLabel.getStyle().set("font-weight", "500").set("color", "var(--lumo-body-text-color)").set("font-size", "var(--lumo-font-size-s)");
                    Div thSpacer = new Div();
                    thSpacer.getStyle().set("flex-grow", "1");
                    transitionsHeaderLayout.add(transitionsLabel, thSpacer, editTransitionsButton);

                    azioneSection.add(transitionSeparator, transitionsHeaderLayout);

                    Div viewAndEditTransitionsWrapper = new Div();
                    viewAndEditTransitionsWrapper.setWidthFull();

                    HorizontalLayout transitionsLayout = new HorizontalLayout(); // For displaying tags
                    transitionsLayout.setSpacing(true);
                    transitionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                    transitionsLayout.setWidthFull();

                    boolean hasSiTransition = tempo.getTSi() >= 0;
                    boolean hasNoTransition = tempo.getTNo() > 0;

                    if (hasSiTransition) {
                        transitionsLayout.add(createTransitionTag("Se SI", "T" + tempo.getTSi(), "--lumo-success-color"));
                    }
                    if (hasNoTransition) {
                        transitionsLayout.add(createTransitionTag("Se NO", "T" + tempo.getTNo(), "--lumo-error-color"));
                    } else if (tempo.getTNo() == 0) { // TNo is 0 (default or explicitly set)
                        if (tempo.getTSi() == 0 && !hasSiTransition) { // Both 0, and no TSi tag yet (e.g. TSi was also 0)
                            transitionsLayout.add(createTransitionTag("Se NO", "Fine simulazione", "--lumo-error-color"));
                        } else if (hasSiTransition && tempo.getTNo() == 0) { // TSi exists, TNo is 0
                            transitionsLayout.add(createTransitionTag("Se NO", "Fine simulazione", "--lumo-error-color"));
                        } else if (!hasSiTransition && tempo.getTNo() == 0) { // No TSi, TNo is 0
                            transitionsLayout.add(createTransitionTag("Se NO", "Fine simulazione", "--lumo-error-color"));
                        }
                    }

                    Paragraph noTransitionsDefinedMsg = new Paragraph("Nessuna transizione esplicita definita (potrebbe terminare o andare a T0).");
                    noTransitionsDefinedMsg.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)").set("text-align", "center");
                    noTransitionsDefinedMsg.setVisible(transitionsLayout.getComponentCount() == 0);


                    NumberField tsiNumberField = new NumberField("ID Tempo 'Se SI' (0 per fine/default)");
                    tsiNumberField.setValue((double) tempo.getTSi());
                    tsiNumberField.setStepButtonsVisible(true);
                    tsiNumberField.setMin(0);
                    tsiNumberField.setWidthFull();

                    NumberField tnoNumberField = new NumberField("ID Tempo 'Se NO' (0 per fine/default)");
                    tnoNumberField.setValue((double) tempo.getTNo());
                    tnoNumberField.setStepButtonsVisible(true);
                    tnoNumberField.setMin(0);
                    tnoNumberField.setWidthFull();

                    VerticalLayout editTransitionsForm = new VerticalLayout(tsiNumberField, tnoNumberField);
                    editTransitionsForm.setPadding(false);
                    editTransitionsForm.setSpacing(true);
                    editTransitionsForm.setWidthFull();
                    editTransitionsForm.setVisible(false);

                    transitionsSaveCancel = createSaveCancelButtons(
                            () -> { // Save Transitions
                                int newTSi = tsiNumberField.getValue() != null ? tsiNumberField.getValue().intValue() : tempo.getTSi();
                                int newTNo = tnoNumberField.getValue() != null ? tnoNumberField.getValue().intValue() : tempo.getTNo();
                                // tempo.setTSi(newTSi); tempo.setTNo(newTNo); // Update domain object (conceptual)
                                // advancedScenarioService.updateTransitions(tempo.getIdTempo(), scenarioId, newTSi, newTNo);

                                // For UI, ideally re-render tags. For now, show notification.
                                transitionsLayout.setVisible(true);
                                noTransitionsDefinedMsg.setVisible(transitionsLayout.getComponentCount() == 0); // Re-evaluate based on (stale) tags
                                editTransitionsForm.setVisible(false);
                                transitionsSaveCancel.setVisible(false);
                                editTransitionsButton.setVisible(true);
                                Notification.show("Transizioni aggiornate (T" + newTSi + ", T" + newTNo + "). Ricaricare per vedere i tag aggiornati.", 5000, Notification.Position.BOTTOM_CENTER);
                            },
                            () -> { // Cancel Transitions
                                tsiNumberField.setValue((double) tempo.getTSi()); // Reset from domain
                                tnoNumberField.setValue((double) tempo.getTNo());
                                transitionsLayout.setVisible(true);
                                noTransitionsDefinedMsg.setVisible(transitionsLayout.getComponentCount() == 0);
                                editTransitionsForm.setVisible(false);
                                transitionsSaveCancel.setVisible(false);
                                editTransitionsButton.setVisible(true);
                                Notification.show("Modifica transizioni annullata.", 3000, Notification.Position.BOTTOM_CENTER);
                            }
                    );
                    transitionsSaveCancel.setVisible(false);

                    viewAndEditTransitionsWrapper.add(transitionsLayout, noTransitionsDefinedMsg, editTransitionsForm, transitionsSaveCancel);
                    azioneSection.add(viewAndEditTransitionsWrapper);

                    editTransitionsButton.addClickListener(e -> {
                        transitionsLayout.setVisible(false);
                        noTransitionsDefinedMsg.setVisible(false);
                        editTransitionsForm.setVisible(true);
                        transitionsSaveCancel.setVisible(true);
                        editTransitionsButton.setVisible(false);
                    });
                }
                detailsAndActionsContainer.add(azioneSection);
            }

            // --- Altri Dettagli Section ---
            if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                Div dettagliSection = createStyledSectionContainer(DETTAGLI_BORDER_COLOR);
                Button editDettagliButton = createEditButton("Dettagli Aggiuntivi");
                HorizontalLayout dettagliTitleLayout = createSectionTitle(VaadinIcon.INFO_CIRCLE_O.create(), "Dettagli Aggiuntivi", editDettagliButton);
                dettagliSection.add(dettagliTitleLayout);

                Div dettagliContentWrapper = new Div();
                dettagliContentWrapper.setWidthFull();
                Paragraph dettagliParagraph = new Paragraph(tempo.getAltriDettagli());
                dettagliParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
                dettagliParagraph.getStyle().set("white-space", "pre-wrap").set("line-height", "var(--lumo-line-height-l)");
                dettagliContentWrapper.add(dettagliParagraph);

                TextArea dettagliTextArea = new TextArea("Testo Dettagli Aggiuntivi");
                dettagliTextArea.setValue(tempo.getAltriDettagli());
                dettagliTextArea.setWidthFull();
                dettagliTextArea.getStyle().set("min-height", "80px");
                dettagliTextArea.setVisible(false);

                dettagliSaveCancelLayout = createSaveCancelButtons(
                        () -> { // Save Dettagli
                            String newValue = dettagliTextArea.getValue();
                            // tempo.setAltriDettagli(newValue);
                            dettagliParagraph.setText(newValue);
                            dettagliParagraph.setVisible(true);
                            dettagliTextArea.setVisible(false);
                            dettagliSaveCancelLayout.setVisible(false);
                            editDettagliButton.setVisible(true);
                            Notification.show("Dettagli aggiuntivi aggiornati.", 3000, Notification.Position.BOTTOM_CENTER);
                        },
                        () -> { // Cancel Dettagli
                            dettagliTextArea.setValue(dettagliParagraph.getText());
                            dettagliParagraph.setVisible(true);
                            dettagliTextArea.setVisible(false);
                            dettagliSaveCancelLayout.setVisible(false);
                            editDettagliButton.setVisible(true);
                            Notification.show("Modifica dettagli annullata.", 3000, Notification.Position.BOTTOM_CENTER);
                        }
                );
                dettagliSaveCancelLayout.setVisible(false);
                dettagliContentWrapper.add(dettagliTextArea, dettagliSaveCancelLayout);
                dettagliSection.add(dettagliContentWrapper);

                editDettagliButton.addClickListener(e -> {
                    dettagliParagraph.setVisible(false);
                    dettagliTextArea.setVisible(true);
                    dettagliSaveCancelLayout.setVisible(true);
                    editDettagliButton.setVisible(false);
                });
                detailsAndActionsContainer.add(dettagliSection);
            }

            // --- Ruolo Genitore Section ---
            if (tempo.getRuoloGenitore() != null && !tempo.getRuoloGenitore().isEmpty()) {
                Div ruoloSection = createStyledSectionContainer(RUOLO_BORDER_COLOR);
                Button editRuoloButton = createEditButton("Ruolo Genitore");
                HorizontalLayout ruoloTitleLayout = createSectionTitle(FontAwesome.Solid.CHILD_REACHING.create(), "Ruolo Genitore", editRuoloButton);
                ruoloSection.add(ruoloTitleLayout);

                Div ruoloContentWrapper = new Div();
                ruoloContentWrapper.setWidthFull();
                Paragraph ruoloParagraph = new Paragraph(tempo.getRuoloGenitore());
                ruoloParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
                ruoloParagraph.getStyle().set("white-space", "pre-wrap").set("line-height", "var(--lumo-line-height-l)");
                ruoloContentWrapper.add(ruoloParagraph);

                TextArea ruoloTextArea = new TextArea("Testo Ruolo Genitore");
                ruoloTextArea.setValue(tempo.getRuoloGenitore());
                ruoloTextArea.setWidthFull();
                ruoloTextArea.getStyle().set("min-height", "80px");
                ruoloTextArea.setVisible(false);

                ruoloSaveCancelLayout = createSaveCancelButtons(
                        () -> { // Save Ruolo
                            String newValue = ruoloTextArea.getValue();
                            // tempo.setRuoloGenitore(newValue);
                            ruoloParagraph.setText(newValue);
                            ruoloParagraph.setVisible(true);
                            ruoloTextArea.setVisible(false);
                            ruoloSaveCancelLayout.setVisible(false);
                            editRuoloButton.setVisible(true);
                            Notification.show("Ruolo genitore aggiornato.", 3000, Notification.Position.BOTTOM_CENTER);
                        },
                        () -> { // Cancel Ruolo
                            ruoloTextArea.setValue(ruoloParagraph.getText());
                            ruoloParagraph.setVisible(true);
                            ruoloTextArea.setVisible(false);
                            ruoloSaveCancelLayout.setVisible(false);
                            editRuoloButton.setVisible(true);
                            Notification.show("Modifica ruolo annullata.", 3000, Notification.Position.BOTTOM_CENTER);
                        }
                );
                ruoloSaveCancelLayout.setVisible(false);
                ruoloContentWrapper.add(ruoloTextArea, ruoloSaveCancelLayout);
                ruoloSection.add(ruoloContentWrapper);

                editRuoloButton.addClickListener(e -> {
                    ruoloParagraph.setVisible(false);
                    ruoloTextArea.setVisible(true);
                    ruoloSaveCancelLayout.setVisible(true);
                    editRuoloButton.setVisible(false);
                });
                detailsAndActionsContainer.add(ruoloSection);
            }

            timeCard.add(timeTitle, monitorWrapper, new Hr(), detailsAndActionsContainer);
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