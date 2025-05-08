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
import it.uniupo.simnova.views.support.AppHeader;
import it.uniupo.simnova.views.support.StyleApp;
import it.uniupo.simnova.views.support.TinyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import java.util.Optional;

/**
 * Vista per la gestione della descrizione dello scenario di simulazione.
 * <p>
 * Permette di inserire e modificare la descrizione testuale dello scenario
 * durante il processo di creazione. Fa parte del flusso di creazione scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Descrizione")
@Route(value = "descrizione")
@Menu(order = 3)
public class DescrizioneView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(DescrizioneView.class);
    /**
     * Servizio per la gestione degli scenari di simulazione.
     */
    private final ScenarioService scenarioService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Area di testo per l'inserimento della descrizione dello scenario.
     */
    private final TinyMce descriptionEditor;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public DescrizioneView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // 1. HEADER con pulsante indietro e titolo
        AppHeader header = new AppHeader(fileStorageService);

        // Modernizzare il pulsante indietro
        Button backButton = StyleApp.getBackButton();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "DESCRIZIONE DELLO SCENARIO",
                "Inserisci una descrizione dettagliata dello scenario di simulazione. Questa descrizione fornirà il contesto generale e le informazioni di background ai partecipanti.",
                VaadinIcon.PENCIL,
                "var(--lumo-primary-color)"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE con area di testo
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        descriptionEditor = TinyEditor.getEditor();

        contentLayout.add(headerSection, descriptionEditor);

        // 3. FOOTER con pulsanti e crediti
        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Aggiunta componenti al layout principale
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Gestione eventi
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("target/" + scenarioId)));

        nextButton.addClickListener(e -> saveDescriptionAndNavigate(nextButton.getUI()));
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

            loadExistingDescription();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Carica la descrizione esistente per lo scenario corrente.
     */
    private void loadExistingDescription() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty()) {
            descriptionEditor.setValue(scenario.getDescrizione());
        }
    }

    /**
     * Salva la descrizione e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveDescriptionAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                // Salva la descrizione, anche se vuota
                boolean success = scenarioService.updateScenarioDescription(
                        scenarioId, descriptionEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("briefing/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio della descrizione",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio della descrizione", e);
                });
            }
        });
    }
}