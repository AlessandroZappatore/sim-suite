package it.uniupo.simnova.views.support.detail;

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
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Centra la card

        // Container principale della card
        Div sceneggiaturaCard = new Div();
        sceneggiaturaCard.addClassName("sceneggiatura-card");
        sceneggiaturaCard.getStyle()
                .set("width", "100%")
                .set("max-width", "800px") // Larghezza massima standard
                .set("margin", "var(--lumo-space-l) 0") // Margine verticale
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)") // Ombra standard
                .set("padding", "var(--lumo-space-l)") // Padding interno standard
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        // Effetto hover sulla card
        sceneggiaturaCard.getElement().executeJs(
                "this.addEventListener('mouseover', function() {" +
                        "  this.style.transform = 'translateY(-2px)';" +
                        "  this.style.boxShadow = 'var(--lumo-box-shadow-l)';" + // Ombra più grande su hover
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
        // headerLayout.setSpacing(true); // La spaziatura può essere gestita dai margini dell'icona
        headerLayout.getStyle()
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("padding-bottom", "var(--lumo-space-m)"); // Aumentato padding sotto l'intestazione

        Icon scriptIcon = new Icon(VaadinIcon.FILE_TEXT_O); // Icona leggermente diversa per varietà
        scriptIcon.addClassName(LumoUtility.TextColor.PRIMARY);
        scriptIcon.getStyle()
                .set("font-size", "var(--lumo-icon-size-m)") // Icona più grande
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)") // Raggio bordo coerente
                .set("margin-right", "var(--lumo-space-m)"); // Spazio a destra dell'icona

        H3 scriptTitle = new H3("Sceneggiatura");
        scriptTitle.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        scriptTitle.getStyle().set("font-weight", "600"); // Titolo più marcato

        headerLayout.add(scriptIcon, scriptTitle);

        // Contenuto della sceneggiatura
        Div contentDisplayWrapper = new Div(); // Wrapper per il contenuto testuale
        contentDisplayWrapper.setWidthFull();

        if (sceneggiaturaText == null || sceneggiaturaText.trim().isEmpty()) {
            Div emptyMessage = EmptySupport.createErrorContent("Nessuna sceneggiatura disponibile");
            // Potremmo voler stilizzare diversamente il messaggio di errore qui o centrarlo
            emptyMessage.getStyle().set("text-align", "center").set("padding", "var(--lumo-space-l)");
            contentDisplayWrapper.add(emptyMessage);
        } else {
            Div scriptTextDisplay = new Div();
            scriptTextDisplay.getStyle()
                    .set("font-family", "var(--lumo-font-family)")
                    .set("line-height", "var(--lumo-line-height-l)") // Interlinea maggiore
                    .set("color", "var(--lumo-body-text-color)")
                    .set("white-space", "pre-wrap") // Mantiene gli a capo e gli spazi
                    .set("padding", "var(--lumo-space-m)") // Padding interno
                    .set("max-height", "60vh") // Altezza massima relativa al viewport
                    .set("overflow-y", "auto") // Scroll verticale se necessario
                    .set("border-left", "3px solid var(--lumo-primary-color)") // Bordo sinistro colorato
                    .set("background-color", "var(--lumo-contrast-5pct)") // Sfondo leggermente diverso
                    .set("border-radius", "var(--lumo-border-radius-s)"); // Leggero arrotondamento

            // Imposta il testo usando setProperty per interpretare i <br>
            // o usa direttamente setText se "white-space: pre-wrap" è sufficiente
            scriptTextDisplay.getElement().setProperty("innerHTML", sceneggiaturaText.replace("\n", "<br />"));

            contentDisplayWrapper.add(scriptTextDisplay);
        }

        sceneggiaturaCard.add(headerLayout, contentDisplayWrapper);
        mainLayout.add(sceneggiaturaCard);
        return mainLayout;
    }
}