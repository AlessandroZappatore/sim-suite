package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.home.CreditsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import java.util.Optional;

/**
 * Vista per la gestione della descrizione dello scenario di simulazione.
 * <p>
 * Permette di inserire e modificare la descrizione testuale dello scenario
 * durante il processo di creazione. Fa parte del flusso di creazione scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Descrizione")
@Route(value = "descrizione")
@Menu(order = 3)
public class DescrizioneView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(DescrizioneView.class);
    /**
     * Servizio per la gestione degli scenari di simulazione.
     */
    private final ScenarioService scenarioService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Area di testo per l'inserimento della descrizione dello scenario.
     */
    private final TinyMce descriptionEditor;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public DescrizioneView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");
        mainLayout.getStyle().set("background-color", "var(--lumo-base-color)");

        // 1. HEADER con pulsante indietro e titolo
        AppHeader header = new AppHeader(fileStorageService);

        // Modernizzare il pulsante indietro
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle()
                .set("margin-right", "auto")
                .set("transition", "all 0.2s ease")
                .set("font-weight", "500");
        backButton.addClassName("hover-effect");

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);
        customHeader.getStyle().set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)");

        VerticalLayout headerSection = new VerticalLayout();
        headerSection.setPadding(true);
        headerSection.setSpacing(false);
        headerSection.setWidthFull();
        headerSection.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "8px")
                .set("margin-top", "1rem")
                .set("margin-bottom", "1rem")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        // Crea un layout dedicato per icona e titolo
        HorizontalLayout titleWithIconLayout = new HorizontalLayout();
        titleWithIconLayout.setSpacing(true);
        titleWithIconLayout.setPadding(false);
        titleWithIconLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleWithIconLayout.getStyle().set("margin-bottom", "0.5rem");

        // Stile dell'icona migliorato
        Icon icon = new Icon(VaadinIcon.PENCIL);
        icon.setSize("2em");
        icon.getStyle()
                .set("margin-right", "0.75em")
                .set("color", "#4285F4") // Colore blu Google-style
                .set("background", "rgba(66, 133, 244, 0.1)")
                .set("padding", "10px")
                .set("border-radius", "50%");

        // Titolo con colore personalizzato
        H2 title = new H2("DESCRIZIONE DELLO SCENARIO");
        title.addClassName(LumoUtility.Margin.Bottom.NONE);
        title.addClassName(LumoUtility.Margin.Top.NONE);
        title.getStyle()
                .set("text-align", "center")
                .set("color", "#4285F4") // Colore blu Google-style
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px");

        // Aggiungi titolo e icona al layout dedicato
        titleWithIconLayout.add(icon, title);

        // Layout principale con centratura
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        headerLayout.add(titleWithIconLayout);

        Paragraph subtitle = new Paragraph("Inserisci una descrizione dettagliata dello scenario di simulazione. Questa descrizione fornirà il contesto generale e le informazioni di background ai partecipanti.");
        subtitle.addClassName(LumoUtility.Margin.Top.XSMALL);
        subtitle.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("max-width", "750px")
                .set("text-align", "center")
                .set("font-weight", "400")
                .set("line-height", "1.6");

        headerSection.add(headerLayout, subtitle);

        // 2. CONTENUTO PRINCIPALE con area di testo
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("850px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        descriptionEditor = new TinyMce();
        descriptionEditor.setWidthFull();
        descriptionEditor.setHeight("450px");
        descriptionEditor.configure("plugins: 'link lists table hr pagebreak image charmap preview', " +
                "toolbar: 'undo redo | blocks | bold italic | alignleft aligncenter alignright | bullist numlist | link image | table hr', " +
                "menubar: true, " +
                "skin: 'oxide', " +
                "content_css: 'default', " +
                "statusbar: true, " +
                "resize: true");

        // Aggiungere un bordo moderno all'editor
        descriptionEditor.getElement().getStyle()
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)");

        contentLayout.add(headerSection, descriptionEditor);

        // 3. FOOTER con pulsanti e crediti
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle()
                .set("border-color", "var(--lumo-contrast-10pct)")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "0 -2px 10px rgba(0, 0, 0, 0.03)");

        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px");
        nextButton.getStyle()
                .set("border-radius", "30px")
                .set("font-weight", "600")
                .set("transition", "transform 0.2s ease");

        // Aggiungere effetto hover tramite classe CSS globale
        UI.getCurrent().getPage().executeJs(
                "document.head.innerHTML += '<style>" +
                        ".hover-effect:hover { transform: translateY(-2px); }" +
                        "button:active { transform: scale(0.98); }" +
                        "</style>';"
        );

        nextButton.addClassName("hover-effect");

        CreditsComponent creditsLayout = new CreditsComponent();

        // Aggiunta dei crediti e del bottone al layout del footer
        footerLayout.add(creditsLayout, nextButton);

        // Aggiunta componenti al layout principale
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Gestione eventi
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("target/" + scenarioId)));

        nextButton.addClickListener(e -> saveDescriptionAndNavigate(nextButton.getUI()));
    }

    /**
     * Gestisce il parametro ricevuto dall'URL (ID scenario).
     *
     * @param event     l'evento di navigazione
     * @param parameter l'ID dello scenario come stringa
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }

            loadExistingDescription();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Carica la descrizione esistente per lo scenario corrente.
     */
    private void loadExistingDescription() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty()) {
            descriptionEditor.setValue(scenario.getDescrizione());
        }
    }

    /**
     * Salva la descrizione e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveDescriptionAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                // Salva la descrizione, anche se vuota
                boolean success = scenarioService.updateScenarioDescription(
                        scenarioId, descriptionEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("briefing/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio della descrizione",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio della descrizione", e);
                });
            }
        });
    }
}
