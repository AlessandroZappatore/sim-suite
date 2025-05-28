package it.uniupo.simnova.views.common.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import static it.uniupo.simnova.views.constant.CreditConst.*; // Importa le costanti dei crediti

/**
 * Componente riutilizzabile per visualizzare i crediti dell'applicazione.
 * Include informazioni sull'ideatore, l'università e i contatti dello sviluppatore,
 * visualizzati in un popup dialog.
 *
 * @author Alessandro Zappatore
 * @version 1.1
 */
public class CreditsComponent extends VerticalLayout {

    /**
     * Costruttore che inizializza il componente dei crediti.
     * Imposta il layout e aggiunge le righe informative (ideatore, sviluppatore, università, versione).
     */
    public CreditsComponent() {
        this.setPadding(false);
        this.setSpacing(false);
        this.setMargin(false);
        this.setWidthFull();
        this.setAlignItems(FlexComponent.Alignment.START);

        Paragraph creditsTitle = new Paragraph("Crediti");
        creditsTitle.addClassNames(
                LumoUtility.FontWeight.BOLD,
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.SECONDARY
        );
        creditsTitle.getStyle().set("margin", "0 0 4px 0"); // Margine inferiore

        // Righe delle informazioni sui crediti
        HorizontalLayout ideatorRow = getRow(VaadinIcon.LIGHTBULB.create());
        HorizontalLayout developerRow = createDeveloperRow();
        HorizontalLayout universityRow = createUniversityRow();
        HorizontalLayout versionRow = createVersionRow();

        this.add(creditsTitle, ideatorRow, developerRow, universityRow, versionRow);
    }

    /**
     * Crea una riga orizzontale standard con un'icona e un link all'ideatore.
     *
     * @param icon L'icona da visualizzare.
     * @return Un {@link HorizontalLayout} contenente l'icona e il link.
     */
    private static HorizontalLayout getRow(Icon icon) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(Alignment.CENTER);

        Anchor linkAnchor = new Anchor(IDEATORLINK, "Ideatore: Antonio Scalogna");
        linkAnchor.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        linkAnchor.getElement().setAttribute("target", "_blank"); // Apre il link in una nuova scheda
        linkAnchor.getElement().setAttribute("rel", "noopener noreferrer"); // Previene problemi di sicurezza

        icon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        row.add(icon, linkAnchor);
        return row;
    }

    /**
     * Crea una riga orizzontale con le informazioni dello sviluppatore.
     * Include un'icona, il nome dello sviluppatore come pulsante che apre un dialogo con i contatti.
     *
     * @return Un {@link HorizontalLayout} contenente le informazioni dello sviluppatore.
     */
    private HorizontalLayout createDeveloperRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(Alignment.CENTER);

        Icon developerIcon = VaadinIcon.USER.create();
        developerIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Button developerNameButton = new Button("Sviluppatore: Alessandro Zappatore");
        developerNameButton.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.XSMALL,
                LumoUtility.Padding.Right.NONE,
                LumoUtility.Padding.Left.NONE,
                LumoUtility.Background.TRANSPARENT // Rende il pulsante trasparente per sembrare un testo
        );

        developerNameButton.getStyle().set("cursor", "pointer"); // Cambia il cursore per indicare che è cliccabile

        developerNameButton.addClickListener(e -> openDeveloperInfoDialog()); // Apre il dialogo con i contatti

        row.add(developerIcon, developerNameButton);
        return row;
    }

    /**
     * Crea una riga orizzontale con le informazioni sull'università.
     * Include un'icona e un link all'università.
     *
     * @return Un {@link HorizontalLayout} contenente le informazioni sull'università.
     */
    private HorizontalLayout createUniversityRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon universityIcon = new Icon(VaadinIcon.ACADEMY_CAP);
        universityIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Anchor universityLink = new Anchor(UNIVERSITYLINK, "Università del Piemonte Orientale");
        universityLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        universityLink.getElement().setAttribute("target", "_blank");
        universityLink.getElement().setAttribute("rel", "noopener noreferrer");

        row.add(universityIcon, universityLink);
        return row;
    }

    /**
     * Crea una riga orizzontale con le informazioni sulla versione dell'applicazione e la data di rilascio.
     * Include icone per la versione e la data.
     *
     * @return Un {@link HorizontalLayout} contenente le informazioni sulla versione.
     */
    private HorizontalLayout createVersionRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon versionIcon = new Icon(VaadinIcon.INFO_CIRCLE);
        versionIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Anchor versionLink = new Anchor(RELEASELINK, "Versione: " + VERSION);
        versionLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        versionLink.getElement().setAttribute("target", "_blank");
        versionLink.getElement().setAttribute("rel", "noopener noreferrer");

        Icon dateIcon = new Icon(VaadinIcon.CALENDAR);
        dateIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        dateIcon.getStyle().set("margin-left", "8px"); // Margine per separare dal link della versione

        Span dateText = new Span("Data: " + DATE);
        dateText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        row.add(versionIcon, versionLink, dateIcon, dateText);
        return row;
    }

    /**
     * Apre un dialogo modale che visualizza le informazioni di contatto dello sviluppatore.
     * Include indirizzo email, link a GitHub e link a LinkedIn.
     */
    private void openDeveloperInfoDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Contatti Sviluppatore");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);
        dialogContent.setAlignItems(Alignment.START);
        dialogContent.setWidthFull();

        // Riga per l'indirizzo email
        HorizontalLayout emailRow = new HorizontalLayout();
        emailRow.setSpacing(true);
        emailRow.setAlignItems(Alignment.CENTER);
        Icon emailIcon = new Icon(VaadinIcon.ENVELOPE);
        emailIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        Anchor emailLink = new Anchor(DEVELOPERMAIL, "alessandrozappatore03@gmail.com");
        emailLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        emailRow.add(emailIcon, emailLink);

        // Riga per il link a GitHub
        HorizontalLayout githubRow = new HorizontalLayout();
        githubRow.setSpacing(true);
        githubRow.setAlignItems(Alignment.CENTER);
        Icon githubIcon = FontAwesome.Brands.GITHUB.create();
        githubIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        Anchor githubLink = new Anchor(GITHUBLINK, "Github: AlessandroZappatore");
        githubLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        githubLink.getElement().setAttribute("target", "_blank");
        githubLink.getElement().setAttribute("rel", "noopener noreferrer");
        githubRow.add(githubIcon, githubLink);

        // Riga per il link a LinkedIn
        HorizontalLayout linkedinRow = new HorizontalLayout();
        linkedinRow.setSpacing(true);
        linkedinRow.setAlignItems(Alignment.CENTER);
        Icon linkedinIcon = FontAwesome.Brands.LINKEDIN.create();
        linkedinIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        Anchor linkedinLink = new Anchor(DEVELOPERLINK, "LinkedIn: Alessandro Zappatore");
        linkedinLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        linkedinLink.getElement().setAttribute("target", "_blank");
        linkedinLink.getElement().setAttribute("rel", "noopener noreferrer");
        linkedinRow.add(linkedinIcon, linkedinLink);

        dialogContent.add(emailRow, githubRow, linkedinRow);

        dialog.add(dialogContent);

        // Pulsante di chiusura del dialogo
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), e -> dialog.close());
        closeButton.addThemeVariants(); // Stili di base
        dialog.getHeader().add(closeButton); // Aggiunge il pulsante all'header del dialogo

        dialog.open(); // Apre il dialogo
    }
}