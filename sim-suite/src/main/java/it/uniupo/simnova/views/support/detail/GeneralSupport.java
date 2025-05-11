package it.uniupo.simnova.views.support.detail;

// Aggiunto se InfoItemSupport.createInfoItem restituisce Component

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
// Aggiunto se createInfoItem è implementato qui
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.Scenario;

import java.util.List;

public class GeneralSupport extends HorizontalLayout { // L'estensione di HorizontalLayout non è attivamente usata se si usa solo il metodo statico

    public GeneralSupport() {
        // Costruttore vuoto, la classe è usata principalmente per il metodo statico
    }

    /**
     * Crea il contenuto della panoramica generale dello scenario utilizzando dati pre-caricati.
     *
     * @param scenario            L'oggetto Scenario principale, già caricato.
     * @param isPediatricScenario true se lo scenario è pediatrico (risultato di scenarioService.isPediatric).
     * @param infoGenitore        Stringa con le informazioni dai genitori, se applicabile e disponibile (può essere null).
     * @param materialiNecessari  Stringa con l'elenco dei materiali necessari (risultato di materialeService.toStringAllMaterialsByScenarioId).
     * @return Un VerticalLayout contenente la panoramica generale.
     */
    public static VerticalLayout createOverviewContentWithData(
            Scenario scenario,
            boolean isPediatricScenario,
            String infoGenitore,
            String materialiNecessari,
            List<String> azioniChiave) {

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

        // Utilizza i dati passati direttamente e le proprietà dell'oggetto 'scenario'
        addInfoItemIfNotEmpty(cardContentLayout, "Descrizione", scenario.getDescrizione(), VaadinIcon.PENCIL, true);
        addInfoItemIfNotEmpty(cardContentLayout, "Briefing", scenario.getBriefing(), VaadinIcon.GROUP);

        if (isPediatricScenario) {
            // 'infoGenitore' è già stato recuperato (o è null) e passato come parametro
            addInfoItemIfNotEmpty(cardContentLayout, "Informazioni dai genitori", infoGenitore, VaadinIcon.FAMILY);
        }

        addInfoItemIfNotEmpty(cardContentLayout, "Patto Aula", scenario.getPattoAula(), VaadinIcon.HANDSHAKE);
        addAzioniChiaveItem(cardContentLayout, azioniChiave);
        addInfoItemIfNotEmpty(cardContentLayout, "Obiettivi Didattici", scenario.getObiettivo(), VaadinIcon.BOOK);
        addInfoItemIfNotEmpty(cardContentLayout, "Moulage", scenario.getMoulage(), VaadinIcon.EYE);
        addInfoItemIfNotEmpty(cardContentLayout, "Liquidi e dosi farmaci", scenario.getLiquidi(), VaadinIcon.DROP);

        // Utilizza la stringa 'materialiNecessari' pre-caricata
        addInfoItemIfNotEmpty(cardContentLayout, "Materiale necessario", materialiNecessari, VaadinIcon.TOOLS);

        card.add(cardContentLayout);
        mainLayout.add(card);
        return mainLayout;
    }

    /**
     * Aggiunge un elemento informativo solo se il contenuto non è vuoto.
     * Aggiunge un divisore (Hr) prima di ogni elemento tranne il primo.
     *
     * @param container   contenitore VerticalLayout dove aggiungere l'elemento
     * @param title       titolo dell'informazione
     * @param content     contenuto dell'informazione
     * @param iconType    icona da utilizzare
     * @param isFirstItem true se è il primo elemento, per non aggiungere un Hr prima
     */
    private static void addInfoItemIfNotEmpty(VerticalLayout container, String title, String content, VaadinIcon iconType, boolean isFirstItem) {
        if (content != null && !content.trim().isEmpty()) {
            if (!isFirstItem && container.getComponentCount() > 0) {
                Hr divider = new Hr();
                divider.getStyle()
                        .set("margin-top", "var(--lumo-space-s)")
                        .set("margin-bottom", "var(--lumo-space-m)")
                        .set("border-color", "var(--lumo-contrast-10pct)");
                container.add(divider);
            }

            Icon icon = new Icon(iconType);
            icon.addClassName(LumoUtility.TextColor.PRIMARY);
            icon.getStyle()
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("padding", "var(--lumo-space-s)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("font-size", "var(--lumo-icon-size-l)");

            container.add(InfoItemSupport.createInfoItem(title, content, icon));
        }
    }

    private static void addAzioniChiaveItem(VerticalLayout container, List<String> azioniChiave) {
        if (azioniChiave != null && !azioniChiave.isEmpty()) {
            StringBuilder content = new StringBuilder();
            for (String azione : azioniChiave) {
                content.append("• ").append(azione).append("\n");
            }
            addInfoItemIfNotEmpty(container, "Azioni Chiave", content.toString(), VaadinIcon.KEY);
        }
    }

    // Metodo sovraccaricato per convenienza, assume che non sia il primo item se non specificato
    private static void addInfoItemIfNotEmpty(VerticalLayout container, String title, String content, VaadinIcon iconType) {
        addInfoItemIfNotEmpty(container, title, content, iconType, false);
    }
}