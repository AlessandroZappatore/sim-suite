package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.home.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Vista per la gestione delle azioni chiave nello scenario di simulazione.
 * <p>
 * Permette di definire le azioni principali che saranno valutate durante il debriefing.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Azioni Chiave")
@Route(value = "azionechiave")
@Menu(order = 6)
public class AzionechiaveView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Layout contenente le caselle di testo per le azioni chiave.
     */
    private final VerticalLayout actionFieldsContainer;
    /**
     * Lista dei campi di testo per le azioni chiave.
     */
    private final List<TextField> actionFields = new ArrayList<>();
    /**
     * Delimitatore utilizzato per separare le azioni chiave nella stringa.
     */
    private static final String DELIMITER = ";";
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(AzionechiaveView.class);

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public AzionechiaveView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // 1. HEADER con pulsante indietro e titolo
        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE con azioni chiave dinamiche
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "AZIONI CHIAVE",
                "Definisci le azioni chiave che saranno valutate durante il debriefing",
                VaadinIcon.KEY,
                "#4285F4"
        );

        // Container per le azioni chiave
        actionFieldsContainer = new VerticalLayout();
        actionFieldsContainer.setWidthFull();
        actionFieldsContainer.setPadding(false);
        actionFieldsContainer.setSpacing(true);

        // Pulsante per aggiungere nuove azioni chiave
        Button addButton = new Button("Aggiungi azione chiave", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addNewActionField());

        contentLayout.add(headerSection, actionFieldsContainer, addButton);

        // 3. FOOTER con pulsanti e crediti

        Button nextButton = StyleApp.getNextButton();

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Gestione eventi
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("pattoaula/" + scenarioId)));

        nextButton.addClickListener(e -> saveAzioniChiaveAndNavigate(nextButton.getUI()));
    }

    /**
     * Aggiunge un nuovo campo di input per le azioni chiave.
     */
    private void addNewActionField() {
        HorizontalLayout fieldLayout = new HorizontalLayout();
        fieldLayout.setWidthFull();
        fieldLayout.setSpacing(true);
        fieldLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        TextField actionField = new TextField();
        actionField.setWidthFull();
        actionField.setPlaceholder("Inserisci un'azione chiave...");
        actionFields.add(actionField);

        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        removeButton.addClickListener(e -> {
            actionFields.remove(actionField);
            actionFieldsContainer.remove(fieldLayout);
        });

        fieldLayout.add(actionField, removeButton);
        actionFieldsContainer.add(fieldLayout);
    }

    /**
     * Gestisce il parametro ricevuto dall'URL (ID scenario).
     *
     * @param event     l'evento di navigazione
     * @param parameter l'ID dello scenario come stringa
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }

            loadExistingAzioniChiave();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Carica le azioni chiave esistenti per lo scenario corrente.
     */
    private void loadExistingAzioniChiave() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getAzioneChiave() != null && !scenario.getAzioneChiave().isEmpty()) {
            String[] actions = scenario.getAzioneChiave().split(DELIMITER);

            for (String action : actions) {
                if (!action.trim().isEmpty()) {
                    HorizontalLayout fieldLayout = new HorizontalLayout();
                    fieldLayout.setWidthFull();
                    fieldLayout.setSpacing(true);
                    fieldLayout.setAlignItems(FlexComponent.Alignment.CENTER);

                    TextField actionField = new TextField();
                    actionField.setWidthFull();
                    actionField.setValue(action.trim());
                    actionFields.add(actionField);

                    Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
                    removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
                    removeButton.addClickListener(e -> {
                        actionFields.remove(actionField);
                        actionFieldsContainer.remove(fieldLayout);
                    });

                    fieldLayout.add(actionField, removeButton);
                    actionFieldsContainer.add(fieldLayout);
                }
            }
        }

        // Se non ci sono azioni, aggiungiamo un campo vuoto
        if (actionFields.isEmpty()) {
            addNewActionField();
        }
    }

    /**
     * Salva le azioni chiave e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveAzioniChiaveAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                // Concateniamo tutte le azioni chiave in una singola stringa separata da punto e virgola
                String azioniChiave = actionFields.stream()
                        .map(TextField::getValue)
                        .filter(value -> value != null && !value.trim().isEmpty())
                        .collect(Collectors.joining(DELIMITER));

                boolean success = scenarioService.updateScenarioAzioneChiave(
                        scenarioId, azioniChiave
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("obiettivididattici/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio delle azioni chiave",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio delle azioni chiave", e);
                });
            }
        });
    }
}