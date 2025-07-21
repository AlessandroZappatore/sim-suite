package it.uniupo.simnova.views.creation;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
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
 * Offre opzioni per creare scenari o accedere alla libreria degli scenari salvati.
 *
 * @author Alessandro Zappatore
 * @version 2.0
 */
@PageTitle("Creation")
@Route("")
public class CreationView extends Composite<VerticalLayout> {

    private final FileStorageService fileStorageService;
    private Button backButton;
    private Button quickScenarioButton;
    private Button advancedScenarioButton;
    private Button patientSimulatedScenarioButton;
    private Button viewSavedScenariosButton;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param fileStorageService Servizio per la gestione dello storage dei file.
     */
    public CreationView(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;

        VerticalLayout layout = getContent();
        layout.addClassName("creation-view");
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setSizeFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle().set("min-height", "100vh");
        layout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        layout.add(createHeader(), createContent(), createFooter());
        attachListeners();
    }

    /**
     * Crea la sezione dell'header.
     */
    private HorizontalLayout createHeader() {
        AppHeader header = new AppHeader(fileStorageService);
        backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna alla Home");
        backButton.setVisible(false);

        return StyleApp.getCustomHeader(backButton, header);
    }

    /**
     * Crea il contenuto principale della vista.
     */
    private VerticalLayout createContent() {
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "SIM SUITE",
                "Seleziona il tipo di scenario da creare o visualizza gli scenari salvati",
                FontAwesome.Solid.PLAY.create(),
                "var(--lumo-primary-text-color)"
        );

        FlexLayout creationOptionsLayout = new FlexLayout();
        creationOptionsLayout.addClassName("creation-options-layout");
        creationOptionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        creationOptionsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        creationOptionsLayout.getStyle()
                .set("gap", "1.5rem")
                .set("width", "100%");

        quickScenarioButton = createOptionCard(
                "Quick Scenario",
                VaadinIcon.BOLT.create(),
                "Scenario veloce.",
                "FF9800"
        );
        advancedScenarioButton = createOptionCard(
                "Advanced Scenario",
                VaadinIcon.CLOCK.create(),
                "Scenario multi-tempo.",
                "FF9800"
        );
        patientSimulatedScenarioButton = createOptionCard(
                "Patient Simulated",
                FontAwesome.Solid.USER_INJURED.create(),
                "Scenario con sceneggiatura.",
                "FF9800"
        );

        creationOptionsLayout.add(quickScenarioButton, advancedScenarioButton, patientSimulatedScenarioButton);

        viewSavedScenariosButton = createOptionCard(
                "Lista Scenari Salvati",
                FontAwesome.Solid.ARCHIVE.create(),
                "Lista completa degli scenari salvati.",
                "4CAF50"
        );
        viewSavedScenariosButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        viewSavedScenariosButton.setWidthFull();
        viewSavedScenariosButton.getStyle()
                .set("margin-top", "2rem")
                .set("padding", "1rem")
        .set("height", "10rem");

        Div contentContainer = new Div(creationOptionsLayout, viewSavedScenariosButton);
        contentContainer.getStyle()
                .set("width", "min(90%, 900px)")
                .set("margin", "0 auto")
                .set("padding", "1rem");

        contentLayout.add(headerSection, contentContainer);
        return contentLayout;
    }

    /**
     * Crea la sezione del footer.
     */
    private VerticalLayout createFooter() {
        VerticalLayout footerLayout = new VerticalLayout(new CreditsComponent());
        footerLayout.setPadding(true);
        footerLayout.setSpacing(false);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle()
                .set("border-color", "var(--lumo-contrast-10pct)")
                .set("background-color", "var(--lumo-contrast-5pct)");
        return footerLayout;
    }

    /**
     * Crea una "card" cliccabile per una opzione di scenario.
     */
    private Button createOptionCard(String title, Icon icon, String description, String accentColor) {
        icon.setSize("28px");
        icon.getStyle()
                .set("margin-bottom", "1rem")
                .set("color", accentColor);

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "1.1rem");

        Span descSpan = new Span(description);
        descSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        descSpan.getStyle().set("margin-top", "0.5rem");

        VerticalLayout cardContent = new VerticalLayout(icon, titleSpan, descSpan);
        cardContent.setPadding(false);
        cardContent.setSpacing(false);
        cardContent.setAlignItems(FlexComponent.Alignment.CENTER);
        cardContent.getStyle().set("text-align", "center");

        Button cardButton = new Button(cardContent);
        cardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cardButton.setHeight("auto");
        cardButton.getStyle()
                .set("padding", "1.5rem")
                .set("border-radius", "16px")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
                .set("flex", "1 1 280px")
                .set("position", "relative")
                .set("overflow", "hidden");

        // Effetto gradiente sottile
        cardButton.getElement().getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)")
                .set("cursor", "pointer")
                .set("transform", "translateY(0) scale(1)");

        // Animazioni più sofisticate
        cardButton.getElement().addEventListener("mouseover", e -> cardButton.getStyle()
                .set("transform", "translateY(-8px) scale(1.02)")
                .set("box-shadow", "var(--lumo-box-shadow-l)")
                .set("filter", "brightness(1.1)"));

        cardButton.getElement().addEventListener("mouseout", e -> cardButton.getStyle()
                .set("transform", "translateY(0) scale(1)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("filter", "brightness(1)"));

        return cardButton;
    }

    /**
     * Aggiunge i listener per la navigazione ai componenti interattivi.
     */
    private void attachListeners() {
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        quickScenarioButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("startCreation/quickScenario")));

        advancedScenarioButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("startCreation/advancedScenario")));

        patientSimulatedScenarioButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("startCreation/patientSimulatedScenario")));

        viewSavedScenariosButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("scenari")));
    }
}