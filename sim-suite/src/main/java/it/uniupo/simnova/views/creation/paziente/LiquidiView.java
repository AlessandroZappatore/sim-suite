package it.uniupo.simnova.views.creation.paziente;

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
 * Vista per la gestione dei liquidi e presidi nello scenario di simulazione.
 * <p>
 * Permette di inserire e modificare la lista di liquidi e presidi disponibili
 * all'inizio della simulazione (T0). Fa parte del flusso di creazione scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Liquidi e dosi farmaci")
@Route(value = "liquidi")
public class LiquidiView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(LiquidiView.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Area di testo per l'inserimento dei liquidi e presidi.
     */
    private final TinyMce liquidiEditor;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public LiquidiView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;


        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());


        AppHeader header = new AppHeader(fileStorageService);


        Button backButton = StyleApp.getBackButton();


        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Liquidi e dosi farmaci",
                "Inserisci i liquidi e dosi farmaci disponibili all'inizio della simulazione (T0)",
                VaadinIcon.DROP.create(),
                "var(--lumo-primary-color)"
        );


        VerticalLayout contentLayout = StyleApp.getContentLayout();

        liquidiEditor = TinyEditor.getEditor();

        contentLayout.add(headerSection, liquidiEditor);


        Button nextButton = StyleApp.getNextButton();

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);


        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );


        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("moulage/" + scenarioId)));

        nextButton.addClickListener(e -> {

            String content = liquidiEditor.getValue();
            boolean isEmpty = content == null || content.trim().isEmpty() ||
                    content.trim().equals("<p><br></p>") || content.trim().equals("<p></p>");

            if (isEmpty) {

                StyleApp.createConfirmDialog(
                        "Descrizione vuota",
                        "Sei sicuro di voler continuare senza una descrizione?",
                        "Prosegui",
                        "Annulla",
                        () -> saveLiquidiAndNavigate(nextButton.getUI())
                );
            } else {

                saveLiquidiAndNavigate(nextButton.getUI());
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

            loadExistingLiquidi();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Carica i liquidi e presidi esistenti per lo scenario.
     */
    private void loadExistingLiquidi() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getLiquidi() != null && !scenario.getLiquidi().isEmpty()) {
            liquidiEditor.setValue(scenario.getLiquidi());
        }
    }

    /**
     * Salva i liquidi e presidi e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveLiquidiAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                boolean success = scenarioService.updateScenarioLiquidi(
                        scenarioId, liquidiEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("pazienteT0/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio di liquidi e presidi", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.error("Errore durante il salvataggio di liquidi e presidi per lo scenario con ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio di liquidi e presidi per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}