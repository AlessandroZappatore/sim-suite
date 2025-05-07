package it.uniupo.simnova.views.creation;

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
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.home.StyleApp;
import it.uniupo.simnova.views.home.TinyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import java.util.Optional;

/**
 * Vista per la gestione del briefing dello scenario di simulazione.
 * <p>
 * Permette di definire il testo introduttivo che verrà presentato ai discenti
 * prima dell'inizio della simulazione. Fa parte del flusso di creazione scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Briefing")
@Route(value = "briefing")
@Menu(order = 4)
public class BriefingView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Area di testo per il briefing.
     */
    private final TinyMce briefingEditor;
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(BriefingView.class);

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public BriefingView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // 1. HEADER con pulsante indietro e titolo
        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "BRIEFING",
                "Definisci il briefing che verrà mostrato ai discenti prima della simulazione",
                VaadinIcon.INFO_CIRCLE,
                "#4285F4"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        // 2. CONTENUTO PRINCIPALE con area di testo
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        briefingEditor = TinyEditor.getEditor();
        contentLayout.add(headerSection, briefingEditor);

        // 3. FOOTER con pulsanti e crediti
        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Aggiunta componenti al layout principale
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Gestione eventi
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("descrizione/" + scenarioId)));

        nextButton.addClickListener(e -> saveBriefingAndNavigate(nextButton.getUI()));
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

            loadExistingBriefing();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Carica il briefing esistente per lo scenario corrente.
     */
    private void loadExistingBriefing() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getBriefing() != null && !scenario.getBriefing().isEmpty()) {
            briefingEditor.setValue(scenario.getBriefing());
        }
    }

    /**
     * Salva il briefing e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveBriefingAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                boolean success = scenarioService.updateScenarioBriefing(
                        scenarioId, briefingEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        if (scenarioService.isPediatric(scenarioId))
                            ui.navigate("infoGenitori/" + scenarioId);
                        else
                            ui.navigate("pattoaula/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio del briefing",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio del briefing", e);
                });
            }
        });
    }
}