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
import it.uniupo.simnova.views.common.utils.FieldGenerator; // Assumendo che questo crei TextField standard
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
@Menu(order = 6)
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

        // Configurazione layout principale
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // 1. HEADER con pulsante indietro e titolo
        AppHeader header = new AppHeader(fileStorageService); // fileStorageService passato all'header
        Button backButton = StyleApp.getBackButton();
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE con azioni chiave dinamiche
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "AZIONI CHIAVE",
                "Definisci le azioni chiave che saranno valutate durante il debriefing",
                VaadinIcon.KEY.create(),
                "var(--lumo-primary-color)"
        );

        // Container per le azioni chiave
        actionFieldsContainer = new VerticalLayout();
        actionFieldsContainer.setWidthFull();
        actionFieldsContainer.setPadding(false);
        actionFieldsContainer.setSpacing(true); // Mantieni la spaziatura se desiderato

        // Pulsante per aggiungere nuove azioni chiave
        Button addButton = new Button("Aggiungi azione chiave", new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.getStyle().set("margin-top", "var(--lumo-space-m)");
        addButton.addClickListener(e -> addNewActionField("")); // Aggiunge un campo vuoto

        contentLayout.add(headerSection, actionFieldsContainer, addButton);

        // 3. FOOTER con pulsanti
        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Gestione eventi
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("pattoaula/" + scenarioId)));

        nextButton.addClickListener(e -> saveAzioniChiaveAndNavigate(nextButton.getUI()));
    }

    /**
     * Aggiunge un nuovo campo di input per un'azione chiave, con un valore iniziale opzionale.
     *
     * @param initialValue il valore iniziale per il campo di testo (può essere vuoto)
     */
    private void addNewActionField(String initialValue) {
        HorizontalLayout fieldLayout = new HorizontalLayout();
        fieldLayout.setWidthFull();
        fieldLayout.setSpacing(true); // Spaziatura tra TextField e bottone Rimuovi
        fieldLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Allinea alla baseline per un aspetto migliore

        TextField actionField = FieldGenerator.createTextField("Azione Chiave #" + size++,
                "Inserisci un'azione chiave",
                false);
        actionField.setValue(initialValue != null ? initialValue : "");

        // AGGIUNTO: Aggiungi il campo alla lista actionFields
        actionFields.add(actionField);

        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        removeButton.setAriaLabel("Rimuovi azione");// Per accessibilità
        removeButton.addClickListener(e -> {
            actionFields.remove(actionField);
            actionFieldsContainer.remove(fieldLayout);
            size--;
        });

        fieldLayout.addAndExpand(actionField); // actionField occupa lo spazio disponibile
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
                // Se nessun ID è fornito, potrebbe essere un nuovo scenario o un errore.
                // Decidi come gestire questo caso. Qui reindirizzo a una pagina di errore.
                logger.warn("ID scenario non fornito nell'URL.");
                event.rerouteToError(NotFoundException.class, "ID scenario non fornito.");
                return;
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) { // Assumendo che existScenario esista
                logger.warn("Tentativo di accesso a scenario non esistente o ID non valido: {}", scenarioId);
                event.rerouteToError(NotFoundException.class, "Scenario con ID " + scenarioId + " non trovato.");
                return;
            }
            logger.info("Caricamento azioni chiave per lo scenario ID: {}", scenarioId);
            loadExistingAzioniChiave();

        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido ricevuto come parametro: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "Formato ID scenario non valido: " + parameter);
        } catch (Exception e) { // Catch generico per altri errori imprevisti durante il setup
            logger.error("Errore imprevisto durante l'impostazione dei parametri per scenario ID: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "Errore durante il caricamento della pagina.");
        }
    }

    /**
     * Carica le azioni chiave esistenti per lo scenario corrente dal servizio.
     */
    private void loadExistingAzioniChiave() {
        actionFields.clear(); // Pulisci i campi esistenti prima di caricarne di nuovi
        actionFieldsContainer.removeAll(); // Rimuovi i componenti UI esistenti

        // Assumiamo che scenarioService.getNomiAzioniChiaveByScenarioId restituisca List<String>
        List<String> nomiAzioni = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);

        if (nomiAzioni != null && !nomiAzioni.isEmpty()) {
            for (String nomeAzione : nomiAzioni) {
                if (nomeAzione != null && !nomeAzione.trim().isEmpty()) {
                    addNewActionField(nomeAzione.trim());
                }
            }
        }

        // Se non ci sono azioni chiave caricate (o se la lista era vuota/null),
        // aggiungiamo un campo vuoto per iniziare.
        if (actionFields.isEmpty()) {
            addNewActionField("");
        }
    }

    /**
     * Salva le azioni chiave e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveAzioniChiaveAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);
            // Raccoglie i valori dai TextField
            List<String> nomiAzioniDaSalvare = actionFields.stream()
                    .map(TextField::getValue)
                    .map(String::trim) // Rimuove spazi bianchi
                    .filter(value -> !value.isEmpty()) // Filtra valori nulli o vuoti
                    .distinct() // Rimuove duplicati se necessario (opzionale, dipende dai requisiti)
                    .collect(Collectors.toList());
            logger.error("Nomi azioni da salvare: {}", nomiAzioniDaSalvare);

            try {
                boolean success = azioneChiaveService.updateAzioniChiaveForScenario(scenarioId, nomiAzioniDaSalvare);

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("obiettivididattici/" + scenarioId); // Naviga alla pagina successiva
                    } else {
                        Notification.show("Errore durante il salvataggio delle azioni chiave.", 5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.warn("Salvataggio azioni chiave fallito per scenario ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                getContent().remove(progressBar);
                logger.error("Errore durante il salvataggio delle azioni chiave per scenario ID: {}", scenarioId, e);
                ui.accessSynchronously(() -> { // Aggiorna l'UI nel thread dell'UI
                    Notification.show("Errore critico: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                });
            }
        });
    }
}

