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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AccessSupport {
    static Logger logger = LoggerFactory.getLogger(AccessSupport.class);

    public static Div getAccessoCard(PazienteT0Service pazienteT0Service, Integer scenarioId) {
        PazienteT0 paziente = pazienteT0Service.getPazienteT0ById(scenarioId);

        Div accessesCard = new Div();
        accessesCard.getStyle()
                .set("margin-top", "var(--lumo-space-m)")
                .set("padding-top", "var(--lumo-space-s)")
                .set("border-top", "1px solid var(--lumo-contrast-10pct)")
                .set("width", "100%");

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

        VerticalLayout addVenosoFormContainer = new VerticalLayout();
        addVenosoFormContainer.setWidthFull();
        addVenosoFormContainer.setSpacing(true);
        addVenosoFormContainer.setPadding(false);
        addVenosoFormContainer.setVisible(false);

        Button addVenosoButton = StyleApp.getButton("Aggiungi Accesso Venoso",
                VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addVenosoButton.getStyle()
                .set("margin-top", "var(--lumo-space-s)")
                .set("margin-bottom", "var(--lumo-space-m)");

        addVenosoButton.addClickListener(e -> {
            addVenosoFormContainer.removeAll();
            AccessoComponent nuovoAccessoVenosoComp = new AccessoComponent("Venoso", false);
            addVenosoFormContainer.add(nuovoAccessoVenosoComp);

            Button saveVenosoButton = StyleApp.getButton("Salva",
                    VaadinIcon.CHECK, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
            Button cancelVenosoButton = StyleApp.getButton("Annulla",
                    VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color)");

            saveVenosoButton.addClickListener(saveEvent -> {
                Accesso nuovoAccesso = nuovoAccessoVenosoComp.getAccesso();
                if (nuovoAccesso.getTipologia() == null || nuovoAccesso.getTipologia().isEmpty() ||
                        nuovoAccesso.getPosizione() == null || nuovoAccesso.getPosizione().isEmpty() ||
                        nuovoAccesso.getLato() == null || nuovoAccesso.getLato().isEmpty() ||
                        nuovoAccesso.getMisura() == null) {
                    Notification.show("Compilare tutti i campi dell'accesso.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                try {
                    pazienteT0Service.addAccesso(scenarioId, nuovoAccesso, true);
                    PazienteT0 pazienteAggiornato = pazienteT0Service.getPazienteT0ById(scenarioId);
                    accessiVenosiGrid.setItems(pazienteAggiornato.getAccessiVenosi());
                    Notification.show("Accesso venoso aggiunto con successo", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    addVenosoFormContainer.removeAll();
                    addVenosoFormContainer.setVisible(false);
                    addVenosoButton.setVisible(true);
                } catch (Exception ex) {
                    logger.error("Errore durante l'aggiunta dell'accesso venoso", ex);
                    Notification.show("Errore durante l'aggiunta dell'accesso venoso: " + ex.getMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            cancelVenosoButton.addClickListener(cancelEvent -> {
                addVenosoFormContainer.removeAll();
                addVenosoFormContainer.setVisible(false);
                addVenosoButton.setVisible(true);
            });

            HorizontalLayout buttonsLayout = new HorizontalLayout(saveVenosoButton, cancelVenosoButton);
            buttonsLayout.setSpacing(true);
            addVenosoFormContainer.add(buttonsLayout);
            addVenosoFormContainer.setVisible(true);
            addVenosoButton.setVisible(false);
        });

        HorizontalLayout addVenosoLayout = new HorizontalLayout(addVenosoButton);
        addVenosoLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addVenosoLayout.setWidthFull();
        accessesCard.add(addVenosoLayout);
        accessesCard.add(addVenosoFormContainer);

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

        VerticalLayout addArteriosoFormContainer = new VerticalLayout();
        addArteriosoFormContainer.setWidthFull();
        addArteriosoFormContainer.setSpacing(true);
        addArteriosoFormContainer.setPadding(false);
        addArteriosoFormContainer.setVisible(false);

        Button addArtButton = StyleApp.getButton("Aggiungi Accesso Arterioso",
                VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addArtButton.getStyle()
                .set("margin-top", "var(--lumo-space-s)")
                .set("margin-bottom", "var(--lumo-space-m)");

        addArtButton.addClickListener(e -> {
            addArteriosoFormContainer.removeAll();
            AccessoComponent nuovoAccessoArteriosoComp = new AccessoComponent("Arterioso", false);
            addArteriosoFormContainer.add(nuovoAccessoArteriosoComp);

            Button saveArteriosoButton = StyleApp.getButton("Salva",
                    VaadinIcon.CHECK, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
            Button cancelArteriosoButton = StyleApp.getButton("Cancella",
                    VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color)");

            saveArteriosoButton.addClickListener(saveEvent -> {
                Accesso nuovoAccesso = nuovoAccessoArteriosoComp.getAccesso();
                if (nuovoAccesso.getTipologia() == null || nuovoAccesso.getTipologia().isEmpty() ||
                        nuovoAccesso.getPosizione() == null || nuovoAccesso.getPosizione().isEmpty() ||
                        nuovoAccesso.getLato() == null || nuovoAccesso.getLato().isEmpty() ||
                        nuovoAccesso.getMisura() == null) {
                    Notification.show("Compilare tutti i campi dell'accesso.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                try {
                    pazienteT0Service.addAccesso(scenarioId, nuovoAccesso, false);
                    PazienteT0 pazienteAggiornato = pazienteT0Service.getPazienteT0ById(scenarioId);
                    accessiArtGrid.setItems(pazienteAggiornato.getAccessiArteriosi());
                    Notification.show("Accesso arterioso aggiunto con successo", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    addArteriosoFormContainer.removeAll();
                    addArteriosoFormContainer.setVisible(false);
                    addArtButton.setVisible(true);
                } catch (Exception ex) {
                    logger.error("Errore durante l'aggiunta dell'accesso arterioso", ex);
                    Notification.show("Errore durante l'aggiunta dell'accesso arterioso: " + ex.getMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            cancelArteriosoButton.addClickListener(cancelEvent -> {
                addArteriosoFormContainer.removeAll();
                addArteriosoFormContainer.setVisible(false);
                addArtButton.setVisible(true);
            });

            HorizontalLayout buttonsLayout = new HorizontalLayout(saveArteriosoButton, cancelArteriosoButton);
            buttonsLayout.setSpacing(true);
            addArteriosoFormContainer.add(buttonsLayout);
            addArteriosoFormContainer.setVisible(true);
            addArtButton.setVisible(false);
        });

        HorizontalLayout addArtLayout = new HorizontalLayout(addArtButton);
        addArtLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addArtLayout.setWidthFull();
        accessesCard.add(addArtLayout);
        accessesCard.add(addArteriosoFormContainer);

        return accessesCard;
    }

    private static Grid<Accesso> createAccessiGrid(List<Accesso> accessi, PazienteT0Service pazienteT0Service, Integer scenarioId, boolean isVenoso) {
        Grid<Accesso> grid = new Grid<>();
        grid.setItems(accessi);
        grid.addColumn(Accesso::getTipologia).setHeader("Tipologia").setAutoWidth(true);
        grid.addColumn(Accesso::getPosizione).setHeader("Posizione").setAutoWidth(true);
        grid.addColumn(Accesso::getLato).setHeader("Lato").setAutoWidth(true);
        grid.addColumn(Accesso::getMisura).setHeader("Misura").setAutoWidth(true);

        grid.addComponentColumn(accesso -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.setTooltipText("Elimina accesso " + accesso.getTipologia());
            deleteButton.getElement().setAttribute("aria-label", "Elimina accesso");
            deleteButton.getStyle()
                    .set("color", "var(--lumo-error-color)")
                    .set("cursor", "pointer");

            deleteButton.addClickListener(e -> {
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
                        Notification.show("Accesso " + (isVenoso ? "venoso" : "arterioso") + " eliminato con successo", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    } catch (Exception ex) {
                        logger.error("Errore durante l'eliminazione dell'accesso", ex);
                        Notification.show("Errore durante l'eliminazione dell'accesso", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
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

