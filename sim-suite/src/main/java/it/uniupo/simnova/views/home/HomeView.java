package it.uniupo.simnova.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.views.support.AppHeader;
import it.uniupo.simnova.views.support.CreditsComponent;

/**
 * Vista principale dell'applicazione SIM SUITE.
 * <p>
 * Questa classe rappresenta la homepage dell'applicazione, contenente il titolo,
 * il sottotitolo e i pulsanti principali per navigare verso le altre sezioni.
 * Implementata utilizzando Vaadin Flow e il tema Lumo.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Home")
@Route("")
public class HomeView extends Composite<VerticalLayout> {

    /**
     * Costruttore della vista Home.
     * <p>
     * Inizializza l'interfaccia utente con:
     * - Un header
     * - Titolo e sottotitolo centrati
     * - Pulsanti principali per la navigazione
     * </p>
     */
    public HomeView(FileStorageService fileStorageService) {
        // Header dell'applicazione
        AppHeader header = new AppHeader(fileStorageService);

        // Titoli
        H1 title = new H1("SIM SUITE");
        title.addClassName(LumoUtility.TextAlignment.CENTER);
        title.addClassNames(
                LumoUtility.FontSize.XXXLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.Margin.Bottom.SMALL
        );
        title.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("text-shadow", "2px 2px 4px rgba(0,0,0,0.1)");

        H3 subtitle = new H3("Piattaforma per la creazione ed esecuzione di scenari di simulazione avanzati");
        subtitle.addClassName(LumoUtility.TextAlignment.CENTER);
        subtitle.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.Bottom.XLARGE
        );

        // Container per i bottoni
        Div buttonContainer = new Div();
        buttonContainer.setWidth("min(90%, 600px)");
        buttonContainer.getStyle()
                .set("display", "grid")
                .set("gap", "1.5rem")
                .set("margin", "0 auto");

        // Bottoni principali
        Button creationButton = createMainButton("SIM CREATION", "vaadin:cogs");
        Button executionButton = createMainButton("SIM EXECUTION", "vaadin:play");

        // Aggiungi azioni ai bottoni
        creationButton.addClickListener(e ->
                creationButton.getUI().ifPresent(ui -> ui.navigate("creation")));
        executionButton.addClickListener(e ->
                executionButton.getUI().ifPresent(ui -> ui.navigate("execution")));

        buttonContainer.add(creationButton, executionButton);

        // Configurazione del layout principale
        getContent().addClassName("home-view");
        getContent().setSizeFull();
        getContent().setPadding(false);
        getContent().setSpacing(false);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
        getContent().setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getContent().getStyle().set("padding", "2rem");
        getContent().getStyle().set("position", "relative"); // Necessario per posizionamento assoluto dei figli

        CreditsComponent credits = new CreditsComponent();
        credits.getStyle()
               .set("position", "absolute")
               .set("bottom", "1rem")
               .set("left", "1rem")
               .set("z-index", "10");
        credits.addClassName(LumoUtility.Border.TOP);
        credits.getStyle().set("border-color", "var(--lumo-contrast-10pct)");
        // Aggiunta dei componenti al layout
        getContent().add(header, title, subtitle, buttonContainer, credits);
        getContent().getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Crea un pulsante principale con stile personalizzato.
     *
     * @param text     Il testo da visualizzare sul pulsante
     * @param iconName Il nome dell'icona da visualizzare (dalla libreria Vaadin)
     * @return Un pulsante configurato con lo stile principale dell'applicazione
     */
    private Button createMainButton(String text, String iconName) {
        Button button = new Button(text, new Icon(iconName));
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        button.setWidthFull();
        button.setHeight("120px");
        button.addClassName(LumoUtility.FontSize.LARGE);
        button.getStyle()
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("transition", "all 0.3s ease")
                .set("background", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("border", "none");

        // Effetto hover
        button.getElement().getThemeList().add("primary");
        button.addClickListener(e -> button.getStyle().set("transform", "translateY(2px)"));

        return button;
    }
}