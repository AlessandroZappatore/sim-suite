package it.uniupo.simnova.views.creation.scenario;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static it.uniupo.simnova.views.constant.TargetConst.*;

@PageTitle("Target")
@Route(value = "target")
@Menu(order = 2)
public class TargetView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    private static final Logger logger = LoggerFactory.getLogger(TargetView.class);
    private final ScenarioService scenarioService;
    private final RadioButtonGroup<String> targetRadioGroup = new RadioButtonGroup<>();
    private final VerticalLayout mediciAssistentiOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout mediciSpecialistiOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout studentiMedicinaOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout studentiInfermieristicaOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout infSpecOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout studentiOdontoiatriaOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout altroOptionsLayout = createConditionalOptionsLayout();

    private final RadioButtonGroup<Integer> mediciAssistentiYearRadio = new RadioButtonGroup<>();
    private final Checkbox mediciSpecialistiAnestesiaChk = new Checkbox(SPEC_ANESTESIA);
    private final Checkbox mediciSpecialistiEmergenzaChk = new Checkbox(SPEC_EMERGENZA);
    private final Checkbox mediciSpecialistiCureIntenseChk = new Checkbox(SPEC_CURE_INTENSE);
    private final Checkbox mediciSpecialistiChirurgiaChk = new Checkbox(SPEC_CHIRURGIA);
    private final Checkbox mediciSpecialistiOstetriciaChk = new Checkbox(SPEC_OSTETRICIA);
    private final Checkbox mediciSpecialistiPediatriaChk = new Checkbox(SPEC_PEDIATRIA);
    private final Checkbox mediciSpecialistiInternaChk = new Checkbox(SPEC_INTERNA);
    private final Checkbox mediciSpecialistiCardiologiaChk = new Checkbox(SPEC_CARDIOLOGIA);
    private final Checkbox mediciSpecialistiDisastriChk = new Checkbox(SPEC_DISASTRI);
    private final Checkbox mediciSpecialistiAltroChk = new Checkbox(ALTRO); // Checkbox "Altro"
    private final TextField mediciSpecialistiAltroField = new TextField();
    private final RadioButtonGroup<Integer> studentiMedicinaYearRadio = new RadioButtonGroup<>();
    private final RadioButtonGroup<Integer> studentiInfermieristicaYearRadio = new RadioButtonGroup<>();
    private final Checkbox infSpecAnestesiaChk = new Checkbox(SPEC_ANESTESIA);
    private final Checkbox infSpecCureIntenseChk = new Checkbox(SPEC_CURE_INTENSE);
    private final Checkbox infSpecCureUrgentiChk = new Checkbox(SPEC_INF_CURE_URGENTI); // Specifica per infermieri
    private final RadioButtonGroup<Integer> studentiOdontoiatriaYearRadio = new RadioButtonGroup<>();
    private final TextField altroField = new TextField();
    private final Button nextButton = StyleApp.getNextButton();
    private Integer scenarioId;
    private String mode;


    public TargetView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;
        // Configurazione layout principale
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "TARGET E LEARNING GROUPS",
                "Seleziona il destinatario per cui è progettato lo scenario di simulazione. Per alcune categorie, saranno richieste informazioni addizionali.",
                VaadinIcon.USER_CARD.create(),
                "var(--lumo-primary-color)"
        );

        VerticalLayout contentLayout = StyleApp.getContentLayout();
        contentLayout.add(headerSection);

        configureContent(contentLayout);

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("startCreation/" + scenarioId)));

        nextButton.addClickListener(e -> saveTargetAndNavigate(e.getSource().getUI()));
        addListeners();

    }


    private void configureContent(VerticalLayout contentLayout) {
        setupTargetRadioGroup();

        setupMediciAssistentiOptions();
        setupMediciSpecialistiOptions();
        setupStudentiMedicinaOptions();
        setupStudentiInfermieristicaOptions();
        setupInfSpecOptions();
        setupStudentiOdontoiatriaOptions();
        setupAltroOptions();

        contentLayout.add(
                targetRadioGroup,
                mediciAssistentiOptionsLayout,
                mediciSpecialistiOptionsLayout,
                studentiMedicinaOptionsLayout,
                studentiInfermieristicaOptionsLayout,
                infSpecOptionsLayout,
                studentiOdontoiatriaOptionsLayout,
                altroOptionsLayout
        );

        getContent().add(contentLayout);
    }

    private VerticalLayout createConditionalOptionsLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-s)")
                .set("margin-bottom", "var(--lumo-space-s)");
        layout.setVisible(false);
        return layout;
    }

    // Helper per nascondere tutti i layout condizionali
    private void hideAllConditionalLayouts() {
        mediciAssistentiOptionsLayout.setVisible(false);
        mediciSpecialistiOptionsLayout.setVisible(false);
        studentiMedicinaOptionsLayout.setVisible(false);
        studentiInfermieristicaOptionsLayout.setVisible(false);
        infSpecOptionsLayout.setVisible(false);
        studentiOdontoiatriaOptionsLayout.setVisible(false);
        altroOptionsLayout.setVisible(false);
    }

    private void setupTargetRadioGroup() {
        targetRadioGroup.setLabel("Seleziona il destinatario");
        targetRadioGroup.setItems(
                MEDICI_ASSISTENTI, MEDICI_SPECIALISTI, STUDENTI_MEDICINA,
                INFERMIERI, STUDENTI_INFERMIERISTICA, INFERMIERI_SPECIALIZZATI,
                ODONTOIATRI, STUDENTI_ODONTOIATRIA, SOCCORRITORI,
                ASSISTENTI_DI_CURA, OPERATORE_SOCIO_SANITARIO, ALTRO
        );
        targetRadioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        targetRadioGroup.setWidthFull();
        targetRadioGroup.setRequired(true); // Campo primario richiesto
        targetRadioGroup.setRequiredIndicatorVisible(true);
    }

    private void setupMediciAssistentiOptions() {
        // Layout interno per l'opzione anno
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        Paragraph label = createBoldParagraph("Anno di corso:");

        HorizontalLayout radioLayout = new HorizontalLayout(mediciAssistentiYearRadio);
        radioLayout.setPadding(false);
        radioLayout.setSpacing(true);

        mediciAssistentiYearRadio.setItems(1, 2, 3, 4, 5, 6);
        mediciAssistentiYearRadio.setRequired(true); // Richiesto SE Medici Assistenti è selezionato

        layout.add(label, radioLayout);
        mediciAssistentiOptionsLayout.add(layout);
    }

    private void setupMediciSpecialistiOptions() {
        Paragraph label = createBoldParagraph("Seleziona specializzazione/i:");

        HorizontalLayout row1 = new HorizontalLayout(mediciSpecialistiAnestesiaChk, mediciSpecialistiEmergenzaChk, mediciSpecialistiCureIntenseChk);
        HorizontalLayout row2 = new HorizontalLayout(mediciSpecialistiChirurgiaChk, mediciSpecialistiOstetriciaChk, mediciSpecialistiPediatriaChk);
        HorizontalLayout row3 = new HorizontalLayout(mediciSpecialistiInternaChk, mediciSpecialistiCardiologiaChk, mediciSpecialistiDisastriChk);

        HorizontalLayout row4 = new HorizontalLayout();
        row4.setAlignItems(FlexComponent.Alignment.BASELINE); // Allinea checkbox e textfield
        mediciSpecialistiAltroField.setPlaceholder("Specifica");
        mediciSpecialistiAltroField.setWidth("200px"); // Adatta larghezza
        mediciSpecialistiAltroField.setEnabled(false); // Disabilitato inizialmente
        row4.add(mediciSpecialistiAltroChk, mediciSpecialistiAltroField);

        mediciSpecialistiOptionsLayout.add(label, row1, row2, row3, row4);

        mediciSpecialistiAltroChk.addValueChangeListener(event -> {
            boolean isChecked = event.getValue();
            mediciSpecialistiAltroField.setEnabled(isChecked);
            mediciSpecialistiAltroField.setRequired(isChecked);
            if (!isChecked) {
                mediciSpecialistiAltroField.clear();
            }
        });
    }

    private void setupStudentiMedicinaOptions() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        Paragraph label = createBoldParagraph("Anno accademico:");
        HorizontalLayout radioLayout = new HorizontalLayout(studentiMedicinaYearRadio);
        radioLayout.setPadding(false);
        radioLayout.setSpacing(true);
        studentiMedicinaYearRadio.setItems(1, 2, 3, 4, 5, 6);
        studentiMedicinaYearRadio.setRequired(true);
        layout.add(label, radioLayout);
        studentiMedicinaOptionsLayout.add(layout);
    }

    private void setupStudentiInfermieristicaOptions() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        Paragraph label = createBoldParagraph("Anno accademico:");
        HorizontalLayout radioLayout = new HorizontalLayout(studentiInfermieristicaYearRadio);
        radioLayout.setPadding(false);
        radioLayout.setSpacing(true);
        studentiInfermieristicaYearRadio.setItems(1, 2, 3);
        studentiInfermieristicaYearRadio.setRequired(true);
        layout.add(label, radioLayout);
        studentiInfermieristicaOptionsLayout.add(layout);
    }

    private void setupInfSpecOptions() {
        Paragraph label = createBoldParagraph("Seleziona specializzazione/i:");
        HorizontalLayout checkLayout = new HorizontalLayout(
                infSpecAnestesiaChk, infSpecCureIntenseChk, infSpecCureUrgentiChk
        );
        checkLayout.setSpacing(true);
        infSpecOptionsLayout.add(label, checkLayout);
    }

    private void setupStudentiOdontoiatriaOptions() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        Paragraph label = createBoldParagraph("Anno accademico:");
        HorizontalLayout radioLayout = new HorizontalLayout(studentiOdontoiatriaYearRadio);
        radioLayout.setPadding(false);
        radioLayout.setSpacing(true);
        studentiOdontoiatriaYearRadio.setItems(1, 2, 3, 4, 5);
        studentiOdontoiatriaYearRadio.setRequired(true);
        layout.add(label, radioLayout);
        studentiOdontoiatriaOptionsLayout.add(layout);
    }

    private void setupAltroOptions() {
        Paragraph label = createBoldParagraph("Specifica:");
        altroField.setWidthFull();
        altroField.setRequired(true);
        altroOptionsLayout.add(label, altroField);
    }

    private Paragraph createBoldParagraph(String text) {
        Paragraph p = new Paragraph(text);
        p.getStyle().set("font-weight", "bold");
        return p;
    }

    private void addListeners() {
        targetRadioGroup.addValueChangeListener(event -> {
            String selectedItem = event.getValue();
            updateConditionalLayoutsVisibility(selectedItem);
            // Aggiorna lo stato required dei campi interni (necessario per validazione manuale)
            updateFieldsRequiredStatus(selectedItem);
        });
    }

    private void updateConditionalLayoutsVisibility(String selectedTarget) {
        hideAllConditionalLayouts();
        if (selectedTarget == null) return;

        switch (selectedTarget) {
            case MEDICI_ASSISTENTI:
                mediciAssistentiOptionsLayout.setVisible(true);
                break;
            case MEDICI_SPECIALISTI:
                mediciSpecialistiOptionsLayout.setVisible(true);
                break;
            case STUDENTI_MEDICINA:
                studentiMedicinaOptionsLayout.setVisible(true);
                break;
            case STUDENTI_INFERMIERISTICA:
                studentiInfermieristicaOptionsLayout.setVisible(true);
                break;
            case INFERMIERI_SPECIALIZZATI:
                infSpecOptionsLayout.setVisible(true);
                break;
            case STUDENTI_ODONTOIATRIA:
                studentiOdontoiatriaOptionsLayout.setVisible(true);
                break;
            case ALTRO:
                altroOptionsLayout.setVisible(true);
                break;
        }
    }

    private void updateFieldsRequiredStatus(String selectedTarget) {
        mediciAssistentiYearRadio.setRequired(false);
        studentiMedicinaYearRadio.setRequired(false);
        studentiInfermieristicaYearRadio.setRequired(false);
        studentiOdontoiatriaYearRadio.setRequired(false);
        altroField.setRequired(false);
        mediciSpecialistiAltroField.setRequired(false);

        if (selectedTarget == null) return;
        switch (selectedTarget) {
            case MEDICI_ASSISTENTI:
                mediciAssistentiYearRadio.setRequired(true);
                break;
            case STUDENTI_MEDICINA:
                studentiMedicinaYearRadio.setRequired(true);
                break;
            case STUDENTI_INFERMIERISTICA:
                studentiInfermieristicaYearRadio.setRequired(true);
                break;
            case STUDENTI_ODONTOIATRIA:
                studentiOdontoiatriaYearRadio.setRequired(true);
                break;
            case ALTRO:
                altroField.setRequired(true);
                break;
            case MEDICI_SPECIALISTI, INFERMIERI_SPECIALIZZATI:
                break;
        }
        mediciAssistentiYearRadio.setRequiredIndicatorVisible(mediciAssistentiYearRadio.isRequired());
        studentiMedicinaYearRadio.setRequiredIndicatorVisible(studentiMedicinaYearRadio.isRequired());
        studentiInfermieristicaYearRadio.setRequiredIndicatorVisible(studentiInfermieristicaYearRadio.isRequired());
        studentiOdontoiatriaYearRadio.setRequiredIndicatorVisible(studentiOdontoiatriaYearRadio.isRequired());
        altroField.setRequiredIndicatorVisible(altroField.isRequired());
        mediciSpecialistiAltroField.setRequiredIndicatorVisible(mediciSpecialistiAltroField.isRequired());
    }

    private void saveTargetAndNavigate(Optional<UI> ui) {
        String selectedTarget = targetRadioGroup.getValue();

        if (selectedTarget == null || selectedTarget.trim().isEmpty()) {
            Notification.show("Selezionare un tipo di destinatario.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            targetRadioGroup.setInvalid(true);
            return;
        }
        targetRadioGroup.setInvalid(false);

        if (MEDICI_ASSISTENTI.equals(selectedTarget) && mediciAssistentiYearRadio.isEmpty()) {
            ValidationError.showValidationError(mediciAssistentiYearRadio, "Selezionare l'anno per i Medici Assistenti.");
            return;
        }
        if (STUDENTI_MEDICINA.equals(selectedTarget) && studentiMedicinaYearRadio.isEmpty()) {
            ValidationError.showValidationError(studentiMedicinaYearRadio, "Selezionare l'anno per gli Studenti di Medicina.");
            return;
        }
        if (STUDENTI_INFERMIERISTICA.equals(selectedTarget) && studentiInfermieristicaYearRadio.isEmpty()) {
            ValidationError.showValidationError(studentiInfermieristicaYearRadio, "Selezionare l'anno per gli Studenti di Infermieristica.");
            return;
        }
        if (STUDENTI_ODONTOIATRIA.equals(selectedTarget) && studentiOdontoiatriaYearRadio.isEmpty()) {
            ValidationError.showValidationError(studentiOdontoiatriaYearRadio, "Selezionare l'anno per gli Studenti di Odontoiatria.");
            return;
        }
        if (ALTRO.equals(selectedTarget) && altroField.isEmpty()) {
            ValidationError.showValidationError(altroField, "Specificare il tipo di destinatario 'Altro'.");
            return;
        }
        if (MEDICI_SPECIALISTI.equals(selectedTarget) && mediciSpecialistiAltroChk.getValue() && mediciSpecialistiAltroField.isEmpty()) {
            ValidationError.showValidationError(mediciSpecialistiAltroField, "Specificare la specializzazione 'Altro'.");
            return;
        }

        String targetString = buildTargetStringManually(selectedTarget);
        logger.info("Stringa target costruita per scenario {}: {}", scenarioId, targetString);

        try {
            boolean success = scenarioService.updateScenarioTarget(scenarioId, targetString);

            if (success) {
                logger.info("Target aggiornato con successo per scenario {}", scenarioId);
                boolean isEditMode = "edit".equals(mode);

                Notification.show("Target salvato con successo", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                if (!isEditMode) {
                    // Naviga alla prossima vista solo se NON è in modalità edit
                    ui.ifPresent(theUI -> theUI.navigate("descrizione/" + scenarioId));
                }
            } else {
                logger.error("Salvataggio target fallito per scenario {} tramite updateScenarioTarget.", scenarioId);
                Notification.show("Errore durante il salvataggio dei destinatari.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            logger.error("Eccezione durante il salvataggio del target per scenario {}: {}", scenarioId, e.getMessage(), e);
            Notification.show("Errore imprevisto durante il salvataggio.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String buildTargetStringManually(String selectedTarget) {
        StringBuilder sb = new StringBuilder(selectedTarget);

        switch (selectedTarget) {
            case MEDICI_ASSISTENTI:
                if (!mediciAssistentiYearRadio.isEmpty()) {
                    sb.append(" (").append(mediciAssistentiYearRadio.getValue()).append(" anno)");
                }
                break;
            case MEDICI_SPECIALISTI:
                List<String> selectedSpecs = getStrings();

                if (!selectedSpecs.isEmpty()) {
                    sb.append(" (").append(String.join(", ", selectedSpecs)).append(")");
                }
                break;
            case STUDENTI_MEDICINA:
                if (!studentiMedicinaYearRadio.isEmpty()) {
                    sb.append(" (").append(studentiMedicinaYearRadio.getValue()).append(" anno)");
                }
                break;
            case STUDENTI_INFERMIERISTICA:
                if (!studentiInfermieristicaYearRadio.isEmpty()) {
                    sb.append(" (").append(studentiInfermieristicaYearRadio.getValue()).append(" anno)");
                }
                break;
            case INFERMIERI_SPECIALIZZATI:
                List<String> selectedInfSpecs = new ArrayList<>();
                if (infSpecAnestesiaChk.getValue()) selectedInfSpecs.add(SPEC_ANESTESIA);
                if (infSpecCureIntenseChk.getValue()) selectedInfSpecs.add(SPEC_CURE_INTENSE);
                if (infSpecCureUrgentiChk.getValue()) selectedInfSpecs.add(SPEC_INF_CURE_URGENTI);
                if (!selectedInfSpecs.isEmpty()) {
                    sb.append(" (").append(String.join(", ", selectedInfSpecs)).append(")");
                }
                break;
            case STUDENTI_ODONTOIATRIA:
                if (!studentiOdontoiatriaYearRadio.isEmpty()) {
                    sb.append(" (").append(studentiOdontoiatriaYearRadio.getValue()).append(" anno)");
                }
                break;
            case ALTRO:
                if (!altroField.isEmpty()) {
                    sb.append(": ").append(altroField.getValue().trim());
                }
                break;
        }

        return sb.toString();
    }

    private List<String> getStrings() {
        List<String> selectedSpecs = new ArrayList<>();
        if (mediciSpecialistiAnestesiaChk.getValue()) selectedSpecs.add(SPEC_ANESTESIA);
        if (mediciSpecialistiEmergenzaChk.getValue()) selectedSpecs.add(SPEC_EMERGENZA);
        if (mediciSpecialistiCureIntenseChk.getValue()) selectedSpecs.add(SPEC_CURE_INTENSE);
        if (mediciSpecialistiChirurgiaChk.getValue()) selectedSpecs.add(SPEC_CHIRURGIA);
        if (mediciSpecialistiOstetriciaChk.getValue()) selectedSpecs.add(SPEC_OSTETRICIA);
        if (mediciSpecialistiPediatriaChk.getValue()) selectedSpecs.add(SPEC_PEDIATRIA);
        if (mediciSpecialistiInternaChk.getValue()) selectedSpecs.add(SPEC_INTERNA);
        if (mediciSpecialistiCardiologiaChk.getValue()) selectedSpecs.add(SPEC_CARDIOLOGIA);
        if (mediciSpecialistiDisastriChk.getValue()) selectedSpecs.add(SPEC_DISASTRI);

        String altroSpecText;
        if (mediciSpecialistiAltroChk.getValue()) {
            if (!mediciSpecialistiAltroField.isEmpty()) {
                altroSpecText = mediciSpecialistiAltroField.getValue().trim();
                selectedSpecs.add(ALTRO + ": " + altroSpecText);
            } else {
                selectedSpecs.add(ALTRO);
            }
        }
        return selectedSpecs;
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro mancante nell'URL.");
                throw new NumberFormatException("Scenario ID è richiesto");
            }

            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0];

            this.scenarioId = Integer.parseInt(scenarioIdStr);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Scenario ID non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("Scenario ID non valido");
            }

            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);

            VerticalLayout mainLayout = getContent();

            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst()
                    .ifPresent(header -> header.setVisible(!"edit".equals(mode)));

            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second)
                    .ifPresent(footer -> {
                        HorizontalLayout footerLayout = (HorizontalLayout) footer;
                        footerLayout.getChildren()
                                .filter(component -> component instanceof CreditsComponent)
                                .forEach(credits -> credits.setVisible(!"edit".equals(mode)));
                    });

            if ("edit".equals(mode)) {
                logger.info("Modalità EDIT: caricamento dati esistenti per scenario {}", this.scenarioId);
                nextButton.setText("Salva");
                nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                nextButton.setIcon(new Icon(VaadinIcon.CHECK));
                loadExistingTargets();
            } else {
                logger.info("Modalità CREATE: aggiunta prima riga vuota per scenario {}", this.scenarioId);
            }
        } catch (NumberFormatException e) {
            logger.error("Errore nel parsing o validazione dello Scenario ID: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante.");
        }
    }

    private void loadExistingTargets() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario == null || scenario.getTarget() == null || scenario.getTarget().trim().isEmpty()) {
            logger.debug("Nessun target esistente o scenario nullo per ID: {}", scenarioId);
            resetAllFields();
            return;
        }

        String targetString = scenario.getTarget().trim();
        logger.debug("Caricamento target esistente per scenario {}: {}", scenarioId, targetString);

        resetAllFields();

        // Estrai il target principale (prima di qualsiasi parentesi o due punti)
        String mainTarget;
        if (targetString.contains("(")) {
            mainTarget = targetString.substring(0, targetString.indexOf("(")).trim();
        } else if (targetString.contains(":")) {
            mainTarget = targetString.substring(0, targetString.indexOf(":")).trim();
        } else {
            mainTarget = targetString;
        }

        targetRadioGroup.setValue(mainTarget);

        try {
            // Estrai dettagli tra parentesi se esistono
            boolean hasParentheses = targetString.contains("(") && targetString.contains(")");
            String detailsInParentheses = "";
            if (hasParentheses) {
                detailsInParentheses = targetString.substring(
                    targetString.indexOf("(") + 1,
                    targetString.lastIndexOf(")")
                );
            }

            switch (mainTarget) {
                case MEDICI_ASSISTENTI:
                    if (hasParentheses) {
                        try {
                            String yearStr = detailsInParentheses.trim();
                            yearStr = yearStr.replace(" anno", "").trim();
                            mediciAssistentiYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Anno non valido per Medici Assistenti: '{}'", targetString);
                        }
                    }
                    break;
                case MEDICI_SPECIALISTI:
                    if (hasParentheses) {
                        if (detailsInParentheses.contains(SPEC_ANESTESIA)) mediciSpecialistiAnestesiaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_EMERGENZA)) mediciSpecialistiEmergenzaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_CURE_INTENSE)) mediciSpecialistiCureIntenseChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_CHIRURGIA)) mediciSpecialistiChirurgiaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_OSTETRICIA)) mediciSpecialistiOstetriciaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_PEDIATRIA)) mediciSpecialistiPediatriaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_INTERNA)) mediciSpecialistiInternaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_CARDIOLOGIA)) mediciSpecialistiCardiologiaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_DISASTRI)) mediciSpecialistiDisastriChk.setValue(true);

                        if (detailsInParentheses.contains(ALTRO)) {
                            mediciSpecialistiAltroChk.setValue(true);
                            String marker = ALTRO + ":";
                            int startIdx = detailsInParentheses.indexOf(marker);
                            if (startIdx != -1) {
                                int endIdx = detailsInParentheses.indexOf(",", startIdx);
                                String altroText = (endIdx == -1)
                                        ? detailsInParentheses.substring(startIdx + marker.length()).trim()
                                        : detailsInParentheses.substring(startIdx + marker.length(), endIdx).trim();
                                mediciSpecialistiAltroField.setValue(altroText);
                                mediciSpecialistiAltroField.setEnabled(true);
                            }
                        }
                    }
                    break;
                case STUDENTI_MEDICINA:
                    if (hasParentheses) {
                        try {
                            String yearStr = detailsInParentheses.trim();
                            yearStr = yearStr.replace(" anno", "").trim();
                            studentiMedicinaYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Anno non valido per Studenti Medicina: '{}'", targetString);
                        }
                    }
                    break;
                case STUDENTI_INFERMIERISTICA:
                    if (hasParentheses) {
                        try {
                            String yearStr = detailsInParentheses.trim();
                            yearStr = yearStr.replace(" anno", "").trim();
                            studentiInfermieristicaYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Anno non valido per Studenti Infermieristica: '{}'", targetString);
                        }
                    }
                    break;
                case INFERMIERI_SPECIALIZZATI:
                    if (hasParentheses) {
                        if (detailsInParentheses.contains(SPEC_ANESTESIA)) infSpecAnestesiaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_CURE_INTENSE)) infSpecCureIntenseChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_INF_CURE_URGENTI)) infSpecCureUrgentiChk.setValue(true);
                    }
                    break;
                case STUDENTI_ODONTOIATRIA:
                    if (hasParentheses) {
                        try {
                            String yearStr = detailsInParentheses.trim();
                            yearStr = yearStr.replace(" anno", "").trim();
                            studentiOdontoiatriaYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Anno non valido per Studenti Odontoiatria: '{}'", targetString);
                        }
                    }
                    break;
                case ALTRO:
                    if (targetString.contains(":")) {
                        altroField.setValue(targetString.substring(targetString.indexOf(":") + 1).trim());
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Errore durante il parsing dei dettagli della stringa target '{}': {}",
                        targetString, e.getMessage(), e);
            resetAllFields();
            targetRadioGroup.clear();
            return;
        }

        updateConditionalLayoutsVisibility(mainTarget);
        updateFieldsRequiredStatus(mainTarget);
    }

    private void resetAllFields() {
        mediciAssistentiYearRadio.clear();
        studentiMedicinaYearRadio.clear();
        studentiInfermieristicaYearRadio.clear();
        studentiOdontoiatriaYearRadio.clear();
        altroField.clear();

        Stream.of(mediciSpecialistiAnestesiaChk, mediciSpecialistiEmergenzaChk, mediciSpecialistiCureIntenseChk,
                mediciSpecialistiChirurgiaChk, mediciSpecialistiOstetriciaChk, mediciSpecialistiPediatriaChk,
                mediciSpecialistiInternaChk, mediciSpecialistiCardiologiaChk, mediciSpecialistiDisastriChk,
                mediciSpecialistiAltroChk).forEach(chk -> chk.setValue(false));
        mediciSpecialistiAltroField.clear();
        mediciSpecialistiAltroField.setEnabled(false);

        Stream.of(infSpecAnestesiaChk, infSpecCureIntenseChk, infSpecCureUrgentiChk)
                .forEach(chk -> chk.setValue(false));

        hideAllConditionalLayouts();
        updateFieldsRequiredStatus(null);

        targetRadioGroup.setInvalid(false);

        mediciAssistentiYearRadio.setInvalid(false);
        studentiMedicinaYearRadio.setInvalid(false);
        studentiInfermieristicaYearRadio.setInvalid(false);
        studentiOdontoiatriaYearRadio.setInvalid(false);
        altroField.setInvalid(false);
        mediciSpecialistiAltroField.setInvalid(false);
    }
}