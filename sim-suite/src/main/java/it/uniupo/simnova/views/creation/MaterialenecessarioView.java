package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.Materiale;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.MaterialeService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.home.CreditsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vista per la gestione del materiale necessario nello scenario di simulazione.
 * <p>
 * Permette di selezionare materiali esistenti o aggiungerne di nuovi per l'allestimento della sala.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 2.0
 */
@PageTitle("Materiale Necessario")
@Route(value = "materialenecessario")
@Menu(order = 8)
public class MaterialenecessarioView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(MaterialenecessarioView.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per la gestione dei materiali.
     */
    private final MaterialeService materialeService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Grid che mostra i materiali disponibili.
     */
    private final Grid<Materiale> materialiDisponibiliGrid;
    /**
     * Grid che mostra i materiali selezionati.
     */
    private final Grid<Materiale> materialiSelezionatiGrid;
    /**
     * Lista di materiali selezionati.
     */
    private final List<Materiale> materialiSelezionati = new ArrayList<>();
    /**
     * Lista di tutti i materiali disponibili.
     */
    private List<Materiale> tuttiMateriali = new ArrayList<>();

    private TextField searchField;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService    servizio per la gestione degli scenari
     * @param materialeService   servizio per la gestione dei materiali
     * @param fileStorageService servizio per la gestione dei file
     */
    public MaterialenecessarioView(ScenarioService scenarioService, MaterialeService materialeService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;
        this.materialeService = materialeService;

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER
        AppHeader header = new AppHeader(fileStorageService);

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
        contentLayout.setMaxWidth("1000px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        H2 title = new H2("MATERIALE NECESSARIO");
        title.addClassName(LumoUtility.Margin.Bottom.NONE);
        title.getStyle().set("text-align", "center");
        title.setWidthFull();

        // Istruzioni
        Paragraph instructions = new Paragraph("Seleziona i materiali necessari per l'allestimento della sala o aggiungine di nuovi");
        instructions.addClassName(LumoUtility.TextColor.SECONDARY);
        instructions.addClassName(LumoUtility.FontSize.MEDIUM);
        instructions.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        // Grid per i materiali disponibili
        materialiDisponibiliGrid = new Grid<>();
        materialiDisponibiliGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        materialiDisponibiliGrid.setHeight("250px");
        materialiDisponibiliGrid.addColumn(Materiale::getNome).setHeader("Materiale disponibile").setAutoWidth(true).setFlexGrow(1);
        materialiDisponibiliGrid.addColumn(Materiale::getDescrizione).setHeader("Descrizione").setAutoWidth(true).setFlexGrow(2);
        materialiDisponibiliGrid.addColumn(
                new ComponentRenderer<>(materiale -> {
                    // Creiamo un container div che conterrà il bottone o niente
                    Div buttonContainer = new Div();

                    // Verifichiamo se il materiale è già selezionato
                    boolean isSelected = materialiSelezionati.contains(materiale);

                    if (!isSelected) {
                        Button addButton = new Button(new Icon(VaadinIcon.PLUS));
                        addButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                        addButton.addClickListener(e -> {
                            if (!materialiSelezionati.contains(materiale)) {
                                materialiSelezionati.add(materiale);
                                aggiornaGrids(); // Aggiorno entrambe le griglie per nascondere il pulsante
                            }
                        });
                        buttonContainer.add(addButton);
                    }

                    return buttonContainer;
                })
        ).setHeader("Aggiungi").setAutoWidth(true).setFlexGrow(0);
        materialiDisponibiliGrid.addColumn(
                new ComponentRenderer<>(materiale -> {
                    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                    deleteButton.addClickListener(e -> showDeleteConfirmDialog(materiale));
                    return deleteButton;
                })
        ).setHeader("Elimina").setAutoWidth(true).setFlexGrow(0);
        // Grid per i materiali selezionati
        materialiSelezionatiGrid = new Grid<>();
        materialiSelezionatiGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        materialiSelezionatiGrid.setHeight("250px");
        materialiSelezionatiGrid.addColumn(Materiale::getNome).setHeader("Materiale selezionato").setAutoWidth(true).setFlexGrow(1);
        materialiSelezionatiGrid.addColumn(Materiale::getDescrizione).setHeader("Descrizione").setAutoWidth(true).setFlexGrow(2);
        materialiSelezionatiGrid.addColumn(
                new ComponentRenderer<>(materiale -> {
                    Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
                    removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                    removeButton.addClickListener(e -> {
                        materialiSelezionati.remove(materiale);
                        aggiornaGrids();
                    });
                    return removeButton;
                })
        ).setHeader("Rimuovi").setAutoWidth(true).setFlexGrow(0);
        // Bottone per aggiungere nuovo materiale
        Button addNewMaterialButton = new Button("Aggiungi nuovo materiale", new Icon(VaadinIcon.PLUS));
        addNewMaterialButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addNewMaterialButton.addClickListener(e -> showNuovoMaterialeDialog());

        // Layout per le due griglie
        HorizontalLayout gridsLayout = new HorizontalLayout();
        gridsLayout.setWidthFull();
        gridsLayout.setSpacing(true);

        VerticalLayout disponibiliLayout = new VerticalLayout();
        disponibiliLayout.setWidthFull();
        disponibiliLayout.setSpacing(false);
        disponibiliLayout.setPadding(false);
        setupMaterialiDisponibiliLayout(disponibiliLayout);

        VerticalLayout selezionatiLayout = new VerticalLayout();
        selezionatiLayout.setWidthFull();
        selezionatiLayout.setSpacing(false);
        selezionatiLayout.setPadding(false);
        selezionatiLayout.add(new Paragraph("Materiali selezionati:"), materialiSelezionatiGrid);

        gridsLayout.add(disponibiliLayout, selezionatiLayout);

        contentLayout.add(title,instructions, gridsLayout);

        // 3. FOOTER con pulsanti e crediti
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle().set("border-color", "var(--lumo-contrast-10pct)");

        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px");

        CreditsComponent credits = new CreditsComponent();

        footerLayout.add(credits, nextButton);

        // Aggiunta di tutti i componenti al layout principale
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        // Listener per i pulsanti
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("obiettivididattici/" + scenarioId)));

        nextButton.addClickListener(e -> saveMaterialiAndNavigate(nextButton.getUI()));
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
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }

            loadData();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + parameter + " non valido");
        }
    }

    /**
     * Carica i materiali esistenti e quelli già selezionati.
     */
    private void loadData() {
        // Carica tutti i materiali disponibili
        tuttiMateriali = materialeService.getAllMaterials();

        // Carica i materiali già associati allo scenario
        List<Materiale> materialiScenario = materialeService.getMaterialiByScenarioId(scenarioId);
        materialiSelezionati.clear();
        materialiSelezionati.addAll(materialiScenario);

        aggiornaGrids();
    }

    /**
     * Aggiorna entrambe le griglie con i dati attuali.
     */
    private void aggiornaGrids() {
        Set<Integer> idsSelezionati = materialiSelezionati.stream()
                .map(Materiale::getId)
                .collect(Collectors.toSet());

        // Filtra i materiali per mostrare solo quelli non selezionati
        List<Materiale> materialiDisponibiliNonSelezionati = tuttiMateriali.stream()
                .filter(m -> !idsSelezionati.contains(m.getId()))
                .collect(Collectors.toList());

        // Se c'è un termine di ricerca attivo, applica il filtro
        if (searchField != null && searchField.getValue() != null && !searchField.getValue().trim().isEmpty()) {
            filtraMateriali(searchField.getValue());
        } else {
            materialiDisponibiliGrid.setItems(materialiDisponibiliNonSelezionati);
        }

        materialiSelezionatiGrid.setItems(materialiSelezionati);
    }

    private void setupMaterialiDisponibiliLayout(VerticalLayout disponibiliLayout) {
        // Campo di ricerca
        searchField = new TextField();
        searchField.setPlaceholder("Cerca materiali...");
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.addValueChangeListener(e -> filtraMateriali(e.getValue()));

        // Bottone per aggiungere nuovo materiale
        Button addNewMaterialButton = new Button("Aggiungi nuovo materiale", new Icon(VaadinIcon.PLUS));
        addNewMaterialButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addNewMaterialButton.addClickListener(e -> showNuovoMaterialeDialog());

        HorizontalLayout actionsLayout = new HorizontalLayout(searchField, addNewMaterialButton);
        actionsLayout.setWidthFull();
        actionsLayout.setFlexGrow(1, searchField);

        disponibiliLayout.add(
                new Paragraph("Materiali disponibili:"),
                actionsLayout,
                materialiDisponibiliGrid
        );
    }

    // Metodo per filtrare i materiali in base alla ricerca
    private void filtraMateriali(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Se la ricerca è vuota, mostra tutti i materiali disponibili
            aggiornaGrids();
        } else {
            // Filtra per nome o descrizione contenente il termine di ricerca (case insensitive)
            String term = searchTerm.toLowerCase();

            Set<Integer> idsSelezionati = materialiSelezionati.stream()
                    .map(Materiale::getId)
                    .collect(Collectors.toSet());

            List<Materiale> materialiDisponibiliFiltrati = tuttiMateriali.stream()
                    .filter(m -> !idsSelezionati.contains(m.getId()))
                    .filter(m -> (m.getNome() != null && m.getNome().toLowerCase().contains(term)) ||
                            (m.getDescrizione() != null && m.getDescrizione().toLowerCase().contains(term)))
                    .collect(Collectors.toList());

            materialiDisponibiliGrid.setItems(materialiDisponibiliFiltrati);
        }
    }


    /**
     * Mostra un dialog per l'aggiunta di un nuovo materiale.
     */
    private void showNuovoMaterialeDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aggiungi nuovo materiale");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        TextField nomeField = new TextField("Nome");
        nomeField.setWidthFull();
        nomeField.setRequired(true);

        TextField descrizioneField = new TextField("Descrizione");
        descrizioneField.setWidthFull();

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (nomeField.getValue() == null || nomeField.getValue().trim().isEmpty()) {
                Notification.show("Inserisci un nome per il materiale", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                Materiale nuovoMateriale = new Materiale(
                        -1, // L'ID sarà generato dal database
                        nomeField.getValue(),
                        descrizioneField.getValue()
                );

                Materiale savedMateriale = materialeService.saveMateriale(nuovoMateriale);
                if (savedMateriale != null) {
                    tuttiMateriali.add(savedMateriale);
                    materialiSelezionati.add(savedMateriale);
                    aggiornaGrids();
                    dialog.close();
                    Notification.show("Materiale aggiunto con successo", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    Notification.show("Errore durante il salvataggio del materiale", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception ex) {
                logger.error("Errore durante il salvataggio del nuovo materiale", ex);
                Notification.show("Errore: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.add(cancelButton, saveButton);

        dialogLayout.add(nomeField, descrizioneField, new Div(), buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    /**
     * Salva i materiali selezionati e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveMaterialiAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                // Estrai gli ID dei materiali selezionati
                List<Integer> idsMateriali = materialiSelezionati.stream()
                        .map(Materiale::getId)
                        .collect(Collectors.toList());

                // Salva l'associazione tra scenario e materiali
                boolean success = materialeService.associaMaterialiToScenario(scenarioId, idsMateriali);

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("esamiReferti/" + scenarioId + "/create");
                    } else {
                        Notification.show("Errore durante il salvataggio dei materiali", 3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio dei materiali", e);
                });
            }
        });
    }

    // Metodo per mostrare il dialog di conferma eliminazione
    private void showDeleteConfirmDialog(Materiale materiale) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Conferma eliminazione");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        Paragraph message = new Paragraph("Sei sicuro di voler eliminare il materiale \"" + materiale.getNome() + "\"?");
        message.getStyle().set("margin-top", "0");

        Paragraph warning = new Paragraph("Questa operazione non può essere annullata e rimuoverà il materiale dal database.");
        warning.addClassName(LumoUtility.TextColor.ERROR);

        Button deleteButton = new Button("Elimina");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> {
            deleteMateriale(materiale);
            confirmDialog.close();
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.addClickListener(e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.add(cancelButton, deleteButton);

        dialogLayout.add(message, warning, buttonLayout);
        confirmDialog.add(dialogLayout);
        confirmDialog.open();
    }

    // Metodo per eliminare effettivamente il materiale
    private void deleteMateriale(Materiale materiale) {
        try {
            boolean success = materialeService.deleteMateriale(materiale.getId());
            if (success) {
                // Rimuovi dalla lista di tutti i materiali
                tuttiMateriali.removeIf(m -> m.getId().equals(materiale.getId()));

                // Se era anche nei materiali selezionati, rimuovilo
                materialiSelezionati.removeIf(m -> m.getId().equals(materiale.getId()));

                // Aggiorna le griglie
                aggiornaGrids();

                Notification.show("Materiale eliminato con successo",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Impossibile eliminare il materiale",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception ex) {
            logger.error("Errore durante l'eliminazione del materiale", ex);
            Notification.show("Errore: " + ex.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}