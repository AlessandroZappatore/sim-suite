package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.*;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@PageTitle("Dettagli Scenario")
@Route(value = "scenario-details", layout = MainLayout.class)
public class ScenarioDetailsView extends Composite<VerticalLayout>
        implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final ScenarioService scenarioService;
    private Integer scenarioId;
    private Scenario scenario;
    private UI ui; // Memorizza l'UI corrente

    @Autowired
    public ScenarioDetailsView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
        this.ui = UI.getCurrent(); // Ottieni l'UI nel costruttore
        getContent().addClassName("scenario-details-view");
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer scenarioId) {
        this.scenarioId = scenarioId;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.ui = UI.getCurrent(); // Aggiorna l'UI prima di ogni accesso
        loadScenarioData();
    }

    private void loadScenarioData() {
        // Mostra indicatore di caricamento
        getContent().removeAll();
        getContent().add(new ProgressBar());

        new Thread(() -> {
            try {
                this.scenario = scenarioService.getScenarioById(scenarioId);

                if (ui != null && !ui.isClosing()) {
                    ui.access(() -> {
                        if (this.scenario == null) {
                            Notification.show("Scenario non trovato", 3000, Position.MIDDLE);
                            ui.navigate("scenarios");
                            return;
                        }
                        initView();
                    });
                }
            } catch (Exception e) {
                if (ui != null && !ui.isClosing()) {
                    ui.access(() -> {
                        Notification.show("Errore nel caricamento dello scenario", 3000, Position.MIDDLE);
                        ui.navigate("scenarios");
                    });
                }
            }
        }).start();
    }

    private void initView() {
        VerticalLayout mainLayout = getContent();
        mainLayout.removeAll();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        // 1. HEADER
        AppHeader header = new AppHeader();

        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        Button editButton = new Button("Modifica", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e ->
                UI.getCurrent().navigate("edit-scenario/" + scenario.getId()));

        HorizontalLayout customHeader = new HorizontalLayout(backButton, header, editButton);
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.expand(header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Titolo e sottotitolo
        H2 title = new H2(scenario.getTitolo());
        title.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.NONE
        );

        Paragraph subtitle = new Paragraph(
                String.format("Paziente: %s | Patologia: %s | Durata: %.1f minuti",
                        scenario.getNomePaziente(),
                        scenario.getPatologia(),
                        scenario.getTimerGenerale())
        );
        subtitle.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Top.NONE
        );

        // Sezioni dettagliate
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setWidthFull();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);

        // Sezioni base
        addSection(detailsLayout, "Descrizione", scenario.getDescrizione());
        addSection(detailsLayout, "Briefing", scenario.getBriefing());
        addSection(detailsLayout, "Patto Aula", scenario.getPattoAula());
        addSection(detailsLayout, "Azione Chiave", scenario.getAzioneChiave());
        addSection(detailsLayout, "Obiettivo", scenario.getObiettivo());
        addSection(detailsLayout, "Materiale", scenario.getMateriale());
        addSection(detailsLayout, "Moulage", scenario.getMoulage());
        addSection(detailsLayout, "Liquidi", scenario.getLiquidi());

        // Sezione Paziente T0
        addPazienteT0Section(detailsLayout);

        // Sezione Esame Fisico
        addEsameFisicoSection(detailsLayout);

        // Sezioni specifiche per tipi di scenario
        if (scenario instanceof AdvancedScenario) {
            addAdvancedScenarioSections(detailsLayout, (AdvancedScenario) scenario);
        }

        if (scenario instanceof PatientSimulatedScenario) {
            addSection(detailsLayout, "Sceneggiatura",
                    ((PatientSimulatedScenario)scenario).getSceneggiatura());
        }

        contentLayout.add(title, subtitle, detailsLayout);

        // 3. FOOTER
        HorizontalLayout footerLayout = new HorizontalLayout(
                new Paragraph("Sviluppato e creato da Alessandro Zappatore")
        );
        footerLayout.addClassName(LumoUtility.TextColor.SECONDARY);
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Assemblaggio finale
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        // Listener pulsanti
        backButton.addClickListener(e ->
                UI.getCurrent().navigate("scenarios"));
    }

    private void addPazienteT0Section(VerticalLayout parentLayout) {
        Details details = new Details("Stato iniziale paziente (T0)");
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        PazienteT0 paziente = scenario.getPazienteT0();
        if (paziente != null) {
            // Parametri vitali
            Grid<String> parametriGrid = new Grid<>();
            parametriGrid.setItems(
                    "PA: " + paziente.getPA() + " mmHg",
                    "FC: " + paziente.getFC() + " bpm",
                    "RR: " + paziente.getRR() + " atti/min",
                    "Temperatura: " + paziente.getT() + " °C",
                    "SpO2: " + paziente.getSpO2() + "%",
                    "EtCO2: " + paziente.getEtCO2() + " mmHg",
                    "Monitor: " + paziente.getMonitor()
            );
            parametriGrid.addColumn(s -> s).setHeader("Parametro");

            // Accessi venosi
            VerticalLayout venosiLayout = new VerticalLayout();
            venosiLayout.setPadding(false);
            venosiLayout.add(new H4("Accessi venosi"));
            if (!paziente.getAccessiVenosi().isEmpty()) {
                venosiLayout.add(createAccessiGrid(paziente.getAccessiVenosi()));
            } else {
                venosiLayout.add(new Paragraph("Nessun accesso venoso"));
            }

            // Accessi arteriosi
            VerticalLayout arteriosiLayout = new VerticalLayout();
            arteriosiLayout.setPadding(false);
            arteriosiLayout.add(new H4("Accessi arteriosi"));
            if (!paziente.getAccessiArteriosi().isEmpty()) {
                arteriosiLayout.add(createAccessiGrid(paziente.getAccessiArteriosi()));
            } else {
                arteriosiLayout.add(new Paragraph("Nessun accesso arterioso"));
            }

            content.add(
                    new H3("Parametri vitali"),
                    parametriGrid,
                    venosiLayout,
                    arteriosiLayout
            );
        } else {
            content.add(new Paragraph("Dati paziente non disponibili"));
        }

        details.setContent(content);
        details.setOpened(true);
        parentLayout.add(details);
    }

    private void addEsameFisicoSection(VerticalLayout parentLayout) {
        Details details = new Details("Esame Fisico");
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);

        EsameFisico esame = scenario.getEsameFisico();
        if (esame != null && !esame.getSections().isEmpty()) {
            // Get all sections from the map
            Map<String, String> sections = esame.getSections();

            // Add each section if it has content
            addSectionIfNotEmpty(content, "Generale", sections.get("Generale"));
            addSectionIfNotEmpty(content, "Pupille", sections.get("Pupille"));
            addSectionIfNotEmpty(content, "Collo", sections.get("Collo"));
            addSectionIfNotEmpty(content, "Torace", sections.get("Torace"));
            addSectionIfNotEmpty(content, "Cuore", sections.get("Cuore"));
            addSectionIfNotEmpty(content, "Addome", sections.get("Addome"));
            addSectionIfNotEmpty(content, "Retto", sections.get("Retto"));
            addSectionIfNotEmpty(content, "Cute", sections.get("Cute"));
            addSectionIfNotEmpty(content, "Estremità", sections.get("Estremita"));
            addSectionIfNotEmpty(content, "Neurologico", sections.get("Neurologico"));
            addSectionIfNotEmpty(content, "FAST", sections.get("FAST"));

            // Check if all sections are empty
            boolean allEmpty = sections.values().stream().allMatch(String::isEmpty);
            if (allEmpty) {
                content.add(new Paragraph("Nessun dato inserito per l'esame fisico"));
            }
        } else {
            content.add(new Paragraph("Dati esame fisico non disponibili"));
        }

        details.setContent(content);
        parentLayout.add(details);
    }
    private void addSectionIfNotEmpty(VerticalLayout content, String title, String value) {
        if (value != null && !value.trim().isEmpty()) {
            addSection(content, title, value);
        }
    }
    private void addAdvancedScenarioSections(VerticalLayout parentLayout, AdvancedScenario advancedScenario) {
        Details details = new Details("Tempi (" + advancedScenario.getTempi().size() + ")");
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        for (Tempo tempo : advancedScenario.getTempi()) {
            VerticalLayout tempoLayout = new VerticalLayout();
            tempoLayout.setSpacing(false);
            tempoLayout.setPadding(false);

            H4 tempoTitle = new H4("Tempo ID " + tempo.getId() + " - Durata: " + tempo.getTimer_tempo() + " min");

            Grid<String> parametriGrid = new Grid<>();
            parametriGrid.setItems(
                    "Azione: " + tempo.getAzione(),
                    "PA: " + tempo.getPA() + " mmHg",
                    "FC: " + tempo.getFC() + " bpm",
                    "RR: " + tempo.getRR() + " atti/min",
                    "Temperatura: " + tempo.getT() + " °C",
                    "SpO2: " + tempo.getSpO2() + "%",
                    "EtCO2: " + tempo.getEtCO2() + " mmHg",
                    "Altri dettagli: " + tempo.getAltriDettagli()
            );
            parametriGrid.addColumn(s -> s).setHeader("Parametro");

            tempoLayout.add(tempoTitle, parametriGrid);
            content.add(tempoLayout, new Hr());
        }

        details.setContent(content);
        details.setOpened(true);
        parentLayout.add(details);
    }

    private Grid<Accesso> createAccessiGrid(List<Accesso> accessi) {
        Grid<Accesso> grid = new Grid<>();
        grid.setItems(accessi);

        grid.addColumn(Accesso::getTipologia)
                .setHeader("Tipologia")
                .setAutoWidth(true);

        grid.addColumn(Accesso::getPosizione)
                .setHeader("Posizione")
                .setAutoWidth(true);

        //grid.setHeightByRows(true);
        return grid;
    }

    private void addSection(VerticalLayout layout, String title, String content) {
        if (content == null || content.isEmpty()) return;

        H3 sectionTitle = new H3(title);
        sectionTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        Paragraph sectionContent = new Paragraph(content);
        sectionContent.setWidthFull();
        sectionContent.getStyle()
                .set("white-space", "pre-line")
                .set("margin-top", "0");

        layout.add(sectionTitle, sectionContent);
    }
}