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

@PageTitle("Tempo")
@Route("tempo")
@Menu(order = 14)
public class TempoView extends Composite<VerticalLayout> {

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
                "Da leggere \"Sono al tempo T0, con Parametri T0, se viene svolta un'azione vado al tempo Tn, " +
                        "con parametri Tn, se no passo al Tm con parametri Tm.\" Riscrivere qua sotto i parametri inseriti precedentemente.");
        instructionText.setWidth("100%");
        instructionText.getStyle().set("font-size", "var(--lumo-font-size-m)");
        instructionText.addClassName(LumoUtility.Margin.Bottom.LARGE);

        // Riga con Tempo corrente e Timer
        HorizontalLayout timeRow = new HorizontalLayout();
        timeRow.setWidthFull();
        timeRow.setSpacing(true);
        timeRow.setAlignItems(FlexComponent.Alignment.END);

        // Campo per il numero del tempo corrente (T0, T1, T2...)
        IntegerField currentTimeField = new IntegerField("Tempo corrente");
        currentTimeField.setMin(0);
        currentTimeField.setValue(0);
        currentTimeField.setWidth("50%");

        // Timer per il tempo specifico (solo orario)
        TimePicker timerPicker = new TimePicker("Timer");
        timerPicker.setStep(Duration.ofMinutes(1));
        timerPicker.setValue(LocalTime.now());
        timerPicker.setWidth("50%");

        timeRow.add(currentTimeField, timerPicker);
        timeRow.addClassName(LumoUtility.Margin.Bottom.LARGE);

        // Parametri medici con unità di misura
        FormLayout medicalParamsForm = new FormLayout();
        medicalParamsForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2),
                new FormLayout.ResponsiveStep("500px", 3)
        );
        medicalParamsForm.setWidthFull();

        NumberField paField = createMedicalField("PA (mmHg)", "mmHg");
        NumberField fcField = createMedicalField("FC (bpm)", "battiti/min");
        NumberField rrField = createMedicalField("FR (rpm)", "respiri/min");
        NumberField tField = createMedicalField("Temperatura (°C)", "°C");
        NumberField spo2Field = createMedicalField("SpO₂ (%)", "%");
        NumberField etco2Field = createMedicalField("EtCO₂ (mmHg)", "mmHg");

        medicalParamsForm.add(paField, fcField, rrField, tField, spo2Field, etco2Field);
        medicalParamsForm.addClassName(LumoUtility.Margin.Bottom.LARGE);

        // Divider
        Hr divider = new Hr();
        divider.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        // Sezione azione
        Paragraph actionTitle = new Paragraph("AZIONE DA SVOLGERE PER PASSARE AL PROSSIMO Tn");
        actionTitle.getStyle()
                .set("font-weight", "bold")
                .set("text-align", "center")
                .set("width", "100%");
        actionTitle.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        // Area dettagli azione
        TextArea actionDetailsArea = new TextArea("Descrizione azione da svolgere");
        actionDetailsArea.setWidthFull();
        actionDetailsArea.setMinHeight("100px");
        actionDetailsArea.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        // Tabella SI | NO con campi per i tempi (T0, T1, T2...)
        HorizontalLayout timeSelectionContainer = new HorizontalLayout();
        timeSelectionContainer.setWidthFull();
        timeSelectionContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        FormLayout timeSelectionForm = new FormLayout();
        timeSelectionForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
        );
        timeSelectionForm.setWidth("auto"); // Larghezza automatica per centrarlo

        // Creazione dei campi per i tempi
        IntegerField timeIfYesField = new IntegerField("Tempo se SI (Tn)");
        timeIfYesField.setMin(0);
        timeIfYesField.setWidth("120px"); // Larghezza fissa per uniformità

        IntegerField timeIfNoField = new IntegerField("Tempo se NO (Tm)");
        timeIfNoField.setMin(0);
        timeIfNoField.setWidth("120px"); // Larghezza fissa per uniformità

        // Aggiungi i campi al form
        timeSelectionForm.add(timeIfYesField, timeIfNoField);
        timeSelectionContainer.add(timeSelectionForm);
        timeSelectionContainer.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        // Area dettagli aggiuntivi
        TextArea additionalDetailsArea = new TextArea("Eventuali altri dettagli");
        additionalDetailsArea.setWidthFull();
        additionalDetailsArea.setMinHeight("150px");
        additionalDetailsArea.addClassName(LumoUtility.Margin.Bottom.LARGE);

        // Aggiunta componenti al contentLayout
        contentLayout.add(pageTitle, instructionText, timeRow,
                medicalParamsForm, divider, actionTitle, actionDetailsArea,
                timeSelectionForm, additionalDetailsArea);

        // 3. FOOTER
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

        // Aggiunta dei layout principali
        mainLayout.add(customHeader, contentLayout, footerLayout);
    }

    private NumberField createMedicalField(String label, String unit) {
        NumberField field = new NumberField(label);
        field.setSuffixComponent(new Paragraph(unit));
        field.setWidthFull();
        field.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return field;
    }
}