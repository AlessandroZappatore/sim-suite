package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
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
    /**
     * Campo per la pressione parziale di anidride carbonica (EtCO₂).
     */
    private final NumberField etco2Field;
    /**
     * Area di testo per il monitoraggio.
     */
    private final TextArea monitorArea;
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
    public PazienteT0View(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER con pulsante indietro
        AppHeader header = new AppHeader();

        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("800px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        // Titolo sezione
        H3 title = new H3("PARAMETRI VITALI PRINCIPALI IN T0");
        title.addClassName(LumoUtility.Margin.Bottom.LARGE);

        // Campi parametri vitali
        paField = createTextField();
        fcField = createNumberField("FC (bpm)", "(es. 72)");
        rrField = createNumberField("RR (atti/min)", "(es. 16)");
        tempField = createNumberField("Temperatura (°C)", "(es. 36.5)");
        spo2Field = createNumberField("SpO₂ (%)", "(es. 98)");
        etco2Field = createNumberField("EtCO₂ (mmHg)", "(es. 35)");

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

        Checkbox venosiCheckbox = new Checkbox("Accessi venosi");
        venosiCheckbox.addClassName(LumoUtility.Margin.Top.MEDIUM);
        venosiCheckbox.addValueChangeListener(e -> {
            venosiContainer.setVisible(e.getValue());
            addVenosiButton.setVisible(e.getValue()); // Mostra/nascondi il pulsante
            if (!e.getValue()) {
                venosiAccessi.clear();
                venosiContainer.removeAll();
            }
        });

        Checkbox arteriosiCheckbox = new Checkbox("Accessi arteriosi");
        arteriosiCheckbox.addClassName(LumoUtility.Margin.Top.MEDIUM);
        arteriosiCheckbox.addValueChangeListener(e -> {
            arteriosiContainer.setVisible(e.getValue());
            addArteriosiButton.setVisible(e.getValue()); // Mostra/nascondi il pulsante
            if (!e.getValue()) {
                arteriosiAccessi.clear();
                arteriosiContainer.removeAll();
            }
        });

        // Area testo per monitor
        monitorArea = new TextArea("Monitoraggio");
        monitorArea.setPlaceholder("Specificare dettagli ECG o altri parametri...");
        monitorArea.setWidthFull();
        monitorArea.setMinHeight("150px");
        monitorArea.addClassName(LumoUtility.Margin.Top.LARGE);

        // Aggiunta componenti al layout
        contentLayout.add(
                title,
                paField, fcField, rrField, tempField, spo2Field, etco2Field,
                venosiCheckbox, venosiContainer, addVenosiButton,
                arteriosiCheckbox, arteriosiContainer, addArteriosiButton,
                monitorArea
        );

        // 3. FOOTER con pulsanti e crediti
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px");

        Paragraph credits = new Paragraph("Sviluppato e creato da Alessandro Zappatore");
        credits.addClassName(LumoUtility.TextColor.SECONDARY);
        credits.addClassName(LumoUtility.FontSize.XSMALL);
        credits.getStyle().set("margin", "0");

        footerLayout.add(credits, nextButton);

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
                Notification.show("Compila tutti i campi obbligatori", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
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
     * Crea un campo di input numerico con etichetta e segnaposto.
     *
     * @param label      l'etichetta del campo
     * @param placeholder il testo segnaposto
     * @return il campo di input numerico creato
     */
    private NumberField createNumberField(String label, String placeholder) {
        NumberField field = new NumberField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.setMin(0);
        field.setStep(0.1);
        field.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return field;
    }
    /**
     * Crea un campo di input di testo con etichetta e segnaposto.
     *
     * @return il campo di input di testo creato
     */
    private TextField createTextField() {
        TextField field = new TextField("PA (mmHg)");
        field.setPlaceholder("(es. 120/80)");
        field.setWidthFull();
        field.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return field;
    }
    /**
     * Aggiunge un accesso venoso al layout e alla lista degli accessi.
     */
    private void addAccessoVenoso() {
        AccessoComponent accesso = new AccessoComponent("Venoso", venosiAccessi.size() + 1);
        venosiAccessi.add(accesso);
        venosiContainer.add(accesso);
    }

    /**
     * Aggiunge un accesso arterioso al layout e alla lista degli accessi.
     */
    private void addAccessoArterioso() {
        AccessoComponent accesso = new AccessoComponent("Arterioso", arteriosiAccessi.size() + 1);
        arteriosiAccessi.add(accesso);
        arteriosiContainer.add(accesso);
    }

    /**
     * Valida i campi di input per assicurarsi che siano compilati correttamente.
     *
     * @return true se tutti i campi sono validi, false altrimenti
     */
    private boolean validateInput() {
        // Validazione dei campi obbligatori
        return !paField.isEmpty() && !fcField.isEmpty() && !rrField.isEmpty() &&
                !tempField.isEmpty() && !spo2Field.isEmpty() && !etco2Field.isEmpty();
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
                // Prepara i dati degli accessi venosi
                List<AccessoData> venosiData = new ArrayList<>();
                for (AccessoComponent accesso : venosiAccessi) {
                    venosiData.add(accesso.getData());
                }

                // Prepara i dati degli accessi arteriosi
                List<AccessoData> arteriosiData = new ArrayList<>();
                for (AccessoComponent accesso : arteriosiAccessi) {
                    arteriosiData.add(accesso.getData());
                }

                System.out.println("Temperatura: " + tempField.getValue());

                // Salva nel database
                boolean success = scenarioService.savePazienteT0(
                        scenarioId,
                        paField.getValue(),
                        fcField.getValue().intValue(),
                        rrField.getValue().intValue(),
                        tempField.getValue(),
                        spo2Field.getValue().intValue(),
                        etco2Field.getValue().intValue(),
                        monitorArea.getValue(),
                        venosiData,
                        arteriosiData
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("esameFisico/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio dei dati",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio dei dati", e);
                });
            }
        });
    }

    /**
     * Componente per la gestione degli accessi venosi e arteriosi.
     * Contiene un campo di selezione per il tipo di accesso e un campo di testo per la posizione.
     */
    private static class AccessoComponent extends HorizontalLayout {
        /**
         * Campo di selezione per il tipo di accesso (venoso o arterioso).
         */
        private final Select<String> tipoSelect;
        /**
         * Campo di testo per la posizione dell'accesso.
         */
        private final TextField posizioneField;

        /**
         * Costruttore del componente di accesso.
         *
         * @param tipo          il tipo di accesso (venoso o arterioso)
         * @param ignoredNumero numero di accessi (non utilizzato)
         */
        public AccessoComponent(String tipo, int ignoredNumero) {
            setWidthFull();
            setAlignItems(Alignment.BASELINE);
            setSpacing(true);

            tipoSelect = new Select<>();
            tipoSelect.setLabel("Tipo accesso " + tipo);
            if (tipo.equals("Venoso")) {
                tipoSelect.setItems("Periferico", "Centrale", "PICC", "Midline", "Altro");
            } else {
                tipoSelect.setItems("Radiale", "Femorale", "Omerale", "Altro");
            }
            tipoSelect.setWidth("200px");

            posizioneField = new TextField("Posizione");
            posizioneField.setWidthFull();

            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            removeButton.getStyle().set("margin-top", "auto");
            removeButton.addClickListener(e -> removeSelf()); // Aggiunto il listener

            add(tipoSelect, posizioneField, removeButton);
        }

        /**
         * Rimuove il componente di accesso dalla vista e dalla lista appropriata.
         */
        private void removeSelf() {
            // Ottieni il parent (che dovrebbe essere il venosiContainer o arteriosiContainer)
            Optional<Component> parentOpt = getParent();

            parentOpt.ifPresent(parent -> {
                if (parent instanceof VerticalLayout container) {
                    // Rimuovi questo componente dal container
                    container.remove(this);

                    // Rimuovi anche dalla lista appropriata
                    if (container == venosiContainer) {
                        venosiAccessi.remove(this);
                    } else if (container == arteriosiContainer) {
                        arteriosiAccessi.remove(this);
                    }
                }
            });
        }

        /**
         * Restituisce i dati dell'accesso come oggetto AccessoData.
         *
         * @return i dati dell'accesso
         */
        public AccessoData getData() {
            return new AccessoData(
                    tipoSelect.getValue(),
                    posizioneField.getValue()
            );
        }
    }

    /**
     * Classe record per rappresentare i dati di un accesso.
     *
     * @param tipo      il tipo di accesso (venoso o arterioso)
     * @param posizione la posizione dell'accesso
     */
    public record AccessoData(String tipo, String posizione) {
    }
}