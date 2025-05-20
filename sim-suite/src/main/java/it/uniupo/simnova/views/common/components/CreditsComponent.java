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

import static it.uniupo.simnova.views.constant.CreditConst.*;

/**
 * Componente riutilizzabile per visualizzare i crediti dell'applicazione.
 * Include informazioni sull'ideatore, l'università e contatti dello sviluppatore
 * visualizzati in un popup.
 *
 * @author Alessandro Zappatore
 * @version 1.1
 */
public class CreditsComponent extends VerticalLayout {

    /**
     * Crea una nuova istanza del componente crediti.
     * Mostra informazioni su ideatore, sviluppatore, università e versione.
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

        // Riga ideatore con nome e link (uses existing helper)
        HorizontalLayout ideatorRow = getRow(VaadinIcon.LIGHTBULB.create());

        // Riga sviluppatore con nome e azione per aprire il popup
        HorizontalLayout developerRow = createDeveloperRow();

        // Riga università con icona (uses existing helper pattern)
        HorizontalLayout universityRow = createUniversityRow();

        // Riga versione e data (uses existing helper pattern)
        HorizontalLayout versionRow = createVersionRow();


        // Aggiunta dei componenti al layout principale
        this.add(creditsTitle, ideatorRow, developerRow, universityRow, versionRow);
    }

    /**
     * Crea una riga standard con icona e Anchor collegato.
     *
     * @param icon icona da mostrare nella riga
     * @return layout orizzontale della riga
     */
    private static HorizontalLayout getRow(Icon icon) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(Alignment.CENTER);

        Anchor linkAnchor = new Anchor(it.uniupo.simnova.views.constant.CreditConst.IDEATORLINK, "Ideatore: Antonio Scalogna");
        linkAnchor.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        linkAnchor.getElement().setAttribute("target", "_blank");
        linkAnchor.getElement().setAttribute("rel", "noopener noreferrer");

        icon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        row.add(icon, linkAnchor);
        return row;
    }

    /**
     * Crea la riga per lo sviluppatore, con nome cliccabile che apre un dialog.
     * @return layout orizzontale per lo sviluppatore
     */
    private HorizontalLayout createDeveloperRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(Alignment.CENTER);

        Icon developerIcon = VaadinIcon.USER.create();
        developerIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        // Use a Button styled as text/link to be clickable
        Button developerNameButton = new Button("Sviluppatore: Alessandro Zappatore");
        developerNameButton.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.XSMALL,
                LumoUtility.Padding.Right.NONE, // Remove default button padding
                LumoUtility.Padding.Left.NONE,
                LumoUtility.Background.TRANSPARENT
        );

        developerNameButton.getStyle().set("cursor", "pointer"); // Indicate it's clickable

        // Add click listener to open the dialog
        developerNameButton.addClickListener(e -> openDeveloperInfoDialog());

        row.add(developerIcon, developerNameButton);
        return row;
    }

    /**
     * Crea la riga per l'università con link.
     * @return layout orizzontale per l'università
     */
    private HorizontalLayout createUniversityRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon universityIcon = new Icon(VaadinIcon.ACADEMY_CAP);
        universityIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Anchor universityLink = new Anchor("https://www.uniupo.it", "Università del Piemonte Orientale");
        universityLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        universityLink.getElement().setAttribute("target", "_blank");
        universityLink.getElement().setAttribute("rel", "noopener noreferrer");

        row.add(universityIcon, universityLink);
        return row;
    }

    /**
     * Crea la riga per versione e data.
     * @return layout orizzontale per versione e data
     */
    private HorizontalLayout createVersionRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon versionIcon = new Icon(VaadinIcon.INFO_CIRCLE);
        versionIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Span versionText = new Span("Versione: " + VERSION);
        versionText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Icon dateIcon = new Icon(VaadinIcon.CALENDAR);
        dateIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        dateIcon.getStyle().set("margin-left", "8px"); // Add some space between version and date

        Span dateText = new Span("Data: " + DATE);
        dateText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        row.add(versionIcon, versionText, dateIcon, dateText);
        return row;
    }

    /**
     * Crea e apre il dialog con i contatti dello sviluppatore.
     */
    private void openDeveloperInfoDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Contatti Sviluppatore");

        // Create the content for the dialog
        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true); // Space between contact lines
        dialogContent.setAlignItems(Alignment.START);
        dialogContent.setWidthFull(); // Ensure content uses dialog width

        // Email Row
        HorizontalLayout emailRow = new HorizontalLayout();
        emailRow.setSpacing(true);
        emailRow.setAlignItems(Alignment.CENTER);
        Icon emailIcon = new Icon(VaadinIcon.ENVELOPE);
        emailIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        Anchor emailLink = new Anchor("mailto:alessandrozappatore03@gmail.com", "alessandrozappatore03@gmail.com");
        emailLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        emailRow.add(emailIcon, emailLink);

        // GitHub Row
        HorizontalLayout githubRow = new HorizontalLayout();
        githubRow.setSpacing(true);
        githubRow.setAlignItems(Alignment.CENTER);
        Icon githubIcon = FontAwesome.Brands.GITHUB.create();
        githubIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        Anchor githubLink = new Anchor("https://github.com/AlessandroZappatore", "Github: AlessandroZappatore");
        githubLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        githubLink.getElement().setAttribute("target", "_blank");
        githubLink.getElement().setAttribute("rel", "noopener noreferrer");
        githubRow.add(githubIcon, githubLink);

        // Linkedin Row
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

        // Add a close button to the dialog header
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), e -> dialog.close());
        closeButton.addThemeVariants(); // Add default button theme (optional, but common for close)
        dialog.getHeader().add(closeButton);

        dialog.open();
    }
}