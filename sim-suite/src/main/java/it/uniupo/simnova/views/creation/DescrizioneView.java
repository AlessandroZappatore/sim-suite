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

    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private final TextArea descriptionArea;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public DescrizioneView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER con pulsante indietro e titolo
        AppHeader header = new AppHeader();
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);

        // 2. CONTENUTO PRINCIPALE con area di testo
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("800px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        descriptionArea = new TextArea("DESCRIZIONE SCENARIO");
        descriptionArea.setPlaceholder("Inserisci una descrizione dettagliata dello scenario...");
        descriptionArea.setWidthFull();
        descriptionArea.setMinHeight("300px");
        descriptionArea.getStyle().set("max-width", "100%");
        descriptionArea.addClassName(LumoUtility.Margin.Top.LARGE);

        contentLayout.add(descriptionArea);

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

        // Aggiunta componenti al layout principale
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Gestione eventi
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("startCreation")));

        nextButton.addClickListener(e -> {
            if (descriptionArea.getValue().trim().isEmpty()) {
                Notification.show("Inserisci una descrizione per lo scenario", 3000, Notification.Position.MIDDLE);
                return;
            }
            saveDescriptionAndNavigate(nextButton.getUI());
        });
    }

    /**
     * Gestisce il parametro ricevuto dall'URL (ID scenario).
     *
     * @param event l'evento di navigazione
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

            loadExistingDescription();
        } catch (NumberFormatException e) {
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
        }
    }

    /**
     * Carica la descrizione esistente per lo scenario corrente.
     */
    private void loadExistingDescription() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty()) {
            descriptionArea.setValue(scenario.getDescrizione());
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
                boolean success = scenarioService.updateScenarioDescription(
                        scenarioId, descriptionArea.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("briefing/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio della descrizione",
                                3000, Notification.Position.MIDDLE);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE);
                    e.printStackTrace();
                });
            }
        });
    }
}