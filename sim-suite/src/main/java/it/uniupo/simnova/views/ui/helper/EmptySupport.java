package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class EmptySupport extends HorizontalLayout {
    public EmptySupport() {
        // Constructor
    }

    public static Div createErrorContent(String errorMessage) {
        Div emptyMessage = new Div();
        emptyMessage.getStyle()
                .set("text-align", "center")
                .set("padding", "var(--lumo-space-l)")
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("font-style", "italic");

        Icon infoIcon = new Icon(VaadinIcon.INFO_CIRCLE);
        infoIcon.getStyle()
                .set("display", "block")
                .set("margin", "0 auto var(--lumo-space-m) auto")
                .set("width", "48px")
                .set("height", "48px")
                .set("color", "var(--lumo-contrast-30pct)");

        Paragraph noContentText = new Paragraph(errorMessage);

        emptyMessage.add(infoIcon, noContentText);

        return emptyMessage;
    }
}
