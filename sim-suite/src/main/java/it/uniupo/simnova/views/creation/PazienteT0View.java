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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PageTitle("Parametri Paziente T0")
@Route(value = "pazienteT0")
@Menu(order = 12)
public class PazienteT0View extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final ScenarioService scenarioService;
    private Integer scenarioId;

    private final TextField paField;
    private final NumberField fcField;
    private final NumberField rrField;
    private final NumberField tempField;
    private final NumberField spo2Field;
    private final NumberField etco2Field;
    private final TextArea monitorArea;

    private static VerticalLayout venosiContainer = null;
    private static VerticalLayout arteriosiContainer = null;
    private static final List<AccessoComponent> venosiAccessi = new ArrayList<>();
    private static final List<AccessoComponent> arteriosiAccessi = new ArrayList<>();

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
        fcField = createNumberField("FC (bpm)", "72");
        rrField = createNumberField("FR (atti/min)", "16");
        tempField = createNumberField("Temperatura (°C)", "36.5");
        spo2Field = createNumberField("SpO₂ (%)", "98");
        etco2Field = createNumberField("EtCO₂ (mmHg)", "35");

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
                Notification.show("Compila tutti i campi obbligatori", 3000, Notification.Position.MIDDLE);
                return;
            }
            saveDataAndNavigate(nextButton.getUI());
        });
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0) {
                throw new NumberFormatException();
            }

            // Qui potresti caricare i dati esistenti se presenti
            // loadExistingData();
        } catch (NumberFormatException e) {
            event.rerouteToError(NotFoundException.class, "ID scenario non valido");
        }
    }

    private NumberField createNumberField(String label, String placeholder) {
        NumberField field = new NumberField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.setMin(0);
        field.setStep(0.1);
        field.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return field;
    }

    private TextField createTextField() {
        TextField field = new TextField("PA (mmHg)");
        field.setPlaceholder("120/80");
        field.setWidthFull();
        field.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return field;
    }

    private void addAccessoVenoso() {
        AccessoComponent accesso = new AccessoComponent("Venoso", venosiAccessi.size() + 1);
        venosiAccessi.add(accesso);
        venosiContainer.add(accesso);
    }

    private void addAccessoArterioso() {
        AccessoComponent accesso = new AccessoComponent("Arterioso", arteriosiAccessi.size() + 1);
        arteriosiAccessi.add(accesso);
        arteriosiContainer.add(accesso);
    }

    private boolean validateInput() {
        // Validazione dei campi obbligatori
        return !paField.isEmpty() && !fcField.isEmpty() && !rrField.isEmpty() &&
                !tempField.isEmpty() && !spo2Field.isEmpty() && !etco2Field.isEmpty();
    }

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

                // Salva nel database
                boolean success = scenarioService.savePazienteT0(
                        scenarioId,
                        paField.getValue(),
                        fcField.getValue().intValue(),
                        rrField.getValue().intValue(),
                        tempField.getValue().floatValue(),
                        spo2Field.getValue().intValue(),
                        etco2Field.getValue().intValue(),
                        monitorArea.getValue(),
                        venosiData,
                        arteriosiData
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("esamefisico/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio dei dati",
                                3000, Notification.Position.MIDDLE);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                    e.printStackTrace();
                });
            }
        });
    }

    // Classe interna per rappresentare un componente accesso
    private static class AccessoComponent extends HorizontalLayout {
        private final Select<String> tipoSelect;
        private final TextField posizioneField;
        private final Button removeButton;

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

            removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            removeButton.getStyle().set("margin-top", "auto");
            removeButton.addClickListener(e -> removeSelf()); // Aggiunto il listener

            add(tipoSelect, posizioneField, removeButton);
        }

        private void removeSelf() {
            // Ottieni il parent (che dovrebbe essere il venosiContainer o arteriosiContainer)
            Optional<Component> parentOpt = Optional.ofNullable(getParent().orElse(null));

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

        public AccessoData getData() {
            return new AccessoData(
                    tipoSelect.getValue(),
                    posizioneField.getValue()
            );
        }
    }

    // Classe per rappresentare i dati di un accesso
    public record AccessoData(String tipo, String posizione) {
    }
}