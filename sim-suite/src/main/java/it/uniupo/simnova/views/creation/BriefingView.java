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

@PageTitle("Briefing")
@Route(value = "briefing")
@Menu(order = 4)
public class BriefingView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private TextArea briefingArea;

    public BriefingView(ScenarioService scenarioService) {
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

        // Area di testo per il briefing
        briefingArea = new TextArea("BRIEFING");
        briefingArea.setPlaceholder("Inserisci il testo da leggere ai discenti prima della simulazione...");
        briefingArea.setWidthFull();
        briefingArea.setMinHeight("300px");
        briefingArea.getStyle().set("max-width", "100%");
        briefingArea.addClassName(LumoUtility.Margin.Top.LARGE);

        contentLayout.add(briefingArea);

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
                backButton.getUI().ifPresent(ui -> ui.navigate("descrizione/" + scenarioId)));

        nextButton.addClickListener(e -> {
            if (briefingArea.getValue().trim().isEmpty()) {
                Notification.show("Inserisci un briefing per lo scenario", 3000, Notification.Position.MIDDLE);
                return;
            }
            saveBriefingAndNavigate(nextButton.getUI());
        });
    }

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

            loadExistingBriefing();
        } catch (NumberFormatException e) {
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
        }
    }

    private void loadExistingBriefing() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getBriefing() != null && !scenario.getBriefing().isEmpty()) {
            briefingArea.setValue(scenario.getBriefing());
        }
    }

    private void saveBriefingAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                boolean success = scenarioService.updateScenarioBriefing(
                        scenarioId, briefingArea.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("pattoaula/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio del briefing", 3000, Notification.Position.MIDDLE);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                    e.printStackTrace();
                });
            }
        });
    }
}