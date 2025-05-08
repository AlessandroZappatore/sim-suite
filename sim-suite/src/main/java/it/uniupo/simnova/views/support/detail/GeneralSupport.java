package it.uniupo.simnova.views.support.detail;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr; // Importato per i divisori
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent; // Per Alignment
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility; // Per utility di stile
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.service.MaterialeService;
import it.uniupo.simnova.service.ScenarioService;


public class GeneralSupport extends HorizontalLayout { // Estensione di HorizontalLayout non usata attivamente

    public GeneralSupport() {
        // Il costruttore è vuoto, la classe è usata principalmente per il metodo statico
    }

    public static VerticalLayout createOverviewContent(Scenario scenario, ScenarioService scenarioService, MaterialeService materialeService) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true); // Aggiunge padding attorno alla card
        mainLayout.setSpacing(false); // La spaziatura è gestita dai margini della card
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Centra la card orizzontalmente
        // mainLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Opzionale per centratura verticale
        // mainLayout.setMinHeight("100vh"); // Opzionale per occupare tutta l'altezza

        // Card per informazioni base con stile migliorato
        Div card = new Div();
        card.addClassName("info-card");
        card.getStyle()
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)") // Ombra leggermente più pronunciata
                .set("padding", "var(--lumo-space-l)") // Più padding interno
                .set("background-color", "var(--lumo-base-color)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("width", "100%") // Occupa la larghezza del contenitore centrato
                .set("max-width", "800px") // Larghezza massima per la leggibilità
                .set("margin", "var(--lumo-space-l) 0"); // Margine verticale

        // Effetto hover sulla card
        card.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-l)'; });" + // Ombra più grande su hover
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-m)'; });"
        );

        // Aggiungi elementi con controlli null-safety
        // Utilizziamo un VerticalLayout interno alla card per gestire meglio i divisori
        VerticalLayout cardContentLayout = new VerticalLayout();
        cardContentLayout.setPadding(false);
        cardContentLayout.setSpacing(false); // La spaziatura sarà data dai divisori e margini degli item
        cardContentLayout.setWidthFull();

        addInfoItemIfNotEmpty(cardContentLayout, "Descrizione", scenario.getDescrizione(), VaadinIcon.PENCIL, true);
        addInfoItemIfNotEmpty(cardContentLayout, "Briefing", scenario.getBriefing(), VaadinIcon.GROUP);

        if (scenarioService.isPediatric(scenario.getId())) {
            addInfoItemIfNotEmpty(cardContentLayout, "Informazioni dai genitori", scenario.getInfoGenitore(), VaadinIcon.FAMILY);
        }

        addInfoItemIfNotEmpty(cardContentLayout, "Patto Aula", scenario.getPattoAula(), VaadinIcon.HANDSHAKE);
        addInfoItemIfNotEmpty(cardContentLayout, "Azioni Chiave", scenario.getAzioneChiave(), VaadinIcon.KEY);
        addInfoItemIfNotEmpty(cardContentLayout, "Obiettivi Didattici", scenario.getObiettivo(), VaadinIcon.BOOK);
        addInfoItemIfNotEmpty(cardContentLayout, "Moulage", scenario.getMoulage(), VaadinIcon.EYE);
        addInfoItemIfNotEmpty(cardContentLayout, "Liquidi e dosi farmaci", scenario.getLiquidi(), VaadinIcon.DROP);

        // Materiale necessario
        String materiali = materialeService.toStringAllMaterialsByScenarioId(scenario.getId());
        addInfoItemIfNotEmpty(cardContentLayout, "Materiale necessario", materiali, VaadinIcon.TOOLS); // Icona cambiata per "materiali/tools"

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
            icon.addClassName(LumoUtility.TextColor.PRIMARY); // Usa utility per colore primario
            icon.getStyle()
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("padding", "var(--lumo-space-s)") // Padding leggermente aumentato
                    .set("border-radius", "var(--lumo-border-radius-l)") // Più arrotondato
                    .set("font-size", "var(--lumo-icon-size-m)"); // Dimensione icona media

            // InfoItemSupport.createInfoItem dovrebbe restituire un Component
            // Assumiamo che crei un layout con titolo e contenuto.
            // Per un migliore controllo dello stile, potremmo ricrearlo qui o modificarlo in InfoItemSupport.
            // Per ora, lo aggiungiamo direttamente.
            container.add(InfoItemSupport.createInfoItem(title, content, icon));
        }
    }

    // Metodo sovraccaricato per convenienza, assume che non sia il primo item se non specificato
    private static void addInfoItemIfNotEmpty(VerticalLayout container, String title, String content, VaadinIcon iconType) {
        addInfoItemIfNotEmpty(container, title, content, iconType, false);
    }
}