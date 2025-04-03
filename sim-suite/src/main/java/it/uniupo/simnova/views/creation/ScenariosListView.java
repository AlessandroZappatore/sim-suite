package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
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
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.PdfExportService;
import it.uniupo.simnova.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@PageTitle("Lista Scenari")
@Route(value = "scenari", layout = MainLayout.class)
@Menu(order = 3)
public class ScenariosListView extends Composite<VerticalLayout> {

    private final ScenarioService scenarioService;
    private final PdfExportService pdfExportService;
    private final Grid<Scenario> scenariosGrid = new Grid<>();
    private ListDataProvider<Scenario> dataProvider;
    private ProgressBar progressBar;

    @Autowired
    public ScenariosListView(ScenarioService scenarioService, PdfExportService pdfExportService) {
        this.scenarioService = scenarioService;
        this.pdfExportService = pdfExportService;
        initView();
        loadData();
    }

    private void initView() {
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // Header
        Button backButton = new Button("Home", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        Button newScenarioButton = new Button("Nuovo Scenario", new Icon(VaadinIcon.PLUS));
        newScenarioButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> refreshButton.getUI().ifPresent(ui -> ui.refreshCurrentRoute(true)));

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, newScenarioButton, refreshButton);

        // Main Content
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1300px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        // Search Field
        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca scenario...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidth("50%");
        searchField.getStyle().set("max-width", "500px");

        configureGrid();

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        contentLayout.add(searchField, progressBar, scenariosGrid);

        // Footer
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

        // Final Assembly
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Event Listeners
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        newScenarioButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("creation")));

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

        // Colonna Titolo
        scenariosGrid.addColumn(Scenario::getTitolo)
                .setHeader("Titolo")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonna Tipo Scenario (utilizzando getScenarioType)
        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String tipo = scenarioService.getScenarioType(scenario.getId());
                    Span tipoSpan = new Span(tipo);

                    // Assegna colori diversi in base al tipo
                    switch (tipo.toLowerCase()) {
                        case "paziente simulato":
                            tipoSpan.addClassNames(
                                    LumoUtility.TextColor.ERROR,
                                    LumoUtility.FontWeight.BOLD
                            );
                            break;
                        case "avanzato":
                            tipoSpan.addClassNames(
                                    LumoUtility.TextColor.SUCCESS,
                                    LumoUtility.FontWeight.SEMIBOLD
                            );
                            break;
                        case "base":
                            tipoSpan.addClassNames(
                                    LumoUtility.TextColor.PRIMARY,
                                    LumoUtility.FontWeight.MEDIUM
                            );
                            break;
                        default:
                            tipoSpan.addClassName(LumoUtility.TextColor.SECONDARY);
                    }

                    return tipoSpan;
                })).setHeader("Tipo")
                .setSortable(true)
                .setAutoWidth(true)
                .setComparator((s1, s2) -> {
                    // Ordinamento per tipo
                    return scenarioService.getScenarioType(s1.getId())
                            .compareTo(scenarioService.getScenarioType(s2.getId()));
                });

        // Colonna Paziente
        scenariosGrid.addColumn(Scenario::getNomePaziente)
                .setHeader("Paziente")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonna Patologia
        scenariosGrid.addColumn(Scenario::getPatologia)
                .setHeader("Patologia")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonna Descrizione (con testo troncato)
        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String descrizione = scenario.getDescrizione();
                    if (descrizione == null) {
                        descrizione = "";
                    }
                    int maxLength = 40;
                    if (descrizione.length() > maxLength) {
                        descrizione = descrizione.substring(0, maxLength) + "...";
                    }
                    Span descSpan = new Span(descrizione);
                    descSpan.getElement().setAttribute("title", scenario.getDescrizione()); // Tooltip
                    return descSpan;
                })).setHeader("Descrizione")
                .setAutoWidth(true);

        // Colonna Azioni
        scenariosGrid.addComponentColumn(scenario -> {
                    HorizontalLayout actions = new HorizontalLayout();
                    actions.setSpacing(true);

                    // Bottone PDF
                    Button pdfButton = new Button(new Icon(VaadinIcon.FILE_TEXT));
                    pdfButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                    pdfButton.getElement().setAttribute("title", "Esporta in PDF");
                    pdfButton.addClickListener(e -> exportToPdf(scenario));

                    // Bottone Simulazione
                    Button simButton = new Button(new Icon(VaadinIcon.DOWNLOAD_ALT));
                    simButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                    simButton.getElement().setAttribute("title", "Esporta in sim.execution");
                    simButton.addClickListener(e -> exportToSimExecution(scenario));

                    // Bottone Elimina
                    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
                    deleteButton.getElement().setAttribute("title", "Elimina scenario");
                    deleteButton.addClickListener(e -> confirmAndDeleteScenario(scenario));

                    actions.add(pdfButton, simButton, deleteButton);
                    return actions;
                }).setHeader("Azioni")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Click su riga per dettagli
        scenariosGrid.addItemClickListener(event -> {
            if (event.getItem() != null) {
                getUI().ifPresent(ui -> ui.navigate("scenari/" + event.getItem().getId()));
            }
        });
    }

    private void exportToPdf(Scenario scenario) {
        Notification.show("Generating PDF...", 3000, Position.MIDDLE);

        try {
            // Creazione della risorsa PDF
            Anchor downloadLink = getAnchor(scenario);
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.getStyle().set("display", "none");

            UI ui = UI.getCurrent();
            if (ui != null) {
                ui.access(() -> {
                    ui.add(downloadLink);
                    downloadLink.getElement().executeJs("this.click()")
                            .then(result -> ui.access(() -> ui.remove(downloadLink)));
                });
            }
        } catch (Exception e) {
            Notification.show("Error generating PDF: " + e.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private Anchor getAnchor(Scenario scenario) {
        StreamResource resource = new StreamResource(
                "scenario_" + scenario.getTitolo() + ".pdf",
                () -> {
                    try {
                        byte[] pdfBytes = pdfExportService.exportScenarioToPdf(scenario.getId());
                        return new ByteArrayInputStream(pdfBytes);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to generate PDF", e);
                    }
                }
        );

        resource.setContentType("application/pdf");

        // Creazione e configurazione dell'anchor per il download
        Anchor downloadLink = new Anchor(resource, "Download PDF");
        return downloadLink;
    }


    private void exportToSimExecution(Scenario scenario) {
        Notification.show("Esportazione in sim.execution per " + scenario.getTitolo() + " non ancora implementata",
                3000, Position.MIDDLE);
    }

    private void confirmAndDeleteScenario(Scenario scenario) {
        Button confirmButton = new Button("Elimina", e -> {
            boolean deleted = scenarioService.deleteScenario(scenario.getId());
            if (deleted) {
                Notification.show("Scenario eliminato con successo", 3000, Position.MIDDLE);
                loadData();
            } else {
                Notification.show("Errore durante l'eliminazione", 3000, Position.MIDDLE);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Annulla");

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        Notification notification = new Notification();
        notification.add(new Paragraph("Sei sicuro di voler eliminare lo scenario \"" + scenario.getTitolo() + "\"?"));
        notification.add(buttons);
        notification.setDuration(0);
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
                            Notification.show("Errore durante il caricamento",
                                    3000, Position.MIDDLE);
                        }
                    } catch (Exception e) {
                        Notification.show("Errore durante l'aggiornamento: " + e.getMessage(),
                                3000, Position.MIDDLE);
                    } finally {
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true);
                    }
                }));
            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Errore durante il recupero: " + e.getMessage(),
                            3000, Position.MIDDLE);
                    progressBar.setVisible(false);
                    scenariosGrid.setVisible(true);
                }));
            }
        }).start();
    }
}