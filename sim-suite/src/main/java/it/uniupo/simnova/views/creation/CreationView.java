package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.views.home.AppHeader;

@PageTitle("Creation")
@Route("creation")
public class CreationView extends Composite<VerticalLayout> {

    public CreationView() {
        AppHeader header = new AppHeader();

        // Creazione dei bottoni
        Button quickScenarioButton = new Button("Quick Scenario");
        Button advancedScenarioButton = new Button("Advanced Scenario");
        Button patientSimulatedScenarioButton = new Button("Patient Simulated Scenario");
        Button visualizzaScenari = new Button("Visualizza Scenari Salvati");
        Button backButton = new Button("Indietro");

        // Creazione delle spiegazioni
        Span quickScenarioTitle = new Span("Quick Scenario:");
        quickScenarioTitle.getStyle().set("font-weight", "bold");
        Div quickScenarioDescription = new Div(quickScenarioTitle, new com.vaadin.flow.component.Text(" Uno scenario veloce con un solo tempo di simulazione."));

        Span advancedScenarioTitle = new Span("Advanced Scenario:");
        advancedScenarioTitle.getStyle().set("font-weight", "bold");
        Div advancedScenarioDescription = new Div(advancedScenarioTitle, new com.vaadin.flow.component.Text(" Un quick scenario con la possibilità di aggiungere i vari tempi di simulazione con il relativo algoritmo."));

        Span patientSimulatedScenarioTitle = new Span("Patient Simulated Scenario:");
        patientSimulatedScenarioTitle.getStyle().set("font-weight", "bold");
        Div patientSimulatedScenarioDescription = new Div(patientSimulatedScenarioTitle, new com.vaadin.flow.component.Text(" Un advanced scenario con la possibilità di aggiungere una sceneggiatura."));

        // Impostazione degli stili per i pulsanti
        setButtonStyle(quickScenarioButton);
        setButtonStyle(advancedScenarioButton);
        setButtonStyle(patientSimulatedScenarioButton);
        setButtonStyle(visualizzaScenari);

        // Impostazione degli stili per le descrizioni
        setDescriptionStyle(quickScenarioDescription);
        setDescriptionStyle(advancedScenarioDescription);
        setDescriptionStyle(patientSimulatedScenarioDescription);

        // Configurazione del layout
        VerticalLayout layout = getContent();
        layout.setWidthFull();
        layout.setHeightFull();
        layout.getStyle().set("display", "flex");
        layout.getStyle().set("flex-direction", "column");
        layout.getStyle().set("justify-content", "center");
        layout.getStyle().set("align-items", "center");
        layout.getStyle().set("padding", "20px"); // Aggiunge spazio intorno al layout

        // Stile per il pulsante di ritorno
        backButton.getStyle().set("width", "10%");
        backButton.getStyle().set("height", "50px");
        backButton.getStyle().set("font-size", "16px");
        backButton.getStyle().set("margin", "20px");
        backButton.getStyle().set("align-self", "flex-start");
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        backButton.addClickListener(e -> {
            backButton.getUI().ifPresent(ui -> ui.navigate(""));
        });

        quickScenarioButton.addClickListener(e -> {
            quickScenarioButton.getUI().ifPresent(ui -> ui.navigate("startCreation"));
        });

        // Aggiunta degli elementi al layout
        layout.add(header, quickScenarioButton, quickScenarioDescription, advancedScenarioButton, advancedScenarioDescription, patientSimulatedScenarioButton, patientSimulatedScenarioDescription, visualizzaScenari, backButton);
    }

    private void setButtonStyle(Button button) {
        button.getStyle().set("width", "65%"); // Larghezza ridotta per dispositivi mobili
        button.getStyle().set("height", "150px");
        button.getStyle().set("font-size", "32px");
        button.getStyle().set("margin", "10px 0"); // Spazio sopra e sotto i pulsanti
    }

    private void setDescriptionStyle(Div description) {
        description.getStyle().set("text-align", "center");
        description.getStyle().set("margin", "16px 0"); // Spazio sopra e sotto le descrizioni
    }
}
