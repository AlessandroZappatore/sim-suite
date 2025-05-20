package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
// Importato per rimuovere listener
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableFunction;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.views.common.utils.FieldGenerator;

public class InfoSupport extends HorizontalLayout {

    public InfoSupport() {
        // Constructor
    }

    // Updated styling method for the new badge structure
    private static void styleBadge(HorizontalLayout badgeLayout, Span textSpan, Button editButton, String color, boolean isEmpty) {
        badgeLayout.getStyle()
                .set("background-color", color + (isEmpty ? "08" : "10"))
                .set("border-radius", "16px")
                .set("padding", "6px 10px 6px 16px") // Adjusted padding for internal button
                .set("display", "inline-flex") // For internal alignment
                .set("align-items", "center")
                .set("border", "1px solid " + color + (isEmpty ? "30" : "40"))
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");

        textSpan.getStyle()
                .set("color", color)
                .set("font-size", "16px")
                .set("font-weight", "500");

        // Ensure edit button icon color matches badge color
        editButton.getStyle().set("color", color);

        // JS hover effect on the badgeLayout
        badgeLayout.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.boxShadow = '0 3px 6px rgba(0,0,0,0.15)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)'; " +
                        "});"
        );
    }

    private static String formatBadgeText(String label, String value, String emptyText) {
        if (value != null && !value.trim().isEmpty()) {
            return label + ": " + value;
        } else {
            return label + ": " + emptyText;
        }
    }

    private static boolean isValueEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Metodo interno principale per creare il badge editabile
    private static Component createEditableBadgeInternal(
            Scenario scenario,
            String label,
            SerializableFunction<Scenario, String> rawValueGetter, // Ottiene il valore grezzo per l'editing
            SerializableFunction<Scenario, String> displayValueGetter,
            SerializableBiConsumer<Scenario, String> valueSetter, // Imposta il valore (gestisce parsing se necessario)
            String emptyText,
            String badgeColor,
            boolean isNumeric) { // Flag to identify numeric fields for specific save handling

        HorizontalLayout itemContainer = new HorizontalLayout();
        itemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContainer.getStyle().set("margin", "6px");

        // --- Componenti Modalità Visualizzazione (Badge con bottone di modifica interno) ---
        HorizontalLayout badgeViewLayout = new HorizontalLayout(); // Container per testo badge e bottone modifica
        badgeViewLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeViewLayout.setSpacing(false); // Spaziatura controllata manualmente o da stili componenti
        badgeViewLayout.getStyle().set("cursor", "default");

        Span actualBadgeTextSpan = new Span(); // Span per il testo del badge

        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        editButton.getStyle()
                .set("margin-left", "8px") // Spazio tra testo e icona
                .set("padding", "2px") // Rende il bottone più compatto
                .set("height", "20px")
                .set("width", "20px")
                .set("font-size", "12px"); // Icona più piccola

        badgeViewLayout.add(actualBadgeTextSpan, editButton);
        badgeViewLayout.expand(actualBadgeTextSpan); // Il testo occupa lo spazio disponibile, spingendo il bottone a destra


        // --- Componenti Modalità Modifica ---
        TextField editField = FieldGenerator.createTextField(label, null, true);
        editField.getStyle().set("flex-grow", "1");

        Button saveButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button cancelButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        HorizontalLayout editControlsLayout = new HorizontalLayout(editField, saveButton, cancelButton);
        editControlsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        editControlsLayout.setVisible(false);
        editControlsLayout.setWidthFull();

        // Funzione per aggiornare il testo e lo stile del badge
        Runnable updateBadgeAppearance = () -> {
            String displayValue = displayValueGetter.apply(scenario);
            boolean isEmpty = isValueEmpty(displayValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, displayValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, editButton, badgeColor, isEmpty);
        };

        updateBadgeAppearance.run(); // Imposta l'aspetto iniziale

        itemContainer.add(badgeViewLayout, editControlsLayout);

        // --- Listener Eventi ---
        editButton.addClickListener(e -> {
            badgeViewLayout.setVisible(false); // Nasconde l'intero badge (testo + bottone modifica)
            editControlsLayout.setVisible(true);
            editField.setValue(rawValueGetter.apply(scenario) != null ? rawValueGetter.apply(scenario) : "");
            editField.focus();
        });

        cancelButton.addClickListener(e -> {
            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);
            updateBadgeAppearance.run(); // Ripristina l'aspetto originale
        });

        saveButton.addClickListener(e -> {
            // Validazione base per campi numerici prima di procedere (anche se non salviamo)
            if (isNumeric) {
                try {
                    String val = editField.getValue();
                    if (val != null && !val.trim().isEmpty()) {
                        Double.parseDouble(val.replace(",", ".")); // Tenta il parsing
                    }
                } catch (NumberFormatException nfe) {
                    Notification.show("Formato numerico non valido per " + label + ": '" + editField.getValue() + "'.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
                    return; // Rimane in modalità modifica se il formato non è valido
                }
            }

            // --- INIZIO SEZIONE SALVATAGGIO INCOMPLETO ---
            // La logica di salvataggio effettiva (valueSetter.accept(...)) è rimossa.
            // L'utente dovrà implementare qui come il valore da editField.getValue()
            // viene effettivamente salvato nello scenario o altrove.
            // --- FINE SEZIONE SALVATAGGIO INCOMPLETO ---

            updateBadgeAppearance.run(); // Aggiorna l'aspetto del badge. Mostrerà il valore originale dallo scenario,
            // dato che il valueSetter non è stato chiamato.
            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);

            Notification.show("Azione 'Salva' per " + label + " registrata. Implementare la logica di salvataggio.", 3000, Notification.Position.BOTTOM_START);
        });

        return itemContainer;
    }

    // Wrapper per campi Stringa semplici
    private static Component createStringEditableBadge(Scenario scenario, String label,
                                                       SerializableFunction<Scenario, String> getter,
                                                       SerializableBiConsumer<Scenario, String> setter,
                                                       String emptyText, String badgeColor) {
        return createEditableBadgeInternal(scenario, label, getter, getter, setter, emptyText, badgeColor, false);
    }

    // Wrapper per campi Numerici (come Timer)
    private static Component createNumericEditableBadge(Scenario scenario,
                                                        String labelText, // Passiamo il label esplicito per il controllo
                                                        SerializableFunction<Scenario, Number> numericGetter,
                                                        SerializableBiConsumer<Scenario, Number> numericSetter,
                                                        String emptyText, String badgeColor) {

        SerializableFunction<Scenario, String> rawValueGetter = s -> {
            Number val = numericGetter.apply(s);
            return (val != null && val.doubleValue() > 0) ? String.format("%.1f", val.doubleValue()).replace(",", ".") : ""; // Usa. per consistenza editing
        };

        SerializableFunction<Scenario, String> displayValueGetter = s -> {
            Number val = numericGetter.apply(s);
            // Visualizza con "min" ma considera che formatBadgeText aggiungerà il label
            return (val != null && val.doubleValue() > 0) ? String.format("%.1f", val.doubleValue()) + " " + "min" : null;
        };

        // Questo valueSetter non sarà chiamato dal saveButton modificato,
        // ma lo lasciamo per completezza se si decidesse di riattivare il salvataggio interno.
        SerializableBiConsumer<Scenario, String> valueSetter = (s, strVal) -> {
            if (strVal == null || strVal.trim().isEmpty()) {
                numericSetter.accept(s, 0.0);
            } else {
                double parsedValue = Double.parseDouble(strVal.replace(",", "."));
                numericSetter.accept(s, parsedValue);
            }
        };
        // Passiamo il labelText corretto e isNumeric = true
        return createEditableBadgeInternal(scenario, labelText, rawValueGetter, displayValueGetter, valueSetter, emptyText, badgeColor, true);
    }


    public static Component getInfo(Scenario scenario) {
        HorizontalLayout badgesContainer = new HorizontalLayout();
        badgesContainer.setWidthFull();
        badgesContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        badgesContainer.setSpacing(false);
        badgesContainer.getStyle().set("flex-wrap", "wrap");

        String badgeColor = "var(--lumo-primary-color)";
        String emptyDefaultText = "N/D";

        badgesContainer.add(createStringEditableBadge(scenario, "Paziente",
                Scenario::getNomePaziente, Scenario::setNomePaziente,
                emptyDefaultText, badgeColor));

        badgesContainer.add(createStringEditableBadge(scenario, "Tipologia",
                Scenario::getTipologia, Scenario::setTipologia,
                emptyDefaultText, badgeColor));

        badgesContainer.add(createStringEditableBadge(scenario, "Patologia",
                Scenario::getPatologia, Scenario::setPatologia,
                emptyDefaultText, badgeColor));

        badgesContainer.add(createNumericEditableBadge(scenario, "Durata", // Label esplicito per il campo Durata
                Scenario::getTimerGenerale,
                (s, num) -> s.setTimerGenerale((float) num.doubleValue()),
                emptyDefaultText, badgeColor));

        badgesContainer.add(createStringEditableBadge(scenario, "Target",
                Scenario::getTarget, Scenario::setTarget,
                emptyDefaultText, badgeColor));

        return badgesContainer;
    }
}