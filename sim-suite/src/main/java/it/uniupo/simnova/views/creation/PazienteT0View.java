package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.Accesso;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.PresidiService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.support.AppHeader;
import it.uniupo.simnova.views.support.FieldGenerator;
import it.uniupo.simnova.views.support.StyleApp;
import it.uniupo.simnova.views.support.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Vista per la gestione dei parametri del paziente T0 nello scenario di simulazione.
 * <p>
 * Permette di definire i parametri vitali principali e gli accessi venosi e arteriosi.
 * Fa parte del flusso di creazione dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Parametri Paziente T0")
@Route(value = "pazienteT0")
@Menu(order = 12)
public class PazienteT0View extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(PazienteT0View.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    private final PresidiService presidiService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;

    /**
     * Campi per i parametri vitali principali.
     */
    private final TextField paField;
    /**
     * Campo per la frequenza cardiaca (FC).
     */
    private final NumberField fcField;
    /**
     * Campo per la frequenza respiratoria (RR).
     */
    private final NumberField rrField;
    /**
     * Campo per la temperatura corporea.
     */
    private final NumberField tempField;
    /**
     * Campo per la saturazione di ossigeno (SpO₂).
     */
    private final NumberField spo2Field;

    private final NumberField fio2Field;

    private final NumberField litrio2Field;
    /**
     * Campo per la pressione parziale di anidride carbonica (EtCO₂).
     */
    private final NumberField etco2Field;
    /**
     * Area di testo per il monitoraggio.
     */
    private final TextArea monitorArea;

    private final MultiSelectComboBox<String> presidiField;
    /**
     * Container per gli accessi venosi.
     */
    private static VerticalLayout venosiContainer = null;
    /**
     * Container per gli accessi arteriosi.
     */
    private static VerticalLayout arteriosiContainer = null;
    /**
     * Lista per memorizzare gli accessi venosi.
     */
    private static final List<AccessoComponent> venosiAccessi = new ArrayList<>();
    /**
     * Lista per memorizzare gli accessi arteriosi.
     */
    private static final List<AccessoComponent> arteriosiAccessi = new ArrayList<>();

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public PazienteT0View(ScenarioService scenarioService, FileStorageService fileStorageService, PresidiService presidiService) {
        this.scenarioService = scenarioService;
        this.presidiService = presidiService;
        // Configurazione layout principale
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // 1. HEADER con pulsante indietro
        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "PARAMETRI VITALI PRINCIPALI IN T0",
                "Definisci i parametri vitali principali del paziente T0",
                VaadinIcon.HEART,
                "var(--lumo-primary-color)"

        );


        // Campi parametri vitali
        paField = FieldGenerator.createTextField(
                "PA (mmHg)",
                "(es. 120/80)",
                true
        );
        fcField = FieldGenerator.createNumberField(
                "FC (battiti/min)",
                "(es. 80)",
                true
        );
        rrField = FieldGenerator.createNumberField(
                "RR (att/min)",
                "(es. 16)",
                true
        );
        tempField = FieldGenerator.createNumberField(
                "Temp. (°C)",
                "(es. 36.5)",
                true
        );
        spo2Field = FieldGenerator.createNumberField(
                "SpO₂ (%)",
                "(es. 98)",
                true
        );
        fio2Field = FieldGenerator.createNumberField(
                "FiO₂ (%)",
                "(es. 21)",
                false
        );
        litrio2Field = FieldGenerator.createNumberField(
                "L/min O₂",
                "(es. 5)",
                false
        );
        etco2Field = FieldGenerator.createNumberField(
                "EtCO₂ (mmHg)",
                "(es. 35)",
                true
        );

        // Container per accessi venosi
        venosiContainer = new VerticalLayout();
        venosiContainer.setWidthFull();
        venosiContainer.setSpacing(true);
        venosiContainer.setPadding(false);
        venosiContainer.setVisible(false);

        // Container per accessi arteriosi
        arteriosiContainer = new VerticalLayout();
        arteriosiContainer.setWidthFull();
        arteriosiContainer.setSpacing(true);
        arteriosiContainer.setPadding(false);
        arteriosiContainer.setVisible(false);

        Button addVenosiButton;
        Button addArteriosiButton;

        addVenosiButton = new Button("Aggiungi accesso venoso", new Icon(VaadinIcon.PLUS));
        addVenosiButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addVenosiButton.addClassName(LumoUtility.Margin.Top.SMALL);
        addVenosiButton.setVisible(false); // Inizialmente nascosto
        addVenosiButton.addClickListener(e -> addAccessoVenoso());

        addArteriosiButton = new Button("Aggiungi accesso arterioso", new Icon(VaadinIcon.PLUS));
        addArteriosiButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addArteriosiButton.addClassName(LumoUtility.Margin.Top.SMALL);
        addArteriosiButton.setVisible(false); // Inizialmente nascosto
        addArteriosiButton.addClickListener(e -> addAccessoArterioso());

        Checkbox venosiCheckbox = FieldGenerator.createCheckbox(
                "Accessi venosi"
        );
        venosiCheckbox.addValueChangeListener(e -> {
            venosiContainer.setVisible(e.getValue());
            addVenosiButton.setVisible(e.getValue()); // Mostra/nascondi il pulsante
            if (!e.getValue()) {
                venosiAccessi.clear();
                venosiContainer.removeAll();
            }
        });

        Checkbox arteriosiCheckbox = FieldGenerator.createCheckbox(
                "Accessi arteriosi"
        );
        arteriosiCheckbox.addValueChangeListener(e -> {
            arteriosiContainer.setVisible(e.getValue());
            addArteriosiButton.setVisible(e.getValue()); // Mostra/nascondi il pulsante
            if (!e.getValue()) {
                arteriosiAccessi.clear();
                arteriosiContainer.removeAll();
            }
        });

        // Area testo per monitor
        monitorArea = FieldGenerator.createTextArea(
                "Monitoraggio",
                "Specificare dettagli ECG o altri parametri...",
                false
        );


        List<String> longPresidiList = PresidiService.getAllPresidi();
        presidiField = FieldGenerator.createMultiSelectComboBox(
                "Presidi",
                longPresidiList,
                false
        );

        // Aggiunta componenti al layout
        contentLayout.add(
                headerSection,
                paField, fcField, rrField, tempField, spo2Field, fio2Field, litrio2Field, etco2Field,
                venosiCheckbox, venosiContainer, addVenosiButton,
                arteriosiCheckbox, arteriosiContainer, addArteriosiButton,
                monitorArea, presidiField
        );

        // 3. FOOTER con pulsanti e crediti
        Button nextButton = StyleApp.getNextButton();

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);
        // Aggiunta di tutti i componenti al layout principale
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        // Listener per i pulsanti
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("liquidi/" + scenarioId)));

        nextButton.addClickListener(e -> {
            if (!validateInput()) {
                return;
            }
            saveDataAndNavigate(nextButton.getUI());
        });
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
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
        }
    }

    /**
     * Aggiunge un accesso venoso al layout e alla lista degli accessi.
     */
    private void addAccessoVenoso() {
        AccessoComponent accesso = new AccessoComponent("Venoso");
        venosiAccessi.add(accesso);
        venosiContainer.add(accesso);
    }

    /**
     * Aggiunge un accesso arterioso al layout e alla lista degli accessi.
     */
    private void addAccessoArterioso() {
        AccessoComponent accesso = new AccessoComponent("Arterioso");
        arteriosiAccessi.add(accesso);
        arteriosiContainer.add(accesso);
    }

    /**
     * Valida i campi di input per assicurarsi che siano compilati correttamente.
     *
     * @return true se tutti i campi sono validi, false altrimenti
     */
    private boolean validateInput() {
        boolean isValid = true;
        if (paField.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(paField, "PA non valido");
        if (fcField.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(fcField, "FC non valido");
        if (rrField.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(rrField, "RR non valido");
        if (tempField.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(tempField, "Temperatura non valida");
        if (spo2Field.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(spo2Field, "SpO₂ non valido");
        if (etco2Field.isEmpty())
            isValid = ValidationError.showErrorAndReturnFalse(etco2Field, "EtCO₂ non valido");

        return isValid;
    }

    /**
     * Salva i dati del paziente e naviga alla vista successiva.
     *
     * @param uiOptional l'oggetto UI opzionale per la navigazione
     */
    private void saveDataAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                // Raccoglie gli accessi venosi e arteriosi direttamente dagli AccessoComponent
                List<Accesso> venosi = new ArrayList<>();
                for (AccessoComponent comp : venosiAccessi) {
                    venosi.add(comp.getAccesso());
                }

                List<Accesso> arteriosi = new ArrayList<>();
                for (AccessoComponent comp : arteriosiAccessi) {
                    arteriosi.add(comp.getAccesso());
                }
                // Salva nel database
                boolean success = scenarioService.savePazienteT0(
                        scenarioId,
                        paField.getValue(),
                        fcField.getValue().intValue(),
                        rrField.getValue().intValue(),
                        tempField.getValue(),
                        spo2Field.getValue().intValue(),
                        fio2Field.getValue() != null ? fio2Field.getValue().intValue() : 0,
                        litrio2Field.getValue() != null ? litrio2Field.getValue().intValue() : 0,
                        etco2Field.getValue().intValue(),
                        monitorArea.getValue(),
                        venosi,
                        arteriosi
                );

                boolean successPresidi = presidiService.savePresidi(scenarioId, presidiField.getValue());

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success && successPresidi) {
                        ui.navigate("esameFisico/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio dei dati",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio dei dati", e);
                });
            }
        });
    }

}