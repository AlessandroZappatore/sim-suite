package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import jakarta.validation.constraints.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Esami e Referti")
@Route(value = "esamiReferti")
@Menu(order = 9)
public class EsamiRefertiView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private final VerticalLayout rowsContainer;
    private int rowCount = 1;
    private final List<FormRow> formRows = new ArrayList<>();
    private final FileStorageService fileStorageService;

    public EsamiRefertiView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = new FileStorageService(); // Cartella Media

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER con pulsante indietro
        AppHeader header = new AppHeader();

        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        rowsContainer = new VerticalLayout();
        rowsContainer.setWidthFull();
        rowsContainer.setSpacing(true);

        // Pulsante per aggiungere nuove righe
        Button addButton = new Button("Aggiungi Esame/Referto", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClassName(LumoUtility.Margin.Top.MEDIUM);
        addButton.addClickListener(event -> addNewRow());

        contentLayout.add(rowsContainer, addButton);

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
                backButton.getUI().ifPresent(ui -> ui.navigate("materialenecessario/" + scenarioId)));

        nextButton.addClickListener(e -> {
            if (formRows.isEmpty()) {
                Notification.show("Aggiungi almeno un esame/referto", 3000, Notification.Position.MIDDLE);
                return;
            }
            saveEsamiRefertiAndNavigate(nextButton.getUI());
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

            // Aggiungi una riga vuota all'inizio
            addNewRow();
        } catch (NumberFormatException e) {
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
        }
    }

    private void addNewRow() {
        // Crea componenti per la nuova riga
        FormRow newRow = new FormRow(rowCount++);
        formRows.add(newRow);

        // Crea container per la riga con bordo
        VerticalLayout rowContainer = new VerticalLayout();
        rowContainer.addClassName(LumoUtility.Padding.MEDIUM);
        rowContainer.addClassName(LumoUtility.Border.ALL);
        rowContainer.addClassName(LumoUtility.BorderColor.CONTRAST_10);
        rowContainer.addClassName(LumoUtility.BorderRadius.MEDIUM);
        rowContainer.setPadding(true);
        rowContainer.setSpacing(false);
        rowContainer.add(newRow.getRowTitle(), newRow.getRowLayout());

        rowsContainer.add(rowContainer);
    }

    private void saveEsamiRefertiAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                // Prepara i dati da salvare
                List<EsameRefertoData> esamiData = new ArrayList<>();
                for (FormRow row : formRows) {
                    String fileName = "";

                    if (row.getUpload().getReceiver() instanceof MemoryBuffer buffer) {
                        if (buffer.getFileName() != null && !buffer.getFileName().isEmpty()) {
                            try (InputStream fileData = buffer.getInputStream()) {
                                fileName = fileStorageService.storeFile(fileData, buffer.getFileName());
                            }
                        }
                    }

                    EsameRefertoData data = new EsameRefertoData(
                            row.getRowNumber(),
                            row.getSelectedExam(),
                            row.getReportField().getValue(),
                            fileName
                    );
                    esamiData.add(data);
                }

                // Salva nel database
                boolean success = scenarioService.saveEsamiReferti(scenarioId, esamiData);

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("moulage/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio degli esami/referti",
                                3000, Notification.Position.MIDDLE);
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

    // Classe interna per rappresentare una riga del form
    private static class FormRow {
        private final int rowNumber;
        private final Paragraph rowTitle;
        private final FormLayout rowLayout;
        private final Button selectExamButton = new Button("Seleziona", new Icon(VaadinIcon.SEARCH));
        private final Dialog examDialog = new Dialog();
        private final TextField selectedExamField = new TextField("Tipo Esame");
        private final Upload upload;
        private final TextField reportField;
        private final List<String> allLabExams = List.of(
                "Emocromo con formula", "Glicemia", "Elettroliti sierici (Na⁺, K⁺, Cl⁻, Ca²⁺, Mg²⁺)",
                "Funzionalità renale (Creatinina, Azotemia)", "Funzionalità epatica (AST, ALT, Bilirubina, ALP, GGT)",
                "PCR (Proteina C Reattiva)", "Procalcitonina", "D-Dimero", "CK-MB, Troponina I/T",
                "INR, PTT, PT", "Gas arteriosi (pH, PaO₂, PaCO₂, HCO₃⁻, BE, Lactati)",
                "Emogas venoso", "Osmolarità sierica", "CPK", "Mioglobina"
        );
        private final List<String> allInstrExams = List.of(
                "ECG (Elettrocardiogramma)", "RX Torace", "TC Torace (con mdc)", "TC Torace (senza mdc)",
                "TC Addome (con mdc)", "TC Addome (senza mdc)", "Ecografia addominale", "Ecografia polmonare",
                "Ecocardio (Transtoracico)", "Ecocardio (Transesofageo)", "Spirometria", "EEG (Elettroencefalogramma)",
                "RM Encefalo", "TC Cranio (con mdc)", "TC Cranio (senza mdc)", "Doppler TSA (Tronchi Sovraortici)",
                "Angio-TC Polmonare", "Fundus oculi"
        );

        public FormRow(int rowNumber) {
            this.rowNumber = rowNumber;

            // Titolo della riga
            this.rowTitle = new Paragraph("Esame/Referto #" + rowNumber);
            rowTitle.addClassName(LumoUtility.FontWeight.BOLD);
            rowTitle.addClassName(LumoUtility.Margin.Bottom.NONE);

            // Configurazione campo esame selezionato
            selectedExamField.setReadOnly(true);
            selectedExamField.setWidthFull();
            selectedExamField.getElement().addEventListener("click", e -> selectExamButton.click());

            // Configurazione pulsante selezione
            selectExamButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            selectExamButton.addClassName(LumoUtility.Margin.Bottom.NONE);

            // Layout orizzontale per i campi di selezione
            HorizontalLayout selectionLayout = new HorizontalLayout(selectedExamField, selectExamButton);
            selectionLayout.setWidthFull();
            selectionLayout.setFlexGrow(1, selectedExamField);
            selectionLayout.setAlignItems(FlexComponent.Alignment.END);

            // Configurazione finestra di dialogo
            examDialog.setHeaderTitle("Seleziona Tipo Esame");
            examDialog.setWidth("600px");
            examDialog.setHeight("70vh");

            // Barra di ricerca
            TextField searchField = new TextField();
            searchField.setPlaceholder("Cerca esame...");
            searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
            searchField.setWidthFull();
            searchField.setClearButtonVisible(true);

            // Creazione delle schede (tabs) per le categorie
            Tabs categoryTabs = new Tabs();
            Tab labTab = new Tab("Laboratorio");
            Tab instrTab = new Tab("Strumentali");
            categoryTabs.add(labTab, instrTab);

            // Contenuti delle schede
            VerticalLayout labContent = createLabExamContent(allLabExams);
            VerticalLayout instrContent = createInstrumentalExamContent(allInstrExams);

            // Layout a schede
            Div pages = new Div(labContent, instrContent);
            pages.setWidthFull();

            // Listener per la ricerca
            searchField.addValueChangeListener(e -> {
                String searchTerm = e.getValue().toLowerCase();

                VerticalLayout filteredLabContent = createLabExamContent(
                        allLabExams.stream()
                                .filter(exam -> exam.toLowerCase().contains(searchTerm))
                                .collect(Collectors.toList())
                );

                VerticalLayout filteredInstrContent = createInstrumentalExamContent(
                        allInstrExams.stream()
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
                    pages.add(createLabExamContent(allLabExams));
                } else {
                    pages.add(createInstrumentalExamContent(allInstrExams));
                }
            });

            // Pulsante per chiudere
            Button closeButton = new Button("Chiudi", e -> examDialog.close());
            examDialog.getFooter().add(closeButton);

            // Aggiunta dei componenti alla finestra
            examDialog.add(searchField, categoryTabs, pages);

            // Listener per il pulsante di selezione
            selectExamButton.addClickListener(e -> examDialog.open());

            // Upload file
            MemoryBuffer buffer = new MemoryBuffer();
            this.upload = new Upload(buffer);
            upload.setDropAllowed(true);
            upload.setWidthFull();
            upload.setAcceptedFileTypes(".pdf", ".jpg", ".png", ".gif", ".mp4", ".mp3");
            upload.setMaxFiles(1);

            this.reportField = new TextField("Referto Testuale");
            reportField.setWidthFull();

            // Configura il layout della riga
            this.rowLayout = new FormLayout();
            rowLayout.setWidthFull();
            rowLayout.add(selectionLayout, upload, reportField);
            rowLayout.setResponsiveSteps(
                    new ResponsiveStep("0", 1),
                    new ResponsiveStep("600px", 2),
                    new ResponsiveStep("900px", 3)
            );
        }

        private VerticalLayout createLabExamContent(List<String> exams) {
            return getVerticalLayout(exams);
        }

        @NotNull
        private VerticalLayout getVerticalLayout(List<String> exams) {
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);
            layout.setWidthFull();

            for (String exam : exams) {
                Button examButton = createExamButton(exam);
                layout.add(examButton);
            }

            return layout;
        }

        private VerticalLayout createInstrumentalExamContent(List<String> exams) {
            return getVerticalLayout(exams);
        }

        private Button createExamButton(String examName) {
            Button button = new Button(examName, e -> {
                selectedExamField.setValue(examName);
                examDialog.close();
            });
            button.setWidthFull();
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            button.getStyle().set("text-align", "left");
            button.getStyle().set("padding-left", "var(--lumo-space-m)");
            button.getStyle().set("justify-content", "flex-start");
            return button;
        }

        // Metodo per ottenere il tipo di esame selezionato
        public String getSelectedExam() {
            return selectedExamField.getValue();
        }

        // Getters
        public int getRowNumber() {
            return rowNumber;
        }

        public Paragraph getRowTitle() {
            return rowTitle;
        }

        public FormLayout getRowLayout() {
            return rowLayout;
        }

        public Upload getUpload() {
            return upload;
        }

        public TextField getReportField() {
            return reportField;
        }
    }

    // Classe per rappresentare i dati di un esame/referto
    public record EsameRefertoData(int idEsame, String tipo, String refertoTestuale, String media) {
    }
}