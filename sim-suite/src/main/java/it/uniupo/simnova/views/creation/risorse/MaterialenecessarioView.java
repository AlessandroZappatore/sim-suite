package it.uniupo.simnova.views.creation.risorse;

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
import it.uniupo.simnova.domain.common.Materiale;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.MaterialeService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
@Route(value = "materialeNecessario")
@Menu(order = 8)
public class MaterialenecessarioView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    // ... (campi esistenti) ...
    private static final Logger logger = LoggerFactory.getLogger(MaterialenecessarioView.class);
    private final ScenarioService scenarioService;
    private final MaterialeService materialeService;
    private Integer scenarioId;
    private final Grid<Materiale> materialiDisponibiliGrid;
    private final Grid<Materiale> materialiSelezionatiGrid;
    private final List<Materiale> materialiSelezionati = new ArrayList<>();
    private List<Materiale> tuttiMateriali = new ArrayList<>();
    private TextField searchField;
    private String mode;
    Button nextButton = StyleApp.getNextButton();


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

        // ... (Configurazione layout principale, header, ecc. come prima) ...
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // 1. HEADER
        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Materiale necessario",
                "Seleziona i materiali necessari per l'allestimento della sala o aggiungine di nuovi",
                VaadinIcon.BED,
                "var(--lumo-primary-color)"
        );

        // Grid per i materiali disponibili
        materialiDisponibiliGrid = new Grid<>();
        materialiDisponibiliGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        materialiDisponibiliGrid.setAllRowsVisible(true);
        materialiDisponibiliGrid.addColumn(Materiale::getNome).setHeader("Materiale disponibile").setFlexGrow(1); // Lascia flexGrow qui
        materialiDisponibiliGrid.addColumn(Materiale::getDescrizione).setHeader("Descrizione").setFlexGrow(2); // Lascia flexGrow qui
        materialiDisponibiliGrid.addColumn(
                        new ComponentRenderer<>(materiale -> {
                            Div buttonContainer = new Div();
                            boolean isSelected = materialiSelezionati.stream().anyMatch(m -> m.getId().equals(materiale.getId())); // Controllo più sicuro per ID
                            if (!isSelected) {
                                Button addButton = new Button(new Icon(VaadinIcon.PLUS));
                                addButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                                addButton.addClickListener(e -> {
                                    // Riconferma prima di aggiungere per evitare race conditions
                                    if (materialiSelezionati.stream().noneMatch(m -> m.getId().equals(materiale.getId()))) {
                                        materialiSelezionati.add(materiale);
                                        aggiornaGrids();
                                    }
                                });
                                buttonContainer.add(addButton);
                            }
                            return buttonContainer;
                        })
                ).setHeader("Aggiungi")
                .setWidth("90px") // Larghezza fissa
                .setFlexGrow(0); // Non deve espandersi

        materialiDisponibiliGrid.addColumn(
                        new ComponentRenderer<>(materiale -> {
                            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                            deleteButton.addClickListener(e -> showDeleteConfirmDialog(materiale));
                            return deleteButton;
                        })
                ).setHeader("Elimina")
                .setWidth("90px") // Larghezza fissa
                .setFlexGrow(0); // Non deve espandersi

        // Grid per i materiali selezionati
        materialiSelezionatiGrid = new Grid<>();
        materialiSelezionatiGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        materialiSelezionatiGrid.setAllRowsVisible(true);
        materialiSelezionatiGrid.addColumn(Materiale::getNome).setHeader("Materiale selezionato").setFlexGrow(1); // Lascia flexGrow qui
        materialiSelezionatiGrid.addColumn(Materiale::getDescrizione).setHeader("Descrizione").setFlexGrow(2); // Lascia flexGrow qui
        materialiSelezionatiGrid.addColumn(
                        new ComponentRenderer<>(materiale -> {
                            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
                            removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                            removeButton.addClickListener(e -> {
                                materialiSelezionati.removeIf(m -> m.getId().equals(materiale.getId())); // Rimuovi per ID
                                aggiornaGrids();
                            });
                            return removeButton;
                        })
                ).setHeader("Rimuovi")
                .setWidth("90px") // Larghezza fissa
                .setFlexGrow(0); // Non deve espandersi


        // Layout per le due griglie
        HorizontalLayout gridsLayout = new HorizontalLayout();
        gridsLayout.setWidthFull();
        gridsLayout.setSpacing(true);
        gridsLayout.setAlignItems(FlexComponent.Alignment.START); // Allinea le griglie all'inizio verticalmente

        VerticalLayout disponibiliLayout = new VerticalLayout();
        disponibiliLayout.setWidth("50%"); // Diamo larghezze esplicite o usa setExpandRatio
        disponibiliLayout.setSpacing(true); // Aggiungi un po' di spazio
        disponibiliLayout.setPadding(false);
        setupMaterialiDisponibiliLayout(disponibiliLayout); // Setup con search etc.

        VerticalLayout selezionatiLayout = new VerticalLayout();
        selezionatiLayout.setWidth("50%"); // Diamo larghezze esplicite o usa setExpandRatio
        selezionatiLayout.setSpacing(true); // Aggiungi un po' di spazio
        selezionatiLayout.setPadding(false);

        HorizontalLayout titleSelezionatiLayout = new HorizontalLayout(new Paragraph("Materiali selezionati:"));
        titleSelezionatiLayout.setHeight("40px"); // Stessa altezza del titolo dei disponibili
        titleSelezionatiLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSelezionatiLayout.setPadding(false);
        titleSelezionatiLayout.setMargin(false);

// Aggiunta di uno spacer che occupa lo stesso spazio del layout di ricerca/bottoni
        Div spacer = new Div();
        spacer.setHeight("52px"); // Stesso spazio che occupa actionsLayout
        spacer.getStyle().set("visibility", "hidden"); // Lo rendiamo invisibile

        selezionatiLayout.add(
                titleSelezionatiLayout,
                spacer,
                materialiSelezionatiGrid
        );
        gridsLayout.add(disponibiliLayout, selezionatiLayout);
        gridsLayout.expand(disponibiliLayout, selezionatiLayout);

        contentLayout.add(headerSection, gridsLayout);

        // 3. FOOTER (come prima)
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        // Listener (come prima)
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("obiettivididattici/" + scenarioId)));
        nextButton.addClickListener(e -> saveMaterialiAndNavigate(nextButton.getUI()));
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro mancante nell'URL.");
                throw new NumberFormatException("ID Scenario è richiesto");
            }

            // Dividi il parametro usando '/' come separatore
            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0]; // Il primo elemento è l'ID dello scenario

            // Verifica e imposta l'ID scenario
            this.scenarioId = Integer.parseInt(scenarioIdStr.trim());
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("ID Scenario non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("ID Scenario non valido");
            }

            // Imposta la modalità se presente come secondo elemento
            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);

            // Modifica la visibilità dell'header e dei crediti
            VerticalLayout mainLayout = getContent();

            // Nasconde il pulsante Indietro, l'header e i credits in modalità "edit"
            mainLayout.getChildren().forEach(component -> {
                if (component instanceof HorizontalLayout layout) {

                    // Gestione dell'header (il primo HorizontalLayout)
                    if (layout.getComponentAt(1) instanceof AppHeader) {
                        layout.setVisible(!"edit".equals(mode));
                    }

                    // Gestione del footer (l'ultimo HorizontalLayout con i credits)
                    if (layout.getComponentCount() > 1 &&
                            layout.getComponentAt(0) instanceof CreditsComponent) {
                        if ("edit".equals(mode)) {
                            logger.info("Modalità EDIT: caricamento dati esistenti per scenario {}", this.scenarioId);
                            nextButton.setText("Salva");
                            nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                            nextButton.setIcon(new Icon(VaadinIcon.CHECK));
                            layout.getComponentAt(0).setVisible(false);
                        }
                    }
                }
            });

            loadData();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido o mancante: '{}'. Errore: {}", parameter, e.getMessage());
            event.rerouteToError(NotFoundException.class, "ID scenario non valido: " + parameter);
        } catch (NotFoundException e) {
            event.rerouteToError(NotFoundException.class, e.getMessage());
        }
    }

    private void loadData() {
        try {
            tuttiMateriali = materialeService.getAllMaterials();
            List<Materiale> materialiScenario = materialeService.getMaterialiByScenarioId(scenarioId);
            materialiSelezionati.clear();
            materialiSelezionati.addAll(materialiScenario);
            aggiornaGrids();
        } catch (Exception e) {
            logger.error("Errore durante il caricamento dei dati per lo scenario {}", scenarioId, e);
            Notification.show("Errore nel caricamento dei materiali", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            // Potresti voler mostrare un messaggio di errore più persistente o reindirizzare
        }
    }

    private void aggiornaGrids() {
        // Usa gli ID per un confronto affidabile
        Set<Integer> idsSelezionati = materialiSelezionati.stream()
                .map(Materiale::getId)
                .collect(Collectors.toSet());

        List<Materiale> materialiDisponibiliNonSelezionati = tuttiMateriali.stream()
                .filter(m -> m.getId() != null && !idsSelezionati.contains(m.getId())) // Aggiunto controllo m.getId() != null
                .collect(Collectors.toList());

        // Applica filtro ricerca se attivo
        String searchTerm = (searchField != null) ? searchField.getValue() : null;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String term = searchTerm.toLowerCase().trim();
            materialiDisponibiliNonSelezionati = materialiDisponibiliNonSelezionati.stream()
                    .filter(m -> (m.getNome() != null && m.getNome().toLowerCase().contains(term)) ||
                            (m.getDescrizione() != null && m.getDescrizione().toLowerCase().contains(term)))
                    .collect(Collectors.toList());
        }

        // Imposta i dati nelle griglie (usa copie difensive se necessario, ma qui va bene)
        materialiDisponibiliGrid.setItems(materialiDisponibiliNonSelezionati);
        materialiSelezionatiGrid.setItems(new ArrayList<>(materialiSelezionati)); // Usa una copia per sicurezza
    }


    private void setupMaterialiDisponibiliLayout(VerticalLayout disponibiliLayout) {
        searchField = new TextField();
        searchField.setPlaceholder("Cerca materiali...");
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.addValueChangeListener(e -> aggiornaGrids());

        Button addNewMaterialButton = new Button("Aggiungi nuovo materiale", new Icon(VaadinIcon.PLUS));
        addNewMaterialButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addNewMaterialButton.addClickListener(e -> showNuovoMaterialeDialog());

        // Layout per ricerca e bottone nuovo materiale
        HorizontalLayout actionsLayout = new HorizontalLayout(searchField, addNewMaterialButton);
        actionsLayout.setWidthFull();
        actionsLayout.setPadding(false);
        actionsLayout.setSpacing(true);

        // Paragrafo in un layout separato con altezza fissa per garantire allineamento
        HorizontalLayout titleLayout = new HorizontalLayout(new Paragraph("Materiali disponibili:"));
        titleLayout.setHeight("40px"); // Altezza fissa per garantire allineamento
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setPadding(false);
        titleLayout.setMargin(false);

        disponibiliLayout.add(
                titleLayout,
                actionsLayout,
                materialiDisponibiliGrid
        );
        disponibiliLayout.setFlexGrow(1.0, materialiDisponibiliGrid);
    }


    // ... (showNuovoMaterialeDialog come prima) ...
    private void showNuovoMaterialeDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aggiungi nuovo materiale");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH); // Allarga i campi

        TextField nomeField = new TextField("Nome");
        // nomeField.setWidthFull(); // Non necessario con STRETCH sul layout
        nomeField.setRequiredIndicatorVisible(true); // Indica che è richiesto
        nomeField.setErrorMessage("Il nome è obbligatorio"); // Messaggio se invalido

        TextField descrizioneField = new TextField("Descrizione");
        // descrizioneField.setWidthFull(); // Non necessario con STRETCH

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            // Validazione più robusta
            if (nomeField.getValue() == null || nomeField.getValue().trim().isEmpty()) {
                nomeField.setInvalid(true); // Mostra l'errore sul campo
                // Notification.show("Inserisci un nome per il materiale", 3000, Notification.Position.MIDDLE)
                //         .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            nomeField.setInvalid(false); // Resetta stato errore

            try {
                Materiale nuovoMateriale = new Materiale(
                        -1, // ID nullo, sarà generato dal DB
                        nomeField.getValue().trim(), // Pulisci spazi
                        descrizioneField.getValue() != null ? descrizioneField.getValue().trim() : "" // Gestisci null e trim
                );

                Materiale savedMateriale = materialeService.saveMateriale(nuovoMateriale);
                if (savedMateriale != null && savedMateriale.getId() != null) { // Controlla anche l'ID di ritorno
                    tuttiMateriali.add(savedMateriale);
                    materialiSelezionati.add(savedMateriale); // Aggiungi direttamente ai selezionati
                    aggiornaGrids();
                    dialog.close();
                    Notification.show("Materiale aggiunto e selezionato", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    Notification.show("Errore: Salvataggio del materiale fallito", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception ex) {
                logger.error("Errore durante il salvataggio del nuovo materiale", ex);
                Notification.show("Errore tecnico: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        //buttonLayout.setWidthFull(); // Non serve se il VLayout fa stretch
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END); // Allinea a destra

        dialogLayout.add(nomeField, descrizioneField, buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }


    // ... (saveMaterialiAndNavigate come prima, ma aggiorna il controllo dell'ID) ...
    private void saveMaterialiAndNavigate(Optional<UI> uiOptional) {
        if (scenarioId == null) {
            Notification.show("ID Scenario non disponibile. Impossibile salvare.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        uiOptional.ifPresent(ui -> {
            // Mostra un indicatore di caricamento
            Dialog progressDialog = new Dialog();
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            progressDialog.add(new H2("Salvataggio..."), progressBar);
            progressDialog.setCloseOnEsc(false);
            progressDialog.setCloseOnOutsideClick(false);
            progressDialog.open();

            // Esegui il salvataggio in background se possibile o usa access per aggiornare l'UI
            try {
                List<Integer> idsMateriali = materialiSelezionati.stream()
                        .map(Materiale::getId)
                        .filter(Objects::nonNull) // Assicurati che non ci siano ID nulli
                        .collect(Collectors.toList());

                boolean success = materialeService.associaMaterialiToScenario(scenarioId, idsMateriali);

                ui.access(() -> { // Usa access per interagire con l'UI dal thread principale
                    progressDialog.close(); // Chiudi il dialogo di progresso
                    if (success) {
                        Notification.show("Materiali salvati.", 2000, Notification.Position.BOTTOM_START)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        // Verifica la modalità: se edit rimane nella pagina attuale, altrimenti naviga
                        if (!"edit".equals(mode)) {
                            // Naviga alla pagina successiva solo se NON è in modalità edit
                            ui.navigate("esamiReferti/" + scenarioId + "/create");
                        }
                    } else {
                        Notification.show("Errore durante il salvataggio dei materiali.", 3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                logger.error("Errore grave durante il salvataggio dei materiali per scenario {}", scenarioId, e);
                ui.access(() -> {
                    progressDialog.close();
                    Notification.show("Errore tecnico: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                });
            }
        });
    }


    // ... (showDeleteConfirmDialog e deleteMateriale come prima, ma aggiungi controllo ID in removeIf) ...
    private void showDeleteConfirmDialog(Materiale materiale) {
        if (materiale == null || materiale.getId() == null) {
            logger.warn("Tentativo di eliminare materiale nullo o senza ID.");
            return;
        }

        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Conferma eliminazione");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        Paragraph message = new Paragraph("Sei sicuro di voler eliminare definitivamente il materiale \"" + materiale.getNome() + "\"?");
        // message.getStyle().set("margin-top", "0"); // Vaadin gestisce il padding

        Paragraph warning = new Paragraph("L'operazione non può essere annullata e rimuoverà il materiale da TUTTI gli scenari che lo utilizzano.");
        warning.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontSize.SMALL);

        Button deleteButton = new Button("Elimina");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR); // Solo errore è più chiaro qui
        deleteButton.getStyle().set("margin-left", "auto"); // Spingi a destra nel layout bottoni
        deleteButton.addClickListener(e -> {
            deleteMateriale(materiale);
            confirmDialog.close();
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.addClickListener(e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, deleteButton);
        buttonLayout.setWidthFull(); // Occupa tutta la larghezza per justify
        // buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END); // Già fatto con margin-left auto

        dialogLayout.add(message, warning, buttonLayout);
        confirmDialog.add(dialogLayout);
        confirmDialog.open();
    }

    private void deleteMateriale(Materiale materiale) {
        if (materiale == null || materiale.getId() == null) return; // Check aggiunto
        Integer materialeId = materiale.getId(); // Salva l'ID

        try {
            boolean success = materialeService.deleteMateriale(materialeId);
            if (success) {
                // Rimuovi dalle liste locali usando l'ID
                tuttiMateriali.removeIf(m -> materialeId.equals(m.getId()));
                materialiSelezionati.removeIf(m -> materialeId.equals(m.getId()));

                aggiornaGrids(); // Aggiorna la UI

                Notification.show("Materiale \"" + materiale.getNome() + "\" eliminato.",
                                3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                // Potrebbe fallire per vincoli di integrità referenziale o altri motivi
                String nomeMateriale = (materiale.getNome() != null) ? materiale.getNome() : "ID " + materialeId;
                Notification.show("Impossibile eliminare il materiale \"" + nomeMateriale + "\". Potrebbe essere in uso.",
                                4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING); // Warning invece di Error se è un fallimento atteso
            }
        } catch (Exception ex) {
            String nomeMateriale = (materiale.getNome() != null) ? materiale.getNome() : "ID " + materialeId;
            logger.error("Errore durante l'eliminazione del materiale {}", nomeMateriale, ex);
            Notification.show("Errore tecnico durante l'eliminazione: " + ex.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}