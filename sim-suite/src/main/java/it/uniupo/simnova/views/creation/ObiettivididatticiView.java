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
 * Vista per la gestione degli obiettivi didattici nello scenario di simulazione.
 * <p>
 * Permette di definire gli obiettivi di apprendimento per la simulazione.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Obiettivi Didattici")
@Route(value = "obiettivididattici")
@Menu(order = 7)
public class ObiettivididatticiView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private static final Logger logger = LoggerFactory.getLogger(ObiettivididatticiView.class);

    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private final TextArea obiettiviArea;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public ObiettivididatticiView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER
        AppHeader header = new AppHeader();

        // Pulsante indietro
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        // Container per header personalizzato
        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("800px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        // Area di testo per gli obiettivi didattici
        obiettiviArea = new TextArea("OBIETTIVI DIDATTICI");
        obiettiviArea.setPlaceholder("Inserisci gli obiettivi didattici dello scenario...");
        obiettiviArea.setWidthFull();
        obiettiviArea.setMinHeight("300px");
        obiettiviArea.getStyle().set("max-width", "100%");
        obiettiviArea.addClassName(LumoUtility.Margin.Top.LARGE);

        // Istruzioni aggiuntive
        Paragraph instructions = new Paragraph("Definisci gli obiettivi di apprendimento per questa simulazione");
        instructions.addClassName(LumoUtility.TextColor.SECONDARY);
        instructions.addClassName(LumoUtility.FontSize.SMALL);
        instructions.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        contentLayout.add(instructions, obiettiviArea);

        // 3. FOOTER con pulsanti e crediti
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px");

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

        // Listener per i pulsanti
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("azionechiave/" + scenarioId)));

        nextButton.addClickListener(e -> {
            if (obiettiviArea.getValue().trim().isEmpty()) {
                Notification.show("Inserisci gli obiettivi didattici per lo scenario", 3000, Notification.Position.MIDDLE);
                return;
            }
            saveObiettiviAndNavigate(nextButton.getUI());
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
            if (scenarioId <= 0) {
                throw new NumberFormatException();
            }

            loadExistingObiettivi();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
        }
    }

    /**
     * Carica gli obiettivi didattici esistenti per lo scenario corrente.
     */
    private void loadExistingObiettivi() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getObiettivo() != null && !scenario.getObiettivo().isEmpty()) {
            obiettiviArea.setValue(scenario.getObiettivo());
        }
    }

    /**
     * Salva gli obiettivi didattici e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveObiettiviAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                boolean success = scenarioService.updateScenarioObiettiviDidattici(
                        scenarioId, obiettiviArea.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("materialenecessario/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio degli obiettivi didattici", 3000, Notification.Position.MIDDLE);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                    logger.error("Errore durante il salvataggio degli obiettivi didattici", e);
                });
            }
        });
    }
}