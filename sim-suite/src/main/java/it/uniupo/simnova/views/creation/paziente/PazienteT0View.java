package it.uniupo.simnova.views.creation.paziente;

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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.components.PresidiService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.ValidationError;
import it.uniupo.simnova.views.ui.helper.AccessoComponent;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PageTitle("Parametri Paziente T0")
@Route(value = "pazienteT0")
public class PazienteT0View extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private static final Logger logger = LoggerFactory.getLogger(PazienteT0View.class);

    private final List<AccessoComponent> venosiAccessi = new ArrayList<>();
    private final List<AccessoComponent> arteriosiAccessi = new ArrayList<>();

    private final VerticalLayout venosiContainer;
    private final VerticalLayout arteriosiContainer;

    private final ScenarioService scenarioService;
    private final PresidiService presidiService;
    private final PazienteT0Service pazienteT0Service;

    private final TextField paField;
    private final NumberField fcField;
    private final NumberField rrField;
    private final NumberField tempField;
    private final NumberField spo2Field;
    private final NumberField fio2Field;
    private final NumberField litrio2Field;
    private final NumberField etco2Field;
    private final TextArea monitorArea;
    private final MultiSelectComboBox<String> presidiField;
    private final Button nextButton;
    private final Checkbox venosiCheckbox = FieldGenerator.createCheckbox("Accessi venosi");
    private final Checkbox arteriosiCheckbox = FieldGenerator.createCheckbox("Accessi arteriosi");

    private Integer scenarioId;
    private String mode;
    private PazienteT0 currentPazienteT0;

    public PazienteT0View(ScenarioService scenarioService, FileStorageService fileStorageService, PresidiService presidiService, PazienteT0Service pazienteT0Service) {
        this.scenarioService = scenarioService;
        this.presidiService = presidiService;
        this.pazienteT0Service = pazienteT0Service;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());
        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna ai liquidi e dosi farmaci");
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);
        VerticalLayout contentLayout = StyleApp.getContentLayout();
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "PARAMETRI VITALI PRINCIPALI IN T0",
                "Definisci i parametri vitali principali del paziente al tempo T0 (iniziale).",
                VaadinIcon.HEART.create(),
                "var(--lumo-primary-color)"
        );
        paField = FieldGenerator.createTextField("PA (mmHg)", "(es. 120/80)", true);
        paField.setPattern("\\d{1,3}/\\d{1,3}");
        paField.setErrorMessage("Formato non valido. Es: 120/80");

        fcField = FieldGenerator.createNumberField("FC (battiti/min)", "(es. 80)", true);
        rrField = FieldGenerator.createNumberField("RR (att/min)", "(es. 16)", true);
        tempField = FieldGenerator.createNumberField("Temp. (°C)", "(es. 36.5)", true);
        spo2Field = FieldGenerator.createNumberField("SpO₂ (%)", "(es. 98)", true);
        fio2Field = FieldGenerator.createNumberField("FiO₂ (%)", "(es. 21)", false);
        litrio2Field = FieldGenerator.createNumberField("L/min O₂", "(es. 5)", false);
        etco2Field = FieldGenerator.createNumberField("EtCO₂ (mmHg)", "(es. 35)", false);

        venosiContainer = new VerticalLayout();
        venosiContainer.setWidthFull();
        venosiContainer.setSpacing(true);
        venosiContainer.setPadding(false);
        venosiContainer.setVisible(false);

        arteriosiContainer = new VerticalLayout();
        arteriosiContainer.setWidthFull();
        arteriosiContainer.setSpacing(true);
        arteriosiContainer.setPadding(false);
        arteriosiContainer.setVisible(false);

        Button addVenosiButton = StyleApp.getButton("Aggiungi accesso venoso", VaadinIcon.PLUS.create(), ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addVenosiButton.setVisible(false);
        addVenosiButton.addClickListener(e -> addAccessoVenoso());

        Button addArteriosiButton = StyleApp.getButton("Aggiungi accesso arterioso", VaadinIcon.PLUS.create(), ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addArteriosiButton.setVisible(false);
        addArteriosiButton.addClickListener(e -> addAccessoArterioso());

        venosiCheckbox.addValueChangeListener(e -> {
            venosiContainer.setVisible(e.getValue());
            addVenosiButton.setVisible(e.getValue());
            if (!e.getValue()) {
                venosiAccessi.clear();
                venosiContainer.removeAll();
            }
        });

        arteriosiCheckbox.addValueChangeListener(e -> {
            arteriosiContainer.setVisible(e.getValue());
            addArteriosiButton.setVisible(e.getValue());
            if (!e.getValue()) {
                arteriosiAccessi.clear();
                arteriosiContainer.removeAll();
            }
        });

        monitorArea = FieldGenerator.createTextArea("Monitoraggio", "Specificare dettagli ECG o altri parametri...", false);
        List<String> allPresidiList = PresidiService.getAllPresidi();
        presidiField = FieldGenerator.createMultiSelectComboBox("Presidi", allPresidiList, false);

        contentLayout.add(
                headerSection,
                paField, fcField, rrField, tempField, spo2Field, fio2Field, litrio2Field, etco2Field,
                venosiCheckbox, venosiContainer, addVenosiButton,
                arteriosiCheckbox, arteriosiContainer, addArteriosiButton,
                monitorArea, presidiField
        );

        nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);
        mainLayout.add(customHeader, contentLayout, footerLayout);
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("liquidi/" + scenarioId)));

        nextButton.addClickListener(e -> {
            if (!validateInput()) {
                return;
            }
            saveDataAndNavigate(nextButton.getUI());
        });
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro mancante nell'URL.");
                throw new NumberFormatException("ID Scenario è richiesto");
            }
            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0];
            this.scenarioId = Integer.parseInt(scenarioIdStr.trim());
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("ID Scenario non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("ID Scenario non valido");
            }
            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";
            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);
            VerticalLayout mainLayout = getContent();
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst()
                    .ifPresent(headerLayout -> headerLayout.setVisible(!"edit".equals(mode)));
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second)
                    .ifPresent(footerLayout -> footerLayout.getChildren()
                            .filter(component -> component instanceof CreditsComponent)
                            .forEach(credits -> credits.setVisible(!"edit".equals(mode))));

            if ("edit".equals(mode)) {
                logger.info("Modalità EDIT: caricamento dati per scenario {}", this.scenarioId);
                nextButton.setText("Salva");
                nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                nextButton.setIcon(new Icon(VaadinIcon.CHECK));
                nextButton.setIconAfterText(false);
            } else {
                logger.info("Modalità CREATE: preparazione per scenario {}", this.scenarioId);
            }
            loadData();
        } catch (NumberFormatException e) {
            logger.error("Errore nel parsing o validazione dell'ID Scenario: '{}'", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante.");
        }
    }

    private void addAccessoVenoso() {
        AccessoComponent accesso = new AccessoComponent("Venoso", true);
        venosiAccessi.add(accesso);
        venosiContainer.add(accesso);
    }

    private void addAccessoArterioso() {
        AccessoComponent accesso = new AccessoComponent("Arterioso", true);
        arteriosiAccessi.add(accesso);
        arteriosiContainer.add(accesso);
    }

    private boolean validateInput() {
        boolean isValid = true;
        if (paField.isInvalid() || paField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(paField, paField.isEmpty() ? "La Pressione Arteriosa (PA) è obbligatoria." : "Formato PA non valido. Es: 120/80");
        }
        if (fcField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(fcField, "La Frequenza Cardiaca (FC) è obbligatoria.");
        }
        if (rrField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(rrField, "La Frequenza Respiratoria (RR) è obbligatoria.");
        }
        if (tempField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(tempField, "La Temperatura è obbligatoria.");
        }
        if (spo2Field.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(spo2Field, "La Saturazione di Ossigeno (SpO₂) è obbligatoria.");
        }
        return isValid;
    }

    /**
     * NUOVO METODO: Salva i dati usando il nuovo PazienteT0Service.
     */
    private void saveDataAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                PazienteT0 pazienteToSave = (currentPazienteT0 != null) ? currentPazienteT0 : new PazienteT0();

                if (pazienteToSave.getId() == null) {
                    pazienteToSave.setId(scenarioId);
                }

                String[] paValues = paField.getValue().split("/");
                pazienteToSave.setPaSistolica(Integer.parseInt(paValues[0].trim()));
                pazienteToSave.setPaDiastolica(Integer.parseInt(paValues[1].trim()));
                pazienteToSave.setFc(fcField.getValue().intValue());
                pazienteToSave.setRr(rrField.getValue().intValue());
                pazienteToSave.setT(tempField.getValue().floatValue());
                pazienteToSave.setSpo2(spo2Field.getValue().intValue());
                pazienteToSave.setFio2(fio2Field.isEmpty() ? null : fio2Field.getValue().intValue());
                pazienteToSave.setLitriOssigeno(litrio2Field.isEmpty() ? null : litrio2Field.getValue().floatValue());
                pazienteToSave.setEtco2(etco2Field.isEmpty() ? null : etco2Field.getValue().intValue());
                pazienteToSave.setMonitor(monitorArea.getValue());

                List<Accesso> tuttiGliAccessi = new ArrayList<>();
                for (AccessoComponent comp : venosiAccessi) {
                    Accesso accesso = comp.getAccesso();
                    accesso.setTipo("venoso");
                    tuttiGliAccessi.add(accesso);
                }
                for (AccessoComponent comp : arteriosiAccessi) {
                    Accesso accesso = comp.getAccesso();
                    accesso.setTipo("arterioso");
                    tuttiGliAccessi.add(accesso);
                }

                pazienteToSave.getAccessi().clear();
                pazienteToSave.getAccessi().addAll(tuttiGliAccessi);

                pazienteT0Service.savePazienteT0(pazienteToSave);
                boolean successPresidi = presidiService.savePresidi(scenarioId, presidiField.getValue());

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (successPresidi) {
                        String message = mode.equals("edit") ? "Dati T0 aggiornati con successo" : "Dati T0 salvati con successo";
                        Notification.show(message, 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        String nextPage = mode.equals("edit") ? "scenari/" + scenarioId : "esameFisico/" + scenarioId;
                        ui.navigate(nextPage);
                    } else {
                        Notification.show("Errore durante il salvataggio dei presidi. Riprova.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });

            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Si è verificato un errore inaspettato: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio dei dati del paziente T0 per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }

    /**
     * NUOVO METODO: Carica i dati usando il nuovo PazienteT0Service.
     */
    private void loadData() {
        try {
            currentPazienteT0 = pazienteT0Service.getPazienteT0ById(scenarioId);

            paField.setValue(String.format("%d/%d", currentPazienteT0.getPaSistolica(), currentPazienteT0.getPaDiastolica()));
            fcField.setValue((double) currentPazienteT0.getFc());
            rrField.setValue((double) currentPazienteT0.getRr());
            tempField.setValue(Math.round(currentPazienteT0.getT() * 10.0) / 10.0);
            spo2Field.setValue((double) currentPazienteT0.getSpo2());
            if (currentPazienteT0.getFio2() != null) fio2Field.setValue((double) currentPazienteT0.getFio2());
            if (currentPazienteT0.getLitriOssigeno() != null) litrio2Field.setValue((double) currentPazienteT0.getLitriOssigeno());
            if (currentPazienteT0.getEtco2() != null) etco2Field.setValue((double) currentPazienteT0.getEtco2());
            monitorArea.setValue(currentPazienteT0.getMonitor());

            venosiAccessi.clear();
            venosiContainer.removeAll();
            arteriosiAccessi.clear();
            arteriosiContainer.removeAll();

            if (currentPazienteT0.getAccessi() != null && !currentPazienteT0.getAccessi().isEmpty()) {
                boolean hasVenosi = false;
                boolean hasArteriosi = false;

                for (Accesso a : currentPazienteT0.getAccessi()) {
                    if ("venoso".equalsIgnoreCase(a.getTipo())) {
                        AccessoComponent comp = new AccessoComponent(a, "Venoso", true);
                        venosiAccessi.add(comp);
                        venosiContainer.add(comp);
                        hasVenosi = true;
                    } else if ("arterioso".equalsIgnoreCase(a.getTipo())) {
                        AccessoComponent comp = new AccessoComponent(a, "Arterioso", true);
                        arteriosiAccessi.add(comp);
                        arteriosiContainer.add(comp);
                        hasArteriosi = true;
                    }
                }
                venosiCheckbox.setValue(hasVenosi);
                arteriosiCheckbox.setValue(hasArteriosi);
            } else {
                venosiCheckbox.setValue(false);
                arteriosiCheckbox.setValue(false);
            }

            presidiField.setValue(PresidiService.getPresidiByScenarioId(scenarioId));

        } catch (EntityNotFoundException e) {
            logger.warn("Nessun dato T0 trovato per lo scenario con ID: {}. Si tratta di una nuova creazione.", scenarioId);
            currentPazienteT0 = null;
        }
    }
}