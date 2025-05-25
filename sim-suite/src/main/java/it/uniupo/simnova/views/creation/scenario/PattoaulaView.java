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
import it.uniupo.simnova.domain.scenario.Scenario;
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
 * Vista per la gestione del patto d'aula nello scenario di simulazione.
 * <p>
 * Permette di definire il patto d'aula per la simulazione.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Patto d'Aula")
@Route(value = "pattoaula")
public class PattoaulaView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(PattoaulaView.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Area di testo per il patto d'aula.
     */
    private final TinyMce pattoAulaEditor;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public PattoaulaView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;


        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());


        AppHeader header = new AppHeader(fileStorageService);


        Button backButton = StyleApp.getBackButton();


        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "PATTO D'AULA / FAMILIARIZZAZIONE",
                "Inserisci il testo del patto d'aula che definisce le regole di interazione durante la simulazione e fornisce indicazioni per la familiarizzazione con l'ambiente virtuale.",
                VaadinIcon.HANDSHAKE.create(),
                "var(--lumo-primary-color)"
        );


        VerticalLayout contentLayout = StyleApp.getContentLayout();

        pattoAulaEditor = TinyEditor.getEditor();

        contentLayout.add(headerSection, pattoAulaEditor);


        Button nextButton = StyleApp.getNextButton();

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);


        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );


        backButton.addClickListener(e -> {
            if (scenarioId != null) {
                backButton.getUI().ifPresent(ui -> {
                    if (scenarioService.isPediatric(scenarioId)) {
                        ui.navigate("infoGenitori/" + scenarioId);
                    } else {
                        ui.navigate("briefing/" + scenarioId);
                    }
                });
            }
        });
        nextButton.addClickListener(e -> {

            String content = pattoAulaEditor.getValue();
            boolean isEmpty = content == null || content.trim().isEmpty() ||
                    content.trim().equals("<p><br></p>") || content.trim().equals("<p></p>");

            if (isEmpty) {

                StyleApp.createConfirmDialog(
                        "Descrizione vuota",
                        "Sei sicuro di voler continuare senza una descrizione?",
                        "Prosegui",
                        "Annulla",
                        () -> savePattoAulaAndNavigate(nextButton.getUI())
                );
            } else {

                savePattoAulaAndNavigate(nextButton.getUI());
            }
        });
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

            loadExistingPattoAula();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Carica il patto d'aula esistente per lo scenario corrente.
     */
    private void loadExistingPattoAula() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getPattoAula() != null && !scenario.getPattoAula().isEmpty()) {
            pattoAulaEditor.setValue(scenario.getPattoAula());
        }
    }

    /**
     * Salva il patto d'aula e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void savePattoAulaAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                boolean success = scenarioService.updateScenarioPattoAula(
                        scenarioId, pattoAulaEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("azionechiave/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio del patto d'aula", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio del patto d'aula", e);
                });
            }
        });
    }
}