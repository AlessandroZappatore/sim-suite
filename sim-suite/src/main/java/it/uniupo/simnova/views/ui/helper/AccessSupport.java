package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AccessSupport {
    private static final Icon ACCESSICON = new Icon(VaadinIcon.LINES);
    static Logger logger = LoggerFactory.getLogger(AccessSupport.class);
    public static Div getAccessoCard(PazienteT0Service pazienteT0Service, Integer scenarioId) {
        PazienteT0 paziente = pazienteT0Service.getPazienteT0ById(scenarioId);
        if (!paziente.getAccessiVenosi().isEmpty() || !paziente.getAccessiArteriosi().isEmpty()) {
            Div accessesCard = new Div();
            accessesCard.getStyle()
                    .set("margin-top", "var(--lumo-space-m)")
                    .set("padding-top", "var(--lumo-space-s)")
                    .set("border-top", "1px solid var(--lumo-contrast-10pct)")
                    .set("width", "100%");

            //Accessi Venosi
            HorizontalLayout accessVenosiTitleLayout = new HorizontalLayout();
            accessVenosiTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            accessVenosiTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            accessVenosiTitleLayout.setWidthFull();
            accessVenosiTitleLayout.setSpacing(true);

            Icon accessVenosiIcon = new Icon(VaadinIcon.LINES);
            accessVenosiIcon.getStyle()
                    .set("color", "var(--lumo-primary-color)")
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("padding", "var(--lumo-space-xs)")
                    .set("border-radius", "50%");

            H4 accessVenosiTitle = new H4("Accessi Venosi");
            accessVenosiTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "500");

            accessVenosiTitleLayout.add(accessVenosiIcon, accessVenosiTitle);
            accessesCard.add(accessVenosiTitleLayout);

            Grid<Accesso> accessiVenosiGrid = createAccessiGrid(paziente.getAccessiVenosi(), pazienteT0Service, scenarioId, true);
            accessiVenosiGrid.getStyle()
                    .set("border", "none")
                    .set("box-shadow", "none")
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("margin-bottom", "var(--lumo-space-s)")
                    .set("margin-left", "auto")
                    .set("margin-right", "auto")
                    .set("max-width", "600px");
            accessesCard.add(accessiVenosiGrid);

            // Pulsante Aggiungi Accesso Venoso
            Button addVenosoButton = StyleApp.getButton("Aggiungi Accesso Venoso",
                    VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
            addVenosoButton.getStyle()
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("margin-bottom", "var(--lumo-space-m)");

            // TODO: Implementare logica di aggiunta accesso venoso
            addVenosoButton.addClickListener(e -> {
                // Logica da implementare
            });

            HorizontalLayout addVenosoLayout = new HorizontalLayout(addVenosoButton);
            addVenosoLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            addVenosoLayout.setWidthFull();
            accessesCard.add(addVenosoLayout);

            //Accessi Arteriosi
            HorizontalLayout accessArtTitleLayout = new HorizontalLayout();
            accessArtTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            accessArtTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            accessArtTitleLayout.setWidthFull();
            accessArtTitleLayout.setSpacing(true);

            Icon accessArtIcon = new Icon(VaadinIcon.LINES);
            accessArtIcon.getStyle()
                    .set("color", "var(--lumo-success-color)")
                    .set("background-color", "var(--lumo-success-color-10pct)")
                    .set("padding", "var(--lumo-space-xs)")
                    .set("border-radius", "50%");

            H4 accessArtTitle = new H4("Accessi Arteriosi");
            accessArtTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "500");

            accessArtTitleLayout.add(accessArtIcon, accessArtTitle);
            accessesCard.add(accessArtTitleLayout);

            Grid<Accesso> accessiArtGrid = createAccessiGrid(paziente.getAccessiArteriosi(), pazienteT0Service, scenarioId, false);
            accessiArtGrid.getStyle()
                    .set("border", "none")
                    .set("box-shadow", "none")
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("margin-left", "auto")
                    .set("margin-right", "auto")
                    .set("max-width", "600px");
            accessesCard.add(accessiArtGrid);

            // Pulsante Aggiungi Accesso Arterioso
            Button addArtButton = StyleApp.getButton("Aggiungi Accesso Arterioso",
                    VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
            addArtButton.getStyle()
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("margin-bottom", "var(--lumo-space-m)");

            // TODO: Implementare logica di aggiunta accesso arterioso
            addArtButton.addClickListener(e -> {
                // Logica da implementare
            });

            HorizontalLayout addArtLayout = new HorizontalLayout(addArtButton);
            addArtLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            addArtLayout.setWidthFull();
            accessesCard.add(addArtLayout);

            return accessesCard;
        }

        return null;
    }

    private static Grid<Accesso> createAccessiGrid(List<Accesso> accessi, PazienteT0Service pazienteT0Service, Integer scenarioId, boolean isVenoso) {
        Grid<Accesso> grid = new Grid<>();
        grid.setItems(accessi);
        grid.addColumn(Accesso::getTipologia).setHeader("Tipologia").setAutoWidth(true);
        grid.addColumn(Accesso::getPosizione).setHeader("Posizione").setAutoWidth(true);
        grid.addColumn(Accesso::getLato).setHeader("Lato").setAutoWidth(true);
        grid.addColumn(Accesso::getMisura).setHeader("Misura").setAutoWidth(true);

        // Colonna per il pulsante di cancellazione
        grid.addComponentColumn(accesso -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.getElement().setAttribute("aria-label", "Elimina accesso");
            deleteButton.getStyle()
                    .set("color", "var(--lumo-error-color)")
                    .set("cursor", "pointer");

            deleteButton.addClickListener(e -> {
                // Creazione del dialog di conferma
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Conferma eliminazione");
                confirmDialog.setText("Sei sicuro di voler eliminare questo accesso " +
                        (isVenoso ? "venoso" : "arterioso") + "?");

                confirmDialog.setCancelable(true);
                confirmDialog.setCancelText("Annulla");
                confirmDialog.setConfirmText("Elimina");
                confirmDialog.setConfirmButtonTheme("error primary");

                confirmDialog.addConfirmListener(event -> {
                    try {
                        pazienteT0Service.deleteAccesso(scenarioId, accesso.getId(), isVenoso);
                        Notification.show("Accesso " + (isVenoso ? "venoso" : "arterioso") + " eliminato con successo", 3000, Notification.Position.BOTTOM_CENTER);
                    } catch (Exception ex) {
                        logger.error("Errore durante l'eliminazione dell'accesso", ex);
                        Notification.show("Errore durante l'eliminazione dell'accesso", 3000, Notification.Position.MIDDLE);
                    }
                });

                confirmDialog.open();
            });

            return deleteButton;
        }).setHeader("Azioni").setAutoWidth(true).setFlexGrow(0);

        grid.setAllRowsVisible(true);
        return grid;
    }
}