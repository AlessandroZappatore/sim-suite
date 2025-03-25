package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.views.home.AppHeader;

@PageTitle("Creation")
@Route("creation")
public class CreationView extends Composite<VerticalLayout> {

    public CreationView() {
        AppHeader header = new AppHeader();

        // Titolo della pagina
        H2 pageTitle = new H2("Creazione Scenario");
        pageTitle.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.FontSize.XXXLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.Margin.Bottom.XLARGE
        );
        pageTitle.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("text-shadow", "1px 1px 2px rgba(0,0,0,0.1)");

        // Create buttons with icons
        Button quickScenarioButton = createScenarioButton(
                "Quick Scenario",
                VaadinIcon.BOLT,
                "Uno scenario veloce con un solo tempo di simulazione"
        );

        Button advancedScenarioButton = createScenarioButton(
                "Advanced Scenario",
                VaadinIcon.CLOCK,
                "Un quick scenario con la possibilità di aggiungere i vari tempi di simulazione"
        );

        Button patientSimulatedScenarioButton = createScenarioButton(
                "Patient Simulated Scenario",
                VaadinIcon.USER_HEART,
                "Un advanced scenario con la possibilità di aggiungere una sceneggiatura"
        );

        Button visualizzaScenari = createScenarioButton(
                "Visualizza Scenari Salvati",
                VaadinIcon.ARCHIVE,
                "Accedi alla libreria degli scenari creati"
        );

        // Configure back button
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("")));
        backButton.addClassNames(
                LumoUtility.Margin.SMALL,
                LumoUtility.Margin.Left.AUTO
        );

        // Button click listeners
        quickScenarioButton.addClickListener(e ->
                quickScenarioButton.getUI().ifPresent(ui -> ui.navigate("startCreation")));

        // Main content container
        Div contentContainer = new Div();
        contentContainer.addClassName("scenario-container");
        contentContainer.getStyle()
                .set("width", "min(90%, 800px)")
                .set("margin", "0 auto")
                .set("padding", "1rem")
                .set("height", "100%");

        // Add buttons to container
        contentContainer.add(
                quickScenarioButton,
                advancedScenarioButton,
                patientSimulatedScenarioButton,
                visualizzaScenari
        );

        // Configure main layout
        VerticalLayout layout = getContent();
        layout.addClassName("creation-view");
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setSizeFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle()
                .set("background", "linear-gradient(to bottom, #f8f9fa, #e9ecef)")
                .set("min-height", "100vh");

        // Add components to layout
        layout.add(header, backButton, pageTitle, contentContainer);
    }

    private Button createScenarioButton(String title, VaadinIcon icon, String description) {
        Div content = new Div();

        // Icon
        Icon buttonIcon = icon.create();
        buttonIcon.setSize("24px");
        buttonIcon.getStyle().set("margin-right", "0.5rem");

        // Title
        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "1.2rem");

        // Description
        Span descSpan = new Span(description);
        descSpan.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
        );
        descSpan.getStyle()
                .set("display", "block")
                .set("margin-top", "0.5rem")
                .set("text-align", "left");

        content.add(buttonIcon, titleSpan, descSpan);

        Button button = new Button(content);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.setWidthFull();
        button.addClassName("scenario-button");
        button.getStyle()
                .set("padding", "1.5rem")
                .set("margin-bottom", "1.5rem")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("transition", "all 0.2s ease")
                .set("text-align", "left")
                .set("justify-content", "flex-start")
                .set("height", "80px");

        // Hover effect
        button.addClickListener(e -> button.getStyle().set("transform", "translateY(2px)"));

        return button;
    }
}