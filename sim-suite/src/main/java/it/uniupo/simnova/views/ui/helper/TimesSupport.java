package it.uniupo.simnova.views.ui.helper;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.views.common.utils.StyleApp;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class TimesSupport {

    private static final String AZIONE_BORDER_COLOR = "var(--lumo-primary-color)";
    private static final String DETTAGLI_BORDER_COLOR = "var(--lumo-success-color)";
    private static final String RUOLO_BORDER_COLOR = "var(--lumo-warning-color)";

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

    public static VerticalLayout createTimelineContent(List<Tempo> tempi,
                                                       int scenarioId,
                                                       AdvancedScenarioService advancedScenarioService,
                                                       boolean isPediatric) {
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


            HorizontalLayout headerLayout = new HorizontalLayout();
            headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            headerLayout.setWidthFull();

            Div leftSpacer = new Div();
            leftSpacer.getStyle().set("flex-grow", "1");
            Div rightSpacer = new Div();
            rightSpacer.getStyle().set("flex-grow", "1");

            H3 timeTitle = new H3(String.format("T%d - %s", tempo.getIdTempo(), formatTime(tempo.getTimerTempo())));
            timeTitle.addClassName(LumoUtility.Margin.Top.NONE);
            timeTitle.getStyle().set("text-align", "center").set("color", "var(--lumo-primary-text-color)");

            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.setTooltipText("Elimina Tempo T" + tempo.getIdTempo());
            deleteButton.getElement().setAttribute("aria-label", "Elimina Tempo " + tempo.getIdTempo());
            deleteButton.addClickListener(e -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setCloseOnEsc(true);
                confirmDialog.setCloseOnOutsideClick(true);

                confirmDialog.add(new H4("Conferma Eliminazione"));
                confirmDialog.add(new Paragraph("Sei sicuro di voler eliminare il Tempo T" + tempo.getIdTempo() + "? Questa operazione non può essere annullata."));

                Button confirmDeleteButton = new Button("Elimina", VaadinIcon.TRASH.create());
                confirmDeleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
                confirmDeleteButton.addClickListener(confirmEvent -> {
                    advancedScenarioService.deleteTempo(tempo.getIdTempo(), scenarioId);
                    Notification.show("Tempo T" + tempo.getIdTempo() + " eliminato con successo. Ricaricare la pagina per aggiornare la timeline.", 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    confirmDialog.close();
                    UI.getCurrent().getPage().reload();
                });

                Button cancelDeleteButton = new Button("Annulla");
                cancelDeleteButton.addClickListener(cancelEvent -> confirmDialog.close());

                HorizontalLayout dialogButtons = new HorizontalLayout(confirmDeleteButton, cancelDeleteButton);
                dialogButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
                dialogButtons.setWidthFull();
                confirmDialog.add(dialogButtons);
                confirmDialog.open();
            });

            headerLayout.add(leftSpacer, timeTitle, rightSpacer, deleteButton);
            headerLayout.setWidthFull();
            headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            timeCard.add(headerLayout);

            List<ParametroAggiuntivo> parametriAggiuntivi = advancedScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenarioId);
            VitalSignsDataProvider tempoDataProvider = new TempoVitalSignsAdapter(tempo, parametriAggiuntivi);
            Component vitalSignsMonitorComponent = MonitorSupport.createVitalSignsMonitor(tempoDataProvider, scenarioId, false, null, null, advancedScenarioService, tempo.getIdTempo());
            Div monitorWrapper = new Div(vitalSignsMonitorComponent);
            monitorWrapper.getStyle().set("display", "flex").set("justify-content", "center").set("margin-bottom", "var(--lumo-space-m)");


            VerticalLayout detailsAndActionsContainer = new VerticalLayout();
            detailsAndActionsContainer.setPadding(false);
            detailsAndActionsContainer.setSpacing(false);
            detailsAndActionsContainer.setWidthFull();
            detailsAndActionsContainer.setAlignItems(FlexComponent.Alignment.CENTER);


            Div azioneSection = createStyledSectionContainer(AZIONE_BORDER_COLOR);
            Button editAzioneButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
            editAzioneButton.setTooltipText("Modifica Azione per T" + tempo.getIdTempo());
            HorizontalLayout azioneTitleLayout = createSectionTitle(VaadinIcon.PLAY_CIRCLE_O.create(), "Azione", editAzioneButton);
            azioneSection.add(azioneTitleLayout);

            Div azioneContentWrapper = new Div();
            azioneContentWrapper.setWidthFull();
            String azioneText = tempo.getAzione();
            if( azioneText == null || azioneText.isEmpty()) {
                azioneText = "Nessuna azione definita.";
            }
            Paragraph azioneParagraph = new Paragraph(azioneText);
            azioneParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
            azioneParagraph.getStyle().set("white-space", "pre-wrap").set("line-height", "var(--lumo-line-height-l)");
            azioneContentWrapper.add(azioneParagraph);

            TextArea azioneTextArea = new TextArea("Testo Azione");
            azioneTextArea.setValue(tempo.getAzione());
            azioneTextArea.setWidthFull();
            azioneTextArea.getStyle().set("min-height", "100px");
            azioneTextArea.setVisible(false);


            final AtomicReference<HorizontalLayout> azioneSaveCancelLayoutRef = new AtomicReference<>();

            Runnable saveAzioneRunnable = () -> {
                String newValue = azioneTextArea.getValue();
                advancedScenarioService.setAzione(tempo.getIdTempo(), scenarioId, newValue);
                azioneParagraph.setText(newValue);
                azioneParagraph.setVisible(true);
                azioneTextArea.setVisible(false);
                if (azioneSaveCancelLayoutRef.get() != null) {
                    azioneSaveCancelLayoutRef.get().setVisible(false);
                }
                editAzioneButton.setVisible(true);
                Notification.show("Azione aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            };

            Runnable cancelAzioneRunnable = () -> {
                azioneTextArea.setValue(azioneParagraph.getText());
                azioneParagraph.setVisible(true);
                azioneTextArea.setVisible(false);
                if (azioneSaveCancelLayoutRef.get() != null) {
                    azioneSaveCancelLayoutRef.get().setVisible(false);
                }
                editAzioneButton.setVisible(true);
            };

            HorizontalLayout currentAzioneSaveCancelLayout = createSaveCancelButtons(saveAzioneRunnable, cancelAzioneRunnable);
            azioneSaveCancelLayoutRef.set(currentAzioneSaveCancelLayout);
            currentAzioneSaveCancelLayout.setVisible(false);
            azioneContentWrapper.add(azioneTextArea, currentAzioneSaveCancelLayout);
            azioneSection.add(azioneContentWrapper);

            editAzioneButton.addClickListener(e -> {
                azioneParagraph.setVisible(false);
                azioneTextArea.setVisible(true);
                if (azioneSaveCancelLayoutRef.get() != null) {
                    azioneSaveCancelLayoutRef.get().setVisible(true);
                }
                editAzioneButton.setVisible(false);
            });


            if (tempo.getTSi() >= 0 || tempo.getTNo() > 0) {
                Hr transitionSeparator = new Hr();
                transitionSeparator.getStyle().set("margin-top", "var(--lumo-space-m)").set("margin-bottom", "var(--lumo-space-s)");

                Button editTransitionsButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
                editTransitionsButton.setTooltipText("Modifica Transizioni per T" + tempo.getIdTempo());
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

                HorizontalLayout transitionsLayout = new HorizontalLayout();
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
                } else if (tempo.getTNo() == 0) {
                    if (tempo.getTSi() == 0 && !hasSiTransition) {
                        transitionsLayout.add(createTransitionTag("Se NO", "Fine simulazione", "--lumo-error-color"));
                    } else if (hasSiTransition && tempo.getTNo() == 0) {
                        transitionsLayout.add(createTransitionTag("Se NO", "Fine simulazione", "--lumo-error-color"));
                    } else if (!hasSiTransition && tempo.getTNo() == 0) {
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


                final AtomicReference<HorizontalLayout> transitionsSaveCancelRef = new AtomicReference<>();

                Runnable saveTransitionsRunnable = () -> {
                    int newTSi = tsiNumberField.getValue() != null ? tsiNumberField.getValue().intValue() : tempo.getTSi();
                    int newTNo = tnoNumberField.getValue() != null ? tnoNumberField.getValue().intValue() : tempo.getTNo();
                    advancedScenarioService.setTransitions(tempo.getIdTempo(), scenarioId, newTSi, newTNo);
                    transitionsLayout.setVisible(true);
                    noTransitionsDefinedMsg.setVisible(transitionsLayout.getComponentCount() == 0);
                    editTransitionsForm.setVisible(false);
                    if (transitionsSaveCancelRef.get() != null) {
                        transitionsSaveCancelRef.get().setVisible(false);
                    }
                    editTransitionsButton.setVisible(true);
                    Notification.show("Transizioni aggiornate (T" + newTSi + ", T" + newTNo + "). Ricaricare per vedere i tag aggiornati.", 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                };

                Runnable cancelTransitionsRunnable = () -> {
                    tsiNumberField.setValue((double) tempo.getTSi());
                    tnoNumberField.setValue((double) tempo.getTNo());
                    transitionsLayout.setVisible(true);
                    noTransitionsDefinedMsg.setVisible(transitionsLayout.getComponentCount() == 0);
                    editTransitionsForm.setVisible(false);
                    if (transitionsSaveCancelRef.get() != null) {
                        transitionsSaveCancelRef.get().setVisible(false);
                    }
                    editTransitionsButton.setVisible(true);
                };

                HorizontalLayout currentTransitionsSaveCancelLayout = createSaveCancelButtons(saveTransitionsRunnable, cancelTransitionsRunnable);
                transitionsSaveCancelRef.set(currentTransitionsSaveCancelLayout);
                currentTransitionsSaveCancelLayout.setVisible(false);

                viewAndEditTransitionsWrapper.add(transitionsLayout, noTransitionsDefinedMsg, editTransitionsForm, currentTransitionsSaveCancelLayout);
                azioneSection.add(viewAndEditTransitionsWrapper);

                editTransitionsButton.addClickListener(e -> {
                    transitionsLayout.setVisible(false);
                    noTransitionsDefinedMsg.setVisible(false);
                    editTransitionsForm.setVisible(true);
                    if (transitionsSaveCancelRef.get() != null) {
                        transitionsSaveCancelRef.get().setVisible(true);
                    }
                    editTransitionsButton.setVisible(false);
                });
            }
            detailsAndActionsContainer.add(azioneSection);


            Div dettagliSection = createStyledSectionContainer(DETTAGLI_BORDER_COLOR);
            Button editDettagliButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
            editDettagliButton.setTooltipText("Modifica Dettagli Aggiuntivi per T" + tempo.getIdTempo());
            HorizontalLayout dettagliTitleLayout = createSectionTitle(VaadinIcon.INFO_CIRCLE_O.create(), "Dettagli Aggiuntivi", editDettagliButton);
            dettagliSection.add(dettagliTitleLayout);

            Div dettagliContentWrapper = new Div();
            dettagliContentWrapper.setWidthFull();
            String dettagliText = tempo.getAltriDettagli();
            if (dettagliText == null || dettagliText.isEmpty()) {
                dettagliText = "Nessun dettaglio aggiuntivo definito.";
            }
            Paragraph dettagliParagraph = new Paragraph(dettagliText);
            dettagliParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
            dettagliParagraph.getStyle().set("white-space", "pre-wrap").set("line-height", "var(--lumo-line-height-l)");
            dettagliContentWrapper.add(dettagliParagraph);

            TextArea dettagliTextArea = new TextArea("Testo Dettagli Aggiuntivi");
            String tempDettagli = tempo.getAltriDettagli() != null ? tempo.getAltriDettagli() : "";
            dettagliTextArea.setValue(tempDettagli);
            dettagliTextArea.setWidthFull();
            dettagliTextArea.getStyle().set("min-height", "80px");
            dettagliTextArea.setVisible(false);


            final AtomicReference<HorizontalLayout> dettagliSaveCancelLayoutRef = new AtomicReference<>();

            Runnable saveDettagliRunnable = () -> {
                String newValue = dettagliTextArea.getValue();
                advancedScenarioService.setDettagliAggiuntivi(tempo.getIdTempo(), scenarioId, newValue);
                dettagliParagraph.setText(newValue);
                dettagliParagraph.setVisible(true);
                dettagliTextArea.setVisible(false);
                if (dettagliSaveCancelLayoutRef.get() != null) {
                    dettagliSaveCancelLayoutRef.get().setVisible(false);
                }
                editDettagliButton.setVisible(true);
                Notification.show("Dettagli aggiuntivi aggiornati.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            };

            Runnable cancelDettagliRunnable = () -> {
                dettagliTextArea.setValue(dettagliParagraph.getText());
                dettagliParagraph.setVisible(true);
                dettagliTextArea.setVisible(false);
                if (dettagliSaveCancelLayoutRef.get() != null) {
                    dettagliSaveCancelLayoutRef.get().setVisible(false);
                }
                editDettagliButton.setVisible(true);
            };

            HorizontalLayout currentDettagliSaveCancelLayout = createSaveCancelButtons(saveDettagliRunnable, cancelDettagliRunnable);
            dettagliSaveCancelLayoutRef.set(currentDettagliSaveCancelLayout);
            currentDettagliSaveCancelLayout.setVisible(false);
            dettagliContentWrapper.add(dettagliTextArea, currentDettagliSaveCancelLayout);
            dettagliSection.add(dettagliContentWrapper);

            editDettagliButton.addClickListener(e -> {
                dettagliParagraph.setVisible(false);
                dettagliTextArea.setVisible(true);
                if (dettagliSaveCancelLayoutRef.get() != null) {
                    dettagliSaveCancelLayoutRef.get().setVisible(true);
                }
                editDettagliButton.setVisible(false);
            });
            detailsAndActionsContainer.add(dettagliSection);


            if (isPediatric) {
                Div ruoloSection = createStyledSectionContainer(RUOLO_BORDER_COLOR);
                Button editRuoloButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
                editRuoloButton.setTooltipText("Modifica Ruolo Genitore per T" + tempo.getIdTempo());
                HorizontalLayout ruoloTitleLayout = createSectionTitle(FontAwesome.Solid.CHILD_REACHING.create(), "Ruolo Genitore", editRuoloButton);
                ruoloSection.add(ruoloTitleLayout);

                Div ruoloContentWrapper = new Div();
                ruoloContentWrapper.setWidthFull();
                String ruoloText = tempo.getRuoloGenitore();
                if (ruoloText == null || ruoloText.isEmpty()) {
                    ruoloText = "Nessun ruolo genitore definito.";
                }
                Paragraph ruoloParagraph = new Paragraph(ruoloText);
                ruoloParagraph.addClassName(LumoUtility.TextColor.SECONDARY);
                ruoloParagraph.getStyle().set("white-space", "pre-wrap").set("line-height", "var(--lumo-line-height-l)");
                ruoloContentWrapper.add(ruoloParagraph);

                TextArea ruoloTextArea = new TextArea("Testo Ruolo Genitore");
                ruoloTextArea.setValue(tempo.getRuoloGenitore());
                ruoloTextArea.setWidthFull();
                ruoloTextArea.getStyle().set("min-height", "80px");
                ruoloTextArea.setVisible(false);


                final AtomicReference<HorizontalLayout> ruoloSaveCancelLayoutRef = new AtomicReference<>();

                Runnable saveRuoloRunnable = () -> {
                    String newValue = ruoloTextArea.getValue();
                    advancedScenarioService.setRuoloGenitore(tempo.getIdTempo(), scenarioId, newValue);
                    ruoloParagraph.setText(newValue);
                    ruoloParagraph.setVisible(true);
                    ruoloTextArea.setVisible(false);
                    if (ruoloSaveCancelLayoutRef.get() != null) {
                        ruoloSaveCancelLayoutRef.get().setVisible(false);
                    }
                    editRuoloButton.setVisible(true);
                    Notification.show("Ruolo genitore aggiornato.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                };

                Runnable cancelRuoloRunnable = () -> {
                    ruoloTextArea.setValue(ruoloParagraph.getText());
                    ruoloParagraph.setVisible(true);
                    ruoloTextArea.setVisible(false);
                    if (ruoloSaveCancelLayoutRef.get() != null) {
                        ruoloSaveCancelLayoutRef.get().setVisible(false);
                    }
                    editRuoloButton.setVisible(true);
                };

                HorizontalLayout currentRuoloSaveCancelLayout = createSaveCancelButtons(saveRuoloRunnable, cancelRuoloRunnable);
                ruoloSaveCancelLayoutRef.set(currentRuoloSaveCancelLayout);
                currentRuoloSaveCancelLayout.setVisible(false);
                ruoloContentWrapper.add(ruoloTextArea, currentRuoloSaveCancelLayout);
                ruoloSection.add(ruoloContentWrapper);

                editRuoloButton.addClickListener(e -> {
                    ruoloParagraph.setVisible(false);
                    ruoloTextArea.setVisible(true);
                    if (ruoloSaveCancelLayoutRef.get() != null) {
                        ruoloSaveCancelLayoutRef.get().setVisible(true);
                    }
                    editRuoloButton.setVisible(false);
                });
                detailsAndActionsContainer.add(ruoloSection);
            }

            timeCard.add(monitorWrapper, new Hr(), detailsAndActionsContainer);
            VerticalLayout cardWrapper = new VerticalLayout(timeCard);
            cardWrapper.setWidthFull();
            cardWrapper.setAlignItems(FlexComponent.Alignment.CENTER);
            cardWrapper.setPadding(false);
            layout.add(cardWrapper);
        }
        HorizontalLayout buttonContainer = new HorizontalLayout();
        buttonContainer.setWidthFull();
        buttonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button addNewTimesButton = StyleApp.getButton("Aggiungi Nuovi Tempi", VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addNewTimesButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
        addNewTimesButton.getStyle().set("background-color", "var(--lumo-success-color");
        addNewTimesButton.addClickListener(ev -> UI.getCurrent().navigate("tempi/" + scenarioId + "/edit"));

        buttonContainer.add(addNewTimesButton);
        layout.add(buttonContainer);
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

