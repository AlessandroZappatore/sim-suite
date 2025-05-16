package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;

public class TabsSupport {

    /**
     * Crea un tab con un'icona e un testo centrato.
     *
     * @param text     Il testo da mostrare nel tab
     * @param iconType L'icona da usare
     * @return Tab configurato
     */
    public static Tab createTabWithIcon(String text, VaadinIcon iconType) {
        Span tabText = new Span(text);
        tabText.getStyle().set("margin-left", "var(--lumo-space-s)");

        HorizontalLayout tabContent = new HorizontalLayout();
        tabContent.setSizeFull();
        tabContent.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Centra orizzontalmente
        tabContent.setAlignItems(FlexComponent.Alignment.CENTER); // Centra verticalmente
        tabContent.setSpacing(false);
        tabContent.add(new com.vaadin.flow.component.icon.Icon(iconType), tabText);
        tabContent.getStyle()
                .set("padding", "var(--lumo-space-s)")
                .set("text-align", "center"); // Assicura che il testo sia centrato

        Tab tab = new Tab(tabContent);
        tab.getStyle().set("flex-grow", "1"); // Ogni tab occuperà spazio equamente

        return tab;
    }
}
