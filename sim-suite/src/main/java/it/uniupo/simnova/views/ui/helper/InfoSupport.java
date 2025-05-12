package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import it.uniupo.simnova.domain.scenario.Scenario;

public class InfoSupport extends HorizontalLayout {
    public InfoSupport() {
        // Constructor
    }

    public static Component getInfo(Scenario scenario) {
        // Contenitore principale con layout orizzontale e centrato
        HorizontalLayout badgesContainer = new HorizontalLayout();
        badgesContainer.setWidthFull();
        badgesContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        badgesContainer.setSpacing(true);
        badgesContainer.getStyle().set("flex-wrap", "wrap");

        // Colore primario per tutti i badge per uniformità
        String badgeColor = "var(--lumo-primary-color)";

        // Crea i badge con informazioni
        Span pazienteBadge = createInfoBadge("Paziente", scenario.getNomePaziente(), badgeColor);
        Span tipologiaBadge = createInfoBadge("Tipologia", scenario.getTipologia(), badgeColor);
        Span patologiaBadge = createInfoBadge("Patologia", scenario.getPatologia(), badgeColor);
        Span durataBadge = createInfoBadge("Durata", String.format("%.1f min", scenario.getTimerGenerale()), badgeColor);
        Span targetBadge = createInfoBadge("Target", scenario.getTarget(), badgeColor);

        // Aggiungi i badge al container
        badgesContainer.add(pazienteBadge, tipologiaBadge, patologiaBadge, durataBadge, targetBadge);

        return badgesContainer;
    }

    /**
     * Crea un badge con etichetta e valore
     *
     * @param label etichetta del badge
     * @param value valore da mostrare
     * @param color colore del badge
     * @return componente Span formattato come badge
     */
    private static Span createInfoBadge(String label, String value, String color) {
        Span badge = new Span(label + ": " + value);
        badge.getStyle()
                .set("background-color", color + "10")  // Colore con opacità al 10%
                .set("color", color)
                .set("border-radius", "16px")
                .set("padding", "6px 16px")
                .set("font-size", "16px")           // Aumentata dimensione del testo
                .set("font-weight", "500")
                .set("margin", "6px")               // Aumentato il margine
                .set("display", "inline-block")
                .set("border", "1px solid " + color + "40")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");

        // Effetto hover
        badge.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.boxShadow = '0 3px 6px rgba(0,0,0,0.15)'; " +
                        "  this.style.transform = 'translateY(-2px)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)'; " +
                        "  this.style.transform = 'translateY(0)'; " +
                        "});"
        );

        return badge;
    }
}
