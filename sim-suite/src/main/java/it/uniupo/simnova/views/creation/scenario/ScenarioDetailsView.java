package it.uniupo.simnova.views.creation.scenario;

import com.flowingcode.vaadin.addons.enhancedtabs.EnhancedTabs;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
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
import it.uniupo.simnova.views.common.utils.FieldGenerator;
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
 * @version 1.5
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
    private final PresidiService presidiService;
    /**
     * ID dello scenario attualmente visualizzato.
     */
    private Integer scenarioId;
    /**
     * Oggetto Scenario attualmente visualizzato.
     * Dichiarato come campo della classe per mantenere lo stato aggiornato.
     */
    private Scenario scenario;

    private H2 titleDisplay;
    private Paragraph authorsDisplay;

    private TextField titleEdit;
    private TextField authorsEdit;
    private HorizontalLayout editButtonsLayout;
    private Button saveTitleAuthorsButton;
    private Button cancelTitleAuthorsButton;
    private Button editTitleAuthorsButton;


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
                               EsameFisicoService esameFisicoService, PazienteT0Service pazienteT0Service, PresidiService presidiService) {
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
        this.presidiService = presidiService;
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
                throw new NumberFormatException("Il parametro ID scenario è nullo o vuoto.");
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0) {
                throw new NumberFormatException("ID scenario deve essere un numero positivo.");
            }
            if (!scenarioService.existScenario(scenarioId)) {
                throw new NotFoundException("Scenario con ID " + scenarioId + " non trovato.");
            }
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}. Causa: {}", parameter, e.getMessage());
            event.rerouteToError(NotFoundException.class, "ID scenario '" + parameter + "' non valido. " + e.getMessage());
        } catch (NotFoundException e) {
            logger.warn("Tentativo di accesso a scenario non esistente: ID {}", scenarioId);
            event.rerouteToError(NotFoundException.class, e.getMessage());
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
            Notification.show("ID scenario non specificato.", 3000, Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("scenari");
            return;
        }
        this.scenario = scenarioService.getScenarioById(scenarioId);
        if (this.scenario == null) {
            logger.error("Scenario non trovato con ID: {} durante beforeEnter", scenarioId);
            event.rerouteToError(NotFoundException.class, "Scenario con ID " + scenarioId + " non trovato.");
            return;
        }
        initView();
    }

    /**
     * Inizializza la vista con i dati dello scenario.
     * Crea e aggiunge i componenti alla vista.
     */
    private void initView() {
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());
        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();
        backButton.addClickListener(e -> UI.getCurrent().navigate("scenari"));

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Dettagli Scenario",
                "Visualizza i dettagli dello scenario selezionato",
                VaadinIcon.INFO_CIRCLE.create(),
                "var(--lumo-primary-color)");

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

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

        titleContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        titleDisplay = new H2(this.scenario.getTitolo());
        titleDisplay.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.XSMALL,
                LumoUtility.FontSize.XXLARGE
        );
        titleDisplay.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px");

        Span boldAutori = new Span("Autori: ");
        boldAutori.getStyle().set("font-weight", "bold");
        Span authorsValue = new Span(this.scenario.getAutori());

        authorsDisplay = new Paragraph();
        authorsDisplay.add(boldAutori, authorsValue);
        authorsDisplay.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Top.NONE,
                LumoUtility.Margin.Bottom.NONE,
                LumoUtility.FontSize.XLARGE
        );

        titleEdit = FieldGenerator.createTextField("Titolo", "Titolo dello scenario", true);
        titleEdit.setVisible(false);

        authorsEdit = FieldGenerator.createTextField("Autori", "Autori dello scenario", true);
        authorsEdit.setVisible(false);

        editTitleAuthorsButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editTitleAuthorsButton.setTooltipText("Modifica titolo e autori");
        editTitleAuthorsButton.getStyle().set("margin-left", "auto");

        HorizontalLayout editButtonContainer = new HorizontalLayout(editTitleAuthorsButton);
        editButtonContainer.setWidthFull();
        editButtonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        saveTitleAuthorsButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveTitleAuthorsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        saveTitleAuthorsButton.setVisible(false);

        cancelTitleAuthorsButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelTitleAuthorsButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        cancelTitleAuthorsButton.setVisible(false);

        editButtonsLayout = new HorizontalLayout(saveTitleAuthorsButton, cancelTitleAuthorsButton);
        editButtonsLayout.setSpacing(true);
        editButtonsLayout.setVisible(false);
        editButtonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        VerticalLayout displayLayout = new VerticalLayout(titleDisplay, authorsDisplay);
        displayLayout.setPadding(false);
        displayLayout.setSpacing(false);
        displayLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        displayLayout.setWidthFull();

        VerticalLayout editLayout = new VerticalLayout(titleEdit, authorsEdit, editButtonsLayout);
        editLayout.setPadding(false);
        editLayout.setSpacing(true);
        editLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        editLayout.setWidthFull();
        editLayout.setVisible(false);

        titleContainer.add(displayLayout, editLayout, editButtonContainer);

        Component subtitle = InfoSupport.getInfo(this.scenario, scenarioService);

        Tab tabInfoGenerali = createTabWithIcon("Informazioni Generali", VaadinIcon.INFO_CIRCLE);
        Tab tabStatoPaziente = createTabWithIcon("Stato Paziente", VaadinIcon.USER);
        Tab tabEsamiReferti = createTabWithIcon("Esami e Referti", VaadinIcon.CLIPBOARD_TEXT);

        Component infoGeneraliContent = GeneralSupport.createOverviewContentWithData(
                this.scenario,
                scenarioService.isPediatric(scenarioId),
                this.scenario.getInfoGenitore(),
                azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId),
                scenarioService,
                materialeNecessario
        );

        Component statoPazienteContent = PatientT0Support.createPatientContent(
                pazienteT0Service.getPazienteT0ById(scenarioId),
                esameFisicoService.getEsameFisicoById(scenarioId),
                scenarioId,
                esameFisicoService,
                pazienteT0Service,
                presidiService,
                advancedScenarioService
        );

        Component esamiRefertiContent = ExamSupport.createExamsContent(
                esameRefertoService,
                scenarioId
        );

        EnhancedTabs enhancedTabs = new EnhancedTabs();
        enhancedTabs.setWidthFull();
        enhancedTabs.getStyle()
                .set("max-width", "1000px")
                .set("margin", "0 auto");

        Div tabsContainer = new Div();
        tabsContainer.setWidthFull();
        tabsContainer.getStyle()
                .set("max-width", "1000px")
                .set("margin", "0 auto")
                .set("overflow", "hidden")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("border-radius", "var(--lumo-border-radius-m) var(--lumo-border-radius-m) 0 0");

        Div contentContainer = new Div();
        contentContainer.addClassName("tab-content");
        contentContainer.getStyle()
                .set("width", "100%")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "0 0 var(--lumo-border-radius-m) var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("transition", "opacity 0.3s ease-in-out");

        Map<Tab, Component> tabsToContent = new HashMap<>();
        tabsToContent.put(tabInfoGenerali, infoGeneraliContent);
        tabsToContent.put(tabStatoPaziente, statoPazienteContent);
        tabsToContent.put(tabEsamiReferti, esamiRefertiContent);

        enhancedTabs.add(tabInfoGenerali, tabStatoPaziente, tabEsamiReferti);

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
                    scenarioId,
                    patientSimulatedScenarioService.getSceneggiatura(scenarioId),
                    patientSimulatedScenarioService
            );
            tabsToContent.put(tabSceneggiatura, sceneggiaturaContent);
            enhancedTabs.add(tabSceneggiatura);
        }

        contentContainer.add(infoGeneraliContent);
        enhancedTabs.addSelectedChangeListener(event -> {
            contentContainer.removeAll();
            Component selectedContent = tabsToContent.get(event.getSelectedTab());
            if (selectedContent != null) {
                contentContainer.add(selectedContent);
            }
            contentContainer.getElement().executeJs(
                    "this.style.opacity = '0'; setTimeout(() => this.style.opacity = '1', 50);"
            );
        });

        enhancedTabs.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m) var(--lumo-border-radius-m) 0 0")
                .set("margin", "0");

        tabsContainer.add(enhancedTabs, contentContainer);
        contentLayout.add(headerSection, titleContainer, subtitle, tabsContainer);

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(null);
        mainLayout.add(customHeader, contentLayout, footerLayout);

        Button scrollToTopButton = StyleApp.getScrollButton();
        Button scrollDownButton = StyleApp.getScrollDownButton();
        VerticalLayout scrollButtonContainer = new VerticalLayout(scrollToTopButton, scrollDownButton);
        mainLayout.add(scrollButtonContainer);


        editTitleAuthorsButton.addClickListener(e -> {
            displayLayout.setVisible(false);
            editTitleAuthorsButton.setVisible(false);
            editLayout.setVisible(true);
            saveTitleAuthorsButton.setVisible(true);
            cancelTitleAuthorsButton.setVisible(true);
            titleEdit.setValue(this.scenario.getTitolo());
            authorsEdit.setValue(this.scenario.getAutori());
            titleEdit.setVisible(true);
            authorsEdit.setVisible(true);
            editButtonsLayout.setVisible(true);
        });

        cancelTitleAuthorsButton.addClickListener(e -> {
            editLayout.setVisible(false);
            displayLayout.setVisible(true);
            editTitleAuthorsButton.setVisible(true);
        });

        saveTitleAuthorsButton.addClickListener(e -> {
            String newTitle = titleEdit.getValue();
            String newAuthors = authorsEdit.getValue();

            if (newTitle == null || newTitle.trim().isEmpty()) {
                Notification.show("Il titolo non può essere vuoto.", 3000, Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                titleEdit.focus();
                return;
            }
            if (newAuthors == null || newAuthors.trim().isEmpty()) {
                Notification.show("Il campo autori non può essere vuoto.", 3000, Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                authorsEdit.focus();
                return;
            }

            try {
                scenarioService.updateScenarioTitleAndAuthors(scenarioId, newTitle, newAuthors);
                this.scenario.setTitolo(newTitle);
                this.scenario.setAutori(newAuthors);

                titleDisplay.setText(newTitle);
                Span updatedBoldAutori = new Span("Autori: ");
                updatedBoldAutori.getStyle().set("font-weight", "bold");
                Span updatedAuthorsValue = new Span(newAuthors);
                authorsDisplay.removeAll();
                authorsDisplay.add(updatedBoldAutori, updatedAuthorsValue);

                editLayout.setVisible(false);
                displayLayout.setVisible(true);
                editTitleAuthorsButton.setVisible(true);

                Notification.show("Titolo e autori aggiornati con successo", 3000, Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (Exception ex) {
                logger.error("Errore durante l'aggiornamento di titolo e autori dello scenario {}", scenarioId, ex);
                Notification.show("Errore durante l'aggiornamento. Riprovare più tardi.", 3000, Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}