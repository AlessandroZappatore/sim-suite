package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField; // Added
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode; // Added
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.*;
import it.uniupo.simnova.views.home.CreditsComponent;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors; // Added

/**
 * Vista per la visualizzazione della lista degli scenari.
 * Consente di visualizzare, cercare, esportare e gestire gli scenari.
 * Utilizza Vaadin per la creazione dell'interfaccia utente.
 * Implementa la ricerca per Titolo, Autori, Tipo e Patologia con paginazione.
 *
 * @author Alessandro Zappatore
 * @version 1.1 // Updated version
 */
@SuppressWarnings({"ThisExpressionReferencesGlobalObjectJS", "JSCheckFunctionSignatures"})
@PageTitle("Lista Scenari")
@Route(value = "scenari", layout = MainLayout.class)
@Menu(order = 3)
public class ScenariosListView extends Composite<VerticalLayout> {
    private final ScenarioService scenarioService;
    private final PdfExportService pdfExportService;
    private final ZipExportService zipExportService;
    private final Grid<Scenario> scenariosGrid = new Grid<>();
    // Removed dataProvider field, it will be managed locally in methods needing it or via grid's setItems

    private ProgressBar progressBar;
    private final AtomicBoolean detached = new AtomicBoolean(false);
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private HorizontalLayout paginationControls;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;
    private Span pageInfo;
    final int MAX_TITLE_LENGTH = 20;
    final int MAX_DESCRIPTION_LENGTH = 30;
    final int MAX_AUTHORS_NAME_LENGTH = 20;
    final int MAX_PATHOLOGY_LENGTH = 20;

    // --- Search Fields ---
    private TextField searchTitolo;
    private TextField searchAutori;
    private ComboBox<String> searchTipo;
    private TextField searchPatologia;
    private Button resetButton;

    // --- Data Storage ---
    private List<Scenario> allScenarios = new ArrayList<>(); // Stores the full list from the service
    private List<Scenario> filteredScenarios = new ArrayList<>(); // Stores the filtered list


    @Autowired
    public ScenariosListView(ScenarioService scenarioService, PdfExportService pdfExportService, ZipExportService zipExportService) {
        this.scenarioService = scenarioService;
        this.pdfExportService = pdfExportService;
        this.zipExportService = zipExportService;
        initView();
        loadData(); // Initial data load
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        detached.set(true);
        executorService.shutdownNow();
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

        Button refreshButton = new Button("Aggiorna Dati", new Icon(VaadinIcon.REFRESH));
        // Modified Refresh Button Listener
        refreshButton.addClickListener(e -> {
            if (!detached.get()) {
                loadData(); // Reload data from service and reapply filters
            }
        });


        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, newScenarioButton, refreshButton);


        // --- Search Filter Layout ---
        configureSearchFilters();
        HorizontalLayout filterLayout = new HorizontalLayout(searchTitolo, searchTipo, searchAutori, searchPatologia, resetButton);
        filterLayout.setWidthFull();
        filterLayout.setPadding(true);
        filterLayout.setSpacing(true);
        filterLayout.setAlignItems(FlexComponent.Alignment.BASELINE); // Align items nicely


        // Main Content
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1350px"); // Adjust as needed
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        configureGrid();
        configurePagination();

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        contentLayout.add(progressBar, filterLayout, scenariosGrid, paginationControls); // Added filterLayout

        // Footer
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle().set("border-color", "var(--lumo-contrast-10pct)");

        CreditsComponent credits = new CreditsComponent();
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
                showJsonUploadDialog();
            }
        });
    }

    /**
     * Configura i campi per la ricerca/filtraggio degli scenari.
     */
    private void configureSearchFilters() {
        searchTitolo = new TextField("Filtra per Titolo");
        searchTitolo.setPlaceholder("Cerca titolo...");
        searchTitolo.setClearButtonVisible(true);
        searchTitolo.setValueChangeMode(ValueChangeMode.LAZY); // Trigger search after a small delay
        searchTitolo.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        searchAutori = new TextField("Filtra per Autori");
        searchAutori.setPlaceholder("Cerca autori...");
        searchAutori.setClearButtonVisible(true);
        searchAutori.setValueChangeMode(ValueChangeMode.LAZY);
        searchAutori.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        searchTipo = new ComboBox<>("Filtra per Tipo");
        searchTipo.setPlaceholder("Seleziona tipo...");
        // Define possible types - Adjust if necessary based on actual types
        searchTipo.setItems("Tutti", "Patient Simulated Scenario", "Advanced Scenario", "Quick Scenario");
        searchTipo.setValue("Tutti"); // Default value
        searchTipo.setClearButtonVisible(true);
        searchTipo.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        searchPatologia = new TextField("Filtra per Patologia");
        searchPatologia.setPlaceholder("Cerca patologia...");
        searchPatologia.setClearButtonVisible(true);
        searchPatologia.setValueChangeMode(ValueChangeMode.LAZY);
        searchPatologia.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        resetButton = new Button("Reset Filtri", new Icon(VaadinIcon.CLOSE_CIRCLE_O));
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(e -> {
            searchTitolo.clear();
            searchAutori.clear();
            searchTipo.setValue("Tutti");
            searchPatologia.clear();
            // applyFiltersAndRefreshGrid will be triggered by the value changes,
            // but call it explicitly if listeners don't fire for clear() or setValue()
            applyFiltersAndRefreshGrid();
        });
    }


    /**
     * Applica i filtri correnti alla lista completa degli scenari
     * e aggiorna la griglia e la paginazione.
     */
    private void applyFiltersAndRefreshGrid() {
        if (detached.get()) return;

        String titoloFilter = searchTitolo.getValue().trim().toLowerCase();
        String autoriFilter = searchAutori.getValue().trim().toLowerCase();
        String tipoFilter = searchTipo.getValue() != null ? searchTipo.getValue() : "Tutti";
        String patologiaFilter = searchPatologia.getValue().trim().toLowerCase();

        // Filter the full list 'allScenarios'
        filteredScenarios = allScenarios.stream()
                .filter(scenario -> titoloFilter.isEmpty() || (scenario.getTitolo() != null && scenario.getTitolo().toLowerCase().contains(titoloFilter)))
                .filter(scenario -> autoriFilter.isEmpty() || (scenario.getAutori() != null && scenario.getAutori().toLowerCase().contains(autoriFilter)))
                .filter(scenario -> {
                    if ("Tutti".equals(tipoFilter)) {
                        return true; // No type filter
                    }
                    String actualType = scenarioService.getScenarioType(scenario.getId());
                    return actualType != null && actualType.equalsIgnoreCase(tipoFilter);
                })
                .filter(scenario -> patologiaFilter.isEmpty() || (scenario.getPatologia() != null && scenario.getPatologia().toLowerCase().contains(patologiaFilter)))
                .collect(Collectors.toList());

        // Reset pagination to the first page whenever filters change
        currentPage = 0;

        // Update the grid items and pagination info
        updateGridItems();
        updatePaginationInfo();
    }


    /**
     * Mostra il dialog per il caricamento del file JSON.
     */
    private void showJsonUploadDialog() {
        if (detached.get()) {
            return;
        }

        // Creazione del layout del dialog
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Paragraph title = new Paragraph("Carica un file JSON per creare un nuovo scenario");
        title.getStyle().set("font-weight", "bold");
        title.getStyle().set("font-size", "1.2em");

        Paragraph description = new Paragraph("Seleziona un file JSON da caricare");

        // Creazione dell'uploader
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".json");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Trascina qui il file JSON o"));
        upload.setWidth("100%");
        upload.setMaxWidth("500px");

        Button cancelButton = new Button("Annulla");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(cancelButton);
        buttons.setSpacing(true);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        dialogLayout.add(title, description, upload, buttons);

        // Creazione della notifica del dialog
        Notification dialog = new Notification();
        dialog.setPosition(Position.MIDDLE);
        dialog.setDuration(0); // Persistent until closed
        dialog.add(dialogLayout);
        dialog.open();

        // Listener per il pulsante Annulla
        cancelButton.addClickListener(e -> dialog.close());

        // Listener per il caricamento completato
        upload.addSucceededListener(event -> {
            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] jsonBytes = inputStream.readAllBytes();
                dialog.close(); // Close upload dialog

                UI ui = UI.getCurrent();
                if (ui == null || detached.get()) return;

                Notification loadingNotification = new Notification("Creazione scenario in corso...", 0, Position.MIDDLE);
                loadingNotification.open();

                executorService.submit(() -> {
                    try {
                        boolean created = scenarioService.createScenarioByJSON(jsonBytes);

                        if (!detached.get() && !ui.isClosing()) {
                            ui.access(() -> {
                                loadingNotification.close();
                                if (created) {
                                    Notification.show("Scenario creato con successo", 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                                    loadData(); // Reload data to see the new scenario
                                } else {
                                    Notification.show("Errore durante la creazione dello scenario", 5000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        // Handle exceptions during background creation
                        if (!detached.get() && !ui.isClosing()) {
                            ui.access(() -> {
                                loadingNotification.close();
                                Notification.show("Errore creazione: " + ex.getMessage(), 5000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                            });
                        }
                    }
                });
            } catch (IOException ex) {
                // Handle exceptions during file reading
                Notification.show("Errore lettura file: " + ex.getMessage(), 5000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                dialog.close();
            }
        });

        // Listener per errori di caricamento
        upload.addFailedListener(event -> Notification.show("Caricamento fallito: " + event.getReason().getMessage(), 5000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR));
    }


    private void configureGrid() {
        scenariosGrid.setWidthFull();
        scenariosGrid.getStyle().set("min-height", "400px"); // Or adjust as needed

        // Colonna Titolo (con testo troncato)
        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String titolo = scenario.getTitolo() != null ? scenario.getTitolo() : "";
                    String displayTitolo = titolo.length() > MAX_TITLE_LENGTH ? titolo.substring(0, MAX_TITLE_LENGTH) + "..." : titolo;
                    Span titoloSpan = new Span(displayTitolo);
                    if (!titolo.isEmpty()) {
                        titoloSpan.getElement().setAttribute("title", titolo); // Tooltip con testo completo
                    }
                    return titoloSpan;
                }))
                .setHeader("Titolo")
                .setSortable(true)
                .setAutoWidth(true)
                .setComparator(Comparator.comparing(Scenario::getTitolo, Comparator.nullsLast(String::compareToIgnoreCase))); // Sorting on full title


        // Colonna Tipo Scenario (utilizzando getScenarioType)
        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String tipo = scenarioService.getScenarioType(scenario.getId());
                    tipo = tipo != null ? tipo : "N/D"; // Handle null type
                    Span tipoSpan = new Span(tipo);

                    // Apply styles based on type
                    switch (tipo.toLowerCase()) {
                        case "patient simulated scenario":
                            tipoSpan.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontWeight.SEMIBOLD);
                            break;
                        case "advanced scenario":
                            tipoSpan.addClassNames(LumoUtility.TextColor.SUCCESS, LumoUtility.FontWeight.SEMIBOLD);
                            break;
                        case "quick scenario":
                            tipoSpan.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontWeight.SEMIBOLD);
                            break;
                        default:
                            tipoSpan.addClassName(LumoUtility.TextColor.SECONDARY);
                    }
                    return tipoSpan;
                }))
                .setHeader("Tipo")
                .setSortable(true)
                .setAutoWidth(true)
                .setComparator(Comparator.comparing(s -> scenarioService.getScenarioType(s.getId()), Comparator.nullsLast(String::compareToIgnoreCase))); // Sorting on type

        // Colonna Autori (con testo troncato)
        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String autori = scenario.getAutori() != null ? scenario.getAutori() : "";
                    String displayAutori = autori.length() > MAX_AUTHORS_NAME_LENGTH ? autori.substring(0, MAX_AUTHORS_NAME_LENGTH) + "..." : autori;
                    Span autoriSpan = new Span(displayAutori);
                    if (!autori.isEmpty()) {
                        autoriSpan.getElement().setAttribute("title", autori); // Tooltip
                    }
                    return autoriSpan;
                }))
                .setHeader("Autori")
                .setSortable(true) // Make Autori sortable
                .setAutoWidth(true)
                .setComparator(Comparator.comparing(Scenario::getAutori, Comparator.nullsLast(String::compareToIgnoreCase))); // Sorting on full authors list

        // Colonna Patologia (con testo troncato)
        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String patologia = scenario.getPatologia() != null ? scenario.getPatologia() : "";
                    String displayPatologia = patologia.length() > MAX_PATHOLOGY_LENGTH ? patologia.substring(0, MAX_PATHOLOGY_LENGTH) + "..." : patologia;
                    Span patoSpan = new Span(displayPatologia);
                    if (!patologia.isEmpty()) {
                        patoSpan.getElement().setAttribute("title", patologia); // Tooltip
                    }
                    return patoSpan;
                }))
                .setHeader("Patologia")
                .setSortable(true)
                .setAutoWidth(true)
                .setComparator(Comparator.comparing(Scenario::getPatologia, Comparator.nullsLast(String::compareToIgnoreCase))); // Sorting on full pathology

        // Colonna Descrizione (con testo HTML interpretato e troncato)
        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String descrizione = scenario.getDescrizione() != null ? scenario.getDescrizione() : "";
                    Div container = new Div();
                    container.setWidthFull();
                    container.getStyle()
                            .set("max-width", "200px")
                            .set("overflow", "hidden")
                            .set("text-overflow", "ellipsis")
                            .set("white-space", "nowrap");

                    if (descrizione.isEmpty()) {
                        return container;
                    }

                    String plainText = Jsoup.parse(descrizione).text(); // Get plain text for tooltip and truncation
                    String shortDesc;

                    if (plainText.length() > MAX_DESCRIPTION_LENGTH) {
                        shortDesc = plainText.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
                        container.setText(shortDesc); // Show truncated plain text in grid
                    } else {
                        // If short enough, attempt to show basic HTML, but still truncate if needed visually
                        // Note: Rendering complex HTML reliably inside a cell can be tricky.
                        // Sticking to truncated text might be safer. Let's keep the truncated text display.
                        // container.add(new Html("<div>" + descrizione + "</div>")); // Could potentially break layout
                        container.setText(plainText); // Show full plain text if short
                    }

                    container.getElement().setAttribute("title", plainText); // Full plain text as tooltip
                    return container;
                }))
                .setHeader("Descrizione")
                .setAutoWidth(true);
        // Note: Sorting HTML/complex content is usually not meaningful or reliable.

        // Colonna Azioni
        scenariosGrid.addComponentColumn(scenario -> {
                    HorizontalLayout actions = new HorizontalLayout();
                    actions.setSpacing(false); // Reduce space between icons
                    actions.setMargin(false);
                    actions.setPadding(false);

                    Button pdfButton = new Button(new Icon(VaadinIcon.FILE_TEXT_O)); // Changed icon slightly
                    pdfButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
                    pdfButton.getElement().setAttribute("title", "Esporta in PDF");
                    pdfButton.getStyle().set("margin-left", "0").set("margin-right", "var(--lumo-space-xs)"); // Adjust spacing
                    pdfButton.addClickListener(e -> {
                        if (!detached.get()) exportToPdf(scenario);
                    });

                    Button simButton = new Button(new Icon(VaadinIcon.DOWNLOAD)); // Changed icon slightly
                    simButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
                    simButton.getElement().setAttribute("title", "Esporta in .zip (sim.execution)");
                    simButton.getStyle().set("margin-left", "0").set("margin-right", "var(--lumo-space-xs)");
                    simButton.addClickListener(e -> {
                        if (!detached.get()) exportToSimExecution(scenario);
                    });

                    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
                    deleteButton.getElement().setAttribute("title", "Elimina scenario");
                    deleteButton.getStyle().set("margin-left", "0");
                    deleteButton.addClickListener(e -> {
                        if (!detached.get()) confirmAndDeleteScenario(scenario);
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

    private void configurePagination() {
        paginationControls = new HorizontalLayout();
        paginationControls.setWidthFull();
        paginationControls.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        paginationControls.setAlignItems(FlexComponent.Alignment.CENTER); // Center vertically too
        paginationControls.setSpacing(true);
        paginationControls.setPadding(true);

        Button firstPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT));
        Button prevPageButton = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        pageInfo = new Span("Pagina -"); // Placeholder text
        Button nextPageButton = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        Button lastPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));

        firstPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY); // Use tertiary for less emphasis
        prevPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        lastPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        pageInfo.getStyle().set("margin", "0 0.5rem"); // Adjust spacing

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
            if (!detached.get()) {
                int totalItems = filteredScenarios.size(); // Use filtered list size
                int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    updateGridItems();
                    updatePaginationInfo();
                }
            }
        });

        lastPageButton.addClickListener(e -> {
            if (!detached.get()) {
                int totalItems = filteredScenarios.size(); // Use filtered list size
                int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
                // Ensure totalPages is at least 1 even if totalItems is 0
                totalPages = Math.max(1, totalPages);
                if (currentPage < totalPages - 1) {
                    currentPage = totalPages - 1;
                    updateGridItems();
                    updatePaginationInfo();
                }
            }
        });

        paginationControls.add(firstPageButton, prevPageButton, pageInfo, nextPageButton, lastPageButton);
        paginationControls.setVisible(false); // Initially hidden until data loads
    }


    /**
     * Aggiorna le informazioni di paginazione (testo "Pagina X di Y") e lo stato
     * abilitato/disabilitato dei pulsanti di navigazione, basandosi sulla
     * lista filtrata.
     */
    private void updatePaginationInfo() {
        if (detached.get()) return;

        int totalItems = filteredScenarios.size(); // Use filtered list size
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE)); // Ensure at least 1 page

        // Update page info text
        if (totalItems == 0) {
            pageInfo.setText("Nessun risultato");
        } else {
            pageInfo.setText("Pagina " + (currentPage + 1) + " di " + totalPages);
        }


        // Update button states in UI thread
        getUI().ifPresent(ui -> ui.access(() -> {
            boolean hasPrev = currentPage > 0;
            boolean hasNext = currentPage < totalPages - 1;

            paginationControls.getChildren().forEach(component -> {
                if (component instanceof Button button) {
                    // Simple check based on icon attribute
                    String iconAttr = button.getIcon() instanceof Icon ? button.getIcon().getElement().getAttribute("icon") : null;
                    if (iconAttr != null) {
                        if (iconAttr.equals("vaadin:angle-double-left") || iconAttr.equals("vaadin:angle-left")) {
                            button.setEnabled(hasPrev);
                        } else if (iconAttr.equals("vaadin:angle-right") || iconAttr.equals("vaadin:angle-double-right")) {
                            button.setEnabled(hasNext);
                        }
                    }
                }
            });
            // Ensure pagination controls are visible if there's data or filters applied
            paginationControls.setVisible(true);
        }));
    }

    /**
     * Aggiorna gli elementi visualizzati nella griglia in base alla pagina corrente
     * della lista *filtrata*.
     */
    private void updateGridItems() {
        if (detached.get()) return;

        // Calculate the slice of the *filtered* list to display
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredScenarios.size());

        if (fromIndex < filteredScenarios.size() && fromIndex < toIndex) {
            List<Scenario> pageItems = filteredScenarios.subList(fromIndex, toIndex);
            // Use ListDataProvider to leverage potential optimizations, though setItems works fine too
            scenariosGrid.setDataProvider(new ListDataProvider<>(pageItems));
            // scenariosGrid.setItems(pageItems); // Alternative simpler way
        } else {
            // If the current page is out of bounds (e.g., after filtering reduces items)
            // or if the list is empty.
            scenariosGrid.setItems(new ArrayList<>()); // Show empty grid
        }

        // Ensure grid is visible after update
        getUI().ifPresent(ui -> ui.access(() -> scenariosGrid.setVisible(true)));
    }

    // --- Export and Delete Methods (largely unchanged, but ensure they use the passed 'scenario') ---

    private void exportToPdf(Scenario scenario) {
        if (detached.get() || scenario == null) return; // Added null check

        Notification.show("Generazione del PDF...", 3000, Position.MIDDLE);
        try {
            StreamResource resource = new StreamResource(
                    "scenario_" + sanitizeFileName(scenario.getTitolo()) + ".pdf", // Sanitize filename
                    () -> {
                        try {
                            byte[] pdfBytes = pdfExportService.exportScenarioToPdf(scenario.getId());
                            return new ByteArrayInputStream(pdfBytes);
                        } catch (IOException | RuntimeException e) { // Catch potential runtime exceptions to Log
                            //  error server-side
                            System.err.println("Error generating PDF for scenario " + scenario.getId() + ": " + e.getMessage());
                            // Show error in UI thread if possible
                            getUI().ifPresent(ui -> ui.access(() -> Notification.show("Errore creazione PDF", 5000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR)));
                            // Return an empty stream to avoid browser errors, though the notification should indicate failure
                            return new ByteArrayInputStream(new byte[0]);
                        }
                    }
            );
            resource.setContentType("application/pdf");
            triggerDownload(resource); // Use helper method for download
        } catch (Exception e) { // Catch errors during resource creation itself
            handleExportError("Errore preparazione PDF", e);
        }
    }

    private void exportToSimExecution(Scenario scenario) {
        if (detached.get() || scenario == null) return;

        Notification.show("Generazione dell'archivio ZIP...", 3000, Position.MIDDLE);
        try {
            StreamResource resource = new StreamResource(
                    "scenario_" + sanitizeFileName(scenario.getTitolo()) + ".zip", // Sanitize filename
                    () -> {
                        try {
                            byte[] zipBytes = zipExportService.exportScenarioToZip(scenario.getId());
                            return new ByteArrayInputStream(zipBytes);
                        } catch (IOException | RuntimeException e) {
                            System.err.println("Error generating ZIP for scenario " + scenario.getId() + ": " + e.getMessage());
                            getUI().ifPresent(ui -> ui.access(() -> Notification.show("Errore creazione ZIP", 5000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR)));
                            return new ByteArrayInputStream(new byte[0]);
                        }
                    }
            );
            resource.setContentType("application/zip");
            triggerDownload(resource); // Use helper method
        } catch (Exception e) {
            handleExportError("Errore preparazione ZIP", e);
        }
    }

    /**
     * Helper method to trigger the download using FileDownloadWrapper.
     *
     * @param resource The StreamResource to download.
     */
    private void triggerDownload(StreamResource resource) {
        UI ui = UI.getCurrent();
        if (ui == null || detached.get() || ui.isClosing()) return;

        Button downloadButton = new Button(); // Invisible button
        downloadButton.getStyle().set("display", "none");
        FileDownloadWrapper downloadWrapper = new FileDownloadWrapper(resource);
        downloadWrapper.wrapComponent(downloadButton);

        // Add wrapper to UI, click the button, then remove wrapper
        ui.access(() -> {
            ui.add(downloadWrapper); // Add wrapper to the ScenariosListView component itself
            downloadButton.getElement().executeJs("this.click()")
                    .then(result -> ui.access(() -> ui.remove(downloadWrapper)));
        });
    }


    /**
     * Handles errors occurring during export setup (before stream generation).
     *
     * @param message Error message prefix.
     * @param e       The exception.
     */
    private void handleExportError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        if (!detached.get()) {
            getUI().ifPresent(ui -> ui.access(() ->
                    Notification.show(message + ": " + e.getMessage(), 5000, Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR)
            ));
        }
    }

    /**
     * Sanitizes a string to be used as part of a filename.
     * Replaces potentially problematic characters with underscores.
     *
     * @param name Original name string.
     * @return Sanitized string.
     */
    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "scenario"; // Default name if empty
        }
        // Replace invalid filename characters (Windows/Linux/Mac) with underscore
        // Adjust the regex as needed for more specific sanitization
        return name.replaceAll("[\\\\/:*?\"<>|\\s]+", "_")
                .replaceAll("_+", "_"); // Replace multiple underscores with one
    }


    private void confirmAndDeleteScenario(Scenario scenario) {
        if (detached.get() || scenario == null) return;

        // Use Vaadin's ConfirmDialog for a better user experience (if dependency available)
        // Or stick to Notification based confirmation:

        Notification confirmationNotification = new Notification();
        confirmationNotification.setDuration(0); // Persistent
        confirmationNotification.setPosition(Position.MIDDLE);

        VerticalLayout confirmLayout = new VerticalLayout();
        confirmLayout.add(new Paragraph("Sei sicuro di voler eliminare lo scenario \"" + scenario.getTitolo() + "\"?"));

        Button confirmButton = new Button("Elimina", event -> {
            if (!detached.get()) {
                // Perform deletion in background if it might take time (optional)
                // executorService.submit(() -> { ... });
                boolean deleted = scenarioService.deleteScenario(scenario.getId());
                // Update UI back in the UI thread
                getUI().ifPresent(ui -> ui.access(() -> {
                    if (deleted) {
                        Notification.show("Scenario eliminato", 3000, Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                        loadData(); // Reload data after deletion
                    } else {
                        Notification.show("Errore durante l'eliminazione", 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }));
            }
            confirmationNotification.close(); // Close confirmation
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Annulla", event -> confirmationNotification.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);
        confirmLayout.add(buttons);
        confirmLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Center buttons

        confirmationNotification.add(confirmLayout);
        confirmationNotification.open();
    }


    /**
     * Carica i dati degli scenari dal service in background,
     * li memorizza nella lista 'allScenarios', e poi
     * applica i filtri e aggiorna la griglia.
     */
    private void loadData() {
        if (detached.get()) return;

        UI ui = UI.getCurrent();
        if (ui == null) return;

        // Show loading state in UI thread
        ui.access(() -> {
            progressBar.setVisible(true);
            scenariosGrid.setVisible(false); // Hide grid during load
            paginationControls.setVisible(false); // Hide pagination during load
            // Optionally disable search fields during load
            searchTitolo.setEnabled(false);
            searchAutori.setEnabled(false);
            searchTipo.setEnabled(false);
            searchPatologia.setEnabled(false);
            resetButton.setEnabled(false);
        });

        executorService.submit(() -> {
            try {
                // Fetch all scenarios from the service
                List<Scenario> fetchedScenarios = scenarioService.getAllScenarios();

                // Store the full list (handle null from service)
                allScenarios = (fetchedScenarios != null) ? new ArrayList<>(fetchedScenarios) : new ArrayList<>();

                // Sort the full list (e.g., by ID descending for newest first by default)
                allScenarios.sort(Comparator.comparing(Scenario::getId, Comparator.nullsLast(Comparator.reverseOrder())));


                // Check if view is still attached before updating UI
                if (detached.get() || ui.isClosing()) {
                    return;
                }

                // Update UI back in the UI thread
                ui.access(() -> {
                    try {
                        // Apply current filters (if any) to the newly loaded data
                        // and update grid/pagination
                        applyFiltersAndRefreshGrid();
                    } finally {
                        // Always ensure loading indicators are hidden and controls re-enabled
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true); // Make grid visible
                        paginationControls.setVisible(true); // Make pagination visible
                        // Re-enable search fields
                        searchTitolo.setEnabled(true);
                        searchAutori.setEnabled(true);
                        searchTipo.setEnabled(true);
                        searchPatologia.setEnabled(true);
                        resetButton.setEnabled(true);
                    }
                });

            } catch (Exception e) {
                System.err.println("Error loading scenarios: " + e.getMessage()); // Log error
                // Handle exceptions during background fetch
                if (!detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        Notification.show("Errore caricamento dati: " + e.getMessage(), 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        // Ensure UI is reset even on error
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true); // Show empty grid potentially
                        paginationControls.setVisible(true);
                        // Re-enable search fields
                        searchTitolo.setEnabled(true);
                        searchAutori.setEnabled(true);
                        searchTipo.setEnabled(true);
                        searchPatologia.setEnabled(true);
                        resetButton.setEnabled(true);
                        // Clear data to avoid showing stale info
                        allScenarios.clear();
                        filteredScenarios.clear();
                        updateGridItems(); // Show empty grid
                        updatePaginationInfo(); // Update pagination for empty state
                    });
                }
            }
        });
    }
}