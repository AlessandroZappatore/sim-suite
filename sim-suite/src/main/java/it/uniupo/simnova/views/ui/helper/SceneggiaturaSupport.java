package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout; // Importato
import com.vaadin.flow.theme.lumo.LumoUtility; // Importato
// Assumendo che sia accessibile staticamente o iniettato


public class SceneggiaturaSupport extends HorizontalLayout { // L'estensione potrebbe non essere necessaria se si usano solo metodi statici

    public SceneggiaturaSupport() {
        // Costruttore vuoto, la classe è usata per metodi statici
    }

    public static Component createSceneggiaturaContent(String sceneggiaturaText) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(false);
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Se non c'è sceneggiatura, restituisci direttamente il messaggio di errore
        if (sceneggiaturaText == null || sceneggiaturaText.trim().isEmpty()) {
            Div emptyMessage = EmptySupport.createErrorContent("Nessuna sceneggiatura disponibile");
            mainLayout.add(emptyMessage);
            return mainLayout;
        }

        // Container principale della card (solo se abbiamo contenuto)
        Div sceneggiaturaCard = new Div();
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


        // Effetto hover sulla card
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

        // Intestazione con icona e titolo
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

        headerLayout.add(scriptIcon, scriptTitle);
        sceneggiaturaCard.add(headerLayout);

        // Contenuto della sceneggiatura
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


        scriptTextDisplay.getElement().setProperty("innerHTML", sceneggiaturaText.replace("\n", "<br />"));
        sceneggiaturaCard.add(scriptTextDisplay);

        mainLayout.add(sceneggiaturaCard);
        return mainLayout;
    }
}