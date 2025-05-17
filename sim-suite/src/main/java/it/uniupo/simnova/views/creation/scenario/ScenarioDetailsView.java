package it.uniupo.simnova.views.creation.scenario;

import com.flowingcode.vaadin.addons.enhancedtabs.EnhancedTabs;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.scenario.components.*;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.uniupo.simnova.views.ui.helper.TabsSupport.createTabWithIcon;

/**
 * Vista per la visualizzazione dei dettagli di uno scenario.
 * <p>
 * Questa classe gestisce il caricamento e la visualizzazione dei dettagli di uno scenario specifico,
 * inclusi i parametri vitali, gli esami e i referti, e altre informazioni pertinenti.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.4
 */
@SuppressWarnings("ThisExpressionReferencesGlobalObjectJS")
@PageTitle("Dettagli Scenario")
@Route(value = "scenari")
public class ScenarioDetailsView extends Composite<VerticalLayout> implements HasUrlParameter<String>, BeforeEnterObserver {
    /**
     * Logger per la registrazione delle informazioni e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioDetailsView.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    private final FileStorageService fileStorageService;
    private final MaterialeService materialeNecessario;
    private final AdvancedScenarioService advancedScenarioService;
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    private final AzioneChiaveService azioneChiaveService;
    private final EsameRefertoService esameRefertoService;
    private final EsameFisicoService esameFisicoService;
    private final PazienteT0Service pazienteT0Service;
    /**
     * ID dello scenario attualmente visualizzato.
     */
    private Integer scenarioId;
    /**
     * Oggetto Scenario caricato.
     */
    private Scenario scenario;


    /**
     * Costruttore della vista dei dettagli dello scenario.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    @Autowired
    public ScenarioDetailsView(ScenarioService scenarioService, FileStorageService fileStorageService,
                               MaterialeService materialeNecessario, AdvancedScenarioService advancedScenarioService,
                               PatientSimulatedScenarioService patientSimulatedScenarioService,
                               AzioneChiaveService azionechiaveService, EsameRefertoService esameRefertoService,
                               EsameFisicoService esameFisicoService, PazienteT0Service pazienteT0Service) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.materialeNecessario = materialeNecessario;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        this.azioneChiaveService = azionechiaveService;
        this.esameRefertoService = esameRefertoService;
        this.esameFisicoService = esameFisicoService;
        this.pazienteT0Service = pazienteT0Service;

        UI.getCurrent();
        getContent().addClassName("scenario-details-view");
        getContent().setPadding(false);
    }

    /**
     * Imposta il parametro dell'URL per la vista.
     *
     * @param event     evento di navigazione
     * @param parameter parametro passato nell'URL
     */
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Metodo chiamato prima di entrare nella vista.
     * Carica i dati dello scenario e gestisce eventuali errori.
     *
     * @param event evento di navigazione
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (scenarioId == null) {
            Notification.show("ID scenario non valido", 3000, Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("scenari");
            return;
        }
        initView();
    }

    /**
     * Inizializza la vista con i dati dello scenario.
     * Crea e aggiunge i componenti alla vista.
     */
    private void initView() {
        scenario = scenarioService.getScenarioById(scenarioId);

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);

        // 1. HEADER
        Button backButton = StyleApp.getBackButton();

        Button editButton = StyleApp.getButton(
                "Modifica Scenario",
                VaadinIcon.EDIT,
                ButtonVariant.LUMO_PRIMARY,
                "var(--lumo-primary-color)"
        );
        editButton.addClickListener(e -> UI.getCurrent().navigate("modificaScenario/" + scenario.getId()));

        HorizontalLayout editButtonContainer = new HorizontalLayout();
        editButtonContainer.setWidthFull();
        editButtonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        editButtonContainer.setPadding(false);
        editButtonContainer.setMargin(false);
        editButtonContainer.add(editButton);

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Dettagli Scenario",
                "Visualizza i dettagli dello scenario selezionato",
                VaadinIcon.INFO_CIRCLE.create(),
                "var(--lumo-primary-color)");

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE (con tabs)
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Titolo e sottotitolo
        // Contenitore per titolo e autori con ombra e bordi arrotondati
        Div titleContainer = new Div();
        titleContainer.setWidthFull();
        titleContainer.getStyle()
                .set("max-width", "800px")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("transition", "box-shadow 0.3s ease-in-out");

        // Effetto hover sul contenitore
        titleContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        // Titolo con stile migliorato
        H2 title = new H2(scenario.getTitolo());
        title.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.XSMALL,
                LumoUtility.FontSize.XXLARGE
        );
        title.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px");

        // Autori con stile migliorato e "Autori" in grassetto
        Span boldAutori = new Span("Autori: ");
        boldAutori.getStyle().set("font-weight", "bold");
        Span autoriValue = new Span(scenario.getAutori());

        Paragraph authors = new Paragraph();
        authors.add(boldAutori, autoriValue);
        authors.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Top.NONE,
                LumoUtility.Margin.Bottom.NONE,
                LumoUtility.FontSize.XLARGE
        );

        // Aggiungi elementi al contenitore
        titleContainer.add(title, authors);

        Component subtitle = InfoSupport.getInfo(scenario);

        // Creazione dei tab e relativo contenuto
        Tab tabInfoGenerali = createTabWithIcon("Informazioni Generali", VaadinIcon.INFO_CIRCLE);
        Tab tabStatoPaziente = createTabWithIcon("Stato Paziente", VaadinIcon.USER);
        Tab tabEsamiReferti = createTabWithIcon("Esami e Referti", VaadinIcon.CLIPBOARD_TEXT);

        // Contenuto dei tab
        Component infoGeneraliContent = GeneralSupport.createOverviewContentWithData(
                scenario,
                scenarioService.isPediatric(scenarioId),
                scenario.getInfoGenitore(),
                materialeNecessario.toStringAllMaterialsByScenarioId(scenarioId),
                azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId)
        );

        Component statoPazienteContent = PatientT0Support.createPatientContent(
                pazienteT0Service.getPazienteT0ById(scenarioId),
                esameFisicoService.getEsameFisicoById(scenarioId),
                scenarioId
        );

        Component esamiRefertiContent = ExamSupport.createExamsContent(
                esameRefertoService.getEsamiRefertiByScenarioId(scenarioId)
        );

        // Configurazione EnhancedTabs
        EnhancedTabs enhancedTabs = new EnhancedTabs();
        enhancedTabs.setWidthFull();
        enhancedTabs.getStyle()
                .set("max-width", "1000px")
                .set("margin", "0 auto");

        // Configurazione del contenitore principale
        Div tabsContainer = new Div();
        tabsContainer.setWidthFull();
        tabsContainer.getStyle()
                .set("max-width", "1000px")
                .set("margin", "0 auto")
                .set("overflow", "hidden")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        // Container per il contenuto del tab selezionato
        Div contentContainer = new Div();
        contentContainer.addClassName("tab-content");
        contentContainer.getStyle()
                .set("width", "100%")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "0 0 var(--lumo-border-radius-m) var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("transition", "opacity 0.3s ease-in-out");

        // Map per associare ogni tab al suo contenuto
        Map<Tab, Component> tabsToContent = new HashMap<>();
        tabsToContent.put(tabInfoGenerali, infoGeneraliContent);
        tabsToContent.put(tabStatoPaziente, statoPazienteContent);
        tabsToContent.put(tabEsamiReferti, esamiRefertiContent);

        // Aggiunta dei tab base a EnhancedTabs
        enhancedTabs.add(tabInfoGenerali, tabStatoPaziente, tabEsamiReferti);

        // Tab aggiuntivi condizionali
        List<Tempo> tempi = advancedScenarioService.getTempiByScenarioId(scenarioId);
        if (!tempi.isEmpty()) {
            Tab tabTimeline = createTabWithIcon("Timeline", VaadinIcon.CLOCK);
            Component timelineContent = TimesSupport.createTimelineContent(tempi, scenarioId, advancedScenarioService);
            tabsToContent.put(tabTimeline, timelineContent);
            enhancedTabs.add(tabTimeline);
        }

        String scenarioType = scenarioService.getScenarioType(scenarioId);
        if ("Patient Simulated Scenario".equalsIgnoreCase(scenarioType)) {
            Tab tabSceneggiatura = createTabWithIcon("Sceneggiatura", VaadinIcon.FILE_TEXT);
            Component sceneggiaturaContent = SceneggiaturaSupport.createSceneggiaturaContent(
                    patientSimulatedScenarioService.getSceneggiatura(scenarioId)
            );
            tabsToContent.put(tabSceneggiatura, sceneggiaturaContent);
            enhancedTabs.add(tabSceneggiatura);
        }

        // Impostazione iniziale del contenuto
        contentContainer.add(infoGeneraliContent);

        // Gestione del cambio tab
        enhancedTabs.addSelectedChangeListener(event -> {
            // Rimuovi tutti i contenuti precedenti
            contentContainer.removeAll();

            // Aggiungi il nuovo contenuto in base al tab selezionato
            Component selectedContent = tabsToContent.get(event.getSelectedTab());
            contentContainer.add(selectedContent);

            // Effetto di transizione
            contentContainer.getElement().executeJs(
                    "this.style.opacity = '0'; setTimeout(() => this.style.opacity = '1', 50);"
            );
        });

        // Personalizzazione stile EnhancedTabs
        enhancedTabs.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m) var(--lumo-border-radius-m) 0 0")
                .set("margin", "0");

        // Aggiungi i tabs e il contenuto al contenitore
        tabsContainer.add(enhancedTabs, contentContainer);

        contentLayout.add(editButtonContainer, headerSection, titleContainer, subtitle, tabsContainer);

        // 3. FOOTER
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(null);

        // Assemblaggio finale
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Pulsante "Torna su"
        Button scrollToTopButton = StyleApp.getScrollButton();
        Button scrollDownButton = StyleApp.getScrollDownButton();
        VerticalLayout scrollButtonContainer = new VerticalLayout(scrollToTopButton, scrollDownButton);

        mainLayout.add(scrollButtonContainer);

        backButton.addClickListener(e -> UI.getCurrent().navigate("scenari"));
    }
}