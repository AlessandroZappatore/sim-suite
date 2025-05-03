package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.home.CreditsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public StartCreationView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER
        AppHeader header = new AppHeader(fileStorageService);

        // Pulsante indietro
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        // Container per header personalizzato
        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("800px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        // Campi del form
        scenarioTitle = createTextField("TITOLO SCENARIO", "Inserisci il titolo dello scenario");
        scenarioTitle.setRequired(true);
        patientName = createTextField("NOME PAZIENTE", "Inserisci il nome del paziente");
        patientName.setRequired(true);
        pathology = createTextField("PATOLOGIA/MALATTIA", "Inserisci la patologia");
        pathology.setRequired(true);
        authorField = createTextField("AUTORE", "Inserisci il tuo nome");
        authorField.setRequired(true);
        // Campo durata timer
        durationField = new ComboBox<>("DURATA SIMULAZIONE (minuti)");
        durationField.setItems(5, 10, 15, 20, 25, 30);
        durationField.setValue(10);
        durationField.setWidthFull();
        durationField.addClassName(LumoUtility.Margin.Top.LARGE);
        durationField.getStyle().set("max-width", "500px");
        durationField.setRequired(true);

        typeField = new ComboBox<>("TIPO SCENARIO");
        typeField.setItems("Adulto", "Pediatrico", "Neonatale", "Prematuro");
        typeField.setValue("Adulto");
        typeField.setWidthFull();
        typeField.addClassName(LumoUtility.Margin.Top.LARGE);
        typeField.getStyle().set("max-width", "500px");
        typeField.setRequired(true);

        contentLayout.add(
                scenarioTitle,
                patientName,
                pathology,
                authorField,
                durationField,
                typeField
        );

        // 3. FOOTER con pulsanti e crediti
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle().set("border-color", "var(--lumo-contrast-10pct)");

        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px");

        CreditsComponent credits = new CreditsComponent();

        footerLayout.add(credits, nextButton);

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
        if (scenarioTitle.isEmpty() || patientName.isEmpty() || pathology.isEmpty() || durationField.isEmpty() || authorField.isEmpty()) {
            Notification.show("Compila tutti i campi obbligatori", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return false;
        }
        return true;
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
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                authorField.getValue(),
                                durationField.getValue().floatValue(),
                                typeField.getValue()
                        );
                        break;
                    case "advancedscenario":
                        scenarioId = scenarioService.startAdvancedScenario(
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                authorField.getValue(),
                                durationField.getValue().floatValue(),
                                typeField.getValue()
                        );
                        break;
                    case "patientsimulatedscenario":
                        scenarioId = scenarioService.startPatientSimulatedScenario(
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

    /**
     * Crea un campo di testo con etichetta e placeholder.
     *
     * @param label       l'etichetta del campo
     * @param placeholder il placeholder del campo
     * @return il campo di testo creato
     */
    private TextField createTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.getStyle().set("max-width", "500px");
        field.addClassName(LumoUtility.Margin.Top.LARGE);
        return field;
    }
}