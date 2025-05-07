package it.uniupo.simnova.views.home;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Collection;

public class FieldGenerator extends HorizontalLayout {
    public FieldGenerator(){

    }

    public static TextField createTextField(String label, String placeholder, boolean required) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.getStyle().set("max-width", "500px");
        field.addClassName(LumoUtility.Margin.Top.LARGE);
        field.setRequired(required);
        return field;
    }

    public static NumberField createNumberField(String label, String placeholder, boolean required) {
        NumberField field = new NumberField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.getStyle().set("max-width", "500px");
        field.addClassName(LumoUtility.Margin.Top.LARGE);
        field.setRequired(required);
        return field;
    }

    /**
     * Crea e configura un ComboBox generico.
     *
     * @param <T> tipo di dati contenuti nel ComboBox (String, Integer, ecc.)
     * @param label Etichetta del campo
     * @param items Collezione di valori disponibili
     * @param defaultValue Valore predefinito
     * @param required Indica se il campo è obbligatorio
     * @return ComboBox configurato
     */
    public static <T> ComboBox<T> createComboBox(String label, Collection<T> items, T defaultValue, boolean required) {
        ComboBox<T> comboBox = new ComboBox<>(label);
        comboBox.setItems(items);

        if (defaultValue != null) {
            comboBox.setValue(defaultValue);
        }

        comboBox.setWidthFull();
        comboBox.addClassName(LumoUtility.Margin.Top.LARGE);
        comboBox.getStyle().set("max-width", "500px");
        comboBox.setRequired(required);

        return comboBox;
    }
}
