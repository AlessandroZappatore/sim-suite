package it.uniupo.simnova.views.common.components;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import static it.uniupo.simnova.views.constant.CreditConst.DATE;
import static it.uniupo.simnova.views.constant.CreditConst.VERSION;

/**
 * Componente riutilizzabile per visualizzare i crediti dell'applicazione.
 * Include informazioni sullo sviluppatore, l'università e i contatti.
 */
public class CreditsComponent extends VerticalLayout {
    /**
     * Crea una nuova istanza del componente crediti.
     */
    public CreditsComponent() {
        this.setPadding(false);
        this.setSpacing(false);
        this.setMargin(false);
        this.setWidthFull();
        this.setAlignItems(FlexComponent.Alignment.START);

        // Titolo sezione crediti
        Paragraph creditsTitle = new Paragraph("Crediti");
        creditsTitle.addClassNames(
                LumoUtility.FontWeight.BOLD,
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.SECONDARY
        );
        creditsTitle.getStyle().set("margin", "0 0 4px 0");

        // Riga sviluppatore con nome
        HorizontalLayout developerRow = getHorizontalLayout();

        // Riga contatti
        HorizontalLayout contactsRow = new HorizontalLayout();
        contactsRow.setSpacing(true);
        contactsRow.setPadding(false);
        contactsRow.setMargin(false);
        contactsRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon emailIcon = new Icon(VaadinIcon.ENVELOPE);
        emailIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Anchor emailLink = new Anchor("mailto:alessandrozappatore03@gmail.com", "alessandrozappatore03@gmail.com");
        emailLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Icon githubIcon = new Icon(VaadinIcon.CODE);
        githubIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        githubIcon.getStyle().set("margin-left", "8px");

        Anchor githubLink = new Anchor("https://github.com/AlessandroZappatore", "Github: AlessandroZappatore");
        githubLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        githubLink.getElement().setAttribute("target", "_blank");
        githubLink.getElement().setAttribute("rel", "noopener noreferrer");

        contactsRow.add(emailIcon, emailLink, githubIcon, githubLink);

        // Riga università con icona
        HorizontalLayout universityRow = new HorizontalLayout();
        universityRow.setSpacing(true);
        universityRow.setPadding(false);
        universityRow.setMargin(false);
        universityRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon universityIcon = new Icon(VaadinIcon.ACADEMY_CAP);
        universityIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Anchor universityLink = new Anchor("https://www.uniupo.it", "Università del Piemonte Orientale");
        universityLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        universityLink.getElement().setAttribute("target", "_blank");
        universityLink.getElement().setAttribute("rel", "noopener noreferrer");

        universityRow.add(universityIcon, universityLink);

        HorizontalLayout versionRow = new HorizontalLayout();
        versionRow.setSpacing(true);
        versionRow.setPadding(false);
        versionRow.setMargin(false);
        versionRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon versionIcon = new Icon(VaadinIcon.INFO_CIRCLE);
        versionIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Span versionText = new Span("Versione: " + VERSION);
        versionText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Icon dateIcon = new Icon(VaadinIcon.CALENDAR);
        dateIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        dateIcon.getStyle().set("margin-left", "8px");

        Span dateText = new Span("Data: " + DATE);
        dateText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        versionRow.add(versionIcon, versionText, dateIcon, dateText);
        // Aggiunta dei componenti al layout
        this.add(creditsTitle, developerRow, contactsRow, universityRow, versionRow);
    }

    private static HorizontalLayout getHorizontalLayout() {
        HorizontalLayout developerRow = new HorizontalLayout();
        developerRow.setSpacing(true);
        developerRow.setPadding(false);
        developerRow.setMargin(false);
        developerRow.setAlignItems(Alignment.CENTER);

        Icon developerIcon = new Icon(VaadinIcon.USER);
        developerIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Span developerText = new Span("Sviluppato da Alessandro Zappatore");
        developerText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        developerRow.add(developerIcon, developerText);
        return developerRow;
    }
}