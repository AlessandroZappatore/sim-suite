package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.views.home.AppHeader;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Tempo")
@Route("tempo")
@Menu(order = 14)
public class TempoView extends Composite<VerticalLayout> {

    private final VerticalLayout timeSectionsContainer;
    private final List<TimeSection> timeSections = new ArrayList<>();
    private int timeCount = 1;
    private Button nextButton;

    public TempoView() {
        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER con pulsante indietro e titolo
        AppHeader header = new AppHeader();

        // Pulsante indietro con RouterLink
        RouterLink backLink = new RouterLink();
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backLink.add(backButton);

        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);
        customHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        // Titolo della pagina
        H2 pageTitle = new H2("TEMPO");
        pageTitle.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.LARGE,
                LumoUtility.Margin.Horizontal.AUTO
        );
        pageTitle.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("width", "100%");

        // Istruzioni
        Paragraph instructionText = new Paragraph(
                "Definisci i tempi dello scenario. Per ogni tempo (T0, T1, T2, ecc.) specifica i parametri " +
                        "e le transizioni possibili. T0 rappresenta lo stato iniziale.");
        instructionText.setWidth("100%");
        instructionText.getStyle().set("font-size", "var(--lumo-font-size-m)");
        instructionText.addClassName(LumoUtility.Margin.Bottom.LARGE);

        // Container per le sezioni dei tempi
        timeSectionsContainer = new VerticalLayout();
        timeSectionsContainer.setWidthFull();
        timeSectionsContainer.setSpacing(true);

        // Aggiungi la sezione iniziale per T0
        addTimeSection(0);

        // Pulsante per aggiungere nuovi tempi
        Button addTimeButton = new Button("Aggiungi Tempo", new Icon(VaadinIcon.PLUS));
        addTimeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTimeButton.addClassName(LumoUtility.Margin.Top.MEDIUM);
        addTimeButton.addClickListener(event -> addTimeSection(timeCount++));

        contentLayout.add(pageTitle, instructionText, timeSectionsContainer, addTimeButton);

        // Listener per i pulsanti
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("")));

        // 3. FOOTER
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px");
        nextButton.addClickListener(e -> saveAllTimeSections());

        Paragraph credits = new Paragraph("Sviluppato e creato da Alessandro Zappatore");
        credits.addClassName(LumoUtility.TextColor.SECONDARY);
        credits.addClassName(LumoUtility.FontSize.XSMALL);
        credits.getStyle().set("margin", "0");

        footerLayout.add(credits, nextButton);

        // Aggiunta dei layout principali
        mainLayout.add(customHeader, contentLayout, footerLayout);
    }

    private void addTimeSection(int timeNumber) {
        TimeSection timeSection = new TimeSection(timeNumber);
        timeSections.add(timeSection);
        timeSectionsContainer.add(timeSection.getLayout());

        if (timeNumber == 0) {
            timeSection.hideRemoveButton();
        }
    }

    private void saveAllTimeSections() {
        for (TimeSection section : timeSections) {
            section.saveData();
        }
        nextButton.getUI().ifPresent(ui -> ui.navigate("moulage"));
    }

    private NumberField createMedicalField(String label, String unit) {
        NumberField field = new NumberField(label);
        field.setSuffixComponent(new Paragraph(unit));
        field.setWidthFull();
        field.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return field;
    }

    private class TimeSection {
        private final int timeNumber;
        private final VerticalLayout layout;
        private final TimePicker timerPicker;
        private final NumberField paField;
        private final NumberField fcField;
        private final NumberField rrField;
        private final NumberField tField;
        private final NumberField spo2Field;
        private final NumberField etco2Field;
        private final TextArea actionDetailsArea;
        private final IntegerField timeIfYesField;
        private final IntegerField timeIfNoField;
        private final TextArea additionalDetailsArea;
        private final Button removeButton;

        public TimeSection(int timeNumber) {
            this.timeNumber = timeNumber;

            layout = new VerticalLayout();
            layout.addClassName(LumoUtility.Padding.MEDIUM);
            layout.addClassName(LumoUtility.Border.ALL);
            layout.addClassName(LumoUtility.BorderColor.CONTRAST_10);
            layout.addClassName(LumoUtility.BorderRadius.MEDIUM);
            layout.setPadding(true);
            layout.setSpacing(false);

            Paragraph sectionTitle = new Paragraph("Tempo T" + timeNumber);
            sectionTitle.addClassName(LumoUtility.FontWeight.BOLD);
            sectionTitle.addClassName(LumoUtility.Margin.Bottom.NONE);

            timerPicker = new TimePicker("Timer");
            timerPicker.setStep(Duration.ofMinutes(1));
            timerPicker.setValue(LocalTime.parse("00:00"));
            timerPicker.setWidthFull();

            FormLayout medicalParamsForm = new FormLayout();
            medicalParamsForm.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 2),
                    new FormLayout.ResponsiveStep("500px", 3)
            );
            medicalParamsForm.setWidthFull();

            paField = createMedicalField("PA (mmHg)", "mmHg");
            fcField = createMedicalField("FC (bpm)", "battiti/min");
            rrField = createMedicalField("FR (rpm)", "respiri/min");
            tField = createMedicalField("Temperatura (°C)", "°C");
            spo2Field = createMedicalField("SpO₂ (%)", "%");
            etco2Field = createMedicalField("EtCO₂ (mmHg)", "mmHg");

            medicalParamsForm.add(paField, fcField, rrField, tField, spo2Field, etco2Field);
            medicalParamsForm.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            Hr divider = new Hr();
            divider.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

            Paragraph actionTitle = new Paragraph(timeNumber == 0 ?
                    "AZIONE INIZIALE (T0)" : "AZIONE DA SVOLGERE PER PASSARE A T" + timeNumber);
            actionTitle.getStyle()
                    .set("font-weight", "bold")
                    .set("text-align", "center")
                    .set("width", "100%");
            actionTitle.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            actionDetailsArea = new TextArea(timeNumber == 0 ?
                    "Descrizione situazione iniziale" : "Descrizione azione da svolgere");
            actionDetailsArea.setWidthFull();
            actionDetailsArea.setMinHeight("100px");
            actionDetailsArea.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            HorizontalLayout timeSelectionContainer = new HorizontalLayout();
            timeSelectionContainer.setWidthFull();
            timeSelectionContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

            FormLayout timeSelectionForm = new FormLayout();
            timeSelectionForm.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 2)
            );
            timeSelectionForm.setWidth("auto");

            timeIfYesField = new IntegerField(timeNumber == 0 ?
                    "Prossimo tempo se SI" : "Tempo se SI (Tn)");
            timeIfYesField.setMin(0);
            timeIfYesField.setWidth("120px");

            timeIfNoField = new IntegerField(timeNumber == 0 ?
                    "Prossimo tempo se NO" : "Tempo se NO (Tm)");
            timeIfNoField.setMin(0);
            timeIfNoField.setWidth("120px");

            timeSelectionForm.add(timeIfYesField, timeIfNoField);
            timeSelectionContainer.add(timeSelectionForm);
            timeSelectionContainer.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            additionalDetailsArea = new TextArea("Eventuali altri dettagli");
            additionalDetailsArea.setWidthFull();
            additionalDetailsArea.setMinHeight("150px");
            additionalDetailsArea.addClassName(LumoUtility.Margin.Bottom.LARGE);

            removeButton = new Button("Rimuovi Tempo", new Icon(VaadinIcon.TRASH));
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            removeButton.addClickListener(event -> {
                timeSections.remove(this);
                timeSectionsContainer.remove(layout);
            });

            layout.add(sectionTitle, timerPicker, medicalParamsForm, divider,
                    actionTitle, actionDetailsArea, timeSelectionForm,
                    additionalDetailsArea, removeButton);
        }

        public VerticalLayout getLayout() {
            return layout;
        }

        public void hideRemoveButton() {
            removeButton.setVisible(false);
        }

        public void saveData() {
            LocalTime time = timerPicker.getValue();
            double pa = paField.getValue() != null ? paField.getValue() : 0;
            double fc = fcField.getValue() != null ? fcField.getValue() : 0;
            double rr = rrField.getValue() != null ? rrField.getValue() : 0;
            double t = tField.getValue() != null ? tField.getValue() : 0;
            double spo2 = spo2Field.getValue() != null ? spo2Field.getValue() : 0;
            double etco2 = etco2Field.getValue() != null ? etco2Field.getValue() : 0;

            String actionDescription = actionDetailsArea.getValue();
            Integer nextTimeIfYes = timeIfYesField.getValue() != null ? timeIfYesField.getValue() : 0;
            Integer nextTimeIfNo = timeIfNoField.getValue() != null ? timeIfNoField.getValue() : 0;
            String additionalDetails = additionalDetailsArea.getValue();

            // Qui puoi implementare la logica per salvare i dati
            System.out.println("Salvati dati per T" + timeNumber);
            System.out.println("Timer: " + time);
            System.out.println("Parametri medici: PA=" + pa + ", FC=" + fc + ", FR=" + rr +
                    ", T=" + t + ", SpO2=" + spo2 + ", EtCO2=" + etco2);
            System.out.println("Azione: " + actionDescription);
            System.out.println("Transizioni: Se SI -> T" + nextTimeIfYes + ", Se NO -> T" + nextTimeIfNo);
            System.out.println("Dettagli aggiuntivi: " + additionalDetails);
        }
    }
}