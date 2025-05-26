package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static it.uniupo.simnova.views.constant.AdditionParametersConst.ADDITIONAL_PARAMETERS;
import static it.uniupo.simnova.views.constant.AdditionParametersConst.CUSTOM_PARAMETER_KEY;

public class AdditionalParamDialog {

    public static void showAdditionalParamsDialog(TimeSection timeSection) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleziona Parametri Aggiuntivi per T" + timeSection.getTimeNumber());
        dialog.setWidth("600px");


        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca parametri...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);


        Button addCustomParamButton = new Button("Crea Nuovo Parametro Personalizzato",
                new Icon(VaadinIcon.PLUS_CIRCLE));
        addCustomParamButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addCustomParamButton.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        addCustomParamButton.addClickListener(e -> {
            dialog.close();
            showCustomParamDialog(timeSection);
        });


        Set<String> alreadySelectedKeys = timeSection.getCustomParameters().keySet();


        List<String> availableParamsLabels = ADDITIONAL_PARAMETERS.entrySet().stream()
                .filter(entry -> !alreadySelectedKeys.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());


        CheckboxGroup<String> paramsSelector = new CheckboxGroup<>();
        paramsSelector.setItems(availableParamsLabels);
        paramsSelector.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        paramsSelector.setWidthFull();
        paramsSelector.getStyle().set("max-height", "300px").set("overflow-y", "auto");


        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue() != null ? e.getValue().trim().toLowerCase() : "";
            List<String> filteredParams = availableParamsLabels.stream()
                    .filter(paramLabel -> paramLabel.toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
            paramsSelector.setItems(filteredParams);
        });


        Button confirmButton = new Button("Aggiungi Selezionati", e -> {
            paramsSelector.getSelectedItems().forEach(selectedLabel -> {

                String paramKey = ADDITIONAL_PARAMETERS.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(selectedLabel))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse("");

                if (!paramKey.isEmpty()) {

                    String unit = "";
                    if (selectedLabel.contains("(") && selectedLabel.contains(")")) {
                        try {
                            unit = selectedLabel.substring(selectedLabel.indexOf("(") + 1, selectedLabel.indexOf(")"));
                        } catch (IndexOutOfBoundsException ex) {
                            unit = "";
                        }
                    }

                    timeSection.addCustomParameter(paramKey, selectedLabel, unit);
                }
            });
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        Button cancelButton = new Button("Annulla", e -> dialog.close());


        HorizontalLayout buttonsLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);


        VerticalLayout dialogContent = new VerticalLayout(
                addCustomParamButton,
                searchField,
                new Paragraph("Seleziona dai parametri predefiniti:"),
                paramsSelector
        );
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);


        dialog.add(dialogContent, buttonsLayout);
        dialog.open();
    }

    /**
     * Mostra un dialog per aggiungere un nuovo parametro personalizzato (non predefinito).
     * Permette di definire nome, unità di misura e valore iniziale.
     *
     * @param timeSection la sezione temporale a cui aggiungere il parametro
     */
    private static void showCustomParamDialog(TimeSection timeSection) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aggiungi Parametro Personalizzato a T" + timeSection.getTimeNumber());
        dialog.setWidth("450px");


        TextField nameField = new TextField("Nome parametro");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);
        nameField.setErrorMessage("Il nome del parametro è obbligatorio");


        TextField unitField = new TextField("Unità di misura (opzionale)");
        unitField.setWidthFull();


        NumberField valueField = new NumberField("Valore iniziale (opzionale)");
        valueField.setWidthFull();


        Button saveButton = new Button("Salva Parametro", e -> {
            String paramName = nameField.getValue() != null ? nameField.getValue().trim() : "";
            String unit = unitField.getValue() != null ? unitField.getValue().trim() : "";
            Double initialValue = valueField.getValue();


            if (paramName.isEmpty()) {
                nameField.setInvalid(true);
                Notification.show(nameField.getErrorMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }
            nameField.setInvalid(false);


            String paramKey = CUSTOM_PARAMETER_KEY + "_" + paramName.replaceAll("\\s+", "_");


            if (timeSection.getCustomParameters().containsKey(paramKey)) {
                Notification.show("Un parametro con questo nome esiste già.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }


            String fullLabel = paramName + (unit.isEmpty() ? "" : " (" + unit + ")");


            timeSection.addCustomParameter(paramKey, fullLabel, unit);


            if (initialValue != null && timeSection.getCustomParameters().containsKey(paramKey)) {
                timeSection.getCustomParameters().get(paramKey).setValue(initialValue);
            } else if (timeSection.getCustomParameters().containsKey(paramKey)) {

                timeSection.getCustomParameters().get(paramKey).setValue(0.0);
            }

            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        Button cancelButton = new Button("Annulla", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);


        VerticalLayout dialogContent = new VerticalLayout(
                new Paragraph("Definisci un nuovo parametro non presente nella lista:"),
                nameField,
                unitField,
                valueField
        );
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);


        dialog.add(dialogContent, buttonsLayout);
        dialog.open();
    }
}
