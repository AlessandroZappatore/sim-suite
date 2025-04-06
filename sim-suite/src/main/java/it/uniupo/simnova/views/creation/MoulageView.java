package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Menu(order = 10)
public class MoulageView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private final TextArea moulageArea;

    private static final Logger logger = LoggerFactory.getLogger(MoulageView.class);

    /**
     * Costruttore della view.
     *
     * @param scenarioService il servizio per la gestione degli scenari
     */
    public MoulageView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;

        // Configurazione del layout principale con altezza piena e senza spazi interni
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER con pulsante indietro e header dell'applicazione
        AppHeader header = new AppHeader();

        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        // Layout orizzontale per l'header personalizzato
        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);

        // 2. CONTENUTO PRINCIPALE con area di testo per il moulage
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("800px"); // Limite massimo di larghezza per migliorare la leggibilità
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto") // Centratura orizzontale
                .set("flex-grow", "1"); // Occupa tutto lo spazio verticale disponibile

        // Configurazione dell'area di testo per il moulage
        moulageArea = new TextArea("MOULAGE");
        moulageArea.setPlaceholder("Descrivi il trucco da applicare al manichino/paziente simulato...");
        moulageArea.setWidthFull();
        moulageArea.setMinHeight("300px"); // Altezza minima per facilitare la scrittura
        moulageArea.getStyle().set("max-width", "100%");
        moulageArea.addClassName(LumoUtility.Margin.Top.LARGE);

        // Istruzioni per l'utente
        Paragraph instructions = new Paragraph("Specifica i dettagli del trucco e gli effetti speciali richiesti");
        instructions.addClassName(LumoUtility.TextColor.SECONDARY);
        instructions.addClassName(LumoUtility.FontSize.SMALL);
        instructions.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        contentLayout.add(instructions, moulageArea);

        // 3. FOOTER con pulsante avanti e crediti
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px"); // Larghezza fissa per uniformità

        Paragraph credits = new Paragraph("Sviluppato e creato da Alessandro Zappatore");
        credits.addClassName(LumoUtility.TextColor.SECONDARY);
        credits.addClassName(LumoUtility.FontSize.XSMALL);
        credits.getStyle().set("margin", "0");

        footerLayout.add(credits, nextButton);

        // Aggiunta di tutti i componenti al layout principale
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        // Gestione degli eventi dei pulsanti
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("esamiReferti/" + scenarioId)));

        nextButton.addClickListener(e -> {
            // Validazione dell'input
            if (moulageArea.getValue().trim().isEmpty()) {
                Notification.show("Inserisci la descrizione del moulage per lo scenario", 3000, Notification.Position.MIDDLE);
                return;
            }
            saveMoulageAndNavigate(nextButton.getUI());
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
            moulageArea.setValue(scenario.getMoulage());
        }
    }

    /**
     * Salva il moulage e naviga alla view successiva.
     *
     * @param uiOptional l'istanza UI opzionale per l'accesso alla UI
     */
    private void saveMoulageAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            // Mostra una progress bar durante l'operazione
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                // Salvataggio del moulage tramite il service
                boolean success = scenarioService.updateScenarioMoulage(
                        scenarioId, moulageArea.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("liquidi/" + scenarioId); // Navigazione alla view successiva
                    } else {
                        Notification.show("Errore durante il salvataggio del moulage", 3000, Notification.Position.MIDDLE);
                        logger.error("Errore durante il salvataggio del moulage per lo scenario con ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                    logger.error("Errore durante il salvataggio del moulage per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}