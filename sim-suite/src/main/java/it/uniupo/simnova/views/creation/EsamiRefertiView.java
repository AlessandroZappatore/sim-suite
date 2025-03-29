package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                            row.getTypeSelect().getValue(),
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
        private final Select<String> typeSelect;
        private final Upload upload;
        private final TextField reportField;

        public FormRow(int rowNumber) {
            this.rowNumber = rowNumber;

            // Titolo della riga
            this.rowTitle = new Paragraph("Esame/Referto #" + rowNumber);
            rowTitle.addClassName(LumoUtility.FontWeight.BOLD);
            rowTitle.addClassName(LumoUtility.Margin.Bottom.NONE);

            // Componenti del form
            this.typeSelect = new Select<>();
            typeSelect.setLabel("Tipo Esame");
            typeSelect.setItems("Emocromo", "Radiografia", "TAC", "RMN", "Ecografia");
            typeSelect.setWidthFull();
            typeSelect.setEmptySelectionAllowed(false); // Sostituisce setRequired(true)

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
            rowLayout.add(typeSelect, upload, reportField);
            rowLayout.setResponsiveSteps(
                    new ResponsiveStep("0", 1),
                    new ResponsiveStep("600px", 2),
                    new ResponsiveStep("900px", 3)
            );
        }

        // Getters
        public int getRowNumber() { return rowNumber; }
        public Paragraph getRowTitle() { return rowTitle; }
        public FormLayout getRowLayout() { return rowLayout; }
        public Select<String> getTypeSelect() { return typeSelect; }
        public Upload getUpload() { return upload; }
        public TextField getReportField() { return reportField; }
    }

    // Classe per rappresentare i dati di un esame/referto
        public record EsameRefertoData(int idEsame, String tipo, String refertoTestuale, String media) {
    }
}