package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
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
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.creation.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
        AppHeader header = new AppHeader();

        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        Button newScenarioButton = new Button("Nuovo Scenario", new Icon(VaadinIcon.PLUS));
        newScenarioButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> loadData());

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header, newScenarioButton, refreshButton);

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

        scenariosGrid.addColumn(Scenario::getDescrizione)
                .setHeader("Descrizione")
                .setAutoWidth(true);

        // Click listener
        scenariosGrid.addItemClickListener(event -> {
            if (event.getItem() != null) {
                getUI().ifPresent(ui -> {
                    ui.navigate("scenario-details/" + event.getItem().getId());
                });
            }
        });
    }

    private void loadData() {
        progressBar.setVisible(true);
        scenariosGrid.setVisible(false);

        new Thread(() -> {
            List<Scenario> scenarios = scenarioService.getAllScenarios();

            getUI().ifPresent(ui -> ui.access(() -> {
                progressBar.setVisible(false);
                scenariosGrid.setVisible(true);

                if (scenarios.isEmpty()) {
                    Notification.show("Nessuno scenario trovato", 3000, Position.MIDDLE);
                }

                this.dataProvider = new ListDataProvider<>(scenarios);
                scenariosGrid.setDataProvider(dataProvider);
            }));
        }).start();
    }
}