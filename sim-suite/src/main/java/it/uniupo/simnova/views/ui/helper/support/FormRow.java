package it.uniupo.simnova.views.ui.helper.support;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static it.uniupo.simnova.views.constant.ExamConst.ALLINSTREXAMS;
import static it.uniupo.simnova.views.constant.ExamConst.ALLLABSEXAMS;

public class FormRow {
    public static final Logger logger = LoggerFactory.getLogger(FormRow.class);

    /**
     * Numero della riga.
     */
    public final int rowNumber;
    /**
     * Titolo della riga.
     */
    public final Paragraph rowTitle;
    /**
     * Layout della riga.
     */
    public final FormLayout rowLayout;
    /**
     * Pulsante per selezionare l'esame.
     */
    public final Button selectExamButton = new Button("Seleziona", new Icon(VaadinIcon.SEARCH));
    /**
     * Finestra di dialogo per la selezione dell'esame.
     */
    public final Dialog examDialog = new Dialog();
    /**
     * Campo di testo per l'esame selezionato.
     */
    public final TextField selectedExamField = new TextField("Tipo Esame");
    /**
     * Campo di testo per l'esame personalizzato.
     */
    public final TextField customExamField = new TextField("Esame Personalizzato");
    /**
     * Gruppo di radio button per la selezione del tipo di esame.
     */
    public final RadioButtonGroup<String> examTypeGroup = new RadioButtonGroup<>();
    /**
     * Campo di upload per il file del referto.
     */
    public final Upload upload;
    /**
     * Campo di testo per il referto testuale.
     */
    public final TextField reportField;
    /**
     * Opzioni per la sorgente del media.
     */
    public final RadioButtonGroup<String> mediaSourceGroup = new RadioButtonGroup<>();
    /**
     * Pulsante per selezionare media esistente.
     */
    public final Button selectMediaButton = new Button("Seleziona da esistenti", new Icon(VaadinIcon.FOLDER_OPEN));
    /**
     * Dialog per visualizzare media esistenti.
     */
    public final Dialog mediaDialog = new Dialog();
    /**
     * Campo di testo per mostrare il media selezionato.
     */
    public final TextField selectedMediaField = new TextField("Media Selezionato");
    /**
     * Servizio per l'accesso ai file.
     */
    public final FileStorageService fileStorageService;
    /**
     * Nome del media esistente selezionato.
     */
    public String selectedExistingMedia = null;


    /**
     * Costruttore per una riga del form.
     *
     * @param rowNumber          numero della riga
     * @param fileStorageService servizio per la gestione dei file
     */
    public FormRow(int rowNumber, FileStorageService fileStorageService) {
        this.rowNumber = rowNumber;
        this.fileStorageService = fileStorageService;

        // Titolo della riga
        this.rowTitle = new Paragraph("Esame/Referto #" + rowNumber);
        rowTitle.addClassName(LumoUtility.FontWeight.BOLD);
        rowTitle.addClassName(LumoUtility.Margin.Bottom.NONE);

        // Configurazione radio button per tipo di esame
        examTypeGroup.setLabel("Tipo di inserimento");
        examTypeGroup.setItems("Seleziona da elenco", "Inserisci manualmente");
        examTypeGroup.setValue("Seleziona da elenco");
        examTypeGroup.addValueChangeListener(e -> updateExamFieldVisibility());

        // Stile per i radio button
        examTypeGroup.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-s)");

        // Configurazione campo esame selezionato
        selectedExamField.setReadOnly(true);
        selectedExamField.setWidthFull();
        selectedExamField.setPrefixComponent(new Icon(VaadinIcon.FILE_TEXT));
        selectedExamField.getElement().addEventListener("click", e -> {
            if ("Seleziona da elenco".equals(examTypeGroup.getValue())) {
                selectExamButton.click();
            }
        });

        // Configurazione campo esame personalizzato
        customExamField.setWidthFull();
        customExamField.setVisible(false);
        customExamField.setPlaceholder("Inserisci il nome dell'esame");
        customExamField.setPrefixComponent(new Icon(VaadinIcon.EDIT));

        // Configurazione pulsante selezione esame
        selectExamButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectExamButton.addClassName(LumoUtility.Margin.Bottom.NONE);
        selectExamButton.setWidth("auto");

        // Layout orizzontale per i campi di selezione esame
        HorizontalLayout selectionLayout = new HorizontalLayout(selectedExamField, selectExamButton);
        selectionLayout.setWidthFull();
        selectionLayout.setFlexGrow(1, selectedExamField);
        selectionLayout.setAlignItems(FlexComponent.Alignment.END);
        selectionLayout.setSpacing(true);

        // Configurazione finestra di dialogo per esami
        examDialog.setHeaderTitle("Seleziona Tipo Esame");
        examDialog.setWidth("600px");
        examDialog.setHeight("70vh");
        examDialog.setDraggable(true);
        examDialog.setResizable(true);

        // Configurazione per la selezione di media esistenti
        mediaSourceGroup.setLabel("Sorgente del media");
        mediaSourceGroup.setItems("Carica nuovo file", "Seleziona da esistenti");
        mediaSourceGroup.setValue("Carica nuovo file");
        mediaSourceGroup.addValueChangeListener(e -> updateMediaFieldVisibility());

        // Configurazione campo media selezionato
        selectedMediaField.setReadOnly(true);
        selectedMediaField.setWidthFull();
        selectedMediaField.setPrefixComponent(new Icon(VaadinIcon.FILE));
        selectedMediaField.setVisible(false);

        // Configurazione pulsante selezione media
        selectMediaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectMediaButton.setWidth("auto");
        selectMediaButton.setVisible(false);

        // Layout per la selezione del media esistente
        HorizontalLayout mediaSelectionLayout = new HorizontalLayout(selectedMediaField, selectMediaButton);
        mediaSelectionLayout.setWidthFull();
        mediaSelectionLayout.setFlexGrow(1, selectedMediaField);
        mediaSelectionLayout.setAlignItems(FlexComponent.Alignment.END);
        mediaSelectionLayout.setSpacing(true);

        // Configurazione dialog per media esistenti
        mediaDialog.setHeaderTitle("Seleziona Media");
        mediaDialog.setWidth("600px");
        mediaDialog.setDraggable(true);
        mediaDialog.setResizable(true);

        // Configurazione dialog per selezione media
        configureMediaDialog();

        // Listener per il pulsante di selezione media
        selectMediaButton.addClickListener(e -> mediaDialog.open());

        // Configurazione ricerca esami
        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca esame...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.addClassName(LumoUtility.Margin.Bottom.SMALL);

        // Creazione delle schede per le categorie di esami
        Tabs categoryTabs = new Tabs();
        Tab labTab = new Tab("Laboratorio");
        Tab instrTab = new Tab("Strumentali");
        categoryTabs.add(labTab, instrTab);
        categoryTabs.setWidthFull();

        // Stile per le tabs
        categoryTabs.getStyle()
                .set("margin-bottom", "0")
                .set("box-shadow", "0 -1px 0 0 var(--lumo-contrast-10pct) inset");

        // Contenuti delle schede
        VerticalLayout labContent = createLabExamContent(ALLLABSEXAMS);
        VerticalLayout instrContent = createInstrumentalExamContent(ALLINSTREXAMS);

        // Layout a schede
        Div pages = new Div(labContent, instrContent);
        pages.setWidthFull();
        pages.getStyle().set("overflow-y", "auto");
        pages.getStyle().set("max-height", "calc(70vh - 150px)");

        // Listener per la ricerca di esami
        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue().toLowerCase();

            VerticalLayout filteredLabContent = createLabExamContent(
                    ALLLABSEXAMS.stream()
                            .filter(exam -> exam.toLowerCase().contains(searchTerm))
                            .collect(Collectors.toList())
            );

            VerticalLayout filteredInstrContent = createInstrumentalExamContent(
                    ALLINSTREXAMS.stream()
                            .filter(exam -> exam.toLowerCase().contains(searchTerm))
                            .collect(Collectors.toList())
            );

            pages.removeAll();
            if (categoryTabs.getSelectedTab() == labTab) {
                pages.add(filteredLabContent);
            } else {
                pages.add(filteredInstrContent);
            }
        });

        categoryTabs.addSelectedChangeListener(event -> {
            pages.removeAll();
            if (event.getSelectedTab() == labTab) {
                pages.add(createLabExamContent(ALLLABSEXAMS));
            } else {
                pages.add(createInstrumentalExamContent(ALLINSTREXAMS));
            }
        });

        // Pulsante per chiudere dialog esami
        Button closeButton = new Button("Chiudi", e -> examDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        examDialog.getFooter().add(closeButton);

        // Aggiunta dei componenti alla finestra di esami
        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(false);
        dialogContent.add(searchField, categoryTabs, pages);
        examDialog.add(dialogContent);

        // Listener per il pulsante di selezione esame
        selectExamButton.addClickListener(e -> {
            if ("Seleziona da elenco".equals(examTypeGroup.getValue())) {
                examDialog.open();
            }
        });

        // Upload file
        MemoryBuffer buffer = new MemoryBuffer();
        this.upload = new Upload(buffer);
        upload.setDropAllowed(true);
        upload.setWidthFull();
        upload.setAcceptedFileTypes(".pdf", ".jpg", "jpeg", ".png", ".gif", ".mp4", ".mp3", ".webp");
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button("Carica File", new Icon(VaadinIcon.UPLOAD)));
        upload.setDropLabel(new Div(new Text("Trascina file qui o clicca per selezionare")));

        // Campo referto testuale
        this.reportField = new TextField("Referto Testuale");
        reportField.setWidthFull();
        reportField.setPrefixComponent(new Icon(VaadinIcon.COMMENT));
        reportField.setPlaceholder("Inserisci il referto dell'esame...");

        // Configura il layout della riga
        this.rowLayout = new FormLayout();
        rowLayout.setWidthFull();
        rowLayout.add(examTypeGroup, 2);
        rowLayout.add(selectionLayout, 2);
        rowLayout.add(customExamField, 2);
        rowLayout.add(mediaSourceGroup, 2);
        rowLayout.add(upload, 2);
        rowLayout.add(mediaSelectionLayout, 2);
        rowLayout.add(reportField, 2);
        rowLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        // Spaziatura
        rowLayout.getChildren().forEach(component ->
                component.getElement().getStyle().set("margin-bottom", "var(--lumo-space-s)"));

        // Imposta la visibilità iniziale
        updateMediaFieldVisibility();
    }

    /**
     * Aggiorna la visibilità dei campi in base al tipo di inserimento selezionato.
     */
    public void updateExamFieldVisibility() {
        boolean isCustom = "Inserisci manualmente".equals(examTypeGroup.getValue());
        selectedExamField.setVisible(!isCustom);
        selectExamButton.setVisible(!isCustom);
        customExamField.setVisible(isCustom);

        if (isCustom) {
            selectedExamField.clear();
        } else {
            customExamField.clear();
        }
    }

    /**
     * Aggiorna la visibilità dei campi relativi al media in base all'opzione selezionata.
     */
    public void updateMediaFieldVisibility() {
        boolean isNewUpload = "Carica nuovo file".equals(mediaSourceGroup.getValue());
        upload.setVisible(isNewUpload);
        selectedMediaField.setVisible(!isNewUpload);
        selectMediaButton.setVisible(!isNewUpload);

        // Se si cambia modalità, resetta l'altra opzione
        if (isNewUpload) {
            selectedMediaField.clear();
            selectedExistingMedia = null;
        } else {
            // Reset dell'upload quando si passa alla selezione
            upload.getElement().executeJs("this.files = []");
        }
    }

    /**
     * Configura il dialog per la selezione dei media esistenti.
     */
    public void configureMediaDialog() {
        // Barra di ricerca
        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca media...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.addClassName(LumoUtility.Margin.Bottom.SMALL);

        // Componente per visualizzare i media
        Div mediaContent = new Div();
        mediaContent.setWidthFull();
        mediaContent.getStyle()
                .set("overflow-y", "auto")
                .set("padding", "var(--lumo-space-m)")
                .set("max-height", "400px");

        // Pulsante per chiudere
        Button closeButton = new Button("Chiudi", e -> mediaDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        mediaDialog.getFooter().add(closeButton);

        // Ottieni e visualizza tutti i media disponibili
        loadAvailableMedia(mediaContent);

        // Aggiunta dei componenti alla finestra
        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(false);
        dialogContent.add(searchField, mediaContent);
        dialogContent.setSizeFull();

        // Imposta una larghezza maggiore per il dialog
        mediaDialog.setWidth("700px");
        mediaDialog.setHeight("600px");
        mediaDialog.add(dialogContent);

        // Listener per la ricerca
        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue().toLowerCase();
            loadAvailableMedia(mediaContent, searchTerm);
        });
    }

    /**
     * Carica i media disponibili nel dialog di selezione.
     *
     * @param container contenitore per i media
     */
    public void loadAvailableMedia(Div container) {
        loadAvailableMedia(container, null);
    }

    /**
     * Carica i media disponibili nel dialog di selezione con filtro di ricerca.
     *
     * @param container  contenitore per i media
     * @param searchTerm termine di ricerca (opzionale)
     */
    public void loadAvailableMedia(Div container, String searchTerm) {
        container.removeAll();

        // Ottieni i media disponibili dal FileStorageService
        List<String> availableMedia = getAvailableMedia();

        // Filtra in base alla ricerca se necessario
        if (searchTerm != null && !searchTerm.isEmpty()) {
            availableMedia = availableMedia.stream()
                    .filter(media -> media.toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
        }

        // Se non ci sono media disponibili
        if (availableMedia.isEmpty()) {
            Paragraph noResults = new Paragraph("Nessun media trovato");
            noResults.addClassName(LumoUtility.TextColor.SECONDARY);
            container.add(noResults);
            return;
        }

        // Crea un layout a griglia per visualizzare i media
        Div mediaGrid = new Div();
        mediaGrid.setWidthFull();
        mediaGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(150px, 1fr))")
                .set("grid-gap", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-s)");

        // Per ogni media disponibile
        for (String media : availableMedia) {
            // Crea un componente per il media
            VerticalLayout mediaItem = new VerticalLayout();
            mediaItem.setPadding(false);
            mediaItem.setSpacing(false);
            mediaItem.setWidth("100%");
            mediaItem.getStyle()
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("border", "1px solid var(--lumo-contrast-10pct)")
                    .set("cursor", "pointer")
                    .set("transition", "all 0.2s ease-in-out")
                    .set("overflow", "hidden");

            // Componente per l'anteprima del media
            Component mediaPreview;
            String mediaLower = media.toLowerCase();

            if (mediaLower.endsWith(".jpg") || mediaLower.endsWith(".jpeg") ||
                    mediaLower.endsWith(".png") || mediaLower.endsWith(".gif") ||
                    mediaLower.endsWith(".webp")) {

                // Crea un'immagine per l'anteprima
                Image image = getImage(media);
                image.getStyle()
                        .set("object-fit", "contain")
                        .set("background-color", "var(--lumo-contrast-5pct)");

                mediaPreview = image;
            } else {
                // Per i file non visualizzabili, usa un'icona
                Icon mediaIcon = getMediaIcon(media);
                mediaIcon.setSize("48px");
                mediaIcon.getStyle().set("margin", "var(--lumo-space-m) auto");

                Div iconContainer = new Div(mediaIcon);
                iconContainer.setWidth("100%");
                iconContainer.setHeight("100px");
                iconContainer.getStyle()
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("background-color", "var(--lumo-contrast-5pct)");

                mediaPreview = iconContainer;
            }

            // Etichetta con il nome del file, troncata se troppo lunga
            Paragraph mediaName = new Paragraph(media);
            mediaName.getStyle()
                    .set("margin", "0")
                    .set("padding", "var(--lumo-space-xs)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("white-space", "nowrap")
                    .set("overflow", "hidden")
                    .set("text-overflow", "ellipsis")
                    .set("text-align", "center")
                    .set("background-color", "var(--lumo-base-color)")
                    .set("width", "100%");

            // Tooltip per mostrare il nome completo al passaggio del mouse
            mediaName.getElement().setAttribute("title", media);

            mediaItem.add(mediaPreview, mediaName);

            // Azione al click
            mediaItem.addClickListener(e -> {
                selectedExistingMedia = media;
                selectedMediaField.setValue(media);
                mediaDialog.close();
            });

            // Effetto hover
            mediaItem.getElement().addEventListener("mouseover", e ->
                    mediaItem.getStyle().set("box-shadow", "0 0 5px var(--lumo-primary-color-50pct)"));
            mediaItem.getElement().addEventListener("mouseout", e ->
                    mediaItem.getStyle().set("box-shadow", "none"));

            mediaGrid.add(mediaItem);
        }

        container.add(mediaGrid);
    }

    public Image getImage(String media) {
        StreamResource resource = new StreamResource(media, () -> {
            try {
                return fileStorageService.readFile(media);
            } catch (Exception e) {
                logger.error("Errore nel caricamento dell'anteprima per {}", media, e);
                return null;
            }
        });

        Image image = new Image(resource, "Anteprima");
        image.setWidth("100%");
        image.setHeight("100px");
        return image;
    }

    /**
     * Determina l'icona appropriata in base al tipo di file.
     *
     * @param filename nome del file
     * @return icona corrispondente
     */
    public Icon getMediaIcon(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
                filename.endsWith(".png") || filename.endsWith(".gif") ||
                filename.endsWith(".webp")) {
            return new Icon(VaadinIcon.PICTURE);
        } else if (filename.endsWith(".pdf")) {
            return new Icon(VaadinIcon.FILE);
        } else if (filename.endsWith(".mp4") || filename.endsWith(".webm") ||
                filename.endsWith(".mov")) {
            return new Icon(VaadinIcon.FILM);
        } else if (filename.endsWith(".mp3") || filename.endsWith(".wav") ||
                filename.endsWith(".ogg")) {
            return new Icon(VaadinIcon.HEADPHONES);
        } else {
            return new Icon(VaadinIcon.FILE);
        }
    }

    /**
     * Ottiene la lista dei media disponibili dal servizio di storage.
     *
     * @return lista dei media disponibili
     */
    public List<String> getAvailableMedia() {
        return fileStorageService.getAllFiles();
    }

    /**
     * Crea il contenuto per gli esami di laboratorio.
     *
     * @param exams lista degli esami da visualizzare
     * @return layout con i pulsanti degli esami
     */
    public VerticalLayout createLabExamContent(List<String> exams) {
        return createExamContent(exams);
    }

    /**
     * Crea il contenuto per gli esami strumentali.
     *
     * @param exams lista degli esami da visualizzare
     * @return layout con i pulsanti degli esami
     */
    public VerticalLayout createInstrumentalExamContent(List<String> exams) {
        return createExamContent(exams);
    }

    /**
     * Crea il layout con i pulsanti per la selezione degli esami.
     *
     * @param exams lista degli esami da visualizzare
     * @return layout configurato
     */
    public VerticalLayout createExamContent(List<String> exams) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();

        if (exams.isEmpty()) {
            Paragraph noResults = new Paragraph("Nessun risultato trovato");
            noResults.addClassName(LumoUtility.TextColor.SECONDARY);
            noResults.getStyle().set("padding", "var(--lumo-space-m)");
            layout.add(noResults);
        } else {
            for (String exam : exams) {
                Button examButton = createExamButton(exam);
                layout.add(examButton);
            }
        }

        return layout;
    }

    /**
     * Crea un pulsante per la selezione di un esame specifico.
     *
     * @param examName nome dell'esame
     * @return pulsante configurato
     */
    public Button createExamButton(String examName) {
        Button button = new Button(examName, e -> {
            selectedExamField.setValue(examName);
            examDialog.close();
        });
        button.setWidthFull();
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.getStyle()
                .set("text-align", "left")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("justify-content", "flex-start")
                .set("border-radius", "var(--lumo-border-radius-m)");

        button.addClickListener(e -> {
            selectedExamField.setValue(examName);
            examDialog.close();
        });

        return button;
    }


    /**
     * Restituisce il nome dell'esame selezionato.
     *
     * @return nome dell'esame selezionato
     */
    public String getSelectedExam() {
        return "Inserisci manualmente".equals(examTypeGroup.getValue())
                ? customExamField.getValue()
                : selectedExamField.getValue();
    }

    /**
     * Restituisce il media selezionato dall'elenco esistente.
     *
     * @return nome del file media selezionato
     */
    public String getSelectedMedia() {
        return selectedExistingMedia;
    }

    /**
     * Restituisce il numero della riga.
     *
     * @return numero della riga
     */
    public int getRowNumber() {
        return rowNumber;
    }

    /**
     * Restituisce il titolo della riga.
     *
     * @return titolo della riga
     */
    public Paragraph getRowTitle() {
        return rowTitle;
    }

    /**
     * Restituisce il layout della riga.
     *
     * @return layout della riga
     */
    public FormLayout getRowLayout() {
        return rowLayout;
    }

    /**
     * Restituisce il pulsante di upload.
     *
     * @return pulsante di upload
     */
    public Upload getUpload() {
        return upload;
    }

    /**
     * Restituisce il campo di testo per il referto.
     *
     * @return campo di testo per il referto
     */
    public TextField getReportField() {
        return reportField;
    }
}