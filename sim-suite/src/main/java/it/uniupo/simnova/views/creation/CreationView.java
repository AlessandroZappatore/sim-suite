package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;

/**
 * Vista principale per la creazione di nuovi scenari di simulazione.
 * <p>
 * Offre diverse opzioni per la creazione di scenari:
 * - Scenario veloce
 * - Scenario avanzato
 * - Scenario con paziente simulato
 * - Accesso alla libreria degli scenari salvati
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Creation")
@Route("creation")
public class CreationView extends Composite<VerticalLayout> {

    /**
     * Costruttore che inizializza l'interfaccia per la selezione del tipo di scenario.
     */
    public CreationView(FileStorageService fileStorageService) {
        // Header dell'applicazione
        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Creazione Scenario",
                "Seleziona il tipo di scenario da creare o visualizza gli scenari salvati",
                VaadinIcon.HAMMER,
                "var(--lumo-primary-text-color)"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Creazione dei pulsanti per i diversi tipi di scenario
        Button quickScenarioButton = createScenarioButton(
                "Quick Scenario",
                VaadinIcon.BOLT,
                "Scenario veloce con un solo tempo",
                "Scenario con un solo tempo di simulazione"
        );

        Button advancedScenarioButton = createScenarioButton(
                "Advanced Scenario",
                VaadinIcon.CLOCK,
                "Scenario più tempi",
                "Scenario con possibilità di aggiungere un algoritmo di simulazione"
        );

        Button patientSimulatedScenarioButton = createScenarioButton(
                "Patient Simulated Scenario",
                VaadinIcon.USER_HEART,
                "Advanced Scenario con sceneggiatura",
                "Scenario avanzato con possibilità di aggiungere una sceneggiatura"
        );

        Button visualizzaScenari = createScenarioButton(
                "Visualizza Scenari Salvati",
                VaadinIcon.ARCHIVE,
                "Libreria scenari",
                "Accedi alla libreria degli scenari creati"
        );

        // Configurazione pulsante indietro
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("")));

        // Gestori degli eventi per i pulsanti
        quickScenarioButton.addClickListener(e ->
                quickScenarioButton.getUI().ifPresent(ui -> ui.navigate("startCreation/quickScenario")));
        advancedScenarioButton.addClickListener(e ->
                advancedScenarioButton.getUI().ifPresent(ui -> ui.navigate("startCreation/advancedScenario")));
        patientSimulatedScenarioButton.addClickListener(e ->
                patientSimulatedScenarioButton.getUI().ifPresent(ui -> ui.navigate("startCreation/patientSimulatedScenario")));
        visualizzaScenari.addClickListener(e ->
                visualizzaScenari.getUI().ifPresent(ui -> ui.navigate("scenari")));

        // Contenitore principale
        Div contentContainer = new Div();
        contentContainer.addClassName("scenario-container");
        contentContainer.getStyle()
                .set("width", "min(90%, 800px)")
                .set("margin", "0 auto")
                .set("padding", "1rem")
                .set("height", "100%");

        // Aggiunta pulsanti al contenitore
        contentContainer.add(
                quickScenarioButton,
                advancedScenarioButton,
                patientSimulatedScenarioButton,
                visualizzaScenari
        );

        contentLayout.add(headerSection, contentContainer);

        // Configurazione layout principale
        VerticalLayout layout = getContent();
        layout.addClassName("creation-view");
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setSizeFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle().set("min-height", "100vh");
        layout.getStyle().set("position", "relative"); // Necessario per posizionamento assoluto dei figli

        // Aggiunta componenti al layout
        layout.add(customHeader, contentLayout);

        // Stili CSS personalizzati per la gestione responsive
        layout.getElement().getStyle().set("--short-desc-display", "none");
        layout.getElement().getStyle().set("--long-desc-display", "block");
        layout.getElement().getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        // Media queries per la visualizzazione responsive
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

        VerticalLayout footerLayout = new VerticalLayout();
        footerLayout.setPadding(true);
        footerLayout.setSpacing(false);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        footerLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle().set("border-color", "var(--lumo-contrast-10pct)");

        CreditsComponent credits = new CreditsComponent();
        footerLayout.add(credits);
        footerLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        layout.add(footerLayout);
    }

    /**
     * Crea un pulsante personalizzato per la selezione del tipo di scenario.
     *
     * @param title     Titolo del pulsante
     * @param icon      Icona da visualizzare
     * @param shortDesc Descrizione breve (per mobile)
     * @param longDesc  Descrizione estesa (per desktop)
     * @return Pulsante configurato
     */
    private Button createScenarioButton(String title, VaadinIcon icon, String shortDesc, String longDesc) {
        Div content = new Div();
        content.addClassName("button-content");

        // Icona
        Icon buttonIcon = icon.create();
        buttonIcon.setSize("24px");
        buttonIcon.getStyle().set("margin-right", "0.5rem");
        buttonIcon.addClassName("buttonIcon");

        // Titolo
        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "1.2rem");
        titleSpan.addClassName("titleSpan");

        // Descrizione breve (mobile)
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

        // Descrizione estesa (desktop)
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
                .set("height", "auto")
                .set("min-height", "80px");

        // Effetto hover
        button.addClickListener(e -> button.getStyle().set("transform", "translateY(2px)"));

        return button;
    }
}