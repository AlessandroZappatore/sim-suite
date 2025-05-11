package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
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
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.*;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.MaterialeService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.support.AppHeader;
import it.uniupo.simnova.views.support.StyleApp;
import it.uniupo.simnova.views.support.detail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Vista per la visualizzazione dei dettagli di uno scenario.
 * <p>
 * Questa classe gestisce il caricamento e la visualizzazione dei dettagli di uno scenario specifico,
 * inclusi i parametri vitali, gli esami e i referti, e altre informazioni pertinenti.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@SuppressWarnings("ThisExpressionReferencesGlobalObjectJS")
@PageTitle("Dettagli Scenario")
@Route(value = "scenari")
public class ScenarioDetailsView extends Composite<VerticalLayout> implements HasUrlParameter<String>, BeforeEnterObserver {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;

    private final FileStorageService fileStorageService;
    private final MaterialeService materialeNecessario;
    /**
     * ID dello scenario attualmente visualizzato.
     */
    private Integer scenarioId;
    /**
     * Oggetto Scenario caricato.
     */
    private Scenario scenario;
    /**
     * Flag per verificare se la vista è stata staccata.
     */
    private final AtomicBoolean detached = new AtomicBoolean(false);
    /**
     * Logger per la registrazione delle informazioni e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioDetailsView.class);
    /**
     * ExecutorService per gestire i task in background.
     */
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();


    /**
     * Costruttore della vista dei dettagli dello scenario.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    @Autowired
    public ScenarioDetailsView(ScenarioService scenarioService, FileStorageService fileStorageService, MaterialeService materialeNecessario) {
        this.scenarioService = scenarioService;
        UI.getCurrent();
        getContent().addClassName("scenario-details-view");
        getContent().setPadding(false);
        this.fileStorageService = fileStorageService;
        this.materialeNecessario = materialeNecessario;
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
            Notification.show("ID scenario non valido", 3000, Position.MIDDLE).addThemeVariants(
                    NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("scenari");
            return;
        }
        initView();
    }

    /**
     * Metodo chiamato quando la vista viene staccata.
     * Ferma l'esecuzione dei task in background.
     *
     * @param detachEvent evento di distacco
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        detached.set(true);
        executorService.shutdownNow();
    }

    /**
     * Inizializza la vista con i dati dello scenario.
     * Crea e aggiunge i componenti alla vista.
     */
    private void initView() {
        if (detached.get()) {
            return;
        }

        scenario = scenarioService.getScenarioById(scenarioId);
        
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);

        // 1. HEADER
        Button backButton = StyleApp.getBackButton();

        Button editButton = StyleApp.getButton("Modifica Scenario",VaadinIcon.EDIT, ButtonVariant.LUMO_PRIMARY, "var(--lumo-primary-color)");
        editButton.addClickListener(e ->
                UI.getCurrent().navigate("modificaScenario/" + scenario.getId()));

        HorizontalLayout editButtonContainer = new HorizontalLayout();
        editButtonContainer.setWidthFull();
        editButtonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        editButtonContainer.setPadding(false);
        editButtonContainer.setMargin(false);
        editButtonContainer.add(editButton);

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Dettagli Scenario",
                "Visualizza i dettagli dello scenario selezionato",
                VaadinIcon.INFO_CIRCLE,
                "var(--lumo-primary-color)"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE (con details)
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

        // Autori con stile migliorato
        Paragraph authors = new Paragraph("Autori: " + scenario.getAutori());
        authors.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Top.NONE,
                LumoUtility.Margin.Bottom.NONE,
                LumoUtility.FontSize.XLARGE
        );

        // Aggiungi elementi al contenitore
        titleContainer.add(title, authors);

        // Aggiungi il contenitore al layout invece degli elementi separati

        Component subtitle = InfoSupport.getInfo(scenario);

        // Sezioni Details
        Details detailsInfoGenerali = new Details("Informazioni Generali", GeneralSupport.createOverviewContentWithData(scenario, scenarioService.isPediatric(scenarioId),scenario.getInfoGenitore(), materialeNecessario.toStringAllMaterialsByScenarioId(scenarioId),scenarioService.getNomiAzioniChiaveByScenarioId(scenarioId)));
        detailsInfoGenerali.setOpened(true); // Espandi il primo pannello di default
        detailsInfoGenerali.addThemeVariants(DetailsVariant.FILLED);
        styleDetailsSummary(detailsInfoGenerali);


        Details detailsStatoPaziente = new Details("Stato Paziente", PatientT0Support.createPatientContent(scenarioService.getPazienteT0ById(scenarioId), scenarioService.getEsameFisicoById(scenarioId), scenarioId));
        detailsStatoPaziente.addThemeVariants(DetailsVariant.FILLED);
        styleDetailsSummary(detailsStatoPaziente);

        Details detailsEsamiReferti = new Details("Esami e Referti", ExamSupport.createExamsContent(scenarioService.getEsamiRefertiByScenarioId(scenarioId)));
        detailsEsamiReferti.addThemeVariants(DetailsVariant.FILLED);
        styleDetailsSummary(detailsEsamiReferti);

        contentLayout.add(editButtonContainer, headerSection, titleContainer, subtitle, detailsInfoGenerali, detailsStatoPaziente, detailsEsamiReferti);


        List<Tempo> tempi = scenarioService.getTempiByScenarioId(scenarioId);
        if (!tempi.isEmpty()) {
            Details timelineDetails = new Details("Timeline", TimesSupport.createTimelineContent(scenarioService.getTempiByScenarioId(scenarioId), scenarioId));
            timelineDetails.addThemeVariants(DetailsVariant.FILLED);
            styleDetailsSummary(timelineDetails);
            contentLayout.add(timelineDetails);
        }


        String scenarioType = scenarioService.getScenarioType(scenarioId);
        if ("Patient Simulated Scenario".equalsIgnoreCase(scenarioType)) {
            Details sceneggiaturaDetails = new Details("Sceneggiatura", SceneggiaturaSupport.createSceneggiaturaContent(ScenarioService.getSceneggiatura(scenarioId)));
            sceneggiaturaDetails.addThemeVariants(DetailsVariant.FILLED);
            styleDetailsSummary(sceneggiaturaDetails);
            contentLayout.add(sceneggiaturaDetails);
        }


        // 3. FOOTER
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(null);
        // Assemblaggio finale
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        backButton.addClickListener(e -> UI.getCurrent().navigate("scenari"));
    }

    private void styleDetailsSummary(Details details) {
        if (details != null && details.getSummary() != null) {
            details.getSummary().getStyle()
                    .set("font-size", "var(--lumo-font-size-xl)")
                    .set("font-weight", "600")
                    .set("padding-top", "var(--lumo-space-s)")
                    .set("padding-bottom", "var(--lumo-space-s)");
        }
    }
}
