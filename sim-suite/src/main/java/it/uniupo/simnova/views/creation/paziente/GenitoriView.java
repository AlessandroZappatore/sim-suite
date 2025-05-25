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
 * Vista per la gestione delle informazioni per i genitori nello scenario di simulazione pediatrica.
 * <p>
 * Permette di definire il testo informativo che verrà presentato ai genitori o tutori
 * prima dell'inizio della simulazione pediatrica. Fa parte del flusso di creazione scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Informazioni per i Genitori")
@Route(value = "infoGenitori")
public class GenitoriView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Area di testo per le informazioni per i genitori.
     */
    private final TinyMce genitoriEditor;
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(GenitoriView.class);

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public GenitoriView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;


        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());


        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Informazioni per i Genitori",
                "Definisci ciò che i genitori del paziente riferiranno durante la simulazione",
                VaadinIcon.FAMILY.create(),
                "var(--lumo-primary-color)"
        );

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        genitoriEditor = TinyEditor.getEditor();

        contentLayout.add(headerSection, genitoriEditor);


        Button nextButton = StyleApp.getNextButton();

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);


        mainLayout.add(customHeader, contentLayout, footerLayout);


        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("briefing/" + scenarioId)));

        nextButton.addClickListener(e -> saveGenitoriInfoAndNavigate(nextButton.getUI()));
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


            if (!scenarioService.isPediatric(scenarioId)) {

                event.rerouteTo("pattoaula/" + scenarioId);
                return;
            }


            loadExistingGenitoriInfo();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Carica le informazioni per i genitori esistenti per lo scenario corrente.
     */
    private void loadExistingGenitoriInfo() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getInfoGenitore() != null && !scenario.getInfoGenitore().isEmpty()) {
            genitoriEditor.setValue(scenario.getInfoGenitore());
        }
    }

    /**
     * Salva le informazioni per i genitori e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveGenitoriInfoAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                boolean success = scenarioService.updateScenarioGenitoriInfo(
                        scenarioId, genitoriEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("pattoaula/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio delle informazioni per i genitori",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio delle informazioni per i genitori", e);
                });
            }
        });
    }
}
