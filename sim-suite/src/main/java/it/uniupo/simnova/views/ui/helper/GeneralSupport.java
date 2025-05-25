package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.scenario.Scenario;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.IFrame;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.vaadin.tinymce.TinyMce;

import java.util.List;

public class GeneralSupport extends HorizontalLayout {
    public static VerticalLayout createOverviewContentWithData(
            Scenario scenario,
            boolean isPediatricScenario,
            String infoGenitore,
            List<String> azioniChiave,
            ScenarioService scenarioService,
            MaterialeService materialeService) {

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(false);
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Div card = new Div();
        card.addClassName("info-card");
        card.getStyle()
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("padding", "var(--lumo-space-l)")
                .set("background-color", "var(--lumo-base-color)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("width", "100%")
                .set("max-width", "800px")
                .set("margin", "var(--lumo-space-l) 0");

        card.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-l)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-m)'; });"
        );

        VerticalLayout cardContentLayout = new VerticalLayout();
        cardContentLayout.setPadding(false);
        cardContentLayout.setSpacing(false);
        cardContentLayout.setWidthFull();

        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Descrizione", scenario.getDescrizione(), VaadinIcon.PENCIL, true, scenarioService);
        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Briefing", scenario.getBriefing(), VaadinIcon.GROUP, scenarioService);

        if (isPediatricScenario) {
            addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Informazioni dai genitori", infoGenitore, VaadinIcon.FAMILY, scenarioService);
        }

        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Patto Aula", scenario.getPattoAula(), VaadinIcon.HANDSHAKE, scenarioService);
        addAzioniChiaveItem(scenario.getId(), cardContentLayout, azioniChiave, scenarioService);
        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Obiettivi Didattici", scenario.getObiettivo(), VaadinIcon.BOOK, scenarioService);
        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Moulage", scenario.getMoulage(), VaadinIcon.EYE, scenarioService);
        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Liquidi e dosi farmaci", scenario.getLiquidi(), VaadinIcon.DROP, scenarioService);

        addMaterialeNecessarioItem(scenario.getId(), cardContentLayout, materialeService);

        card.add(cardContentLayout);
        mainLayout.add(card);
        return mainLayout;
    }

    private static void addInfoItemIfNotEmpty(Integer scenarioId, VerticalLayout container, String title, String content, VaadinIcon iconType, boolean isFirstItem, ScenarioService scenarioService) {
        if (!isFirstItem && container.getComponentCount() > 0) {
            Hr divider = new Hr();
            divider.getStyle()
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("margin-bottom", "var(--lumo-space-s)")
                    .set("border-color", "var(--lumo-contrast-10pct)");
            container.add(divider);
        }

        VerticalLayout itemLayout = new VerticalLayout();
        itemLayout.setPadding(false);
        itemLayout.setSpacing(false);
        itemLayout.setWidthFull();

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleGroup = new HorizontalLayout();
        titleGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon icon = new Icon(iconType);
        icon.addClassName(LumoUtility.TextColor.PRIMARY);
        icon.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("margin-right", "var(--lumo-space-xs)");

        H5 titleLabel = new H5(title);
        titleLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        titleLabel.getStyle().set("font-weight", "600");
        titleGroup.add(icon, titleLabel);

        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editButton.setTooltipText("Modifica " + title);
        headerRow.add(titleGroup, editButton);
        itemLayout.add(headerRow);

        Div contentDisplay = new Div();
        contentDisplay.getStyle()
                .set("font-family", "var(--lumo-font-family)")
                .set("line-height", "var(--lumo-line-height-m)")
                .set("color", "var(--lumo-body-text-color)")
                .set("white-space", "pre-wrap")
                .set("padding", "var(--lumo-space-xs) 0 var(--lumo-space-s) calc(var(--lumo-icon-size-m) + var(--lumo-space-m))")
                .set("width", "100%")
                .set("box-sizing", "border-box");
        if (content == null || content.trim().isEmpty()) {
            contentDisplay.setText("Sezione vuota");
            contentDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
        } else {
            contentDisplay.getElement().setProperty("innerHTML", content.replace("\n", "<br />"));
        }
        itemLayout.add(contentDisplay);

        TinyMce contentEditor = TinyEditor.getEditor();
        contentEditor.setValue(content == null ? "" : content);
        contentEditor.setVisible(false);

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancelButton = new Button("Annulla");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout editorActions = new HorizontalLayout(saveButton, cancelButton);
        editorActions.setVisible(false);
        editorActions.getStyle()
                .set("margin-top", "var(--lumo-space-xs)")
                .set("padding-left", "calc(var(--lumo-icon-size-m) + var(--lumo-space-m))");

        itemLayout.add(contentEditor, editorActions);

        editButton.addClickListener(e -> {
            contentDisplay.setVisible(false);
            String currentHtml = contentDisplay.getElement().getProperty("innerHTML");
            String editorValue;
            if (content == null || content.trim().isEmpty()) {
                editorValue = "";
            } else {
                editorValue = currentHtml.replace("<br />", "\n").replace("<br>", "\n");
            }
            contentEditor.setValue(editorValue);
            contentEditor.setVisible(true);
            editorActions.setVisible(true);
            editButton.setVisible(false);
        });

        saveButton.addClickListener(e -> {
            String newContent = contentEditor.getValue();
            if (newContent == null || newContent.trim().isEmpty()) {
                contentDisplay.setText("Sezione vuota");
                contentDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
            } else {
                contentDisplay.getElement().setProperty("innerHTML", newContent.replace("\n", "<br />"));
                contentDisplay.getStyle().remove("color");
                contentDisplay.getStyle().remove("font-style");
            }
            switch (title) {
                case "Descrizione" -> scenarioService.updateScenarioDescription(scenarioId, newContent);
                case "Briefing" -> scenarioService.updateScenarioBriefing(scenarioId, newContent);
                case "Informazioni dai genitori" -> scenarioService.updateScenarioGenitoriInfo(scenarioId, newContent);
                case "Patto Aula" -> scenarioService.updateScenarioPattoAula(scenarioId, newContent);
                case "Obiettivi Didattici" -> scenarioService.updateScenarioObiettiviDidattici(scenarioId, newContent);
                case "Moulage" -> scenarioService.updateScenarioMoulage(scenarioId, newContent);
                case "Liquidi e dosi farmaci" -> scenarioService.updateScenarioLiquidi(scenarioId, newContent);
            }
            contentEditor.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
            Notification.show("Sezione " + title + " aggiornata.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        cancelButton.addClickListener(e -> {
            contentEditor.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        container.add(itemLayout);
    }

    private static void addAzioniChiaveItem(Integer scenarioId, VerticalLayout container, List<String> azioniChiave, ScenarioService scenarioService) {
        if (azioniChiave != null && !azioniChiave.isEmpty()) {
            StringBuilder content = new StringBuilder();
            for (String azione : azioniChiave) {
                content.append("• ").append(azione).append("\n");
            }
            addInfoItemIfNotEmpty(scenarioId, container, "Azioni Chiave", content.toString(), VaadinIcon.KEY, false, scenarioService);
        } else {
            addInfoItemIfNotEmpty(scenarioId, container, "Azioni Chiave", null, VaadinIcon.KEY, false, scenarioService);
        }
    }

    private static void addMaterialeNecessarioItem(Integer scenarioId, VerticalLayout container, MaterialeService materialeService) {
        if (container.getComponentCount() > 0) {
            Hr divider = new Hr();
            divider.getStyle()
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("margin-bottom", "var(--lumo-space-s)")
                    .set("border-color", "var(--lumo-contrast-10pct)");
            container.add(divider);
        }

        VerticalLayout itemLayout = new VerticalLayout();
        itemLayout.setPadding(false);
        itemLayout.setSpacing(false);
        itemLayout.setWidthFull();

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleGroup = new HorizontalLayout();
        titleGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon icon = new Icon(VaadinIcon.TOOLS);
        icon.addClassName(LumoUtility.TextColor.PRIMARY);
        icon.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("margin-right", "var(--lumo-space-xs)");

        H5 titleLabel = new H5("Materiale necessario");
        titleLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        titleLabel.getStyle().set("font-weight", "600");
        titleGroup.add(icon, titleLabel);

        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editButton.setTooltipText("Modifica Materiale necessario");
        headerRow.add(titleGroup, editButton);
        itemLayout.add(headerRow);

        Div contentDisplay = new Div();
        contentDisplay.getStyle()
                .set("font-family", "var(--lumo-font-family)")
                .set("line-height", "var(--lumo-line-height-m)")
                .set("color", "var(--lumo-body-text-color)")
                .set("white-space", "pre-wrap")
                .set("padding", "var(--lumo-space-xs) 0 var(--lumo-space-s) calc(var(--lumo-icon-size-m) + var(--lumo-space-m))")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        Runnable updateContentDisplay = () -> {
            String updatedContent = materialeService.toStringAllMaterialsByScenarioId(scenarioId);
            if (updatedContent == null || updatedContent.trim().isEmpty()) {
                contentDisplay.setText("Sezione vuota");
                contentDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
            } else {
                contentDisplay.getElement().setProperty("innerHTML", updatedContent.replace("\n", "<br />"));
                contentDisplay.getStyle().remove("color");
                contentDisplay.getStyle().remove("font-style");
            }
        };

        updateContentDisplay.run();

        itemLayout.add(contentDisplay);

        IFrame iframe = new IFrame();
        iframe.setWidth("100%");
        iframe.setHeight("600px");
        iframe.setVisible(false);
        iframe.getStyle().set("border", "none");

        Button closeIframeButton = StyleApp.getButton("Chiudi Editor", VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color)");
        closeIframeButton.setVisible(false);

        HorizontalLayout iframeControls = new HorizontalLayout(closeIframeButton);
        iframeControls.setWidthFull();
        iframeControls.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        iframeControls.setVisible(false);

        itemLayout.add(iframe, iframeControls);

        editButton.addClickListener(e -> {
            String url = "materialeNecessario/" + scenarioId + "/edit";
            iframe.setSrc(url);
            iframe.setVisible(true);
            closeIframeButton.setVisible(true);
            iframeControls.setVisible(true);
            contentDisplay.setVisible(false);
            editButton.setVisible(false);
            Notification.show("Apertura editor materiali.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        closeIframeButton.addClickListener(e -> {
            iframe.setVisible(false);
            closeIframeButton.setVisible(false);
            iframeControls.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);

            updateContentDisplay.run();

            Notification.show("Editor materiali chiuso e contenuto aggiornato.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        container.add(itemLayout);
    }

    private static void addInfoItemIfNotEmpty(Integer scenarioId, VerticalLayout container, String title, String content, VaadinIcon iconType, ScenarioService scenarioService) {
        addInfoItemIfNotEmpty(scenarioId, container, title, content, iconType, false, scenarioService);
    }
}