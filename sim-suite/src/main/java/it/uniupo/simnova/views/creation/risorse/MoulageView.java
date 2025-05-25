package it.uniupo.simnova.views.creation.risorse;

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
 * View per la gestione del moulage (trucco ed effetti speciali) nello scenario di simulazione.
 *
 * <p>Questa view permette all'utente di inserire o modificare la descrizione del trucco
 * da applicare al manichino/paziente simulato per lo scenario corrente.</p>
 *
 * <p>Implementa {@link HasUrlParameter} per ricevere l'ID dello scenario come parametro nell'URL.</p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Moulage")
@Route(value = "moulage")
public class MoulageView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(MoulageView.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Area di testo per la descrizione del moulage.
     */
    private final TinyMce moulageEditor;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;

    /**
     * Costruttore della view.
     *
     * @param scenarioService il servizio per la gestione degli scenari
     */
    public MoulageView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;


        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());


        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();


        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Moulage",
                "Inserisci la descrizione del trucco da applicare al manichino/paziente simulato",
                VaadinIcon.EYE.create(),
                "var(--lumo-primary-color)"
        );


        VerticalLayout contentLayout = StyleApp.getContentLayout();

        moulageEditor = TinyEditor.getEditor();

        contentLayout.add(headerSection, moulageEditor);


        Button nextButton = StyleApp.getNextButton();

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);


        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );


        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("esamiReferti/" + scenarioId)));

        nextButton.addClickListener(e -> {

            String content = moulageEditor.getValue();
            boolean isEmpty = content == null || content.trim().isEmpty() ||
                    content.trim().equals("<p><br></p>") || content.trim().equals("<p></p>");

            if (isEmpty) {

                StyleApp.createConfirmDialog(
                        "Descrizione vuota",
                        "Sei sicuro di voler continuare senza una descrizione?",
                        "Prosegui",
                        "Annulla",
                        () -> saveMoulageAndNavigate(nextButton.getUI())
                );
            } else {

                saveMoulageAndNavigate(nextButton.getUI());
            }
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

            loadExistingMoulage();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Carica il moulage esistente per lo scenario corrente.
     */
    private void loadExistingMoulage() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getMoulage() != null && !scenario.getMoulage().isEmpty()) {
            moulageEditor.setValue(scenario.getMoulage());
        }
    }

    /**
     * Salva il moulage e naviga alla view successiva.
     *
     * @param uiOptional l'istanza UI opzionale per l'accesso alla UI
     */
    private void saveMoulageAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {

            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {

                boolean success = scenarioService.updateScenarioMoulage(
                        scenarioId, moulageEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("liquidi/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio del moulage", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.error("Errore durante il salvataggio del moulage per lo scenario con ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio del moulage per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}