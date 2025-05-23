package it.uniupo.simnova.views.creation.paziente;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.*;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Vista per la gestione dell'esame fisico del paziente nello scenario di simulazione.
 * <p>
 * Permette di inserire e modificare i risultati dell'esame fisico divisi per sezioni anatomiche.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Esame Fisico")
@Route(value = "esameFisico")
@Menu(order = 13)
public class EsamefisicoView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(EsamefisicoView.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    private final EsameFisicoService esameFisicoService;
    /**
     * Servizio per la gestione del caricamento dei file.
     */
    private final FileStorageService fileStorageService;
    /**
     * Mappa per memorizzare le aree di testo delle sezioni dell'esame fisico.
     */
    private final Map<String, TinyMce> examSections = new HashMap<>();
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public EsamefisicoView(ScenarioService scenarioService, FileStorageService fileStorageService, EsameFisicoService esameFisicoService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.esameFisicoService = esameFisicoService;
        setupView();
    }

    /**
     * Configura la struttura principale della vista.
     */
    private void setupView() {
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "ESAME FISICO",
                "Definisci il briefing che verrà mostrato ai discenti prima della simulazione",
                VaadinIcon.STETHOSCOPE.create(),
                "var(--lumo-primary-color)"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        contentLayout.add(headerSection);
        setupExamSections(contentLayout);

        // 3. FOOTER
        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        Button scrollToTopButton = StyleApp.getScrollButton();
        Button scrollDownButton = StyleApp.getScrollDownButton();
        VerticalLayout scrollLayout = new VerticalLayout(scrollToTopButton, scrollDownButton);

        mainLayout.add(scrollLayout);
        // Aggiunta componenti al layout principale
        mainLayout.add(customHeader, contentLayout, footerLayout);

        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("pazienteT0/" + scenarioId)));
        nextButton.addClickListener(e -> saveExamAndNavigate(nextButton.getUI()));

    }


    /**
     * Configura le sezioni dell'esame fisico.
     *
     * @param contentLayout layout a cui aggiungere le sezioni
     */
    private void setupExamSections(VerticalLayout contentLayout) {
    VerticalLayout examSectionsLayout = new VerticalLayout();
    examSectionsLayout.setWidthFull();
    examSectionsLayout.setSpacing(true);
    examSectionsLayout.setPadding(false);

    // Definizione delle sezioni dell'esame fisico con colori per categorie
    String[][] sections = {
            {"Generale", "Stato generale, livello di coscienza, etc.", "#4285F4"},
            {"Pupille", "Dimensione, reattività, simmetria", "#4285F4"},
            {"Collo", "Esame del collo, tiroide, linfonodi", "#0F9D58"},
            {"Torace", "Ispezione, palpazione, percussione, auscultazione", "#0F9D58"},
            {"Cuore", "Frequenza, ritmo, soffi", "#0F9D58"},
            {"Addome", "Ispezione, palpazione, dolorabilità, organomegalie", "#DB4437"},
            {"Retto", "Esame rettale se indicato", "#DB4437"},
            {"Cute", "Colorito, turgore, lesioni", "#F4B400"},
            {"Estremità", "Edemi, pulsazioni periferiche", "#F4B400"},
            {"Neurologico", "Stato mentale, nervi cranici, forza, sensibilità", "#673AB7"},
            {"FAST", "Focused Assessment with Sonography for Trauma", "#673AB7"}
    };

    // Creazione delle aree di testo per ogni sezione
    for (String[] section : sections) {
        String sectionName = section[0];
        String sectionDesc = section[1];
        String sectionColor = section[2];

        // Layout per la sezione completa
        VerticalLayout sectionLayout = new VerticalLayout();
        sectionLayout.setWidthFull();
        sectionLayout.setPadding(true);
        sectionLayout.setSpacing(false);
        sectionLayout.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "12px")
                .set("margin-bottom", "1.5rem")
                .set("border-left", "4px solid " + sectionColor)
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)");

        // Layout per il titolo e descrizione
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setSpacing(true);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Utilizzo delle icone da getSectionIcon invece delle lettere
        Icon sectionIcon = getSectionIcon(sectionName);
        Div iconCircle = new Div();
        iconCircle.add(sectionIcon);
        iconCircle.getStyle()
                .set("background-color", sectionColor)
                .set("color", "white")
                .set("border-radius", "50%")
                .set("width", "36px")
                .set("height", "36px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-right", "12px");

        // Imposta le dimensioni dell'icona
        sectionIcon.getStyle()
                .set("color", "white")
                .set("width", "20px")
                .set("height", "20px");

        // Layout per titolo e descrizione
        VerticalLayout titleDescLayout = new VerticalLayout();
        titleDescLayout.setPadding(false);
        titleDescLayout.setSpacing(false);

        // Titolo migliorato
        H3 sectionTitle = new H3(sectionName);
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "18px")
                .set("color", sectionColor)
                .set("font-weight", "600");

        // Descrizione migliorata
        Paragraph sectionDescription = new Paragraph(sectionDesc);
        sectionDescription.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "14px")
                .set("font-weight", "400");

        titleDescLayout.add(sectionTitle, sectionDescription);
        headerLayout.add(iconCircle, titleDescLayout);

        // Editor con bordo abbinato al colore della sezione
        TinyMce editor = TinyEditor.getEditor();
        editor.getStyle()
                .set("margin-top", "12px")
                .set("border", "1px solid " + sectionColor + "30")
                .set("border-radius", "8px");

        examSections.put(sectionName, editor);

        sectionLayout.add(headerLayout, editor);
        examSectionsLayout.add(sectionLayout);
    }

    contentLayout.add(examSectionsLayout);
}

/**
 * Restituisce l'icona corrispondente alla sezione dell'esame fisico
 *
 * @param sectionTitle il nome della sezione
 * @return l'icona corrispondente
 */
private static Icon getSectionIcon(String sectionTitle) {
    return switch (sectionTitle) {
        case "Generale" -> new Icon(VaadinIcon.CLIPBOARD_PULSE);
        case "Pupille" -> new Icon(VaadinIcon.EYE);
        case "Collo" -> new Icon(VaadinIcon.USER);
        case "Torace" -> FontAwesome.Solid.LUNGS.create();
        case "Cuore" -> new Icon(VaadinIcon.HEART);
        case "Addome" -> FontAwesome.Solid.A.create();
        case "Retto" -> FontAwesome.Solid.POOP.create();
        case "Cute" -> FontAwesome.Solid.HAND_DOTS.create();
        case "Estremità" -> FontAwesome.Solid.HANDS.create();
        case "Neurologico" -> FontAwesome.Solid.BRAIN.create();
        case "FAST" -> new Icon(VaadinIcon.AMBULANCE);
        default -> new Icon(VaadinIcon.INFO);
    };
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

            loadExistingExamData();
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Carica i dati dell'esame fisico esistente per lo scenario corrente.
     */
    private void loadExistingExamData() {
        EsameFisico esameFisico = esameFisicoService.getEsameFisicoById(scenarioId);

        if (esameFisico != null) {
            Map<String, String> savedSections = esameFisico.getSections();
            savedSections.forEach((sectionName, value) -> {
                TinyMce editor = examSections.get(sectionName);
                if (editor != null) {
                    editor.setValue(value != null ? value : "");
                }
            });
        } else {
            examSections.values().forEach(editor -> editor.setValue(""));
        }
    }

    /**
     * Salva l'esame fisico e naviga alla vista successiva in base al tipo di scenario.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveExamAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                Map<String, String> examData = new HashMap<>();
                examSections.forEach((section, editor) -> examData.put(section, editor.getValue()));

                boolean success = esameFisicoService.addEsameFisico(
                        scenarioId, examData
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (!success) {
                        Notification.show("Errore durante il salvataggio dell'esame fisico",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.error("Errore durante il salvataggio dell'esame fisico per lo scenario con ID: {}", scenarioId);
                        return;
                    }

                    // Navigazione differenziata in base al tipo di scenario
                    String scenarioType = scenarioService.getScenarioType(scenarioId);
                    if ("Quick Scenario".equals(scenarioType)) {
                        ui.navigate("scenari/" + scenarioId);
                    } else if ("Advanced Scenario".equals(scenarioType) ||
                            "Patient Simulated Scenario".equals(scenarioType)) {
                        ui.navigate("tempi/" + scenarioId + "/create");
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio dell'esame fisico per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}
