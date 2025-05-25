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
        tabContent.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        tabContent.setAlignItems(FlexComponent.Alignment.CENTER);
        tabContent.setSpacing(false);
        tabContent.add(new com.vaadin.flow.component.icon.Icon(iconType), tabText);
        tabContent.getStyle()
                .set("padding", "var(--lumo-space-s)")
                .set("text-align", "center");

        Tab tab = new Tab(tabContent);
        tab.getStyle().set("flex-grow", "1");

        return tab;
    }
}
