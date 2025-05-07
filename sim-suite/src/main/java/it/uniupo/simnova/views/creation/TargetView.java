package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox; // Import Checkbox
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
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.home.CreditsComponent;
import it.uniupo.simnova.views.home.StyleApp;
import it.uniupo.simnova.views.home.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@PageTitle("Target")
@Route(value = "target")
@Menu(order = 2)
public class TargetView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private static final Logger logger = LoggerFactory.getLogger(TargetView.class);
    private String mode;

    // --- Costanti Target Principali ---
    private static final String MEDICI_ASSISTENTI = "Medici Assistenti";
    private static final String MEDICI_SPECIALISTI = "Medici specialisti";
    private static final String STUDENTI_MEDICINA = "Studenti di medicina";
    private static final String INFERMIERI = "Infermieri";
    private static final String STUDENTI_INFERMIERISTICA = "Studenti di Infermieristica";
    private static final String INFERMIERI_SPECIALIZZATI = "Infermieri Specializzati";
    private static final String ODONTOIATRI = "Odontoiatri";
    private static final String STUDENTI_ODONTOIATRIA = "Studenti di Odontoiatria";
    private static final String SOCCORRITORI = "Soccorritori";
    private static final String ASSISTENTI_DI_CURA = "Assistenti di cura";
    private static final String OPERATORE_SOCIO_SANITARIO = "Operatore Socio Sanitario";
    private static final String ALTRO = "Altro";

    // --- Costanti Specializzazioni ---
    private static final String SPEC_ANESTESIA = "Anestesia";
    private static final String SPEC_EMERGENZA = "Emergenza/Urgenza";
    private static final String SPEC_CURE_INTENSE = "Cure intense";
    private static final String SPEC_CHIRURGIA = "Chirurgia";
    private static final String SPEC_OSTETRICIA = "Ostetricia/Ginecologia";
    private static final String SPEC_PEDIATRIA = "Pediatria";
    private static final String SPEC_INTERNA = "M. Interna";
    private static final String SPEC_CARDIOLOGIA = "Cardiologia";
    private static final String SPEC_DISASTRI = "Med. dei Disastri/Umanitaria";
    // "Altro" è già definito sopra
    private static final String SPEC_INF_CURE_URGENTI = "Cure Urgenti"; // Cure Urgenti per Inf.


    // --- Componenti UI ---
    // Principale
    private final RadioButtonGroup<String> targetRadioGroup = new RadioButtonGroup<>();

    // Layout contenitori opzioni aggiuntive
    private final VerticalLayout mediciAssistentiOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout mediciSpecialistiOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout studentiMedicinaOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout studentiInfermieristicaOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout infSpecOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout studentiOdontoiatriaOptionsLayout = createConditionalOptionsLayout();
    private final VerticalLayout altroOptionsLayout = createConditionalOptionsLayout();

    // Componenti specifici per opzioni aggiuntive
    // Medici Assistenti
    private final RadioButtonGroup<Integer> mediciAssistentiYearRadio = new RadioButtonGroup<>();

    // Medici Specialisti (usando Checkbox)
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

    // Studenti Medicina
    private final RadioButtonGroup<Integer> studentiMedicinaYearRadio = new RadioButtonGroup<>();

    // Studenti Infermieristica
    private final RadioButtonGroup<Integer> studentiInfermieristicaYearRadio = new RadioButtonGroup<>();

    // Infermieri Specializzati (usando Checkbox)
    private final Checkbox infSpecAnestesiaChk = new Checkbox(SPEC_ANESTESIA);
    private final Checkbox infSpecCureIntenseChk = new Checkbox(SPEC_CURE_INTENSE);
    private final Checkbox infSpecCureUrgentiChk = new Checkbox(SPEC_INF_CURE_URGENTI); // Specifica per infermieri

    // Studenti Odontoiatria
    private final RadioButtonGroup<Integer> studentiOdontoiatriaYearRadio = new RadioButtonGroup<>();

    // Altro
    private final TextField altroField = new TextField();

    // Pulsanti Navigazione
    private final Button nextButton = StyleApp.getNextButton();


    public TargetView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;
        // Aggiunto per AppHeader

        // Configurazione layout principale
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "TARGET E LEARNING GROUPS",
                "Seleziona il destinatario per cui è progettato lo scenario di simulazione. Per alcune categorie, saranno richieste informazioni addizionali.",
                VaadinIcon.USER_CARD,
                "#4285F4"
        );

        VerticalLayout contentLayout = StyleApp.getContentLayout();
        // Configurazione Contenuto
        configureContent(contentLayout);
        // Configurazione Footer
        Button nextButton = StyleApp.getNextButton();

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(
                customHeader,
                headerSection,
                contentLayout,
                footerLayout
        );

        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("startCreation/" + scenarioId)));

        // Navigazione Avanti (con salvataggio)
        nextButton.addClickListener(e -> saveTargetAndNavigate(e.getSource().getUI()));
        // Aggiunta Listener
        addListeners();

        // Nasconde inizialmente le opzioni aggiuntive (viene fatto anche in createConditionalOptionsLayout)
        // hideAllConditionalLayouts(); // Opzionale ridondanza
    }


    private void configureContent(VerticalLayout contentLayout) {
        // Setup RadioGroup principale
        setupTargetRadioGroup();

        // Setup opzioni aggiuntive per tutti i tipi di destinatari
        setupMediciAssistentiOptions();
        setupMediciSpecialistiOptions(); // Modificato per usare Checkbox
        setupStudentiMedicinaOptions();
        setupStudentiInfermieristicaOptions();
        setupInfSpecOptions(); // Modificato per usare Checkbox
        setupStudentiOdontoiatriaOptions();
        setupAltroOptions();

        // Aggiunta componenti al layout contenuto
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

    // Helper per creare i layout condizionali con stile comune
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
        layout.setVisible(false); // Nascosto di default
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


    // --- Setup Specifici per Componenti ---

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

        // Disposizione Checkbox (esempio: 3 per riga)
        HorizontalLayout row1 = new HorizontalLayout(mediciSpecialistiAnestesiaChk, mediciSpecialistiEmergenzaChk, mediciSpecialistiCureIntenseChk);
        HorizontalLayout row2 = new HorizontalLayout(mediciSpecialistiChirurgiaChk, mediciSpecialistiOstetriciaChk, mediciSpecialistiPediatriaChk);
        HorizontalLayout row3 = new HorizontalLayout(mediciSpecialistiInternaChk, mediciSpecialistiCardiologiaChk, mediciSpecialistiDisastriChk);

        // Riga per "Altro"
        HorizontalLayout row4 = new HorizontalLayout();
        row4.setAlignItems(FlexComponent.Alignment.BASELINE); // Allinea checkbox e textfield
        mediciSpecialistiAltroField.setPlaceholder("Specifica");
        mediciSpecialistiAltroField.setWidth("200px"); // Adatta larghezza
        mediciSpecialistiAltroField.setEnabled(false); // Disabilitato inizialmente
        row4.add(mediciSpecialistiAltroChk, mediciSpecialistiAltroField);

        mediciSpecialistiOptionsLayout.add(label, row1, row2, row3, row4);

        // Aggiungi listener per abilitare/disabilitare/richiedere il textfield "Altro"
        mediciSpecialistiAltroChk.addValueChangeListener(event -> {
            boolean isChecked = event.getValue();
            mediciSpecialistiAltroField.setEnabled(isChecked);
            mediciSpecialistiAltroField.setRequired(isChecked); // Richiesto se il checkbox è flaggato
            if (!isChecked) {
                mediciSpecialistiAltroField.clear(); // Pulisci se deselezionato
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
        // Layout orizzontale per i checkbox
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

    // Helper per creare paragrafi in grassetto
    private Paragraph createBoldParagraph(String text) {
        Paragraph p = new Paragraph(text);
        p.getStyle().set("font-weight", "bold");
        return p;
    }

    // --- Listeners ---

    private void addListeners() {
        // Navigazione Indietro


        // Listener per mostrare/nascondere opzioni aggiuntive
        targetRadioGroup.addValueChangeListener(event -> {
            String selectedItem = event.getValue();
            updateConditionalLayoutsVisibility(selectedItem);
            // Aggiorna lo stato required dei campi interni (necessario per validazione manuale)
            updateFieldsRequiredStatus(selectedItem);
        });
    }

    // Aggiorna visibilità layout
    private void updateConditionalLayoutsVisibility(String selectedTarget) {
        hideAllConditionalLayouts(); // Nascondi tutto prima
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

    // Aggiorna stato required dei campi interni (per validazione manuale)
    private void updateFieldsRequiredStatus(String selectedTarget) {
        // Resetta tutti i required interni a false
        mediciAssistentiYearRadio.setRequired(false);
        studentiMedicinaYearRadio.setRequired(false);
        studentiInfermieristicaYearRadio.setRequired(false);
        studentiOdontoiatriaYearRadio.setRequired(false);
        altroField.setRequired(false);
        mediciSpecialistiAltroField.setRequired(false); // Gestito dal listener del suo checkbox

        // Imposta required=true solo per i campi del target selezionato
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
            case MEDICI_SPECIALISTI:
                // Il campo testo "Altro" è richiesto solo se il suo checkbox è selezionato (gestito nel listener)
                // Potremmo voler richiedere che ALMENO un checkbox sia selezionato, ma la validazione manuale diventa complessa.
                // Per semplicità, non impostiamo required sui checkbox qui.
                break;
            case INFERMIERI_SPECIALIZZATI:
                // Come sopra, validare che almeno un checkbox sia selezionato è complesso manualmente.
                break;
        }

        // Rendi visibili gli indicatori required aggiornati
        mediciAssistentiYearRadio.setRequiredIndicatorVisible(mediciAssistentiYearRadio.isRequired());
        studentiMedicinaYearRadio.setRequiredIndicatorVisible(studentiMedicinaYearRadio.isRequired());
        studentiInfermieristicaYearRadio.setRequiredIndicatorVisible(studentiInfermieristicaYearRadio.isRequired());
        studentiOdontoiatriaYearRadio.setRequiredIndicatorVisible(studentiOdontoiatriaYearRadio.isRequired());
        altroField.setRequiredIndicatorVisible(altroField.isRequired());
        mediciSpecialistiAltroField.setRequiredIndicatorVisible(mediciSpecialistiAltroField.isRequired());
    }


    // --- Salvataggio e Navigazione ---

    private void saveTargetAndNavigate(Optional<UI> ui) {
        String selectedTarget = targetRadioGroup.getValue();

        // Validazione base: un target deve essere selezionato
        if (selectedTarget == null || selectedTarget.trim().isEmpty()) {
            Notification.show("Selezionare un tipo di destinatario.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            targetRadioGroup.setInvalid(true); // Evidenzia il campo
            return;
        }
        targetRadioGroup.setInvalid(false); // Rimuovi eventuale stato di errore precedente

        // Validazione campi condizionali (esempio)
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

        // Costruzione della stringa target
        String targetString = buildTargetStringManually(selectedTarget);
        logger.info("Stringa target costruita per scenario {}: {}", scenarioId, targetString);

        // Chiamata al servizio per salvare
        try {
            boolean success = scenarioService.updateScenarioTarget(scenarioId, targetString);

            if (success) {
                logger.info("Target aggiornato con successo per scenario {}", scenarioId);
                // Verifica la modalità: se è "edit" mostra solo la notifica, altrimenti naviga
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

    // Costruisce la stringa target leggendo manualmente i valori dai componenti
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
            // Nessun dettaglio per INFERMIERI, ODONTOIATRI, SOCCORRITORI, ASSISTENTI_DI_CURA, OPERATORE_SOCIO_SANITARIO
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

        // Gestisci "Altro"
        String altroSpecText;
        if (mediciSpecialistiAltroChk.getValue()) {
            if (!mediciSpecialistiAltroField.isEmpty()) {
                altroSpecText = mediciSpecialistiAltroField.getValue().trim();
                selectedSpecs.add(ALTRO + ": " + altroSpecText); // Aggiungi con il testo
            } else {
                selectedSpecs.add(ALTRO); // Aggiungi solo "Altro" se il campo è vuoto ma checkato
            }
        }
        return selectedSpecs;
    }


    // --- Gestione Parametri e Caricamento Dati ---

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro mancante nell'URL.");
                throw new NumberFormatException("Scenario ID è richiesto");
            }

            // Dividi il parametro usando '/' come separatore
            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0]; // Il primo elemento è l'ID dello scenario

            // Verifica e imposta l'ID scenario
            this.scenarioId = Integer.parseInt(scenarioIdStr);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Scenario ID non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("Scenario ID non valido");
            }

            // Imposta la modalità se presente come secondo elemento
            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);

            // Modifica la visibilità dell'header e dei crediti
            VerticalLayout mainLayout = getContent();

            // Gestione dell'header (il primo HorizontalLayout)
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst()
                    .ifPresent(header -> header.setVisible(!"edit".equals(mode)));

            // Gestione del footer con i crediti (l'ultimo HorizontalLayout)
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second) // Prendi l'ultimo elemento
                    .ifPresent(footer -> {
                        HorizontalLayout footerLayout = (HorizontalLayout) footer;
                        footerLayout.getChildren()
                                .filter(component -> component instanceof CreditsComponent)
                                .forEach(credits -> credits.setVisible(!"edit".equals(mode)));
                    });

            // Inizializza la vista in base alla modalità
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

    /**
     * Carica i target esistenti per lo scenario corrente, popolando manualmente i componenti.
     */
    private void loadExistingTargets() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        String targetString;

        if (scenario != null && scenario.getTarget() != null && !scenario.getTarget().trim().isEmpty()) {
            targetString = scenario.getTarget().trim();
            logger.debug("Caricamento target esistente per scenario {}: {}", scenarioId, targetString);
        } else {
            logger.debug("Nessun target esistente o scenario nullo per ID: {}", scenarioId);
            // Resetta i campi a uno stato vuoto/default
            resetAllFields();
            return; // Esce se non c'è nulla da caricare
        }

        // Resetta i campi prima di caricarli
        resetAllFields();

        // Determina il tipo di target principale
        String mainTarget = targetString.split("[:(]")[0].trim();
        targetRadioGroup.setValue(mainTarget); // Imposta il radio button principale

        // Popola i campi condizionali in base alla stringa
        try {
            // Gestisci diversamente il parsing in base alla categoria e al formato
            String substring = targetString.substring(targetString.indexOf("(") + 1, targetString.indexOf(")"));
            String substring1 = targetString.substring(targetString.indexOf("(") + 1, targetString.lastIndexOf(")"));
            switch (mainTarget) {
                case MEDICI_ASSISTENTI:
                    if (targetString.contains("(") && targetString.contains(")")) {
                        try {
                            // Estrai il contenuto tra parentesi, converte in int e imposta
                            String yearStr = substring.trim();
                            // Rimuovi " anno" se presente nel testo estratto
                            yearStr = yearStr.replace(" anno", "").trim();
                            mediciAssistentiYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Anno non valido per Medici Assistenti: '{}'", targetString);
                        }
                    }
                    break;
                case MEDICI_SPECIALISTI:
                    if (targetString.contains("(") && targetString.contains(")")) {
                        // Popola i checkbox
                        if (substring1.contains(SPEC_ANESTESIA)) mediciSpecialistiAnestesiaChk.setValue(true);
                        if (substring1.contains(SPEC_EMERGENZA)) mediciSpecialistiEmergenzaChk.setValue(true);
                        if (substring1.contains(SPEC_CURE_INTENSE)) mediciSpecialistiCureIntenseChk.setValue(true);
                        if (substring1.contains(SPEC_CHIRURGIA)) mediciSpecialistiChirurgiaChk.setValue(true);
                        if (substring1.contains(SPEC_OSTETRICIA)) mediciSpecialistiOstetriciaChk.setValue(true);
                        if (substring1.contains(SPEC_PEDIATRIA)) mediciSpecialistiPediatriaChk.setValue(true);
                        if (substring1.contains(SPEC_INTERNA)) mediciSpecialistiInternaChk.setValue(true);
                        if (substring1.contains(SPEC_CARDIOLOGIA)) mediciSpecialistiCardiologiaChk.setValue(true);
                        if (substring1.contains(SPEC_DISASTRI)) mediciSpecialistiDisastriChk.setValue(true);

                        // Gestisci "Altro"
                        if (substring1.contains(ALTRO)) {
                            mediciSpecialistiAltroChk.setValue(true);
                            // Estrai il testo dopo "Altro:" se presente
                            String marker = ALTRO + ":";
                            int startIdx = substring1.indexOf(marker);
                            if (startIdx != -1) {
                                int endIdx = substring1.indexOf(",", startIdx); // Trova la prossima virgola
                                String altroText = (endIdx == -1)
                                        ? substring1.substring(startIdx + marker.length()).trim() // Fino alla fine
                                        : substring1.substring(startIdx + marker.length(), endIdx).trim(); // Fino alla virgola
                                mediciSpecialistiAltroField.setValue(altroText);
                                mediciSpecialistiAltroField.setEnabled(true); // Abilita il campo
                            }
                        }
                    }
                    break;
                case STUDENTI_MEDICINA:
                    if (targetString.contains("(") && targetString.contains(")")) {
                        try {
                            String yearStr = substring.trim();
                            yearStr = yearStr.replace(" anno", "").trim();
                            studentiMedicinaYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Anno non valido per Studenti Medicina: '{}'", targetString);
                        }
                    }
                    break;
                case STUDENTI_INFERMIERISTICA:
                    if (targetString.contains("(") && targetString.contains(")")) {
                        try {
                            String yearStr = substring.trim();
                            yearStr = yearStr.replace(" anno", "").trim();
                            studentiInfermieristicaYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Anno non valido per Studenti Infermieristica: '{}'", targetString);
                        }
                    }
                    break;
                case INFERMIERI_SPECIALIZZATI:
                    if (targetString.contains("(") && targetString.contains(")")) {
                        if (substring1.contains(SPEC_ANESTESIA)) infSpecAnestesiaChk.setValue(true);
                        if (substring1.contains(SPEC_CURE_INTENSE)) infSpecCureIntenseChk.setValue(true);
                        if (substring1.contains(SPEC_INF_CURE_URGENTI)) infSpecCureUrgentiChk.setValue(true);
                    }
                    break;
                case STUDENTI_ODONTOIATRIA:
                    if (targetString.contains("(") && targetString.contains(")")) {
                        try {
                            String yearStr = substring.trim();
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
                    } else if (!targetString.equals(ALTRO)) {
                        // Se la stringa è diversa da "Altro" ma non ha ':' la mettiamo comunque
                        altroField.setValue(targetString);
                    }
                    break;
                // Nessun dettaglio per INFERMIERI, ODONTOIATRI, SOCCORRITORI, ASSISTENTI_DI_CURA, OPERATORE_SOCIO_SANITARIO
            }
        } catch (Exception e) {
            logger.error("Errore durante il parsing dei dettagli della stringa target '{}'. Resetting fields.", targetString, e);
            resetAllFields(); // Resetta in caso di errore grave di parsing
            targetRadioGroup.clear(); // Pulisci anche la selezione principale
        }

        // Aggiorna visibilità e stato required DOPO aver popolato i campi
        updateConditionalLayoutsVisibility(mainTarget);
        updateFieldsRequiredStatus(mainTarget);
    }

    // Resetta tutti i campi a uno stato vuoto/default
    private void resetAllFields() {
        mediciAssistentiYearRadio.clear();
        studentiMedicinaYearRadio.clear();
        studentiInfermieristicaYearRadio.clear();
        studentiOdontoiatriaYearRadio.clear();
        altroField.clear();

        // Resetta Checkbox Medici Specialisti
        Stream.of(mediciSpecialistiAnestesiaChk, mediciSpecialistiEmergenzaChk, mediciSpecialistiCureIntenseChk,
                mediciSpecialistiChirurgiaChk, mediciSpecialistiOstetriciaChk, mediciSpecialistiPediatriaChk,
                mediciSpecialistiInternaChk, mediciSpecialistiCardiologiaChk, mediciSpecialistiDisastriChk,
                mediciSpecialistiAltroChk).forEach(chk -> chk.setValue(false));
        mediciSpecialistiAltroField.clear();
        mediciSpecialistiAltroField.setEnabled(false); // Disabilita campo altro

        // Resetta Checkbox Infermieri Specializzati
        Stream.of(infSpecAnestesiaChk, infSpecCureIntenseChk, infSpecCureUrgentiChk)
                .forEach(chk -> chk.setValue(false));

        // Nascondi tutti i layout e resetta stato required
        hideAllConditionalLayouts();
        updateFieldsRequiredStatus(null);

        // Resetta stato invalidazione
        // Resetta stato invalidazione
        targetRadioGroup.setInvalid(false);

        // Gestisci i campi individualmente invece di usare un cast generico
        mediciAssistentiYearRadio.setInvalid(false);
        studentiMedicinaYearRadio.setInvalid(false);
        studentiInfermieristicaYearRadio.setInvalid(false);
        studentiOdontoiatriaYearRadio.setInvalid(false);
        altroField.setInvalid(false);
        mediciSpecialistiAltroField.setInvalid(false);
    }
}
