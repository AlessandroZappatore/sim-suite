package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

import java.util.List;
import java.util.Optional;

/**
 * Vista per l'inizio della creazione di uno scenario.
 * <p>
 * Permette di inserire i dettagli iniziali dello scenario come titolo, nome del paziente, patologia e durata.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
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

    private final AdvancedScenarioService advancedScenarioService;

    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    /**
     * Campi di input per il titolo dello scenario, nome del paziente, patologia e durata.
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
     * Tipo di scenario selezionato.
     */
    private String scenarioType;

    private final TextField authorField;

    private final ComboBox<String> typeField;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
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

        // Campi del form
        scenarioTitle = FieldGenerator.createTextField(
                "TITOLO SCENARIO",
                "Inserisci il titolo dello scenario",
                true
        );
        patientName = FieldGenerator.createTextField(
                "NOME PAZIENTE",
                "Inserisci il nome del paziente",
                true
        );
        pathology = FieldGenerator.createTextField(
                "PATOLOGIA/MALATTIA",
                "Inserisci la patologia",
                true
        );
        authorField = FieldGenerator.createTextField(
                "AUTORE",
                "Inserisci il tuo nome",
                true
        );

        // Campo durata timer
        List<Integer> durations = List.of(5, 10, 15, 20, 25, 30);
        durationField = FieldGenerator.createComboBox(
                "DURATA SIMULAZIONE (minuti)",
                durations,
                10,
                true
        );

        // Campo tipo scenario
        List<String> scenarioTypes = List.of("Adulto", "Pediatrico", "Neonatale", "Prematuro");
        typeField = FieldGenerator.createComboBox(
                "TIPO SCENARIO",
                scenarioTypes,
                "Adulto",
                true
        );

        contentLayout.add(
                headerSection,
                scenarioTitle,
                patientName,
                pathology,
                authorField,
                durationField,
                typeField
        );

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
            this.scenarioType = "quickScenario";
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
        else if (durationField.getValue() == null || durationField.getValue() < 0)
            isValid = ValidationError.showErrorAndReturnFalse(durationField, "Inserisci una durata valida");

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
                        Notification.show("Tipo di scenario non riconosciuto",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                }

                logger.info("ID scenario creato: {}", scenarioId);

                if (scenarioId > 0) {
                    logger.info("Navigando a descrizione/{}", scenarioId);
                    ui.navigate("target/" + scenarioId);
                } else {
                    Notification.show("Errore durante il salvataggio dello scenario",
                            3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception e) {
                Notification.show("Errore: " + e.getMessage(),
                        5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                logger.error("Errore durante il salvataggio dello scenario", e);
            }
        });
    }
}