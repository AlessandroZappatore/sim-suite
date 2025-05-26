package it.uniupo.simnova.views.ui.helper;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;

import java.util.*;
import java.util.function.Supplier;


public class InfoSupport extends HorizontalLayout {
    private static final Map<String, Supplier<Icon>> labelIconMap = new HashMap<>();
    private static final Map<String, Supplier<Icon>> tipologiaIconMap = new HashMap<>();
    private static final List<String> TIPOLOGIA_OPTIONS = Arrays.asList("Adulto", "Pediatrico", "Neonatale", "Prematuro");
    private static final List<Integer> DURATION_OPTIONS = List.of(5, 10, 15, 20, 25, 30);

    static {
        labelIconMap.put("Paziente", FontAwesome.Solid.USER_INJURED::create);
        labelIconMap.put("Patologia", FontAwesome.Solid.DISEASE::create);
        labelIconMap.put("Durata", FontAwesome.Solid.STOPWATCH_20::create);
        labelIconMap.put("Target", FontAwesome.Solid.BULLSEYE::create);

        tipologiaIconMap.put("Adulto", FontAwesome.Solid.USER::create);
        tipologiaIconMap.put("Pediatrico", FontAwesome.Solid.CHILD::create);
        tipologiaIconMap.put("Neonatale", FontAwesome.Solid.BABY::create);
        tipologiaIconMap.put("Prematuro", FontAwesome.Solid.HANDS_HOLDING_CHILD::create);
    }

    private static void styleBadge(HorizontalLayout badgeLayout, Span textSpan, String color, boolean isEmpty) {
        badgeLayout.getStyle()
                .set("background-color", color + (isEmpty ? "08" : "10"))
                .set("border-radius", "16px")
                .set("padding", "6px 10px 6px 16px")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("border", "1px solid " + color + (isEmpty ? "30" : "40"))
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");

        textSpan.getStyle()
                .set("color", color)
                .set("font-size", "16px")
                .set("font-weight", "500");

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
            if ("Durata".equals(label)) {
                return value + " min";
            }
            return label + ": " + value;
        } else {
            return label + ": " + emptyText;
        }
    }

    private static boolean isValueEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static Component createEditableBadgeInternal(
            Scenario scenario,
            String label,
            SerializableFunction<Scenario, String> rawValueGetter,
            SerializableFunction<Scenario, String> displayValueGetter,
            String emptyText,
            String badgeColor,
            ScenarioService scenarioService) {

        HorizontalLayout itemContainer = new HorizontalLayout();
        itemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContainer.getStyle().set("margin", "6px");

        HorizontalLayout badgeViewLayout = new HorizontalLayout();
        badgeViewLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeViewLayout.setSpacing(false);
        badgeViewLayout.getStyle().set("cursor", "default");

        Icon icon = labelIconMap.containsKey(label) ? labelIconMap.get(label).get() : null;

        if (icon != null) {
            icon.getStyle().set("margin-right", "8px");
            icon.addClassName(LumoUtility.TextColor.PRIMARY);
            badgeViewLayout.add(icon);
        }

        Span actualBadgeTextSpan = new Span();
        Button editButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");

        badgeViewLayout.add(actualBadgeTextSpan, editButton);
        badgeViewLayout.expand(actualBadgeTextSpan);

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

        Runnable updateBadgeAppearance = () -> {
            String displayValue = displayValueGetter.apply(scenario);
            boolean isEmpty = isValueEmpty(displayValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, displayValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);
        };

        updateBadgeAppearance.run();
        itemContainer.add(badgeViewLayout, editControlsLayout);

        editButton.addClickListener(e -> {
            badgeViewLayout.setVisible(false);
            editControlsLayout.setVisible(true);
            editField.setValue(rawValueGetter.apply(scenario) != null ? rawValueGetter.apply(scenario) : "");
            editField.focus();
        });

        cancelButton.addClickListener(e -> {
            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);
            updateBadgeAppearance.run();
        });

        saveButton.addClickListener(e -> {
            String newValue = editField.getValue();
            boolean isEmpty = isValueEmpty(newValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, newValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);

            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);

            scenarioService.updateSingleField(scenario.getId(), label, newValue);

            Notification.show(label + " aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        return itemContainer;
    }

    private static Component createStringEditableBadge(Scenario scenario, String label,
                                                       SerializableFunction<Scenario, String> getter,
                                                       String emptyText, String badgeColor, ScenarioService scenarioService) {
        return createEditableBadgeInternal(scenario, label, getter, getter, emptyText, badgeColor, scenarioService);
    }

    private static Component createDurationSelectBadge(
            Scenario scenario,
            SerializableFunction<Scenario, Number> numericGetter,
            String emptyText,
            String badgeColor,
            ScenarioService scenarioService) {

        String label = "Durata";

        HorizontalLayout itemContainer = new HorizontalLayout();
        itemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContainer.getStyle().set("margin", "6px");

        HorizontalLayout badgeViewLayout = new HorizontalLayout();
        badgeViewLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeViewLayout.setSpacing(false);
        badgeViewLayout.getStyle().set("cursor", "default");

        Icon icon = labelIconMap.getOrDefault(label, FontAwesome.Solid.QUESTION_CIRCLE::create).get();
        if (icon != null) {
            icon.getStyle().set("margin-right", "8px");
            icon.addClassName(LumoUtility.TextColor.PRIMARY);
            badgeViewLayout.add(icon);
        }

        Span actualBadgeTextSpan = new Span();
        Button editButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");

        badgeViewLayout.add(actualBadgeTextSpan, editButton);
        badgeViewLayout.expand(actualBadgeTextSpan);

        ComboBox<Integer> durationSelect = FieldGenerator.createComboBox("Durata", DURATION_OPTIONS,
                (scenario.getTimerGenerale() > 0) ? Math.round(scenario.getTimerGenerale()) : null,
                true);

        Button saveButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button cancelButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        HorizontalLayout editControlsLayout = new HorizontalLayout(durationSelect, saveButton, cancelButton);
        editControlsLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        editControlsLayout.setVisible(false);
        editControlsLayout.setWidthFull();


        SerializableFunction<Scenario, String> displayValueFormatter = s -> {
            Number val = numericGetter.apply(s);
            if (val != null) {
                int intVal = val.intValue();
                if (val.doubleValue() == intVal && intVal > 0) {
                    return String.valueOf(intVal);
                } else if (val.doubleValue() > 0) {
                    return String.valueOf(intVal);
                }
            }
            return null;
        };

        Runnable updateBadgeAppearance = () -> {
            String currentDisplayValue = displayValueFormatter.apply(scenario);
            boolean isEmpty = isValueEmpty(currentDisplayValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, currentDisplayValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);
        };

        updateBadgeAppearance.run();
        itemContainer.add(badgeViewLayout, editControlsLayout);

        editButton.addClickListener(e -> {
            badgeViewLayout.setVisible(false);
            editControlsLayout.setVisible(true);
            Number currentValue = numericGetter.apply(scenario);
            if (currentValue != null) {
                int currentIntValue = currentValue.intValue();
                if (currentValue.doubleValue() == currentIntValue && DURATION_OPTIONS.contains(currentIntValue)) {
                    durationSelect.setValue(currentIntValue);
                } else {
                    durationSelect.clear();
                }
            } else {
                durationSelect.clear();
            }
            durationSelect.focus();
        });

        cancelButton.addClickListener(e -> {
            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);
            updateBadgeAppearance.run();
        });

        saveButton.addClickListener(e -> {
            Integer selectedValue = durationSelect.getValue();
            String valueToSaveAndDisplay = (selectedValue != null) ? String.valueOf(selectedValue) : null;


            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);
            scenarioService.updateSingleField(scenario.getId(), label, valueToSaveAndDisplay);


            updateBadgeAppearance.run();
            Notification.show(label + " aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        return itemContainer;
    }


    private static Component createTipologiaSelectBadge(
            Scenario scenario,
            SerializableFunction<Scenario, String> displayValueGetter,
            String emptyText,
            String badgeColor,
            ScenarioService scenarioService) {

        String label = "Tipologia";

        HorizontalLayout itemContainer = new HorizontalLayout();
        itemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContainer.getStyle().set("margin", "6px");

        HorizontalLayout badgeViewLayout = new HorizontalLayout();
        badgeViewLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeViewLayout.setSpacing(false);
        badgeViewLayout.getStyle().set("cursor", "default");

        Span actualBadgeTextSpan = new Span();
        Button editButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");

        Icon tipologiaIcon = tipologiaIconMap.getOrDefault(displayValueGetter.apply(scenario), FontAwesome.Solid.USERS::create).get();
        tipologiaIcon.getStyle().set("margin-right", "8px");
        tipologiaIcon.addClassName(LumoUtility.TextColor.PRIMARY);
        badgeViewLayout.add(tipologiaIcon);


        badgeViewLayout.add(actualBadgeTextSpan, editButton);
        badgeViewLayout.expand(actualBadgeTextSpan);

        Select<String> selectBox = FieldGenerator.createSelect(label, TIPOLOGIA_OPTIONS, scenario.getTipologia(), true);
        selectBox.getStyle().set("flex-grow", "1");

        Button saveButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button cancelButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        HorizontalLayout editControlsLayout = new HorizontalLayout(selectBox, saveButton, cancelButton);
        editControlsLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        editControlsLayout.setVisible(false);
        editControlsLayout.setWidthFull();

        Runnable updateBadgeAppearance = () -> {
            String displayValue = displayValueGetter.apply(scenario);
            boolean isEmpty = isValueEmpty(displayValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, displayValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);

            Icon currentIconToSet = tipologiaIconMap.getOrDefault(displayValue, FontAwesome.Solid.USERS::create).get();
            currentIconToSet.getStyle().set("margin-right", "8px");
            currentIconToSet.addClassName(LumoUtility.TextColor.PRIMARY);
            if (badgeViewLayout.getComponentCount() > 1 && badgeViewLayout.getComponentAt(0) instanceof Icon) {
                badgeViewLayout.replace(badgeViewLayout.getComponentAt(0), currentIconToSet);
            } else {
                badgeViewLayout.addComponentAsFirst(currentIconToSet);
            }
        };

        updateBadgeAppearance.run();
        itemContainer.add(badgeViewLayout, editControlsLayout);

        editButton.addClickListener(e -> {
            badgeViewLayout.setVisible(false);
            editControlsLayout.setVisible(true);
            String currentTipologia = displayValueGetter.apply(scenario);
            selectBox.setValue(currentTipologia);
            selectBox.focus();
        });

        cancelButton.addClickListener(e -> {
            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);
            updateBadgeAppearance.run();
        });

        saveButton.addClickListener(e -> {
            String newSelectedValue = selectBox.getValue();

            boolean isEmpty = isValueEmpty(newSelectedValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, newSelectedValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);

            Icon currentIconToSet = tipologiaIconMap.getOrDefault(newSelectedValue, FontAwesome.Solid.USERS::create).get();
            currentIconToSet.getStyle().set("margin-right", "8px");
            currentIconToSet.addClassName(LumoUtility.TextColor.PRIMARY);
            if (badgeViewLayout.getComponentCount() > 1 && badgeViewLayout.getComponentAt(0) instanceof Icon) {
                badgeViewLayout.replace(badgeViewLayout.getComponentAt(0), currentIconToSet);
            }

            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);

            scenarioService.updateSingleField(scenario.getId(), label, newSelectedValue);
            UI.getCurrent().getPage().reload();
            Notification.show(label + " aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        return itemContainer;
    }

    private static Component createTargetDialogBadge(
            Scenario scenario,
            SerializableFunction<Scenario, String> displayValueGetter,
            String emptyText,
            String badgeColor) {
        String label = "Target";

        if (scenario.getId() < 0) {
            Span errorSpan = new Span("ID Scenario non disponibile per Target");
            errorSpan.getStyle().set("color", "red");
            return errorSpan;
        }

        HorizontalLayout itemContainer = new HorizontalLayout();
        itemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContainer.getStyle().set("margin", "6px");

        HorizontalLayout badgeViewLayout = new HorizontalLayout();
        badgeViewLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeViewLayout.setSpacing(false);
        badgeViewLayout.getStyle().set("cursor", "default");

        Icon icon = labelIconMap.getOrDefault(label, FontAwesome.Solid.QUESTION_CIRCLE::create).get();
        icon.getStyle().set("margin-right", "8px");
        icon.addClassName(LumoUtility.TextColor.PRIMARY);

        Span actualBadgeTextSpan = new Span();
        Button openDialogButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        openDialogButton.getStyle().set("margin-left", "auto");
        openDialogButton.getElement().setAttribute("title", "Modifica Target in finestra");

        Runnable updateBadgeAppearance = () -> {
            String displayValue = displayValueGetter.apply(scenario);
            boolean isEmpty = isValueEmpty(displayValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, displayValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);
            if (!(badgeViewLayout.getComponentCount() > 0 && badgeViewLayout.getComponentAt(0) instanceof Icon)) {
                badgeViewLayout.addComponentAsFirst(icon);
            }
        };

        updateBadgeAppearance.run();

        badgeViewLayout.add(actualBadgeTextSpan, openDialogButton);
        badgeViewLayout.expand(actualBadgeTextSpan);


        itemContainer.add(badgeViewLayout);


        openDialogButton.addClickListener(e -> {
            String targetId = String.valueOf(scenario.getId());
            String iframeUrl = "target/" + targetId + "/edit";

            IFrame iframe = new IFrame(iframeUrl);
            iframe.setWidth("100%");
            iframe.setHeight("100%");
            iframe.getStyle().set("border", "none");

            Dialog dialog = new Dialog();
            dialog.add(iframe);

            dialog.setWidth("80vw");
            dialog.setHeight("70vh");
            dialog.setModal(true);
            dialog.setDraggable(true);
            dialog.setResizable(true);
            dialog.setCloseOnEsc(true);
            dialog.setCloseOnOutsideClick(false);

            HorizontalLayout headerLayout = new HorizontalLayout();
            Span dialogTitle = new Span("Modifica Target: " + displayValueGetter.apply(scenario));
            Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL), event -> dialog.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            headerLayout.add(dialogTitle, closeButton);
            headerLayout.setFlexGrow(1, dialogTitle);
            headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            headerLayout.setWidthFull();
            headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            if (dialog.getHeader() != null) {
                dialog.getHeader().removeAll();
                dialog.getHeader().add(headerLayout);
            }


            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    updateBadgeAppearance.run();
                    Notification.show("Finestra Modifica Target chiusa.", 2000, Notification.Position.BOTTOM_START);
                    UI.getCurrent().getPage().reload();
                }
            });

            dialog.open();
            Notification.show("Apertura finestra per modifica Target...", 2000, Notification.Position.BOTTOM_START);
        });

        return itemContainer;
    }


    public static Component getInfo(Scenario scenario, ScenarioService scenarioService) {
        HorizontalLayout badgesContainer = new HorizontalLayout();
        badgesContainer.setWidthFull();
        badgesContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        badgesContainer.setSpacing(false);
        badgesContainer.getStyle().set("flex-wrap", "wrap");

        String badgeColor = "var(--lumo-primary-color)";
        String emptyDefaultText = "N/D";

        badgesContainer.add(createStringEditableBadge(scenario, "Paziente",
                Scenario::getNomePaziente,
                emptyDefaultText, badgeColor, scenarioService));

        badgesContainer.add(createTipologiaSelectBadge(scenario,
                Scenario::getTipologia,
                emptyDefaultText, badgeColor, scenarioService));

        badgesContainer.add(createStringEditableBadge(scenario, "Patologia",
                Scenario::getPatologia,
                emptyDefaultText, badgeColor, scenarioService));

        badgesContainer.add(createDurationSelectBadge(scenario,
                Scenario::getTimerGenerale,
                emptyDefaultText, badgeColor, scenarioService));

        badgesContainer.add(createTargetDialogBadge(scenario,
                Scenario::getTarget,
                emptyDefaultText, badgeColor));

        return badgesContainer;
    }
}