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
                "Scenario veloce con un solo tempo",
                "Scenario con un solo tempo di simulazione"
        );

        Button advancedScenarioButton = createScenarioButton(
                "Advanced Scenario",
                VaadinIcon.CLOCK,
                "Scenario con algoritmo",
                "Scenario con possibilità di aggiungere un algoritmo di simulazione"
        );

        Button patientSimulatedScenarioButton = createScenarioButton(
                "Patient Simulated Scenario",
                VaadinIcon.USER_HEART,
                "Scenario con sceneggiatura",
                "Scenario avanzato con possibilità di aggiungere una sceneggiatura"
        );

        Button visualizzaScenari = createScenarioButton(
                "Visualizza Scenari Salvati",
                VaadinIcon.ARCHIVE,
                "Libreria scenari",
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
                quickScenarioButton.getUI().ifPresent(ui -> ui.navigate("startCreation/quickScenario")));
        advancedScenarioButton.addClickListener(e ->
                advancedScenarioButton.getUI().ifPresent(ui -> ui.navigate("startCreation/advancedScenario")));
        patientSimulatedScenarioButton.addClickListener(e ->
                patientSimulatedScenarioButton.getUI().ifPresent(ui -> ui.navigate("startCreation/patientSimulatedScenario")));
        visualizzaScenari.addClickListener(e ->
                visualizzaScenari.getUI().ifPresent(ui -> ui.navigate("scenarios")));
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
        layout.getStyle().set("min-height", "100vh");

        // Add components to layout
        layout.add(header, backButton, pageTitle, contentContainer);

        // Aggiungi stili CSS personalizzati
        layout.getElement().getStyle().set("--short-desc-display", "none");
        layout.getElement().getStyle().set("--long-desc-display", "block");

        // Aggiungi il CSS per le media queries
        layout.getElement().executeJs("""
                    const style = document.createElement('style');
                    style.textContent = `
                        @media (max-width: 600px) {
                            .short-desc { display: block !important; }
                            .long-desc { display: none !important; }
                        }
                        @media (min-width: 601px) {
                            .short-desc { display: none !important; }
                            .long-desc { display: block !important; }
                        }
                    `;
                    document.head.appendChild(style);
                """);
    }

    private Button createScenarioButton(String title, VaadinIcon icon, String shortDesc, String longDesc) {
        Div content = new Div();
        content.addClassName("button-content");

        // Icon
        Icon buttonIcon = icon.create();
        buttonIcon.setSize("24px");
        buttonIcon.getStyle().set("margin-right", "0.5rem");
        buttonIcon.addClassName("buttonIcon");

        // Title
        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "1.2rem");
        titleSpan.addClassName("titleSpan");

        // Short description (for mobile)
        Span shortDescSpan = new Span(shortDesc);
        shortDescSpan.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY,
                "descSpan",
                "short-desc"
        );
        shortDescSpan.getStyle()
                .set("display", "var(--short-desc-display)")
                .set("margin-top", "0.5rem")
                .set("text-align", "center");

        // Long description (for desktop)
        Span longDescSpan = new Span(longDesc);
        longDescSpan.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY,
                "descSpan",
                "long-desc"
        );
        longDescSpan.getStyle()
                .set("display", "var(--long-desc-display)")
                .set("margin-top", "0.5rem")
                .set("text-align", "center");

        content.add(buttonIcon, titleSpan, shortDescSpan, longDescSpan);

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
                .set("text-align", "center")
                .set("justify-content", "flex-start")
                .set("height", "auto") // Cambiato da 80px ad auto per adattarsi al contenuto
                .set("min-height", "80px");

        // Hover effect
        button.addClickListener(e -> button.getStyle().set("transform", "translateY(2px)"));

        return button;
    }
}