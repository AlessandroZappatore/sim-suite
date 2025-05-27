package it.uniupo.simnova.views.creation;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.ZipExportService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.*;
import it.uniupo.simnova.service.scenario.operations.ScenarioDeletionService;
import it.uniupo.simnova.service.scenario.operations.ScenarioImportService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.DialogSupport;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static it.uniupo.simnova.views.ui.helper.support.SanitizedFileName.sanitizeFileName;

/**
 * Vista per la visualizzazione della lista degli scenari.
 * Consente di visualizzare, cercare, esportare e gestire gli scenari.
 * Utilizza Vaadin per la creazione dell'interfaccia utente.
 * Implementa la ricerca per Titolo, Autori, Tipo e Patologia con paginazione.
 *
 * @author Alessandro Zappatore
 * @version 1.2
 */
@SuppressWarnings({"ThisExpressionReferencesGlobalObjectJS", "JSCheckFunctionSignatures"})
@PageTitle("Lista Scenari")
@Route(value = "scenari")
public class ScenariosListView extends Composite<VerticalLayout> {
    private static final int PAGE_SIZE = 10;
    public final AtomicBoolean detached;
    final int MAX_TITLE_LENGTH = 20;
    final int MAX_DESCRIPTION_LENGTH = 30;
    final int MAX_AUTHORS_NAME_LENGTH = 20;
    final int MAX_PATHOLOGY_LENGTH = 20;
    private final ScenarioService scenarioService;
    private final ScenarioImportService scenarioImportService;
    private final ZipExportService zipExportService;
    private final AzioneChiaveService azioneChiaveService;
    private final PazienteT0Service pazienteT0Service;
    private final EsameFisicoService esameFisicoService;
    private final EsameRefertoService esameRefertoService;
    private final AdvancedScenarioService advancedScenarioService;
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    private final ScenarioDeletionService scenarioDeletionService;
    private final MaterialeService materialeService;
    private final Grid<Scenario> scenariosGrid = new Grid<>();
    private final FileStorageService fileStorageService;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private ProgressBar progressBar;
    private HorizontalLayout paginationControls;
    private int currentPage = 0;
    private Span pageInfo;
    private ComboBox<String> searchPatientType;
    private TextField searchTitolo;
    private TextField searchAutori;
    private ComboBox<String> searchTipo;
    private TextField searchPatologia;
    private Button resetButton;
    private List<Scenario> allScenarios = new ArrayList<>();
    private List<Scenario> filteredScenarios = new ArrayList<>();

    @Autowired
    public ScenariosListView(ScenarioService scenarioService,
                             ZipExportService zipExportService,
                             FileStorageService fileStorageService,
                             ScenarioImportService scenarioImportService,
                             AzioneChiaveService azioneChiaveService,
                             PazienteT0Service pazienteT0Service,
                             EsameFisicoService esameFisicoService,
                             EsameRefertoService esameRefertoService,
                             AdvancedScenarioService advancedScenarioService,
                             PatientSimulatedScenarioService patientSimulatedScenarioService,
                             ScenarioDeletionService scenarioDeletionService,
                             MaterialeService materialeService) {
        this.scenarioService = scenarioService;
        this.zipExportService = zipExportService;
        this.fileStorageService = fileStorageService;
        this.scenarioImportService = scenarioImportService;
        this.azioneChiaveService = azioneChiaveService;
        this.pazienteT0Service = pazienteT0Service;
        this.esameFisicoService = esameFisicoService;
        this.esameRefertoService = esameRefertoService;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        this.scenarioDeletionService = scenarioDeletionService;
        this.materialeService = materialeService;
        this.detached = new AtomicBoolean(false);
        initView();
        loadData();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        detached.set(true);
        executorService.shutdownNow();
    }

    private void initView() {
        @SuppressWarnings("unused") VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        Button newScenarioButton = StyleApp.getButton("Nuovo scenario", VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "--lumo-primary-color");
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);
        customHeader.add(newScenarioButton);

        configureSearchFilters();
        HorizontalLayout filterLayout = new HorizontalLayout(searchPatientType, searchTitolo, searchTipo, searchAutori, searchPatologia, resetButton);
        filterLayout.setWidthFull();
        filterLayout.setPadding(true);
        filterLayout.setSpacing(true);
        filterLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        configureGrid();
        configurePagination();

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        contentLayout.add(progressBar, filterLayout, scenariosGrid, paginationControls);

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(null);

        getContent().add(customHeader, contentLayout, footerLayout);

        backButton.addClickListener(e -> {
            if (!detached.get()) {
                getUI().ifPresent(ui -> ui.navigate(""));
            }
        });

        newScenarioButton.addClickListener(e -> {
            if (!detached.get()) {
                DialogSupport.showZipUploadDialog(detached, executorService, scenarioImportService, this::loadData);
            }
        });
    }

    /**
     * Configura i campi per la ricerca/filtraggio degli scenari.
     */
    private void configureSearchFilters() {
        List<String> allPatientTypes = List.of("Tutti", "Adulto", "Pediatrico", "Neonatale", "Prematuro");
        searchPatientType = FieldGenerator.createComboBox(
                "Filtra per Tipo Paziente",
                allPatientTypes,
                "Tutti",
                false
        );
        searchPatientType.setClearButtonVisible(true);
        searchPatientType.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        searchTitolo = FieldGenerator.createTextField(
                "Filtra per Titolo",
                "Cerca titolo...",
                false
        );
        searchTitolo.setClearButtonVisible(true);
        searchTitolo.setValueChangeMode(ValueChangeMode.LAZY);
        searchTitolo.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        searchAutori = FieldGenerator.createTextField(
                "Filtra per Autori",
                "Cerca autori...",
                false
        );
        searchAutori.setClearButtonVisible(true);
        searchAutori.setValueChangeMode(ValueChangeMode.LAZY);
        searchAutori.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        List<String> allTypes = List.of("Tutti", "Patient Simulated Scenario", "Advanced Scenario", "Quick Scenario");
        searchTipo = FieldGenerator.createComboBox(
                "Filtra per Tipo",
                allTypes,
                "Tutti",
                false
        );
        searchTipo.setClearButtonVisible(true);
        searchTipo.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        searchPatologia = FieldGenerator.createTextField(
                "Filtra per Patologia",
                "Cerca patologia...",
                false
        );
        searchPatologia.setClearButtonVisible(true);
        searchPatologia.setValueChangeMode(ValueChangeMode.LAZY);
        searchPatologia.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        resetButton = new Button("Reset Filtri", new Icon(VaadinIcon.CLOSE_CIRCLE_O));
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.getStyle().set("margin-top", "auto");
        resetButton.addClickListener(e -> {
            searchPatientType.setValue("Tutti");
            searchTitolo.clear();
            searchAutori.clear();
            searchTipo.setValue("Tutti");
            searchPatologia.clear();
            applyFiltersAndRefreshGrid();
        });
    }

    /**
     * Applica i filtri correnti alla lista completa degli scenari
     * e aggiorna la griglia e la paginazione.
     */
    private void applyFiltersAndRefreshGrid() {
        if (detached.get()) return;

        String tipologiaPatientFilter = searchPatientType.getValue() != null ? searchPatientType.getValue() : "Tutti";
        String titoloFilter = searchTitolo.getValue().trim().toLowerCase();
        String autoriFilter = searchAutori.getValue().trim().toLowerCase();
        String tipoFilter = searchTipo.getValue() != null ? searchTipo.getValue() : "Tutti";
        String patologiaFilter = searchPatologia.getValue().trim().toLowerCase();

        filteredScenarios = allScenarios.stream()
                .filter(scenario -> {
                    if ("Tutti".equals(tipologiaPatientFilter)) {
                        return true;
                    }
                    String actualPatientType = scenario.getTipologia();
                    return actualPatientType != null && actualPatientType.equalsIgnoreCase(tipologiaPatientFilter);
                })
                .filter(scenario -> titoloFilter.isEmpty() || (scenario.getTitolo() != null && scenario.getTitolo().toLowerCase().contains(titoloFilter)))
                .filter(scenario -> autoriFilter.isEmpty() || (scenario.getAutori() != null && scenario.getAutori().toLowerCase().contains(autoriFilter)))
                .filter(scenario -> {
                    if ("Tutti".equals(tipoFilter)) {
                        return true;
                    }
                    String actualScenarioType = scenarioService.getScenarioType(scenario.getId());
                    return actualScenarioType != null && actualScenarioType.equalsIgnoreCase(tipoFilter);
                })
                .filter(scenario -> patologiaFilter.isEmpty() || (scenario.getPatologia() != null && scenario.getPatologia().toLowerCase().contains(patologiaFilter)))
                .collect(Collectors.toList());

        currentPage = 0;
        updateGridItems();
        updatePaginationInfo();
    }

    private void configureGrid() {
        scenariosGrid.setWidthFull();
        scenariosGrid.addClassName(LumoUtility.BorderRadius.MEDIUM);
        scenariosGrid.addClassName(LumoUtility.BoxShadow.SMALL);
        scenariosGrid.getStyle().set("min-height", "400px");

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String patientType = scenario.getTipologia();
                    if (patientType == null) {
                        patientType = "Unknown";
                    }

                    Span span;
                    Icon icon;
                    String title;
                    String colorClass;

                    switch (patientType) {
                        case "Adulto":
                            icon = FontAwesome.Solid.USER.create();
                            title = "Paziente adulto";
                            colorClass = LumoUtility.TextColor.PRIMARY;
                            break;
                        case "Pediatrico":
                            icon = FontAwesome.Solid.CHILD.create();
                            title = "Paziente pediatrico";
                            colorClass = LumoUtility.TextColor.SUCCESS;
                            break;
                        case "Neonatale":
                            icon = FontAwesome.Solid.BABY.create();
                            title = "Paziente neonatale";
                            colorClass = LumoUtility.TextColor.WARNING;
                            break;
                        case "Prematuro":
                            icon = FontAwesome.Solid.HANDS_HOLDING_CHILD.create();
                            title = "Paziente prematuro";
                            colorClass = LumoUtility.TextColor.ERROR;
                            break;
                        default:
                            icon = FontAwesome.Solid.INFO_CIRCLE.create();
                            title = "Tipo paziente non specificato";
                            colorClass = LumoUtility.TextColor.TERTIARY;
                            break;
                    }

                    span = new Span(icon);

                    span.addClassName(colorClass);

                    span.getElement().setAttribute("title", title);

                    icon.setSize("24px");

                    return span;
                }))
                .setHeader("Tipo Paziente")
                .setFlexGrow(0)
                .setComparator(Comparator.comparing(Scenario::getTipologia, Comparator.nullsLast(String::compareToIgnoreCase)));


        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String titolo = scenario.getTitolo() != null ? scenario.getTitolo() : "";
                    String displayTitolo = titolo.length() > MAX_TITLE_LENGTH ? titolo.substring(0, MAX_TITLE_LENGTH) + "..." : titolo;
                    Span titoloSpan = new Span(displayTitolo);
                    if (!titolo.isEmpty()) {
                        titoloSpan.getElement().setAttribute("title", titolo);
                    }
                    return titoloSpan;
                }))
                .setHeader("Titolo")
                .setSortable(true)
                .setFlexGrow(1)
                .setComparator(Comparator.comparing(Scenario::getTitolo, Comparator.nullsLast(String::compareToIgnoreCase)));

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String tipo = scenarioService.getScenarioType(scenario.getId());
                    tipo = tipo != null ? tipo : "N/D";

                    HorizontalLayout container = new HorizontalLayout();
                    container.setSpacing(true);
                    container.setPadding(false);
                    container.setAlignItems(FlexComponent.Alignment.CENTER);

                    Icon icon;
                    Span tipoSpan = new Span(tipo);
                    String iconColorClass = LumoUtility.TextColor.SECONDARY;
                    String fontWeightClass = LumoUtility.FontWeight.NORMAL;

                    switch (tipo.toLowerCase()) {
                        case "patient simulated scenario":
                            icon = FontAwesome.Solid.USER_INJURED.create();
                            icon.setSize("24px");
                            iconColorClass = LumoUtility.TextColor.ERROR;
                            fontWeightClass = LumoUtility.FontWeight.SEMIBOLD;
                            tipoSpan.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontWeight.SEMIBOLD);
                            break;
                        case "advanced scenario":
                            icon = VaadinIcon.CLOCK.create();
                            icon.setSize("20px");
                            iconColorClass = LumoUtility.TextColor.SUCCESS;
                            fontWeightClass = LumoUtility.FontWeight.SEMIBOLD;
                            tipoSpan.addClassNames(LumoUtility.TextColor.SUCCESS, LumoUtility.FontWeight.SEMIBOLD);
                            break;
                        case "quick scenario":
                            icon = VaadinIcon.BOLT.create();
                            icon.setSize("20px");
                            iconColorClass = LumoUtility.TextColor.PRIMARY;
                            fontWeightClass = LumoUtility.FontWeight.SEMIBOLD;
                            tipoSpan.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontWeight.SEMIBOLD);
                            break;
                        default:
                            icon = VaadinIcon.QUESTION.create();
                            tipoSpan.addClassName(LumoUtility.TextColor.SECONDARY);
                            break;
                    }

                    icon.addClassName(iconColorClass);

                    if (!tipoSpan.getClassNames().contains(LumoUtility.FontWeight.SEMIBOLD) &&
                            !tipoSpan.getClassNames().contains(LumoUtility.FontWeight.NORMAL) &&
                            !fontWeightClass.equals(LumoUtility.FontWeight.NORMAL)) {
                        tipoSpan.addClassName(fontWeightClass);
                    }

                    container.add(icon, tipoSpan);
                    return container;
                }))
                .setHeader("Tipo")
                .setSortable(true)
                .setFlexGrow(2)
                .setComparator(Comparator.comparing(s -> scenarioService.getScenarioType(s.getId()), Comparator.nullsLast(String::compareToIgnoreCase)));

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String autori = scenario.getAutori() != null ? scenario.getAutori() : "";
                    String displayAutori = autori.length() > MAX_AUTHORS_NAME_LENGTH ? autori.substring(0, MAX_AUTHORS_NAME_LENGTH) + "..." : autori;
                    Span autoriSpan = new Span(displayAutori);
                    if (!autori.isEmpty()) {
                        autoriSpan.getElement().setAttribute("title", autori);
                    }
                    return autoriSpan;
                }))
                .setHeader("Autori")
                .setSortable(true)
                .setFlexGrow(1)
                .setComparator(Comparator.comparing(Scenario::getAutori, Comparator.nullsLast(String::compareToIgnoreCase)));

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String patologia = scenario.getPatologia() != null ? scenario.getPatologia() : "";
                    String displayPatologia = patologia.length() > MAX_PATHOLOGY_LENGTH ? patologia.substring(0, MAX_PATHOLOGY_LENGTH) + "..." : patologia;
                    Span patoSpan = new Span(displayPatologia);
                    if (!patologia.isEmpty()) {
                        patoSpan.getElement().setAttribute("title", patologia);
                    }
                    return patoSpan;
                }))
                .setHeader("Patologia")
                .setSortable(true)
                .setFlexGrow(1)
                .setComparator(Comparator.comparing(Scenario::getPatologia, Comparator.nullsLast(String::compareToIgnoreCase)));

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String descrizione = scenario.getDescrizione() != null ? scenario.getDescrizione() : "";
                    Div container = new Div();
                    container.setWidthFull();
                    container.getStyle()
                            .set("max-width", "200px")
                            .set("overflow", "hidden")
                            .set("text-overflow", "ellipsis")
                            .set("white-space", "nowrap");
                    if (descrizione.isEmpty()) return container;
                    String plainText = Jsoup.parse(descrizione).text();
                    container.setText(plainText.length() > MAX_DESCRIPTION_LENGTH ? plainText.substring(0, MAX_DESCRIPTION_LENGTH) + "..." : plainText);
                    container.getElement().setAttribute("title", plainText);
                    return container;
                }))
                .setHeader("Descrizione")
                .setFlexGrow(1);


        scenariosGrid.addComponentColumn(scenario -> {
                    HorizontalLayout actions = new HorizontalLayout();
                    actions.setSpacing(false);
                    actions.setMargin(false);
                    actions.setPadding(false);
                    actions.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

                    Button pdfButton = new Button(FontAwesome.Regular.FILE_PDF.create());
                    pdfButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
                    pdfButton.getElement().setAttribute("title", "Esporta in PDF");
                    pdfButton.getStyle().set("margin-right", "var(--lumo-space-xs)");
                    pdfButton.addClickListener(e -> {
                        if (!detached.get()) exportToPdf(scenario);
                    });

                    Button simButton = new Button(FontAwesome.Solid.FILE_EXPORT.create());
                    simButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
                    simButton.getElement().setAttribute("title", "Esporta in .zip (sim.execution)");
                    simButton.getStyle().set("margin-right", "var(--lumo-space-xs)");
                    simButton.addClickListener(e -> {
                        if (!detached.get()) exportToSimExecution(scenario);
                    });

                    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
                    deleteButton.getElement().setAttribute("title", "Elimina scenario");
                    deleteButton.addClickListener(e -> {
                        if (!detached.get()) confirmAndDeleteScenario(scenario);
                    });

                    actions.add(pdfButton, simButton, deleteButton);
                    return actions;
                }).setHeader("Azioni")
                .setFlexGrow(0)
                .setWidth("120px");


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
        paginationControls.setAlignItems(FlexComponent.Alignment.CENTER);
        paginationControls.setSpacing(true);
        paginationControls.setPadding(true);

        Button firstPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT));
        Button prevPageButton = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        pageInfo = new Span("Pagina -");
        Button nextPageButton = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        Button lastPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));

        firstPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        prevPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        nextPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        lastPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        pageInfo.getStyle().set("margin", "0 var(--lumo-space-s)");

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
                int totalPages = (int) Math.ceil((double) filteredScenarios.size() / PAGE_SIZE);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    updateGridItems();
                    updatePaginationInfo();
                }
            }
        });
        lastPageButton.addClickListener(e -> {
            if (!detached.get()) {
                int totalPages = Math.max(1, (int) Math.ceil((double) filteredScenarios.size() / PAGE_SIZE));
                if (currentPage < totalPages - 1) {
                    currentPage = totalPages - 1;
                    updateGridItems();
                    updatePaginationInfo();
                }
            }
        });

        paginationControls.add(firstPageButton, prevPageButton, pageInfo, nextPageButton, lastPageButton);
        paginationControls.setVisible(false);
    }

    private void updatePaginationInfo() {
        if (detached.get()) return;
        int totalItems = filteredScenarios.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));

        if (totalItems == 0) {
            pageInfo.setText("Nessun risultato");
        } else {
            pageInfo.setText("Pagina " + (currentPage + 1) + " di " + totalPages);
        }

        getUI().ifPresent(ui -> ui.access(() -> {
            boolean hasPrev = currentPage > 0;
            boolean hasNext = currentPage < totalPages - 1;
            ((Button) paginationControls.getComponentAt(0)).setEnabled(hasPrev);
            ((Button) paginationControls.getComponentAt(1)).setEnabled(hasPrev);
            ((Button) paginationControls.getComponentAt(3)).setEnabled(hasNext);
            ((Button) paginationControls.getComponentAt(4)).setEnabled(hasNext);
            paginationControls.setVisible(true);
        }));
    }

    private void updateGridItems() {
        if (detached.get()) return;
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredScenarios.size());

        if (fromIndex < filteredScenarios.size() && fromIndex < toIndex) {
            scenariosGrid.setItems(new ListDataProvider<>(filteredScenarios.subList(fromIndex, toIndex)));
        } else {
            scenariosGrid.setItems(new ArrayList<>());
        }
        getUI().ifPresent(ui -> ui.access(() -> scenariosGrid.setVisible(true)));
    }

    private void exportToPdf(Scenario scenario) {
        if (detached.get() || scenario == null) return;
        String scenarioType = scenarioService.getScenarioType(scenario.getId());

        if (!"Advanced Scenario".equals(scenarioType) && !"Patient Simulated Scenario".equals(scenarioType)) {
            executeExport(scenario, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, zipExportService);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setHeaderTitle("Seleziona elementi per il PDF");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        Checkbox descChk = new Checkbox("Descrizione", true);
        Checkbox briefingChk = new Checkbox("Briefing", true);
        Checkbox infoGenChk = new Checkbox("Informazioni dai genitori", true);
        Checkbox pattoChk = new Checkbox("Patto d'aula", true);
        Checkbox azioniChk = new Checkbox("Azioni chiave", true);
        Checkbox obiettiviChk = new Checkbox("Obiettivi didattici", true);
        Checkbox moulageChk = new Checkbox("Moulage", true);
        Checkbox liquidiChk = new Checkbox("Liquidi e farmaci in T0", true);
        Checkbox matNecChk = new Checkbox("Materiale necessario", true);
        Checkbox paramChk = new Checkbox("Parametri vitali", true);
        Checkbox accessiChk = new Checkbox("Accessi", true);
        Checkbox esameFisicoChk = new Checkbox("Esame fisico", true);
        Checkbox esamiERefertiChk = new Checkbox("Esami e referti", true);
        Checkbox timelineChk = new Checkbox("Timeline", true);

        var scenarioNew = scenarioService.getScenarioById(scenario.getId());
        if (scenarioNew.getDescrizione() == null || scenarioNew.getDescrizione().isEmpty()) {
            descChk.setEnabled(false);
            descChk.setValue(false);
        }
        if (scenarioNew.getBriefing() == null || scenarioNew.getBriefing().isEmpty()) {
            briefingChk.setEnabled(false);
            briefingChk.setValue(false);
        }

        var patologia = scenarioNew.getPatologia();
        if (patologia == null || patologia.isEmpty()) {
            pattoChk.setEnabled(false);
            pattoChk.setValue(false);
        }

        var obiettivo = scenarioNew.getObiettivo();
        if (obiettivo == null || obiettivo.isEmpty()) {
            obiettiviChk.setEnabled(false);
            obiettiviChk.setValue(false);
        }

        var infoGenitore = scenarioNew.getInfoGenitore();
        if (infoGenitore == null || infoGenitore.isEmpty()) {
            infoGenChk.setEnabled(false);
            infoGenChk.setValue(false);
        }

        var pattoAula = scenarioNew.getPattoAula();
        if (pattoAula == null || pattoAula.isEmpty()) {
            pattoChk.setEnabled(false);
            pattoChk.setValue(false);
        }

        var moulageVal = scenarioNew.getMoulage();
        if (moulageVal == null || moulageVal.isEmpty()) {
            moulageChk.setEnabled(false);
            moulageChk.setValue(false);
        }

        var liquidiVal = scenarioNew.getLiquidi();
        if (liquidiVal == null || liquidiVal.isEmpty()) {
            liquidiChk.setEnabled(false);
            liquidiChk.setValue(false);
        }

        var azioniChiave = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioNew.getId());
        if (azioniChiave == null || azioniChiave.isEmpty()) {
            azioniChk.setEnabled(false);
            azioniChk.setValue(false);
        }

        var esameFisico = esameFisicoService.getEsameFisicoById(scenario.getId());
        if (esameFisico != null) {
            boolean allSectionsEmpty = esameFisico.getSections().values().stream()
                    .allMatch(value -> value == null || value.trim().isEmpty());
            if (allSectionsEmpty) {
                esameFisicoChk.setEnabled(false);
                esameFisicoChk.setValue(false);
            }
        } else {
            esameFisicoChk.setEnabled(false);
            esameFisicoChk.setValue(false);
        }


        var pazienteT0 = pazienteT0Service.getPazienteT0ById(scenario.getId());
        if (pazienteT0 == null) {
            paramChk.setEnabled(false);
            paramChk.setValue(false);
            accessiChk.setEnabled(false);
            accessiChk.setValue(false);
        } else {
            var accessiVenosi = pazienteT0.getAccessiVenosi();
            var accessiArteriosi = pazienteT0.getAccessiArteriosi();
            boolean hasAccessi = (accessiVenosi != null && !accessiVenosi.isEmpty()) ||
                    (accessiArteriosi != null && !accessiArteriosi.isEmpty());
            if (!hasAccessi) {
                accessiChk.setEnabled(false);
                accessiChk.setValue(false);
            }
        }
        var esamiReferti = esameRefertoService.getEsamiRefertiByScenarioId(scenario.getId());
        if (esamiReferti == null || esamiReferti.isEmpty()) {
            esamiERefertiChk.setEnabled(false);
            esamiERefertiChk.setValue(false);
        }

        var tempi = advancedScenarioService.getTempiByScenarioId(scenario.getId());
        if (tempi == null || tempi.isEmpty()) {
            timelineChk.setEnabled(false);
            timelineChk.setValue(false);
        }

        var materialeNecessario = materialeService.getMaterialiByScenarioId(scenario.getId());
        if (materialeNecessario == null || materialeNecessario.isEmpty()) {
            matNecChk.setEnabled(false);
            matNecChk.setValue(false);
        }

        layout.add(descChk, briefingChk, infoGenChk, pattoChk, azioniChk, obiettiviChk,
                moulageChk, liquidiChk, matNecChk, paramChk, accessiChk, esameFisicoChk,
                esamiERefertiChk, timelineChk);

        Checkbox sceneggiaturaChk = new Checkbox("Sceneggiatura", true);
        if ("Patient Simulated Scenario".equals(scenarioType)) {
            var sceneggiatura = patientSimulatedScenarioService.getPatientSimulatedScenarioById(scenario.getId()).getSceneggiatura();
            if (sceneggiatura == null || sceneggiatura.isEmpty()) {
                sceneggiaturaChk.setEnabled(false);
                sceneggiaturaChk.setValue(false);
            }
            layout.add(sceneggiaturaChk);
        }
        dialog.add(layout);

        Button confirmButton = new Button("Genera PDF", e -> {
            dialog.close();
            executeExport(scenario,
                    descChk.getValue(), pattoChk.getValue(), infoGenChk.getValue(),
                    pattoChk.getValue(), azioniChk.getValue(), obiettiviChk.getValue(),
                    moulageChk.getValue(), liquidiChk.getValue(), matNecChk.getValue(),
                    paramChk.getValue(), accessiChk.getValue(), esameFisicoChk.getValue(),
                    esamiERefertiChk.getValue(), timelineChk.getValue(), sceneggiaturaChk.getValue(), zipExportService);

        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Annulla", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(cancelButton, confirmButton);

        dialog.open();
    }

    private void executeExport(Scenario scenario, boolean frontPage, boolean objectives, boolean generalInfo,
                               boolean briefing, boolean signs, boolean xray, boolean tests, boolean ecg,
                               boolean algorithms, boolean timeline, boolean debriefing, boolean medications,
                               boolean equipment, boolean config, boolean scen,
                               ZipExportService zipExportService) {
        Notification.show("Generazione del PDF...", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        try {
            StreamResource resource = new StreamResource(
                    "Pdf_scenario_" + limitLength(sanitizeFileName(scenario.getTitolo())) + ".zip",
                    () -> {
                        try {
                            byte[] pdfBytes = zipExportService.exportScenarioPdfToZip(
                                    scenario.getId(), frontPage, objectives, generalInfo, briefing,
                                    signs, xray, tests, ecg, algorithms, timeline, debriefing,
                                    medications, equipment, config, scen);
                            return new ByteArrayInputStream(pdfBytes);
                        } catch (IOException | RuntimeException e) {
                            System.err.println("Errore generazione PDF per lo scenario " + scenario.getId() + ": " + e.getMessage());
                            getUI().ifPresent(ui -> ui.access(() ->
                                    Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE)
                                            .addThemeVariants(NotificationVariant.LUMO_ERROR)));
                            return new ByteArrayInputStream(new byte[0]);
                        }
                    }
            );
            resource.setContentType("application/zip");
            triggerDownload(resource);
        } catch (Exception e) {
            handleExportError("Errore preparazione PDF", e);
        }
    }

    private void exportToSimExecution(Scenario scenario) {
        if (detached.get() || scenario == null) return;
        Notification.show("Generazione dell'archivio ZIP...", 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        try {
            StreamResource resource = new StreamResource(
                    "Execution_scenario_" + limitLength(sanitizeFileName(scenario.getTitolo())) + ".zip",
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
            triggerDownload(resource);
        } catch (Exception e) {
            handleExportError("Errore preparazione ZIP", e);
        }
    }

    private void triggerDownload(StreamResource resource) {
        UI ui = UI.getCurrent();
        if (ui == null || detached.get() || ui.isClosing()) return;

        Button downloadButton = new Button();
        downloadButton.getStyle().set("display", "none");

        FileDownloadWrapper downloadWrapper = new FileDownloadWrapper(resource);
        downloadWrapper.wrapComponent(downloadButton);

        ui.access(() -> {
            this.getContent().add(downloadWrapper);
            downloadButton.getElement().executeJs("this.click()")
                    .then(result -> ui.access(() -> {
                        if (!detached.get()) {
                            this.getContent().remove(downloadWrapper);
                        }
                    }));
        });
    }

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
     * Mostra un Dialog di conferma per l'eliminazione.
     */
    private void confirmAndDeleteScenario(Scenario scenario) {
        if (detached.get() || scenario == null) return;

        Dialog confirmDialog = new Dialog();
        confirmDialog.setCloseOnEsc(false);
        confirmDialog.setCloseOnOutsideClick(false);
        confirmDialog.setHeaderTitle("Conferma Eliminazione");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(new Paragraph("Sei sicuro di voler eliminare lo scenario:"));
        Span scenarioTitleSpan = new Span("\"" + scenario.getTitolo() + "\"?");
        scenarioTitleSpan.getStyle().set("font-weight", "bold");
        dialogLayout.add(scenarioTitleSpan);
        dialogLayout.add(new Paragraph("L'azione non è reversibile."));
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        confirmDialog.add(dialogLayout);

        Button confirmButton = new Button("Elimina", event -> {
            if (!detached.get()) {
                String scenarioName = scenario.getTitolo() != null ? scenario.getTitolo() : "Scenario";
                boolean deleted = scenarioDeletionService.deleteScenario(scenario.getId());
                getUI().ifPresent(ui -> ui.access(() -> {
                    if (deleted) {
                        Notification.show("Scenario " + scenarioName + " eliminato.", 3000, Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                        loadData();
                    } else {
                        Notification.show("Errore durante l'eliminazione.", 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }));
            }
            confirmDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", event -> confirmDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.open();
    }

    public void loadData() {
        if (detached.get()) return;
        UI ui = UI.getCurrent();
        if (ui == null) return;

        ui.access(() -> {
            progressBar.setVisible(true);
            scenariosGrid.setVisible(false);
            paginationControls.setVisible(false);
            searchPatientType.setEnabled(false);
            searchTitolo.setEnabled(false);
            searchAutori.setEnabled(false);
            searchTipo.setEnabled(false);
            searchPatologia.setEnabled(false);
            resetButton.setEnabled(false);
        });

        executorService.submit(() -> {
            try {
                List<Scenario> fetchedScenarios = scenarioService.getAllScenarios();
                allScenarios = (fetchedScenarios != null) ? new ArrayList<>(fetchedScenarios) : new ArrayList<>();
                allScenarios.sort(Comparator.comparing(Scenario::getId, Comparator.nullsLast(Comparator.reverseOrder())));

                if (detached.get() || ui.isClosing()) return;
                ui.access(() -> {
                    try {
                        applyFiltersAndRefreshGrid();
                    } finally {
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true);
                        paginationControls.setVisible(true);
                        searchPatientType.setEnabled(true);
                        searchTitolo.setEnabled(true);
                        searchAutori.setEnabled(true);
                        searchTipo.setEnabled(true);
                        searchPatologia.setEnabled(true);
                        resetButton.setEnabled(true);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading scenarios: " + e.getMessage());
                if (!detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        Notification.show("Errore caricamento dati: " + e.getMessage(), 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true);
                        paginationControls.setVisible(true);
                        searchPatientType.setEnabled(true);
                        searchTitolo.setEnabled(true);
                        searchAutori.setEnabled(true);
                        searchTipo.setEnabled(true);
                        searchPatologia.setEnabled(true);
                        resetButton.setEnabled(true);
                        allScenarios.clear();
                        filteredScenarios.clear();
                        updateGridItems();
                        updatePaginationInfo();
                    });
                }
            }
        });
    }

    private String limitLength(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= 30) {
            return text;
        }
        return text.substring(0, 30);
    }
}