package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.views.home.AppHeader;

@PageTitle("Parametri Paziente T0")
@Route("pazienteT0")
@Menu(order = 12)
public class PazienteT0View extends Composite<VerticalLayout> {

    private Select<String> venosiSelect;
    private Select<String> arteriosiSelect;

    public PazienteT0View() {
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
        NumberField paField = createNumberField("PA (mmHg)", "120/80");
        NumberField fcField = createNumberField("FC (bpm)", "72");
        NumberField rrField = createNumberField("FR (atti/min)", "16");
        NumberField tempField = createNumberField("Temperatura (°C)", "36.5");
        NumberField spo2Field = createNumberField("SpO₂ (%)", "98");
        NumberField etco2Field = createNumberField("EtCO₂ (mmHg)", "35");

        // Checkbox e select per accessi
        Checkbox venosiCheckbox = new Checkbox("Accessi venosi");
        venosiCheckbox.addClassName(LumoUtility.Margin.Top.MEDIUM);

        venosiSelect = new Select<>();
        venosiSelect.setLabel("Tipo accesso venoso");
        venosiSelect.setItems("Periferico", "Centrale", "PICC", "Midline", "Altro");
        venosiSelect.setWidthFull();
        venosiSelect.setVisible(false);

        Checkbox arteriosiCheckbox = new Checkbox("Accessi arteriosi");
        arteriosiCheckbox.addClassName(LumoUtility.Margin.Top.MEDIUM);

        arteriosiSelect = new Select<>();
        arteriosiSelect.setLabel("Tipo accesso arterioso");
        arteriosiSelect.setItems("Radiale", "Femorale", "Umerale", "Altro");
        arteriosiSelect.setWidthFull();
        arteriosiSelect.setVisible(false);

        // Listener per checkbox
        venosiCheckbox.addValueChangeListener(e -> {
            venosiSelect.setVisible(e.getValue());
            if (!e.getValue()) venosiSelect.clear();
        });

        arteriosiCheckbox.addValueChangeListener(e -> {
            arteriosiSelect.setVisible(e.getValue());
            if (!e.getValue()) arteriosiSelect.clear();
        });

        // Area testo per monitor
        TextArea monitorArea = new TextArea("Monitoraggio");
        monitorArea.setPlaceholder("Specificare dettagli ECG o altri parametri...");
        monitorArea.setWidthFull();
        monitorArea.setMinHeight("150px");
        monitorArea.addClassName(LumoUtility.Margin.Top.LARGE);

        // Aggiunta componenti al layout
        contentLayout.add(
                title,
                paField, fcField, rrField, tempField, spo2Field, etco2Field,
                venosiCheckbox, venosiSelect,
                arteriosiCheckbox, arteriosiSelect,
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
                backButton.getUI().ifPresent(ui -> ui.navigate("liquidi")));

        nextButton.addClickListener(e -> {
            // Qui puoi aggiungere la logica per salvare i dati
            nextButton.getUI().ifPresent(ui -> ui.navigate("esamefisico"));
        });
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
}