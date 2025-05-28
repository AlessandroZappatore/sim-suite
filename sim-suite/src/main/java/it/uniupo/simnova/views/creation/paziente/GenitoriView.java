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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

/**
 * Vista per la gestione delle ***informazioni per i genitori** nello scenario di simulazione pediatrica.
 * Permette di definire il testo informativo che verrà presentato ai genitori o tutori
 * prima dell'inizio della simulazione. Questa vista è parte del flusso di creazione dello scenario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Informazioni per i Genitori")
@Route(value = "infoGenitori")
public class GenitoriView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private static final Logger logger = LoggerFactory.getLogger(GenitoriView.class);

    private final ScenarioService scenarioService;
    private final TinyMce genitoriEditor; // Editor WYSIWYG per il testo delle informazioni
    private Integer scenarioId; // ID dello scenario corrente

    /**
     * Costruttore della vista {@code GenitoriView}.
     * Inizializza i servizi e configura la struttura base dell'interfaccia utente.
     *
     * @param scenarioService    Il servizio per la gestione degli scenari.
     * @param fileStorageService Il servizio per la gestione dei file, utilizzato per l'AppHeader.
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
                VaadinIcon.FAMILY.create(), // Icona famiglia
                "var(--lumo-primary-color)"
        );

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        genitoriEditor = TinyEditor.getEditor(); // Crea l'editor TinyMCE

        contentLayout.add(headerSection, genitoriEditor);

        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per la navigazione indietro (alla vista del briefing)
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("briefing/" + scenarioId)));

        // Listener per il salvataggio e la navigazione successiva
        nextButton.addClickListener(e -> saveGenitoriInfoAndNavigate(nextButton.getUI()));
    }

    /**
     * Gestisce il parametro dell'URL (ID dello scenario).
     * Verifica la validità dell'ID, la sua esistenza e se lo scenario è di tipo pediatrico.
     * Se lo scenario non è pediatrico, reindirizza direttamente alla vista successiva (`pattoaula`).
     *
     * @param event     L'evento di navigazione.
     * @param parameter L'ID dello scenario come stringa.
     * @throws NotFoundException Se l'ID dello scenario non è valido o lo scenario non esiste.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            this.scenarioId = Integer.parseInt(parameter);
            // Verifica che lo scenario esista nel servizio
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }

            // Se lo scenario non è pediatrico, salta questa vista e naviga direttamente alla successiva
            if (!scenarioService.isPediatric(scenarioId)) {
                event.rerouteTo("pattoaula/" + scenarioId); // Reindirizza
                return;
            }

            loadExistingGenitoriInfo(); // Carica le informazioni esistenti se lo scenario è pediatrico
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            // Reindirizza a una pagina di errore 404
            event.rerouteToError(NotFoundException.class, "ID scenario " + parameter + " non valido.");
        }
    }

    /**
     * Carica le informazioni per i genitori esistenti per lo scenario corrente.
     * Popola l'editor TinyMCE con il testo recuperato dal database.
     */
    private void loadExistingGenitoriInfo() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getInfoGenitore() != null && !scenario.getInfoGenitore().isEmpty()) {
            genitoriEditor.setValue(scenario.getInfoGenitore());
        }
    }

    /**
     * Salva il testo delle informazioni per i genitori nell'editor e naviga alla vista successiva.
     * Mostra notifiche di stato e di errore.
     *
     * @param uiOptional L'istanza opzionale dell'UI corrente per la navigazione.
     */
    private void saveGenitoriInfoAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar); // Mostra una progress bar durante il salvataggio

            try {
                boolean success = scenarioService.updateScenarioGenitoriInfo(
                        scenarioId, genitoriEditor.getValue() // Salva il contenuto dell'editor
                );

                // Aggiorna l'UI dopo il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar); // Rimuove la progress bar
                    if (success) {
                        ui.navigate("pattoaula/" + scenarioId); // Naviga alla vista successiva (Patto Aula)
                    } else {
                        Notification.show("Errore durante il salvataggio delle informazioni per i genitori",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.error("Errore durante il salvataggio delle informazioni per i genitori per lo scenario con ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                // Gestisce errori durante il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio delle informazioni per i genitori per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}