package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.EsameFisico;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Vista per la gestione dell'esame fisico del paziente nello scenario di simulazione.
 * <p>
 * Permette di inserire e modificare i risultati dell'esame fisico divisi per sezioni anatomiche.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Esame Fisico")
@Route(value = "esameFisico")
@Menu(order = 13)
public class EsamefisicoView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private final Map<String, TextArea> examSections = new HashMap<>();

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public EsamefisicoView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
        setupView();
    }

    /**
     * Configura la struttura principale della vista.
     */
    private void setupView() {
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER
        setupHeader(mainLayout);

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

        setupTitle(contentLayout);
        setupExamSections(contentLayout);

        // 3. FOOTER
        setupFooter(mainLayout, contentLayout);
    }

    /**
     * Configura l'header della vista con pulsante indietro e titolo.
     *
     * @param mainLayout layout principale a cui aggiungere l'header
     */
    private void setupHeader(VerticalLayout mainLayout) {
        AppHeader header = new AppHeader();

        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);
        customHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("pazienteT0/" + scenarioId)));

        mainLayout.add(customHeader);
    }

    /**
     * Configura il titolo della vista.
     *
     * @param contentLayout layout a cui aggiungere il titolo
     */
    private void setupTitle(VerticalLayout contentLayout) {
        H2 pageTitle = new H2("ESAME FISICO");
        pageTitle.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.LARGE,
                LumoUtility.Margin.Horizontal.AUTO
        );
        pageTitle.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("color", "var(--lumo-primary-text-color)");

        contentLayout.add(pageTitle);
    }

    /**
     * Configura le sezioni dell'esame fisico.
     *
     * @param contentLayout layout a cui aggiungere le sezioni
     */
    private void setupExamSections(VerticalLayout contentLayout) {
        VerticalLayout examSectionsLayout = new VerticalLayout();
        examSectionsLayout.setWidthFull();
        examSectionsLayout.setSpacing(true);
        examSectionsLayout.setPadding(false);

        // Definizione delle sezioni dell'esame fisico
        String[][] sections = {
                {"Generale", "Stato generale, livello di coscienza, etc."},
                {"Pupille", "Dimensione, reattività, simmetria"},
                {"Collo", "Esame del collo, tiroide, linfonodi"},
                {"Torace", "Ispezione, palpazione, percussione, auscultazione"},
                {"Cuore", "Frequenza, ritmo, soffi"},
                {"Addome", "Ispezione, palpazione, dolorabilità, organomegalie"},
                {"Retto", "Esame rettale se indicato"},
                {"Cute", "Colorito, turgore, lesioni"},
                {"Estremità", "Edemi, pulsazioni periferiche"},
                {"Neurologico", "Stato mentale, nervi cranici, forza, sensibilità"},
                {"FAST", "Focused Assessment with Sonography for Trauma"}
        };

        // Creazione delle aree di testo per ogni sezione
        for (String[] section : sections) {
            TextArea area = createExamSection(section[0], section[1]);
            examSections.put(section[0], area);
            examSectionsLayout.add(area);
        }

        contentLayout.add(examSectionsLayout);
    }

    /**
     * Configura il footer con pulsante avanti e crediti.
     *
     * @param mainLayout    layout principale
     * @param contentLayout layout del contenuto
     */
    private void setupFooter(VerticalLayout mainLayout, VerticalLayout contentLayout) {
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

        nextButton.addClickListener(e -> saveExamAndNavigate(nextButton.getUI()));

        mainLayout.add(contentLayout, footerLayout);
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
            if (scenarioId <= 0) {
                throw new NumberFormatException();
            }

            loadExistingExamData();
        } catch (NumberFormatException e) {
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
        }
    }

    /**
     * Crea un'area di testo per una sezione specifica dell'esame fisico.
     *
     * @param title       titolo della sezione
     * @param placeholder testo descrittivo della sezione
     * @return TextArea configurata
     */
    private TextArea createExamSection(String title, String placeholder) {
        TextArea area = new TextArea(title);
        area.setPlaceholder(placeholder);
        area.setWidthFull();
        area.setMinHeight("120px");
        area.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return area;
    }

    /**
     * Carica i dati dell'esame fisico esistente per lo scenario corrente.
     */
    private void loadExistingExamData() {
        EsameFisico esameFisico = scenarioService.getEsameFisicoById(scenarioId);

        if (esameFisico != null) {
            Map<String, String> savedSections = esameFisico.getSections();
            savedSections.forEach((sectionName, value) -> {
                TextArea textArea = examSections.get(sectionName);
                if (textArea != null) {
                    textArea.setValue(value != null ? value : "");
                }
            });
        } else {
            examSections.values().forEach(textArea -> textArea.setValue(""));
        }
    }

    /**
     * Salva l'esame fisico e naviga alla vista successiva in base al tipo di scenario.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveExamAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                Map<String, String> examData = new HashMap<>();
                examSections.forEach((section, area) -> examData.put(section, area.getValue()));

                boolean success = scenarioService.addEsameFisico(
                        scenarioId, examData
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (!success) {
                        Notification.show("Errore durante il salvataggio dell'esame fisico",
                                3000, Notification.Position.MIDDLE);
                        return;
                    }

                    // Navigazione differenziata in base al tipo di scenario
                    String scenarioType = scenarioService.getScenarioType(scenarioId);
                    if ("Quick Scenario".equals(scenarioType)) {
                        ui.navigate("scenari/" + scenarioId);
                    } else if ("Advanced Scenario".equals(scenarioType) ||
                            "Patient Simulated Scenario".equals(scenarioType)) {
                        ui.navigate("tempo/" + scenarioId);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE);
                    e.printStackTrace();
                });
            }
        });
    }
}