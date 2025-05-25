package it.uniupo.simnova.views.creation.scenario;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Vista per la gestione delle azioni chiave nello scenario di simulazione.
 * <p>
 * Permette di definire le azioni principali che saranno valutate durante il debriefing.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 * Questa versione è aggiornata per funzionare con una gestione delle azioni chiave
 * basata su tabelle separate (AzioniChiave e AzioneScenario) invece di una stringa delimitata.
 *
 * @author Alessandro Zappatore
 * @version 1.1
 */
@PageTitle("Azioni Chiave")
@Route(value = "azionechiave")
public class AzionechiaveView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(AzionechiaveView.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    private final AzioneChiaveService azioneChiaveService;
    /**
     * Layout contenente le caselle di testo per le azioni chiave.
     */
    private final VerticalLayout actionFieldsContainer;
    /**
     * Lista dei campi di testo per le azioni chiave.
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<TextField> actionFields = new ArrayList<>();
    private int size = 1;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService    servizio per la gestione degli scenari
     * @param fileStorageService servizio per la gestione dei file (usato nell'header)
     */
    public AzionechiaveView(ScenarioService scenarioService, FileStorageService fileStorageService, AzioneChiaveService azioneChiaveService) {
        this.scenarioService = scenarioService;
        this.azioneChiaveService = azioneChiaveService;


        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());


        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "AZIONI CHIAVE",
                "Definisci le azioni chiave che saranno valutate durante il debriefing",
                VaadinIcon.KEY.create(),
                "var(--lumo-primary-color)"
        );


        actionFieldsContainer = new VerticalLayout();
        actionFieldsContainer.setWidthFull();
        actionFieldsContainer.setPadding(false);
        actionFieldsContainer.setSpacing(true);


        Button addButton = new Button("Aggiungi azione chiave", new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.getStyle().set("margin-top", "var(--lumo-space-m)");
        addButton.addClickListener(e -> addNewActionField(""));

        contentLayout.add(headerSection, actionFieldsContainer, addButton);


        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);


        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("pattoaula/" + scenarioId)));

        nextButton.addClickListener(e -> {

            List<String> content = actionFields.stream()
                    .map(TextField::getValue)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            boolean isEmpty = content.isEmpty();

            if (isEmpty) {

                StyleApp.createConfirmDialog(
                        "Descrizione vuota",
                        "Sei sicuro di voler continuare senza una descrizione?",
                        "Prosegui",
                        "Annulla",
                        () -> saveAzioniChiaveAndNavigate(nextButton.getUI(), content)
                );
            } else {

                saveAzioniChiaveAndNavigate(nextButton.getUI(), content);
            }
        });    }

    /**
     * Aggiunge un nuovo campo di input per un'azione chiave, con un valore iniziale opzionale.
     *
     * @param initialValue il valore iniziale per il campo di testo (può essere vuoto)
     */
    private void addNewActionField(String initialValue) {
        HorizontalLayout fieldLayout = new HorizontalLayout();
        fieldLayout.setWidthFull();
        fieldLayout.setSpacing(true);
        fieldLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        TextField actionField = FieldGenerator.createTextField("Azione Chiave #" + size++,
                "Inserisci un'azione chiave",
                false);
        actionField.setValue(initialValue != null ? initialValue : "");


        actionFields.add(actionField);

        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        removeButton.setAriaLabel("Rimuovi azione");
        removeButton.addClickListener(e -> {
            actionFields.remove(actionField);
            actionFieldsContainer.remove(fieldLayout);
            size--;
        });

        fieldLayout.addAndExpand(actionField);
        fieldLayout.add(removeButton);
        actionFieldsContainer.add(fieldLayout);
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


                logger.warn("ID scenario non fornito nell'URL.");
                event.rerouteToError(NotFoundException.class, "ID scenario non fornito.");
                return;
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Tentativo di accesso a scenario non esistente o ID non valido: {}", scenarioId);
                event.rerouteToError(NotFoundException.class, "Scenario con ID " + scenarioId + " non trovato.");
                return;
            }
            logger.info("Caricamento azioni chiave per lo scenario ID: {}", scenarioId);
            loadExistingAzioniChiave();

        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido ricevuto come parametro: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "Formato ID scenario non valido: " + parameter);
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'impostazione dei parametri per scenario ID: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "Errore durante il caricamento della pagina.");
        }
    }

    /**
     * Carica le azioni chiave esistenti per lo scenario corrente dal servizio.
     */
    private void loadExistingAzioniChiave() {
        actionFields.clear();
        actionFieldsContainer.removeAll();


        List<String> nomiAzioni = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);

        if (nomiAzioni != null && !nomiAzioni.isEmpty()) {
            for (String nomeAzione : nomiAzioni) {
                if (nomeAzione != null && !nomeAzione.trim().isEmpty()) {
                    addNewActionField(nomeAzione.trim());
                }
            }
        }



        if (actionFields.isEmpty()) {
            addNewActionField("");
        }
    }

    /**
     * Salva le azioni chiave e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveAzioniChiaveAndNavigate(Optional<UI> uiOptional, List<String> nomiAzioniDaSalvare) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);


            try {
                boolean success = azioneChiaveService.updateAzioniChiaveForScenario(scenarioId, nomiAzioniDaSalvare);

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("obiettivididattici/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio delle azioni chiave.", 5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.warn("Salvataggio azioni chiave fallito per scenario ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                getContent().remove(progressBar);
                logger.error("Errore durante il salvataggio delle azioni chiave per scenario ID: {}", scenarioId, e);
                ui.accessSynchronously(() -> Notification.show("Errore critico: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR));
            }
        });
    }
}

