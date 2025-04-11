package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
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
import it.uniupo.simnova.service.JSONExportService;
import it.uniupo.simnova.service.PdfExportService;
import it.uniupo.simnova.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Vista per la visualizzazione della lista degli scenari.
 * Consente di visualizzare, cercare, esportare e gestire gli scenari.
 * Utilizza Vaadin per la creazione dell'interfaccia utente.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Lista Scenari")
@Route(value = "scenari", layout = MainLayout.class)
@Menu(order = 3)
public class ScenariosListView extends Composite<VerticalLayout> {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per l'esportazione in PDF.
     */
    private final PdfExportService pdfExportService;
    /**
     * Griglia per visualizzare gli scenari.
     */
    private final Grid<Scenario> scenariosGrid = new Grid<>();
    /**
     * Provider di dati per la griglia.
     */
    private ListDataProvider<Scenario> dataProvider;
    /**
     * Barra di progresso per il caricamento dei dati.
     */
    private ProgressBar progressBar;
    /**
     * Flag per verificare se la vista è stata staccata.
     */
    private final AtomicBoolean detached = new AtomicBoolean(false);
    /**
     * ExecutorService per gestire i task in background.
     */
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Layout per i controlli di paginazione.
     */
    private HorizontalLayout paginationControls;

    /**
     * Indica la pagina corrente.
     */
    private int currentPage = 0;

    /**
     * Dimensione della pagina (numero di elementi per pagina).
     */
    private static final int PAGE_SIZE = 10;

    /**
     * Span per visualizzare le informazioni sulla pagina corrente.
     */
    private Span pageInfo;

    /**
     * Costruttore della vista.
     *
     * @param scenarioService  Servizio per la gestione degli scenari
     * @param pdfExportService Servizio per l'esportazione in PDF
     */
    @Autowired
    public ScenariosListView(ScenarioService scenarioService, PdfExportService pdfExportService) {
        this.scenarioService = scenarioService;
        this.pdfExportService = pdfExportService;
        initView();
        loadData();
    }

    /**
     * Metodo chiamato quando la vista viene staccata.
     * Chiude l'ExecutorService per evitare perdite di memoria.
     *
     * @param detachEvent Evento di stacco della vista
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        detached.set(true);
        executorService.shutdownNow();
    }

    /**
     * Inizializza la vista e crea i componenti dell'interfaccia utente.
     */
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
        refreshButton.addClickListener(e -> {
            if (!detached.get()) {
                refreshButton.getUI().ifPresent(ui -> ui.refreshCurrentRoute(true));
            }
        });

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, newScenarioButton, refreshButton);

        // Main Content
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1350px");
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
        configurePagination();

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        contentLayout.add(searchField, progressBar, scenariosGrid, paginationControls);

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
        backButton.addClickListener(e -> {
            if (!detached.get()) {
                getUI().ifPresent(ui -> ui.navigate(""));
            }
        });

        newScenarioButton.addClickListener(e -> {
            if (!detached.get()) {
                getUI().ifPresent(ui -> ui.navigate("creation"));
            }
        });

        searchField.addValueChangeListener(e -> {
            if (!detached.get()) {
                String searchTerm = e.getValue().toLowerCase();
                dataProvider.addFilter(scenario ->
                        scenario.getTitolo().toLowerCase().contains(searchTerm) ||
                                scenario.getPatologia().toLowerCase().contains(searchTerm) ||
                                scenario.getDescrizione().toLowerCase().contains(searchTerm)
                );
                // Reset alla prima pagina quando si filtra
                currentPage = 0;
                updateGridItems();
                updatePaginationInfo();
            }
        });
    }

    /**
     * Configura la griglia per visualizzare gli scenari.
     */
    private void configureGrid() {
        scenariosGrid.setWidthFull();
        scenariosGrid.getStyle().set("min-height", "400px");

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
                    int maxLength = 30;
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
                    pdfButton.addClickListener(e -> {
                        if (!detached.get()) {
                            exportToPdf(scenario);
                        }
                    });

                    // Bottone Simulazione
                    Button simButton = new Button(new Icon(VaadinIcon.DOWNLOAD_ALT));
                    simButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                    simButton.getElement().setAttribute("title", "Esporta in sim.execution");
                    simButton.addClickListener(e -> {
                        if (!detached.get()) {
                            exportToSimExecution(scenario);
                        }
                    });

                    // Bottone Elimina
                    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
                    deleteButton.getElement().setAttribute("title", "Elimina scenario");
                    deleteButton.addClickListener(e -> {
                        if (!detached.get()) {
                            confirmAndDeleteScenario(scenario);
                        }
                    });

                    actions.add(pdfButton, simButton, deleteButton);
                    return actions;
                }).setHeader("Azioni")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Click su riga per dettagli
        scenariosGrid.addItemClickListener(event -> {
            if (!detached.get() && event.getItem() != null) {
                getUI().ifPresent(ui -> ui.navigate("scenari/" + event.getItem().getId()));
            }
        });
    }

    /**
     * Configura i controlli di paginazione per la griglia.
     */
    private void configurePagination() {
        paginationControls = new HorizontalLayout();
        paginationControls.setWidthFull();
        paginationControls.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        paginationControls.setSpacing(true);
        paginationControls.setPadding(true);

        Button firstPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT));
        Button prevPageButton = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        pageInfo = new Span("Pagina 1");
        Button nextPageButton = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        Button lastPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));

        // Aggiungi stile ai bottoni
        firstPageButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        prevPageButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        nextPageButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        lastPageButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        pageInfo.getStyle().set("margin", "0 1rem");

        // Azioni per i pulsanti
        firstPageButton.addClickListener(e -> {
            if (currentPage > 0 && !detached.get()) {
                currentPage = 0;
                updateGridItems();
                updatePaginationInfo();
            }
        });

        prevPageButton.addClickListener(e -> {
            if (currentPage > 0 && !detached.get()) {
                currentPage--;
                updateGridItems();
                updatePaginationInfo();
            }
        });

        nextPageButton.addClickListener(e -> {
            if (!detached.get() && dataProvider != null) {
                int totalItems = dataProvider.getItems().size();
                int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    updateGridItems();
                    updatePaginationInfo();
                }
            }
        });

        lastPageButton.addClickListener(e -> {
            if (!detached.get() && dataProvider != null) {
                int totalItems = dataProvider.getItems().size();
                int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
                if (currentPage < totalPages - 1) {
                    currentPage = totalPages - 1;
                    updateGridItems();
                    updatePaginationInfo();
                }
            }
        });

        paginationControls.add(firstPageButton, prevPageButton, pageInfo, nextPageButton, lastPageButton);
    }

    /**
     * Aggiorna le informazioni di paginazione e lo stato dei pulsanti.
     */
    private void updatePaginationInfo() {
        if (dataProvider == null) return;

        int totalItems = dataProvider.getItems().size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));

        pageInfo.setText("Pagina " + (currentPage + 1) + " di " + totalPages);

        // Aggiorna lo stato dei pulsanti nel thread dell'UI
        getUI().ifPresent(ui -> ui.access(() -> paginationControls.getChildren().forEach(component -> {
            if (component instanceof Button button) {
                Icon icon = (Icon) button.getIcon();
                if (icon != null) {
                    if (icon.getElement().getAttribute("icon").equals("vaadin:angle-double-left") ||
                            icon.getElement().getAttribute("icon").equals("vaadin:angle-left")) {
                        button.setEnabled(currentPage > 0);
                    } else if (icon.getElement().getAttribute("icon").equals("vaadin:angle-right") ||
                            icon.getElement().getAttribute("icon").equals("vaadin:angle-double-right")) {
                        button.setEnabled(currentPage < totalPages - 1);
                    }
                }
            }
        })));
    }

    /**
     * Aggiorna gli elementi visualizzati nella griglia in base alla pagina corrente.
     */
    private void updateGridItems() {
        if (dataProvider == null) return;

        // Ottieni gli elementi filtrati dal dataProvider
        List<Scenario> filteredItems = new ArrayList<>();
        dataProvider.getItems().forEach(filteredItems::add);

        // Calcola l'indice di inizio e fine per la pagina corrente
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredItems.size());

        if (fromIndex < toIndex) {
            List<Scenario> pageItems = filteredItems.subList(fromIndex, toIndex);
            scenariosGrid.setItems(pageItems);
        } else {
            scenariosGrid.setItems(new ArrayList<>());
        }
    }

    /**
     * Esporta lo scenario selezionato in PDF.
     *
     * @param scenario Scenario da esportare
     */
    private void exportToPdf(Scenario scenario) {
        if (detached.get()) {
            return;
        }

        Notification.show("Generazione del PDF...", 3000, Position.MIDDLE);

        try {
            // Creazione della risorsa PDF
            Anchor downloadLink = getAnchorPdf(scenario);
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.getStyle().set("display", "none");

            UI ui = UI.getCurrent();
            if (ui != null && !detached.get()) {
                ui.access(() -> {
                    ui.add(downloadLink);
                    downloadLink.getElement().executeJs("this.click()").then(result -> ui.access(() -> ui.remove(downloadLink)));
                });
            }
        } catch (Exception e) {
            if (!detached.get()) {
                Notification.show("Errore durante la generazione del PDF: " + e.getMessage(), 5000, Position.MIDDLE);
            }
        }
    }

    /**
     * Crea un anchor per il download del PDF dello scenario.
     *
     * @param scenario Scenario da esportare
     * @return Anchor per il download del PDF
     */
    private Anchor getAnchorPdf(Scenario scenario) {
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
        return new Anchor(resource, "Download PDF");
    }

    /**
     * Esporta lo scenario selezionato in sim.execution.
     *
     * @param scenario Scenario da esportare
     */
    private void exportToSimExecution(Scenario scenario) {
        if (detached.get()) {
            return;
        }

        Notification.show("Generazione del JSON...", 3000, Position.MIDDLE);

        try {
            // Creazione della risorsa PDF
            Anchor downloadLink = getAnchorJSON(scenario);
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.getStyle().set("display", "none");

            UI ui = UI.getCurrent();
            if (ui != null && !detached.get()) {
                ui.access(() -> {
                    ui.add(downloadLink);
                    downloadLink.getElement().executeJs("this.click()").then(result -> ui.access(() -> ui.remove(downloadLink)));
                });
            }
        } catch (Exception e) {
            if (!detached.get()) {
                Notification.show("Errore durante la generazione del JSON: " + e.getMessage(), 5000, Position.MIDDLE);
            }
        }
    }

    private Anchor getAnchorJSON(Scenario scenario) {
        StreamResource resource = new StreamResource(
                "scenario_" + scenario.getTitolo() + ".json",
                () -> {
                    byte[] JSONBytes = JSONExportService.exportScenarioToJSON(scenario.getId());
                    return new ByteArrayInputStream(JSONBytes);
                }
        );

        resource.setContentType("application/json");

        // Creazione e configurazione dell'anchor per il download
        return new Anchor(resource, "Download JSON");
    }

    /**
     * Conferma l'eliminazione dello scenario selezionato.
     *
     * @param scenario Scenario da eliminare
     */
    private void confirmAndDeleteScenario(Scenario scenario) {
        if (detached.get()) {
            return;
        }

        Button confirmButton = new Button("Elimina", e -> {
            if (!detached.get()) {
                boolean deleted = scenarioService.deleteScenario(scenario.getId());
                if (deleted) {
                    Notification.show("Scenario eliminato con successo", 3000, Position.MIDDLE);
                    loadData();
                } else {
                    Notification.show("Errore durante l'eliminazione", 3000, Position.MIDDLE);
                }
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

    /**
     * Carica i dati degli scenari e li visualizza nella griglia.
     * Utilizza un ExecutorService per eseguire il caricamento in background.
     */
    private void loadData() {
        if (detached.get()) {
            return;
        }

        // Show loading state
        UI ui = UI.getCurrent();
        if (ui == null) return;

        ui.access(() -> {
            progressBar.setVisible(true);
            scenariosGrid.setVisible(false);
            paginationControls.setVisible(false);
        });

        executorService.submit(() -> {
            try {
                List<Scenario> scenarios = scenarioService.getAllScenarios();

                // Check if view is still attached before updating UI
                if (detached.get() || ui.isClosing()) {
                    return;
                }

                ui.access(() -> {
                    try {
                        if (scenarios != null) {
                            this.dataProvider = new ListDataProvider<>(scenarios);
                            // Reset alla prima pagina quando si caricano nuovi dati
                            currentPage = 0;
                            updateGridItems();
                            updatePaginationInfo();
                        } else {
                            Notification.show("Errore durante il caricamento dei dati", 3000, Position.MIDDLE);
                        }
                    } finally {
                        // Always ensure loading indicators are hidden
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true);
                        paginationControls.setVisible(true);
                    }
                });
            } catch (Exception e) {
                if (!detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        Notification.show("Errore: " + e.getMessage(), 3000, Position.MIDDLE);
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true);
                        paginationControls.setVisible(true);
                    });
                }
            }
        });
    }
}