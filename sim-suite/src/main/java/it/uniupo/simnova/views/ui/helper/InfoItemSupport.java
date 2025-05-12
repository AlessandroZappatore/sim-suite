package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class InfoItemSupport {

    public static VerticalLayout createInfoItem(String title, String content, Icon titleIcon) {
        if (content == null || content.isEmpty()) {
            return new VerticalLayout();
        }

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        // Crea un layout orizzontale per il titolo con l'icona
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setPadding(false);

        // Aggiungi l'icona solo se non è null
        if (titleIcon != null) {
            titleIcon.setSize("1em");
            titleLayout.add(titleIcon);
        }

        H4 itemTitle = new H4(title);
        itemTitle.addClassName(LumoUtility.Margin.Bottom.XSMALL);
        itemTitle.addClassName(LumoUtility.Margin.Top.XSMALL);
        titleLayout.add(itemTitle);

        layout.add(titleLayout);

        // Usando Html invece di Paragraph per contenuto creato con TinyMCE
        if (title.equals("Descrizione")
                || title.equals("Briefing")
                || title.equals("Informazioni dai genitori")
                || title.equals("Patto Aula")
                || title.equals("Obiettivi Didattici")
                || title.equals("Moulage")
                || title.equals("Liquidi e dosi farmaci")
                || title.equals("Generale")
                || title.equals("Pupille")
                || title.equals("Collo")
                || title.equals("Torace")
                || title.equals("Cuore")
                || title.equals("Addome")
                || title.equals("Retto")
                || title.equals("Cute")
                || title.equals("Estremità")
                || title.equals("Neurologico")
                || title.equals("FAST")
                || title.equals("Sceneggiatura")) {
            Html htmlContent = new Html("<div>" + content + "</div>");
            layout.add(htmlContent);
        } else {
            Paragraph itemContent = new Paragraph(content);
            itemContent.getStyle()
                    .set("white-space", "pre-line")
                    .set("margin-top", "0");
            layout.add(itemContent);
        }

        return layout;
    }
}
