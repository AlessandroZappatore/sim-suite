package it.uniupo.simnova.views.creation;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.dto.ScenarioDetailsDto;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vista per l'inizio della creazione di uno scenario.
 * Permette l'inserimento dei dettagli iniziali come titolo, paziente, patologia e durata.
 *
 * @author Alessandro Zappatore
 * @version 2.0 (Refactored)
 */
@PageTitle("Inizio Creation")
@Route("startCreation")
public class StartCreationView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private static final Logger logger = LoggerFactory.getLogger(StartCreationView.class);

    private final ScenarioService scenarioService;
    private final AdvancedScenarioService advancedScenarioService;
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    private final FileStorageService fileStorageService;
    private final Binder<ScenarioDetailsDto> binder = new Binder<>(ScenarioDetailsDto.class);
    private final ScenarioDetailsDto scenarioDetails = new ScenarioDetailsDto();
    private TextField scenarioTitle;
    private TextField patientName;
    private TextField pathology;
    private TextField authorField;
    private ComboBox<Integer> durationField;
    private Select<String> typeField;
    private Button backButton;
    private Button nextButton;
    private String scenarioType;

    public StartCreationView(ScenarioService scenarioService, FileStorageService fileStorageService,
                             AdvancedScenarioService advancedScenarioService, PatientSimulatedScenarioService patientSimulatedScenarioService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;

        initComponents();
        setupBinder();
        createLayout();
        attachListeners();
    }

    private void initComponents() {
        scenarioTitle = FieldGenerator.createTextField("TITOLO SCENARIO", "Inserisci il titolo", true);
        scenarioTitle.setPrefixComponent(FontAwesome.Solid.TAGS.create());

        patientName = FieldGenerator.createTextField("NOME PAZIENTE", "Inserisci il nome", true);
        patientName.setPrefixComponent(FontAwesome.Solid.USER_INJURED.create());

        pathology = FieldGenerator.createTextField("PATOLOGIA/MALATTIA", "Inserisci la patologia", true);
        pathology.setPrefixComponent(FontAwesome.Solid.DISEASE.create());

        authorField = FieldGenerator.createTextField("AUTORE", "Inserisci il tuo nome", true);
        authorField.setPrefixComponent(FontAwesome.Solid.SIGNATURE.create());

        durationField = FieldGenerator.createComboBox("DURATA (minuti)", List.of(5, 10, 15, 20, 25, 30), 10, true);
        durationField.setPrefixComponent(FontAwesome.Solid.STOPWATCH_20.create());

        typeField = FieldGenerator.createSelect("TIPO PAZIENTE", List.of("Adulto", "Pediatrico", "Neonatale", "Prematuro"), "Adulto", true);
        typeField.setPrefixComponent(FontAwesome.Solid.USER.create());

        backButton = StyleApp.getBackButton();
        nextButton = StyleApp.getNextButton();
    }

    private void setupBinder() {
        binder.setBean(scenarioDetails);

        binder.forField(scenarioTitle).asRequired("Il titolo è obbligatorio").bind(ScenarioDetailsDto::getTitle, ScenarioDetailsDto::setTitle);
        binder.forField(patientName).asRequired("Il nome del paziente è obbligatorio").bind(ScenarioDetailsDto::getPatientName, ScenarioDetailsDto::setPatientName);
        binder.forField(pathology).asRequired("La patologia è obbligatoria").bind(ScenarioDetailsDto::getPathology, ScenarioDetailsDto::setPathology);
        binder.forField(authorField).asRequired("L'autore è obbligatorio").bind(ScenarioDetailsDto::getAuthor, ScenarioDetailsDto::setAuthor);
        binder.forField(durationField).asRequired("La durata è obbligatoria").bind(ScenarioDetailsDto::getDuration, ScenarioDetailsDto::setDuration);
        binder.forField(typeField).asRequired("Il tipo di paziente è obbligatorio").bind(ScenarioDetailsDto::getType, ScenarioDetailsDto::setType);
    }

    private void createLayout() {
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Inizio Creazione Scenario",
                "Compila i campi per iniziare a costruire la tua simulazione.",
                VaadinIcon.START_COG.create(),
                "var(--lumo-primary-color)"
        );

        FormLayout formLayout = new FormLayout(
                scenarioTitle, patientName, pathology,
                authorField, durationField, typeField
        );
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setMaxWidth("1200px");

        VerticalLayout card = new VerticalLayout(headerSection, formLayout);
        card.setClassName("form-card");
        card.setPadding(false);
        card.setSpacing(true);
        card.setMaxWidth("100%");

        card.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, headerSection, formLayout);

        VerticalLayout contentLayout = StyleApp.getContentLayout();
        contentLayout.add(card);
        contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        contentLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, card);
        contentLayout.setHeightFull();

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);
        mainLayout.setFlexGrow(1, contentLayout);
    }

    private void attachListeners() {
        backButton.addClickListener(e -> e.getSource().getUI().ifPresent(ui -> ui.navigate("")));

        nextButton.addClickListener(e -> {
            if (binder.writeBeanIfValid(scenarioDetails)) {
                saveScenarioAndNavigate(e.getSource().getUI());
            } else {
                Notification.show("Per favore, correggi gli errori nel form.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Map<String, Icon> iconMap = Map.of(
                "Adulto", FontAwesome.Solid.USER.create(),
                "Pediatrico", FontAwesome.Solid.CHILD.create(),
                "Neonatale", FontAwesome.Solid.BABY.create(),
                "Prematuro", FontAwesome.Solid.HANDS_HOLDING_CHILD.create()
        );
        typeField.addValueChangeListener(event -> {
            Icon newIcon = iconMap.getOrDefault(event.getValue(), FontAwesome.Solid.QUESTION.create());
            typeField.setPrefixComponent(newIcon);
        });
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        this.scenarioType = (parameter != null) ? parameter.toLowerCase() : "quickscenario";
    }

    private void saveScenarioAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            try {
                int scenarioId;
                float duration = scenarioDetails.getDuration().floatValue();

                switch (scenarioType) {
                    case "quickscenario":
                        scenarioId = scenarioService.startQuickScenario(
                                -1,
                                scenarioDetails.getTitle(), scenarioDetails.getPatientName(),
                                scenarioDetails.getPathology(), scenarioDetails.getAuthor(),
                                duration, scenarioDetails.getType()
                        );
                        break;
                    case "advancedscenario":
                        scenarioId = advancedScenarioService.startAdvancedScenario(
                                scenarioDetails.getTitle(), scenarioDetails.getPatientName(),
                                scenarioDetails.getPathology(), scenarioDetails.getAuthor(),
                                duration, scenarioDetails.getType()
                        );
                        break;
                    case "patientsimulatedscenario":
                        scenarioId = patientSimulatedScenarioService.startPatientSimulatedScenario(
                                scenarioDetails.getTitle(), scenarioDetails.getPatientName(),
                                scenarioDetails.getPathology(), scenarioDetails.getAuthor(),
                                duration, scenarioDetails.getType()
                        );
                        break;
                    default:
                        Notification.show("Tipo di scenario non riconosciuto: " + scenarioType, 3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                }

                if (scenarioId > 0) {
                    logger.info("Scenario creato con ID: {}. Navigando a target/{}", scenarioId, scenarioId);
                    ui.navigate("target/" + scenarioId);
                } else {
                    Notification.show("Errore: ID scenario non valido.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception e) {
                logger.error("Errore durante il salvataggio dello scenario di tipo '{}'", scenarioType, e);
                Notification.show("Errore durante il salvataggio: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}