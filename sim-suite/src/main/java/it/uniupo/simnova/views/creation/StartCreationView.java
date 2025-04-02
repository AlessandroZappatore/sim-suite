package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;

import java.util.Optional;

@PageTitle("StartCreation")
@Route("startCreation")
@Menu(order = 2)
public class StartCreationView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final ScenarioService scenarioService;
    private final TextField scenarioTitle;
    private final TextField patientName;
    private final TextField pathology;
    private final NumberField durationField;
    private String scenarioType;

    public StartCreationView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER
        AppHeader header = new AppHeader();

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
        contentLayout.setMaxWidth("800px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        // Campi del form
        scenarioTitle = createTextField("TITOLO SCENARIO", "Inserisci il titolo dello scenario");
        patientName = createTextField("NOME PAZIENTE", "Inserisci il nome del paziente");
        pathology = createTextField("PATOLOGIA/MALATTIA", "Inserisci la patologia");

        // Campo durata timer
        durationField = new NumberField("DURATA SIMULAZIONE (minuti)");
        durationField.setMin(1);
        durationField.setValue(10.0);
        durationField.setStep(1);
        durationField.setWidthFull();
        durationField.addClassName(LumoUtility.Margin.Top.LARGE);
        durationField.getStyle().set("max-width", "500px");

        contentLayout.add(
                scenarioTitle,
                patientName,
                pathology,
                durationField
        );

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
                backButton.getUI().ifPresent(ui -> ui.navigate("creation")));

        nextButton.addClickListener(e -> {
            if (validateFields()) {
                saveScenarioAndNavigate(nextButton.getUI());
            }
        });
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null) {
            this.scenarioType = parameter.toLowerCase();
        } else {
            // Default o gestione errore
            this.scenarioType = "quickScenario";
        }
    }

    private boolean validateFields() {
        if (scenarioTitle.isEmpty() || patientName.isEmpty() || pathology.isEmpty()) {
            Notification.show("Compila tutti i campi obbligatori", 3000, Notification.Position.MIDDLE);
            return false;
        }
        return true;
    }

    private void saveScenarioAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            try {
                int scenarioId = -1;

                switch (scenarioType) {
                    case "quickscenario":
                        scenarioId = scenarioService.startQuickScenario(
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                durationField.getValue().floatValue()
                        );
                        break;
                    case "advancedscenario":
                        scenarioId = scenarioService.startAdvancedScenario(
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                durationField.getValue().floatValue()
                        );
                        break;
                    case "patientsimulatedscenario":
                        scenarioId = scenarioService.startPatientSimulatedScenario(
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                durationField.getValue().floatValue()
                        );
                        break;
                    default:
                        Notification.show("Tipo di scenario non riconosciuto",
                                3000, Notification.Position.MIDDLE);
                        return;
                }

                System.out.println("ID scenario creato: " + scenarioId);

                if (scenarioId > 0) {
                    System.out.println("Navigando a descrizione/" + scenarioId);
                    ui.navigate("descrizione/" + scenarioId);
                } else {
                    Notification.show("Errore durante il salvataggio dello scenario",
                            3000, Notification.Position.MIDDLE);
                }
            } catch (Exception e) {
                Notification.show("Errore: " + e.getMessage(),
                        5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        });
    }

    private TextField createTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.getStyle().set("max-width", "500px");
        field.addClassName(LumoUtility.Margin.Top.LARGE);
        return field;
    }
}