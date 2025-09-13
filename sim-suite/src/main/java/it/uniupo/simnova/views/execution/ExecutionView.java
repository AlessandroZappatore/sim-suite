package it.uniupo.simnova.views.execution;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.dom.Element;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;
import it.uniupo.simnova.service.scenario.helper.TimelineConfiguration;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.ReusableTimer;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.TimesSupport;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@PageTitle("Execution")
@Route(value = "execution")
public class ExecutionView extends Composite<VerticalLayout> implements HasUrlParameter<String>, BeforeEnterObserver {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionView.class);
    private final ScenarioService scenarioService;
    private final FileStorageService fileStorageService;
    private final MaterialeService materialeService;
    private final AzioneChiaveService azioneChiaveService;
    private final EsameRefertoService esameRefertoService;
    private final AdvancedScenarioService advancedScenarioService;
    private Integer scenarioId;
    private Scenario scenario;


    @Autowired
    public ExecutionView(ScenarioService scenarioService, FileStorageService fileStorageService,
                         MaterialeService materialeService, AzioneChiaveService azioneChiaveService,
                         EsameRefertoService esameRefertoService, AdvancedScenarioService advancedScenarioService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.materialeService = materialeService;
        this.azioneChiaveService = azioneChiaveService;
        this.esameRefertoService = esameRefertoService;
        this.advancedScenarioService = advancedScenarioService;
        logger.info("Inizializzazione della vista di esecuzione con ID scenario predefinito: {}", scenarioId);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Il parametro ID scenario è nullo o vuoto.");
                throw new NumberFormatException("Il parametro ID scenario è nullo o vuoto.");
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0) {
                logger.warn("ID scenario non valido: {}. Deve essere un numero intero positivo.", scenarioId);
                throw new NumberFormatException("ID scenario deve essere un numero positivo.");
            }
            if (!scenarioService.existScenario(scenarioId)) {
                logger.warn("Tentativo di accesso a scenario non esistente con ID: {}.", scenarioId);
                throw new NotFoundException("Scenario con ID " + scenarioId + " non trovato.");
            }
            logger.info("Parametro ID scenario {} impostato con successo.", scenarioId);
        } catch (NumberFormatException e) {
            logger.error("Errore di formato per l'ID scenario ricevuto: '{}'. Dettagli: {}", parameter, e.getMessage());
            event.rerouteToError(NotFoundException.class, "ID scenario '" + parameter + "' non valido. " + e.getMessage());
        } catch (NotFoundException e) {
            logger.warn("Scenario non trovato durante l'impostazione del parametro: {}", e.getMessage());
            event.rerouteToError(NotFoundException.class, e.getMessage());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (scenarioId == null) {
            Notification.show("ID scenario non specificato. Impossibile caricare i dettagli.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("scenari");
            return;
        }
        this.scenario = scenarioService.getScenarioById(scenarioId);
        logger.info("Scenario con ID {} caricato con successo per la visualizzazione dettagliata.", scenarioId);
        initView();
    }


    private void initView() {
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("scenari")));

        ReusableTimer generalTimer = new ReusableTimer(
                "Timer Generale",
                Math.round(scenario.getTimerGenerale()),
                Notification.Position.MIDDLE
        );
        generalTimer.getStyle().set("margin-bottom", "var(--lumo-space-m)");


        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);
        customHeader.expand(header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();
        contentLayout.setMaxWidth("90%");

        HorizontalLayout threeColumnLayout = new HorizontalLayout();
        threeColumnLayout.setWidthFull();
        threeColumnLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        threeColumnLayout.setAlignItems(FlexComponent.Alignment.START);
        threeColumnLayout.setSpacing(true);

        VerticalLayout leftSidebar = new VerticalLayout();
        leftSidebar.setWidth("20%");
        leftSidebar.setSpacing(true);
        leftSidebar.addClassName("sticky-sidebar");

        Span leftTitle = new Span("Informazioni Generali");
        leftTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.SEMIBOLD);
        Button descriptionButton = StyleApp.getButton(
                "Descrizione",
                VaadinIcon.PENCIL.create(),
                ButtonVariant.LUMO_CONTRAST,
                "var(--lumo-primary-color)"
        );
        descriptionButton.addClickListener(e -> handleInfoButtonClick(scenario.getDescrizione(), "Descrizione"));
        Button briefingButton = StyleApp.getButton(
                "Briefing",
                VaadinIcon.GROUP.create(),
                ButtonVariant.LUMO_CONTRAST,
                "var(--lumo-primary-color)"
        );
        briefingButton.addClickListener(e -> handleInfoButtonClick(scenario.getBriefing(), "Briefing"));
        Button infoGenitoriButton = StyleApp.getButton(
                "Info Genitori",
                VaadinIcon.FAMILY.create(),
                ButtonVariant.LUMO_CONTRAST,
                "var(--lumo-primary-color)"
        );
        infoGenitoriButton.addClickListener(e -> handleInfoButtonClick(scenario.getInfoGenitore(), "Informazioni Genitori"));
        Button pattoAulaButton = StyleApp.getButton(
                "Patto d'Aula",
                VaadinIcon.HANDSHAKE.create(),
                ButtonVariant.LUMO_CONTRAST,
                "var(--lumo-primary-color)"
        );
        pattoAulaButton.addClickListener(e -> handleInfoButtonClick(scenario.getPattoAula(), "Patto d'Aula"));
        Button azioniChiaveButton = StyleApp.getButton(
                "Azioni Chiave",
                VaadinIcon.KEY.create(),
                ButtonVariant.LUMO_CONTRAST,
                "var(--lumo-primary-color)"
        );
        azioniChiaveButton.addClickListener(e -> handleInfoButtonClick(azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId).toString(), "Azioni Chiave"));

        Button obiettiviDidatticiButton = StyleApp.getButton(
                "Obiettivi Didattici",
                VaadinIcon.BOOK.create(),
                ButtonVariant.LUMO_CONTRAST,
                "var(--lumo-primary-color)"
        );
        obiettiviDidatticiButton.addClickListener(e -> handleInfoButtonClick(scenario.getObiettivo(), "Obiettivi Didattici"));
        Button moulageButton = StyleApp.getButton(
                "Moulage",
                VaadinIcon.EYE.create(),
                ButtonVariant.LUMO_CONTRAST,
                "var(--lumo-primary-color)"
        );
        moulageButton.addClickListener(e -> handleInfoButtonClick(scenario.getMoulage(), "Moulage"));
        Button liquidiEDosiFarmaciButton = StyleApp.getButton(
                "Liquidi e Dosi Farmaci",
                VaadinIcon.DROP.create(),
                ButtonVariant.LUMO_CONTRAST,
                "var(--lumo-primary-color)"
        );
        liquidiEDosiFarmaciButton.addClickListener(e -> handleInfoButtonClick(scenario.getLiquidi(), "Liquidi e Dosi Farmaci"));
        Button materialeNecessarioButton = StyleApp.getButton(
                "Materiale Necessario",
                VaadinIcon.TOOLS.create(),
                ButtonVariant.LUMO_CONTRAST,
                "var(--lumo-primary-color)"
        );
        materialeNecessarioButton.addClickListener(e -> handleInfoButtonClick(
                materialeService.toStringAllMaterialsByScenarioId(scenarioId), "Materiale Necessario"));

        leftSidebar.add(generalTimer, leftTitle, descriptionButton, briefingButton,
                infoGenitoriButton, pattoAulaButton, azioniChiaveButton,
                obiettiviDidatticiButton, moulageButton, liquidiEDosiFarmaciButton,
                materialeNecessarioButton);

        VerticalLayout centerContent = new VerticalLayout();
        List<Tempo> tempi = advancedScenarioService.getTempiByScenarioId(scenarioId);
        if (!tempi.isEmpty()) {
            TimelineConfiguration readOnlyConfig = getTimelineConfiguration();

            Component timelineContent = TimesSupport.createTimelineContent(
                    tempi,
                    scenarioService.isPediatric(scenarioId),
                    readOnlyConfig
            );

            centerContent.add(timelineContent);
        } else {
            logger.debug("Nessun tempo trovato per lo scenario ID {}. Scheda 'Timeline' non aggiunta.", scenarioId);
        }

        VerticalLayout rightSidebar = new VerticalLayout();
        rightSidebar.setWidth("20%");
        rightSidebar.setSpacing(false);
        rightSidebar.addClassName("sticky-sidebar");

        Span rightTitle = new Span("Esami e Referti");
        rightTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.Margin.Bottom.MEDIUM);
        rightSidebar.add(rightTitle);

        List<EsameReferto> esamiReferti = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);

        if (esamiReferti.isEmpty()) {
            rightSidebar.add(new Span("Nessun esame disponibile per questo scenario."));
        } else {
            esamiReferti.forEach(esame -> {
                VerticalLayout esameLayout = new VerticalLayout();
                esameLayout.setSpacing(false);
                esameLayout.setPadding(false);
                esameLayout.getStyle().set("gap", "var(--lumo-space-s)");

                Span esameTitle = new Span(esame.getTipoEsame());
                esameTitle.getStyle().set("font-weight", "bold");

                HorizontalLayout buttonLayout = new HorizontalLayout();

                Button viewMediaButton = StyleApp.getButton(
                        "Esame",
                        VaadinIcon.PAPERCLIP.create(),
                        ButtonVariant.LUMO_PRIMARY,
                        "var(--lumo-primary-color)"
                );
                boolean hasMedia = esame.getMedia() != null && !esame.getMedia().trim().isEmpty();
                viewMediaButton.setEnabled(hasMedia);
                if (hasMedia) {
                    viewMediaButton.addClickListener(e -> showMediaDialog("Esame: " + esame.getTipoEsame(), esame.getMedia()));
                }

                Button viewReportButton = StyleApp.getButton(
                        "Referto",
                        VaadinIcon.FILE_TEXT_O.create(),
                        ButtonVariant.LUMO_PRIMARY,
                        "var(--lumo-primary-color)"
                );
                boolean hasReferto = esame.getReferto() != null && !esame.getReferto().trim().isEmpty();
                viewReportButton.setEnabled(hasReferto);
                if (hasReferto) {
                    viewReportButton.addClickListener(e -> handleInfoButtonClick(esame.getReferto(), "Referto: " + esame.getTipoEsame()));
                }

                buttonLayout.add(viewMediaButton, viewReportButton);
                esameLayout.add(esameTitle, buttonLayout);
                rightSidebar.add(esameLayout, new Hr());
            });
        }

        threeColumnLayout.add(leftSidebar, centerContent, rightSidebar);
        threeColumnLayout.expand(centerContent);

        contentLayout.add(threeColumnLayout);

        HorizontalLayout footerSection = StyleApp.getFooterLayout(null);
        mainLayout.add(customHeader, contentLayout, footerSection);
    }

    private TimelineConfiguration getTimelineConfiguration() {
        TimelineConfiguration readOnlyConfig = new TimelineConfiguration(advancedScenarioService, scenarioId, false);

        readOnlyConfig.setHeaderGenerator((tempo, config) -> {
            VerticalLayout timeHeader = new VerticalLayout();
            timeHeader.setSpacing(false);
            timeHeader.setPadding(false);
            timeHeader.setAlignItems(FlexComponent.Alignment.CENTER);

            H3 timeTitle = new H3("T" + tempo.getIdTempo());
            timeTitle.getStyle().set("margin-bottom", "var(--lumo-space-s)");

            ReusableTimer timer = new ReusableTimer(
                    "Timer T" + tempo.getIdTempo(),
                    (int) tempo.getTimerTempo() / 60,
                    Notification.Position.BOTTOM_START
            );
            timer.getStyle().set("font-size", "var(--lumo-font-size-xl)");
            timer.getStyle().set("margin-bottom", "var(--lumo-space-m)");

            timeHeader.add(timeTitle, timer);
            return timeHeader;
        });

        readOnlyConfig.setCardCustomizer((cardWrapper, tempo) -> {
            VerticalLayout notesSection = new VerticalLayout();
            notesSection.setWidthFull();
            notesSection.getStyle()
                    .set("background-color", "var(--lumo-contrast-5pct)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("padding", "var(--lumo-space-m)");

            H4 notesTitle = new H4("Note Esecuzione");
            notesTitle.getStyle().set("margin-top", "0");

            TextArea notesArea = new TextArea();
            notesArea.setPlaceholder("Scrivi qui le tue note per lo step T" + tempo.getIdTempo() + "...");
            notesArea.setWidthFull();

            notesSection.add(notesTitle, notesArea);

            cardWrapper.add(notesSection);
        });
        return readOnlyConfig;
    }

    private void showInfoDialog(String title, String content) {
        String safeContent = Jsoup.clean(content, Safelist.basic());
        Div contentDiv = new Div();
        contentDiv.getElement().setProperty("innerHTML", safeContent);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.add(contentDiv);

        Button closeButton = new Button("Chiudi", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(closeButton);

        dialog.setWidth("80%");
        dialog.setMaxWidth("800px");
        dialog.open();
    }


    private void showMediaDialog(String title, String mediaPath) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);

        StreamResource resource = fileStorageService.getFileAsResource(mediaPath);
        if (resource == null) {
            dialog.add(new Span("Impossibile caricare il file: " + mediaPath));
        } else {
            String lowerCasePath = mediaPath.toLowerCase();
            Component mediaComponent;

            if (lowerCasePath.endsWith(".pdf")) {
                Element objectElement = new Element("object");
                objectElement.setAttribute("data", resource);
                objectElement.setAttribute("type", "application/pdf");
                objectElement.getStyle().set("width", "100%");
                objectElement.getStyle().set("height", "75vh");

                Element fallbackText = new Element("p");
                fallbackText.setText("Il tuo browser non supporta l'anteprima PDF. ");

                Element fallbackLink = new Element("a");
                fallbackLink.setAttribute("href", resource);
                fallbackLink.setAttribute("target", "_blank");
                fallbackLink.setText("Apri il PDF qui.");

                fallbackText.appendChild(fallbackLink);
                objectElement.appendChild(fallbackText);

                Div pdfContainer = new Div();
                pdfContainer.getElement().appendChild(objectElement);
                mediaComponent = pdfContainer;

            } else if (lowerCasePath.endsWith(".png") || lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg") || lowerCasePath.endsWith(".gif")) {
                Image image = new Image(resource, "Anteprima Immagine");
                image.setMaxWidth("100%");
                mediaComponent = image;
            } else if (lowerCasePath.endsWith(".mp3") || lowerCasePath.endsWith(".wav") || lowerCasePath.endsWith(".ogg")) {
                Div audioContainer = new Div();
                Element audioElement = new Element("audio");
                audioElement.setAttribute("src", resource);
                audioElement.setAttribute("controls", true);
                audioContainer.getElement().appendChild(audioElement);
                mediaComponent = audioContainer;
            } else if (lowerCasePath.endsWith(".mp4") || lowerCasePath.endsWith(".webm")) {
                Div videoContainer = new Div();
                Element videoElement = new Element("video");
                videoElement.setAttribute("src", resource);
                videoElement.setAttribute("controls", true);
                videoElement.getStyle().set("max-width", "100%");
                videoContainer.getElement().appendChild(videoElement);
                mediaComponent = videoContainer;
            } else {
                Button openButton = new Button("Apri in nuova scheda", VaadinIcon.EXTERNAL_LINK.create());
                openButton.addClickListener(event -> UI.getCurrent().getPage().open("media/" + mediaPath, "_blank"));
                mediaComponent = openButton;
            }
            dialog.add(mediaComponent);
        }

        Button closeButton = new Button("Chiudi", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(closeButton);

        dialog.setWidth("90%");
        dialog.setMaxWidth("1000px");
        dialog.open();
    }

    private void handleInfoButtonClick(String content, String contentName) {
        if (content == null || content.trim().isEmpty()) {
            String errorMessage = "Nessun contenuto per '" + contentName.toLowerCase() + "' disponibile per questo scenario.";
            Notification.show(errorMessage, 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            String dialogTitle = contentName.contains(":") ? contentName : contentName + " Scenario";
            showInfoDialog(dialogTitle, content);
        }
    }
}