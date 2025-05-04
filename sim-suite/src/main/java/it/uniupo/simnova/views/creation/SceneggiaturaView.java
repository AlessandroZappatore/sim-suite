package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.PatientSimulatedScenario;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.home.CreditsComponent;
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
@Menu(order = 15)
public class SceneggiaturaView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
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
    public SceneggiaturaView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        // Configurazione del layout principale con altezza piena e senza spazi interni
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER con pulsante indietro e header dell'applicazione
        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        // Layout orizzontale per l'header personalizzato
        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);

        // Crea la sezione dell'intestazione
        VerticalLayout headerSection = new VerticalLayout();
        headerSection.setPadding(false);
        headerSection.setSpacing(false);
        headerSection.setWidthFull();

        H2 title = new H2("SCENEGGIATURA");
        title.addClassName(LumoUtility.Margin.Bottom.NONE);
        title.getStyle().set("text-align", "center");
        title.setWidthFull();

        Paragraph subtitle = new Paragraph("Definisci la sceneggiatura dettagliata dello scenario, includendo tutte le azioni, i dialoghi e gli eventi chiave che si svolgeranno durante la simulazione. Questa sezione serve come guida per i facilitatori e supporta la coerenza dell'esperienza formativa.");
        subtitle.addClassName(LumoUtility.Margin.Top.XSMALL);
        subtitle.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        headerSection.add(title, subtitle);


        // 2. CONTENUTO PRINCIPALE con area di testo per la sceneggiatura
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("800px"); // Limite massimo di larghezza per migliorare la leggibilità
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto") // Centratura orizzontale
                .set("flex-grow", "1"); // Occupa tutto lo spazio verticale disponibile

        sceneggiaturaEditor = new TinyMce();
        sceneggiaturaEditor.setWidthFull();
        sceneggiaturaEditor.setHeight("400px");
        sceneggiaturaEditor.configure("plugins: 'link lists', " +
                "toolbar: 'undo redo | bold italic | alignleft aligncenter alignright | bullist numlist | link', " +
                "menubar: true, " +
                "statusbar: true");


        contentLayout.add(headerSection, sceneggiaturaEditor);

        // 3. FOOTER con pulsante avanti e crediti
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle().set("border-color", "var(--lumo-contrast-10pct)");

        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px"); // Larghezza fissa per uniformità

        CreditsComponent credits = new CreditsComponent();

        footerLayout.add(credits, nextButton);

        // Aggiunta di tutti i componenti al layout principale
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        // Gestione degli eventi dei pulsanti
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("tempi/" + scenarioId+"/edit")));

        nextButton.addClickListener(e -> {
            // Validazione dell'input
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
            // Validazione dell'ID scenario
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }

            // Verifica che sia uno scenario di tipo PatientSimulated
            String scenarioType = scenarioService.getScenarioType(scenarioId);
            if (!"Patient Simulated Scenario".equals(scenarioType)) {
                event.rerouteToError(NotFoundException.class, "Questa funzionalità è disponibile solo per Patient Simulated Scenario");
                return;
            }

            // Caricamento della sceneggiatura esistente se presente
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
        PatientSimulatedScenario scenario = scenarioService.getPatientSimulatedScenarioById(scenarioId);
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
            // Mostra una progress bar durante l'operazione
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                // Salvataggio della sceneggiatura tramite il service
                boolean success = scenarioService.updateScenarioSceneggiatura(
                        scenarioId, sceneggiaturaEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        logger.info("Sceneggiatura salvata con successo per lo scenario con ID: {}", scenarioId);
                        ui.navigate("scenari/" + scenarioId); // Navigazione alla view successiva
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