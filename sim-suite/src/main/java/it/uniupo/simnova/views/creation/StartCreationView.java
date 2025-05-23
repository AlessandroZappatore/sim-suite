package it.uniupo.simnova.views.creation;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Vista per l'inizio della creazione di uno scenario.
 * <p>
 * Permette di inserire i dettagli iniziali dello scenario come titolo, nome del paziente, patologia e durata.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.1 // Version updated to reflect changes
 */
@PageTitle("StartCreation")
@Route("startCreation")
@Menu(order = 2)
public class StartCreationView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione delle informazioni e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(StartCreationView.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per la gestione degli scenari avanzati.
     */
    private final AdvancedScenarioService advancedScenarioService;
    /**
     * Servizio per la gestione degli scenari simulati con pazienti.
     */
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    /**
     * Campi di input per il titolo dello scenario.
     */
    private final TextField scenarioTitle;
    /**
     * Campo di input per il nome del paziente.
     */
    private final TextField patientName;
    /**
     * Campo di input per la patologia.
     */
    private final TextField pathology;
    /**
     * Campo di input per la durata della simulazione.
     */
    private final ComboBox<Integer> durationField;
    /**
     * Campo di input per l'autore dello scenario.
     */
    private final TextField authorField;
    /**
     * Campo di input per il tipo di scenario.
     */
    private final Select<String> typeField;
    /**
     * Tipo di scenario selezionato.
     */
    private String scenarioType;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService                 servizio per la gestione degli scenari
     * @param fileStorageService              servizio per la gestione dei file
     * @param advancedScenarioService         servizio per scenari avanzati
     * @param patientSimulatedScenarioService servizio per scenari con paziente simulato
     */
    public StartCreationView(ScenarioService scenarioService, FileStorageService fileStorageService, AdvancedScenarioService advancedScenarioService, PatientSimulatedScenarioService patientSimulatedScenarioService) {
        this.scenarioService = scenarioService;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // 1. HEADER
        AppHeader header = new AppHeader(fileStorageService);

        // Pulsante indietro
        Button backButton = StyleApp.getBackButton();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "INIZIO CREAZIONE SCENARIO",
                "Compila i campi richiesti per iniziare la creazione del tuo scenario.",
                VaadinIcon.START_COG.create(),
                "var(--lumo-primary-color)"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // --- Campi del form con icone ---

        // Titolo Scenario
        scenarioTitle = FieldGenerator.createTextField(
                "TITOLO SCENARIO",
                "Inserisci il titolo dello scenario",
                true
        );
        Icon titleIcon = FontAwesome.Solid.TAGS.create();
        HorizontalLayout scenarioTitleLayout = createFieldWithIconLayout(titleIcon, scenarioTitle);
        scenarioTitleLayout.setWidthFull();
        scenarioTitleLayout.getStyle().set("max-width", "500px");
        scenarioTitleLayout.getStyle().set("margin", "0 auto");

        // Nome Paziente
        patientName = FieldGenerator.createTextField(
                "NOME PAZIENTE",
                "Inserisci il nome del paziente",
                true
        );
        Icon patientNameIcon = FontAwesome.Solid.USER_INJURED.create();
        HorizontalLayout patientNameLayout = createFieldWithIconLayout(patientNameIcon, patientName);
        patientNameLayout.setWidthFull();
        patientNameLayout.getStyle().set("max-width", "500px");
        patientNameLayout.getStyle().set("margin", "0 auto");

        // Patologia
        pathology = FieldGenerator.createTextField(
                "PATOLOGIA/MALATTIA",
                "Inserisci la patologia",
                true
        );
        Icon pathologyIcon = FontAwesome.Solid.DISEASE.create();
        HorizontalLayout pathologyLayout = createFieldWithIconLayout(pathologyIcon, pathology);
        pathologyLayout.setWidthFull();
        pathologyLayout.getStyle().set("max-width", "500px");
        pathologyLayout.getStyle().set("margin", "0 auto");

        // Autore
        authorField = FieldGenerator.createTextField(
                "AUTORE",
                "Inserisci il tuo nome",
                true
        );
        Icon authorIcon = FontAwesome.Solid.SIGNATURE.create();
        HorizontalLayout authorLayout = createFieldWithIconLayout(authorIcon, authorField);
        authorLayout.setWidthFull();
        authorLayout.getStyle().set("max-width", "500px");
        authorLayout.getStyle().set("margin", "0 auto");

        // Durata
        List<Integer> durations = List.of(5, 10, 15, 20, 25, 30);
        durationField = FieldGenerator.createComboBox(
                "DURATA SIMULAZIONE (minuti)",
                durations,
                10,
                true
        );
        Icon durationIcon = FontAwesome.Solid.STOPWATCH_20.create();
        HorizontalLayout durationLayout = createFieldWithIconLayout(durationIcon, durationField);
        durationLayout.setWidthFull();
        durationLayout.getStyle().set("max-width", "500px");
        durationLayout.getStyle().set("margin", "0 auto");

        // Tipo Scenario
        List<String> scenarioTypes = List.of("Adulto", "Pediatrico", "Neonatale", "Prematuro");
        typeField = FieldGenerator.createSelect(
                "TIPO SCENARIO",
                scenarioTypes,
                "Adulto",
                true
        );

        Map<String, Icon> iconMap = new HashMap<>();
        iconMap.put("Adulto", FontAwesome.Solid.USER.create());
        iconMap.put("Pediatrico", FontAwesome.Solid.CHILD.create());
        iconMap.put("Neonatale", FontAwesome.Solid.BABY.create());
        iconMap.put("Prematuro", FontAwesome.Solid.HANDS_HOLDING_CHILD.create());

        final Icon[] scenarioTypeIcon = {iconMap.get(typeField.getValue())};
        scenarioTypeIcon[0].getStyle().set("margin-right", "10px");
        scenarioTypeIcon[0].setSize("24px");

        // Icona a sinistra del ComboBox
        HorizontalLayout typeFieldWithIcon = new HorizontalLayout(scenarioTypeIcon[0], typeField);
        typeFieldWithIcon.setAlignItems(FlexComponent.Alignment.BASELINE); // Allineamento baseline
        typeFieldWithIcon.setWidthFull();
        typeFieldWithIcon.getStyle().set("max-width", "500px");
        typeFieldWithIcon.getStyle().set("margin", "0 auto");
        typeFieldWithIcon.expand(typeField); // Fa espandere il ComboBox

        typeField.addValueChangeListener(event -> {
            Icon newIcon = iconMap.getOrDefault(event.getValue(), FontAwesome.Solid.QUESTION.create());
            newIcon.getStyle().set("margin-right", "10px"); // Aggiornato a margin-right
            newIcon.setSize("24px");
            int iconIndex = typeFieldWithIcon.indexOf(scenarioTypeIcon[0]);
            if (iconIndex != -1) {
                typeFieldWithIcon.replace(scenarioTypeIcon[0], newIcon);
            } else {
                // This case should ideally not be reached if scenarioTypeIcon[0] is always in the layout
                typeFieldWithIcon.addComponentAsFirst(newIcon);
            }
            scenarioTypeIcon[0] = newIcon;
        });

        contentLayout.add(
                headerSection,
                scenarioTitleLayout,
                patientNameLayout,
                pathologyLayout,
                authorLayout,
                durationLayout,
                typeFieldWithIcon
        );

        contentLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        // 3. FOOTER con pulsanti e crediti
        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Aggiunta di tutti i componenti al layout principale
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        // Listener per i pulsanti
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("creation")));

        nextButton.addClickListener(e -> {
            if (validateFields()) {
                saveScenarioAndNavigate(nextButton.getUI());
            }
        });
    }

    /**
     * Helper method to create a HorizontalLayout with an icon and a field.
     *
     * @param icon  The icon to display.
     * @param field The field component.
     * @return A HorizontalLayout containing the icon and field.
     */
    private HorizontalLayout createFieldWithIconLayout(Icon icon, Component field) { // Changed FlexComponent to Component
        icon.setSize("24px"); // Standard size for these new icons
        icon.getStyle().set("margin-right", "8px"); // Spacing between icon and field

        HorizontalLayout layout = new HorizontalLayout(icon, field);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.setWidthFull(); // Make the layout take full width
        layout.expand(field); // Make the field expand to take available space
        return layout;
    }


    /**
     * Gestisce il parametro ricevuto dall'URL (tipo di scenario).
     *
     * @param event     l'evento di navigazione
     * @param parameter il tipo di scenario come stringa
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null) {
            this.scenarioType = parameter.toLowerCase();
        } else {
            // Default o gestione errore
            this.scenarioType = "quickscenario"; // Mantieni il default originale
        }
    }

    /**
     * Valida i campi del form.
     *
     * @return true se tutti i campi sono validi, false altrimenti
     */
    private boolean validateFields() {
        boolean isValid = true;

        if (scenarioTitle.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(scenarioTitle, "Compila il campo TITOLO SCENARIO");

        if (patientName.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(patientName, "Compila il campo NOME PAZIENTE");

        if (pathology.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(pathology, "Compila il campo PATOLOGIA/MALATTIA");

        if (authorField.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(authorField, "Compila il campo AUTORE");

        if (durationField.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(durationField, "Compila il campo DURATA SIMULAZIONE");
        else if (durationField.getValue() == null || durationField.getValue() <= 0) // Durata deve essere positiva
            isValid = ValidationError.showErrorAndReturnFalse(durationField, "Inserisci una durata valida (maggiore di 0)");

        if (typeField.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(typeField, "Compila il campo TIPO SCENARIO");
        return isValid;
    }

    /**
     * Salva lo scenario e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveScenarioAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            try {
                int scenarioId;

                switch (scenarioType) {
                    case "quickscenario":
                        scenarioId = scenarioService.startQuickScenario(
                                -1,
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                authorField.getValue(),
                                durationField.getValue().floatValue(),
                                typeField.getValue()
                        );
                        break;
                    case "advancedscenario":
                        scenarioId = advancedScenarioService.startAdvancedScenario(
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                authorField.getValue(),
                                durationField.getValue().floatValue(),
                                typeField.getValue()
                        );
                        break;
                    case "patientsimulatedscenario":
                        scenarioId = patientSimulatedScenarioService.startPatientSimulatedScenario(
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                authorField.getValue(),
                                durationField.getValue().floatValue(),
                                typeField.getValue()
                        );
                        break;
                    default:
                        Notification.show("Tipo di scenario non riconosciuto: " + scenarioType,
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                }

                logger.info("ID scenario creato: {}", scenarioId);

                if (scenarioId > 0) {
                    logger.info("Navigando a target/{}", scenarioId); // Modificato da descrizione a target
                    ui.navigate("target/" + scenarioId);
                } else {
                    Notification.show("Errore durante il salvataggio dello scenario (ID non valido)",
                            3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception e) {
                Notification.show("Errore durante il salvataggio: " + e.getMessage(),
                        5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                logger.error("Errore durante il salvataggio dello scenario", e);
            }
        });
    }
}

