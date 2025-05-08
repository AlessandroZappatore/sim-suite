package it.uniupo.simnova.views.support;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;

@SuppressWarnings("ThisExpressionReferencesGlobalObjectJS")
public class FieldGenerator extends HorizontalLayout {
    public FieldGenerator() {

    }

    /**
     * Crea e configura un campo di testo con stile avanzato.
     *
     * @param label       Etichetta del campo
     * @param placeholder Testo suggerito nel campo
     * @param required    Indica se il campo è obbligatorio
     * @return Campo di testo configurato con stile migliorato
     */
    public static TextField createTextField(String label, String placeholder, boolean required) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out");
        if (!label.equals("Azione Chiave")) {
            field.getStyle().set("max-width", "500px");
        }
        // Aggiunge un effetto hover e focus
        field.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        field.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        field.addClassName(LumoUtility.Margin.Top.LARGE);
        field.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            field.setRequired(true);
            field.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            field.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return field;
    }

    /**
     * Crea e configura un campo numerico con stile avanzato.
     *
     * @param label       Etichetta del campo
     * @param placeholder Testo suggerito nel campo
     * @param required    Indica se il campo è obbligatorio
     * @return Campo numerico configurato con stile migliorato
     */
    public static NumberField createNumberField(String label, String placeholder, boolean required) {
        NumberField field = new NumberField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.getStyle()
                .set("max-width", "500px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out");

        // Aggiunge un effetto hover e focus
        field.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        field.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        field.addClassName(LumoUtility.Margin.Top.LARGE);
        field.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            field.setRequired(true);
            field.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            field.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }
        return field;
    }

    /**
     * Crea e configura un ComboBox generico con stile avanzato.
     *
     * @param <T>          tipo di dati contenuti nel ComboBox (String, Integer, ecc.)
     * @param label        Etichetta del campo
     * @param items        Collezione di valori disponibili
     * @param defaultValue Valore predefinito
     * @param required     Indica se il campo è obbligatorio
     * @return ComboBox configurato con stile migliorato
     */
    public static <T> ComboBox<T> createComboBox(String label, Collection<T> items, T defaultValue, boolean required) {
        ComboBox<T> comboBox = new ComboBox<>(label);
        comboBox.setItems(items);

        if (defaultValue != null) {
            comboBox.setValue(defaultValue);
        }

        comboBox.setWidthFull();
        comboBox.getStyle()
                .set("max-width", "500px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out");

        // Aggiunge un effetto hover e focus
        comboBox.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        comboBox.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        comboBox.addClassName(LumoUtility.Margin.Top.LARGE);
        comboBox.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            comboBox.setRequired(true);
            comboBox.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            comboBox.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return comboBox;
    }

    /**
     * Crea e configura un Select generico con stile avanzato.
     *
     * @param <T>          tipo di dati contenuti nel Select (String, Integer, ecc.)
     * @param label        Etichetta del campo
     * @param items        Collezione di valori disponibili
     * @param defaultValue Valore predefinito
     * @param required     Indica se il campo è obbligatorio
     * @return Select configurato con stile migliorato
     */
    public static <T> Select<T> createSelect(String label, Collection<T> items, T defaultValue, boolean required) {
        Select<T> select = new Select<>();
        select.setLabel(label);
        select.setItems(items);

        if (defaultValue != null) {
            select.setValue(defaultValue);
        }

        select.setWidthFull();
        select.getStyle()
                .set("max-width", "500px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out");

        // Aggiunge un effetto hover e focus
        select.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        select.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        select.addClassName(LumoUtility.Margin.Top.LARGE);
        select.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            select.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            select.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return select;
    }

    public static NumberField createMedicalField(String label, String placeholder, boolean required, String unit) {
        NumberField field = createNumberField(label, placeholder, required);

        if (unit != null && !unit.isEmpty()) {
            Paragraph unitLabel = new Paragraph(unit);
            field.setSuffixComponent(unitLabel);
        }

        field.getStyle().set("max-width", "320px");
        return field;
    }

    public static TextArea createTextArea(String label, String placeholder, boolean required) {
        TextArea textArea = new TextArea(label);
        textArea.setPlaceholder(placeholder);
        textArea.setWidthFull();
        textArea.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("min-height", "100px");
        textArea.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        textArea.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        textArea.addClassName(LumoUtility.Margin.Top.LARGE);
        textArea.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            textArea.setRequired(true);
            textArea.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            textArea.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return textArea;
    }

    public static HorizontalLayout createTimerPickerWithPresets(String label) {
        // Creo il TimePicker base con lo stesso stile degli altri componenti
        TimePicker timerPicker = new TimePicker(label);
        timerPicker.setStep(Duration.ofSeconds(1));
        timerPicker.setPlaceholder("hh:mm:ss");
        timerPicker.setClearButtonVisible(true);

        // Applico lo stile consistente con gli altri componenti
        timerPicker.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("border-left", "3px solid var(--lumo-success-color-50pct)"); // Bordo verde (non obbligatorio)

        // Aggiungo effetto hover e focus
        timerPicker.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        timerPicker.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        timerPicker.addClassName(LumoUtility.Margin.Top.LARGE);
        timerPicker.addClassName(LumoUtility.Padding.SMALL);

        // Layout che conterrà TimePicker e pulsanti
        HorizontalLayout timerLayout = new HorizontalLayout();
        timerLayout.setWidthFull();
        timerLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        // Pulsanti per valori predefiniti
        Button oneMinBtn = new Button("2 min", e -> timerPicker.setValue(LocalTime.of(0, 2, 0)));
        Button fiveMinBtn = new Button("5 min", e -> timerPicker.setValue(LocalTime.of(0, 5, 0)));
        Button tenMinBtn = new Button("10 min", e -> timerPicker.setValue(LocalTime.of(0, 10, 0)));

        // Stile per i pulsanti (più piccoli e meno evidenti)
        for (Button btn : new Button[]{oneMinBtn, fiveMinBtn, tenMinBtn}) {
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btn.getStyle().set("min-width", "60px");
        }

        // Layout pulsanti preimpostati
        HorizontalLayout presetButtons = new HorizontalLayout(oneMinBtn, fiveMinBtn, tenMinBtn);
        presetButtons.setSpacing(true);

        // Aggiunge componenti al layout principale
        timerLayout.add(timerPicker, presetButtons);
        timerLayout.setFlexGrow(1, timerPicker);

        return timerLayout;
    }

    /**
     * Crea e configura una checkbox con stile avanzato.
     *
     * @param label Etichetta della checkbox
     * @return Checkbox configurata con stile migliorato
     */
    public static Checkbox createCheckbox(String label) {
        Checkbox checkbox = new Checkbox(label);

        checkbox.getStyle()
                .set("margin-top", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("transition", "opacity 0.3s ease-in-out");

        // Aggiunge un effetto hover
        checkbox.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.opacity = '0.85'; });" +
                        "this.addEventListener('mouseout', function() { this.style.opacity = '1'; });"
        );

        checkbox.addClassName(LumoUtility.Margin.Top.MEDIUM);

        return checkbox;
    }

    /**
     * Crea un campo IntegerField per la navigazione condizionale tra tempi.
     *
     * @param label       Etichetta del campo (es. "Se SI, vai a T:")
     * @param placeholder Placeholder del campo (default: "ID Tempo")
     * @param required    Indica se il campo è obbligatorio
     * @return IntegerField configurato per la navigazione tra tempi
     */
    public static IntegerField createTimeNavigationField(String label, String placeholder, boolean required) {
        IntegerField field = new IntegerField(label);
        field.setMin(0);                      // Può puntare a T0
        field.setStepButtonsVisible(true);    // Pulsanti +/-
        field.setWidth("150px");              // Larghezza fissa
        field.setPlaceholder(placeholder != null ? placeholder : "ID Tempo");

        field.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out");

        // Aggiunge un effetto hover e focus
        field.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        field.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        field.addClassName(LumoUtility.Margin.Top.LARGE);
        field.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            field.setRequired(true);
            field.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            field.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return field;
    }

}
