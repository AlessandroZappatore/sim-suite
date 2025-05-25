package it.uniupo.simnova.views.creation.scenario;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.*;
import it.uniupo.simnova.domain.scenario.PatientSimulatedScenario;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import java.util.Optional;

/**
 * View per la gestione della sceneggiatura dello scenario di simulazione.
 *
 * <p>Questa view permette all'utente di inserire o modificare la sceneggiatura
 * dettagliata dello scenario corrente, includendo azioni, dialoghi ed eventi chiave.</p>
 *
 * <p>Implementa {@link HasUrlParameter} per ricevere l'ID dello scenario come parametro nell'URL.</p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Sceneggiatura")
@Route(value = "sceneggiatura")
public class SceneggiaturaView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Area di testo per l'inserimento della sceneggiatura.
     */
    private final TinyMce sceneggiaturaEditor;
    /**
     * Logger per la registrazione delle operazioni.
     */
    private static final Logger logger = LoggerFactory.getLogger(SceneggiaturaView.class);

    /**
     * Costruttore della view.
     *
     * @param scenarioService il servizio per la gestione degli scenari
     */
    public SceneggiaturaView(ScenarioService scenarioService, FileStorageService fileStorageService, PatientSimulatedScenarioService patientSimulatedScenarioService) {
        this.scenarioService = scenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;


        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());


        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();


        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Sceneggiatura",
                "Inserisci la sceneggiatura dettagliata dello scenario corrente, includendo azioni, dialoghi ed eventi chiave",
                VaadinIcon.FILE_TEXT.create(),
                "var(--lumo-primary-color)"
        );



        VerticalLayout contentLayout = StyleApp.getContentLayout();

        sceneggiaturaEditor = TinyEditor.getEditor();

        contentLayout.add(headerSection, sceneggiaturaEditor);


        Button nextButton = StyleApp.getNextButton();

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);


        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );


        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("tempi/" + scenarioId + "/edit")));

        nextButton.addClickListener(e -> {

            if (sceneggiaturaEditor.getValue().trim().isEmpty()) {
                Notification.show("Inserisci la sceneggiatura per lo scenario", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }
            saveSceneggiaturaAndNavigate(nextButton.getUI());
        });
    }

    /**
     * Gestisce il parametro ID scenario ricevuto dall'URL.
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


            String scenarioType = scenarioService.getScenarioType(scenarioId);
            if (!"Patient Simulated Scenario".equals(scenarioType)) {
                event.rerouteToError(NotFoundException.class, "Questa funzionalità è disponibile solo per Patient Simulated Scenario");
                return;
            }


            loadExistingSceneggiatura();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
        }
    }

    /**
     * Carica la sceneggiatura esistente per lo scenario corrente.
     */
    private void loadExistingSceneggiatura() {
        PatientSimulatedScenario scenario = patientSimulatedScenarioService.getPatientSimulatedScenarioById(scenarioId);
        if (scenario != null && scenario.getSceneggiatura() != null && !scenario.getSceneggiatura().isEmpty()) {
            sceneggiaturaEditor.setValue(scenario.getSceneggiatura());
        }
    }

    /**
     * Salva la sceneggiatura e naviga alla view successiva.
     *
     * @param uiOptional l'istanza UI opzionale per l'accesso alla UI
     */
    private void saveSceneggiaturaAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {

            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {

                boolean success = patientSimulatedScenarioService.updateScenarioSceneggiatura(
                        scenarioId, sceneggiaturaEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        logger.info("Sceneggiatura salvata con successo per lo scenario con ID: {}", scenarioId);
                        ui.navigate("scenari/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio della sceneggiatura", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.error("Errore durante il salvataggio della sceneggiatura per lo scenario con ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio della sceneggiatura per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}