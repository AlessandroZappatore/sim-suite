package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.vaadin.tinymce.TinyMce;

public class SceneggiaturaSupport extends HorizontalLayout {
    public static Component createSceneggiaturaContent(Integer scenarioId, String sceneggiaturaText,
                                                       PatientSimulatedScenarioService patientSimulatedScenarioService) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(false);
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        final String[] currentSceneggiatura = {sceneggiaturaText};

        Div sceneggiaturaCard = new Div();
        sceneggiaturaCard.setId("sceneggiatura-view-card");
        sceneggiaturaCard.addClassName("sceneggiatura-card");
        sceneggiaturaCard.getStyle()
                .set("width", "100%")
                .set("max-width", "800px")
                .set("margin", "var(--lumo-space-l) 0")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("padding", "var(--lumo-space-l)")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease")
                .set("box-sizing", "border-box");

        sceneggiaturaCard.getElement().executeJs(
                "this.addEventListener('mouseover', function() {" +
                        "  this.style.transform = 'translateY(-2px)';" +
                        "  this.style.boxShadow = 'var(--lumo-box-shadow-l)';" +
                        "});" +
                        "this.addEventListener('mouseout', function() {" +
                        "  this.style.transform = 'translateY(0)';" +
                        "  this.style.boxShadow = 'var(--lumo-box-shadow-m)';" +
                        "});"
        );

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setPadding(false);
        headerLayout.getStyle()
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("padding-bottom", "var(--lumo-space-m)");

        Icon scriptIcon = new Icon(VaadinIcon.FILE_TEXT_O);
        scriptIcon.addClassName(LumoUtility.TextColor.PRIMARY);
        scriptIcon.getStyle()
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("margin-right", "var(--lumo-space-m)");

        H3 scriptTitle = new H3("Sceneggiatura");
        scriptTitle.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        scriptTitle.getStyle().set("font-weight", "600");


        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editButton.setTooltipText("Modifica la sceneggiatura");
        editButton.getStyle().set("margin-left", "auto");

        headerLayout.add(scriptIcon, scriptTitle, editButton);
        sceneggiaturaCard.add(headerLayout);

        // Display area for the script text
        Div scriptTextDisplay = new Div();
        scriptTextDisplay.getStyle()
                .set("font-family", "var(--lumo-font-family)")
                .set("line-height", "var(--lumo-line-height-l)")
                .set("color", "var(--lumo-body-text-color)")
                .set("white-space", "pre-wrap")
                .set("padding", "var(--lumo-space-m)")
                .set("max-height", "60vh")
                .set("overflow-y", "auto")
                .set("border-left", "3px solid var(--lumo-primary-color)")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        if (sceneggiaturaText == null || sceneggiaturaText.trim().isEmpty()) {
            scriptTextDisplay.getElement().setProperty("innerHTML", "Sceneggiatura non disponibile");
        } else {
            scriptTextDisplay.getElement().setProperty("innerHTML", sceneggiaturaText.replace("\n", "<br />"));
        }
        sceneggiaturaCard.add(scriptTextDisplay); // Add display div to the card

        // Editor for the script text
        TinyMce editor = TinyEditor.getEditor();
        editor.setValue((sceneggiaturaText == null || sceneggiaturaText.trim().isEmpty()) ? "" : sceneggiaturaText);
        editor.setVisible(false); // Initially hidden
        editor.getStyle()
                .set("width", "100%")
                .set("padding", "var(--lumo-space-m)")
                .set("max-height", "60vh")
                .set("overflow-y", "auto")
                .set("border-left", "3px solid var(--lumo-primary-color)")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("box-sizing", "border-box");
        sceneggiaturaCard.add(editor); // Add editor directly to the card

        Button saveButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle().set("margin-top", "var(--lumo-space-m)");

        Button cancelButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);
        cancelButton.getStyle().set("margin-top", "var(--lumo-space-m)");

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setVisible(false); // Initially hidden
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        sceneggiaturaCard.add(buttonLayout); // Add buttons directly to the card

        editButton.addClickListener(e -> {
            scriptTextDisplay.setVisible(false);
            editor.setVisible(true);
            buttonLayout.setVisible(true);
            editor.setValue((currentSceneggiatura[0] == null || currentSceneggiatura[0].trim().isEmpty()) ? "" : currentSceneggiatura[0]);
        });

        cancelButton.addClickListener(e -> {
            editor.setVisible(false);
            buttonLayout.setVisible(false);
            scriptTextDisplay.setVisible(true);
        });

        saveButton.addClickListener(e -> {
            String updatedText = editor.getValue();
            patientSimulatedScenarioService.updateScenarioSceneggiatura(scenarioId, updatedText);

            currentSceneggiatura[0] = updatedText;

            // Update the display div
            if (currentSceneggiatura[0] == null || currentSceneggiatura[0].trim().isEmpty()) {
                scriptTextDisplay.getElement().setProperty("innerHTML", "Sceneggiatura non disponibile");
            } else {
                scriptTextDisplay.getElement().setProperty("innerHTML", currentSceneggiatura[0].replace("\n", "<br />"));
            }

            editor.setVisible(false);
            buttonLayout.setVisible(false);
            scriptTextDisplay.setVisible(true);
            Notification.show("Sceneggiatura aggiornata.", 3000, Notification.Position.BOTTOM_CENTER);
        });

        mainLayout.add(sceneggiaturaCard); // Only the card needs to be added to the main layout

        return mainLayout;
    }
}