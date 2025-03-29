package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@PageTitle("Lista Scenari")
@Route(value = "scenarios", layout = MainLayout.class)
@Menu(order = 3)
public class ScenariosListView extends Composite<VerticalLayout> {

    private final ScenarioService scenarioService;
    private final Grid<Scenario> scenariosGrid = new Grid<>();
    private ListDataProvider<Scenario> dataProvider;
    private ProgressBar progressBar;

    @Autowired
    public ScenariosListView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
        initView();
        loadData();
    }

    private void initView() {
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER
        Button backButton = new Button("Home", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        Button newScenarioButton = new Button("Nuovo Scenario", new Icon(VaadinIcon.PLUS));
        newScenarioButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> refreshButton.getUI().ifPresent(ui -> ui.navigate("scenarios")));

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, newScenarioButton, refreshButton);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        // Barra di ricerca
        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca scenario...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidth("50%");
        searchField.getStyle().set("max-width", "500px");

        // Configurazione griglia
        configureGrid();

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        contentLayout.add(searchField, progressBar, scenariosGrid);

        // 3. FOOTER
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Paragraph credits = new Paragraph("Sviluppato e creato da Alessandro Zappatore");
        credits.addClassName(LumoUtility.TextColor.SECONDARY);
        credits.addClassName(LumoUtility.FontSize.XSMALL);
        credits.getStyle().set("margin", "0");

        footerLayout.add(credits);

        // Assemblaggio finale
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Event listeners
        backButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("")));

        newScenarioButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("creation")));

        // Filtro ricerca
        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue().toLowerCase();
            dataProvider.addFilter(scenario ->
                    scenario.getTitolo().toLowerCase().contains(searchTerm) ||
                            scenario.getPatologia().toLowerCase().contains(searchTerm) ||
                            scenario.getDescrizione().toLowerCase().contains(searchTerm)
            );
        });
    }

    private void configureGrid() {
        scenariosGrid.setWidthFull();
        scenariosGrid.getStyle().set("min-height", "500px");

        // Colonne
        scenariosGrid.addColumn(Scenario::getTitolo)
                .setHeader("Titolo")
                .setSortable(true)
                .setAutoWidth(true);

        scenariosGrid.addColumn(Scenario::getNomePaziente)
                .setHeader("Paziente")
                .setSortable(true)
                .setAutoWidth(true);

        scenariosGrid.addColumn(Scenario::getPatologia)
                .setHeader("Patologia")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonna descrizione con limite di caratteri
        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
            String descrizione = scenario.getDescrizione();
            int maxLength = 50;
            if (descrizione != null && descrizione.length() > maxLength) {
                descrizione = descrizione.substring(0, maxLength) + "...";
            }
            return new Span(descrizione);
        })).setHeader("Descrizione").setAutoWidth(true);

        // Aggiungi colonna azioni
        scenariosGrid.addComponentColumn(scenario -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            // Pulsante Esporta PDF
            Button pdfButton = new Button(new Icon(VaadinIcon.FILE_TEXT));
            pdfButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            pdfButton.getElement().setAttribute("title", "Esporta in PDF");
            pdfButton.addClickListener(e -> exportToPdf(scenario));

            // Pulsante Esporta sim.execution
            Button simButton = new Button(new Icon(VaadinIcon.DOWNLOAD_ALT));
            simButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            simButton.getElement().setAttribute("title", "Esporta in sim.execution");
            simButton.addClickListener(e -> exportToSimExecution(scenario));

            // Pulsante Elimina
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Elimina scenario");
            deleteButton.addClickListener(e -> confirmAndDeleteScenario(scenario));

            actions.add(pdfButton, simButton, deleteButton);
            return actions;
        }).setHeader("Azioni").setAutoWidth(true);

        // Click listener per la riga
        scenariosGrid.addItemClickListener(event -> {
            if (event.getItem() != null) {
                getUI().ifPresent(ui -> ui.navigate("scenario-details/" + event.getItem().getId()));
            }
        });
    }

    private void exportToPdf(Scenario scenario) {
        // TODO: Implementare l'esportazione in PDF
        Notification.show("Esportazione in PDF per " + scenario.getTitolo() + " non ancora implementata",
                3000, Position.MIDDLE);
    }

    private void exportToSimExecution(Scenario scenario) {
        // TODO: Implementare l'esportazione in sim.execution
        Notification.show("Esportazione in sim.execution per " + scenario.getTitolo() + " non ancora implementata",
                3000, Position.MIDDLE);
    }

    private void confirmAndDeleteScenario(Scenario scenario) {
        // Mostra una finestra di conferma prima di eliminare
        Button confirmButton = new Button("Elimina", e -> {
            boolean deleted = scenarioService.deleteScenario(scenario.getId());
            if (deleted) {
                Notification.show("Scenario eliminato con successo", 3000, Position.MIDDLE);
                loadData(); // Ricarica i dati
            } else {
                Notification.show("Errore durante l'eliminazione dello scenario", 3000, Position.MIDDLE);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Annulla");

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        Notification notification = new Notification();
        notification.add(new Paragraph("Sei sicuro di voler eliminare lo scenario \"" + scenario.getTitolo() + "\"?"));
        notification.add(buttons);
        notification.setDuration(0); // Non si chiude automaticamente
        notification.setPosition(Position.MIDDLE);
        notification.open();

        cancelButton.addClickListener(e -> notification.close());
        confirmButton.addClickListener(e -> notification.close());
    }

    private void loadData() {
        progressBar.setVisible(true);
        scenariosGrid.setVisible(false);

        new Thread(() -> {
            try {
                List<Scenario> scenarios = scenarioService.getAllScenarios();

                getUI().ifPresent(ui -> ui.access(() -> {
                    try {
                        if (scenarios != null) {
                            this.dataProvider = new ListDataProvider<>(scenarios);
                            scenariosGrid.setDataProvider(dataProvider);
                        } else {
                            Notification.show("Errore durante il caricamento degli scenari",
                                    3000, Position.MIDDLE);
                        }
                    } catch (Exception e) {
                        Notification.show("Errore durante l'aggiornamento della griglia: " + e.getMessage(),
                                3000, Position.MIDDLE);
                    } finally {
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true);
                    }
                }));
            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Errore durante il recupero degli scenari: " + e.getMessage(),
                            3000, Position.MIDDLE);
                    progressBar.setVisible(false);
                    scenariosGrid.setVisible(true);
                }));
            }
        }).start();
    }
}