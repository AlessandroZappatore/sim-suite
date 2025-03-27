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
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@PageTitle("Esame Fisico")
@Route(value = "esamefisico")
@Menu(order = 13)
public class EsamefisicoView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private Map<String, TextArea> examSections = new HashMap<>();

    public EsamefisicoView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;

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
        customHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

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

        // Titolo della pagina
        H2 pageTitle = new H2("ESAME FISICO");
        pageTitle.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.LARGE,
                LumoUtility.Margin.Horizontal.AUTO
        );
        pageTitle.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("color", "var(--lumo-primary-text-color)");

        // Creazione delle sezioni di esame
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

        // Aggiungi tutte le sezioni richieste
        for (String[] section : sections) {
            TextArea area = createExamSection(section[0], section[1]);
            examSections.put(section[0], area);
            examSectionsLayout.add(area);
        }

        contentLayout.add(pageTitle, examSectionsLayout);

        // 3. FOOTER
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

        // Aggiunta dei layout principali
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per i pulsanti
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("pazienteT0/" + scenarioId)));

        nextButton.addClickListener(e -> {
            saveExamAndNavigate(nextButton.getUI());
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

            loadExistingExamData();
        } catch (NumberFormatException e) {
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
        }
    }

    private TextArea createExamSection(String title, String placeholder) {
        TextArea area = new TextArea(title);
        area.setPlaceholder(placeholder);
        area.setWidthFull();
        area.setMinHeight("120px");
        area.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return area;
    }

    private void loadExistingExamData() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null) {
            EsameFisico esameFisico = scenario.getEsameFisico();

            if (esameFisico != null) {
                // Get all sections from the exam
                Map<String, String> savedSections = esameFisico.getSections();

                // Update only the UI fields that exist in our view
                savedSections.forEach((sectionName, value) -> {
                    TextArea textArea = examSections.get(sectionName);
                    if (textArea != null) {
                        textArea.setValue(value != null ? value : "");
                    }
                });

                // Initialize any missing sections with empty values
                examSections.keySet().forEach(sectionName -> {
                    if (!savedSections.containsKey(sectionName)) {
                        examSections.get(sectionName).setValue("");
                    }
                });
            } else {
                // Initialize all fields with empty values if no exam exists
                examSections.values().forEach(textArea -> textArea.setValue(""));
            }
        }
    }

    private void saveExamAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                Map<String, String> examData = new HashMap<>();
                examSections.forEach((section, area) -> {
                    examData.put(section, area.getValue());
                });

                boolean success = scenarioService.addEsameFisico(
                        scenarioId, examData
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("parametri/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio dell'esame fisico",
                                3000, Notification.Position.MIDDLE);
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