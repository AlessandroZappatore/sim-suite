package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import it.uniupo.simnova.views.home.AppHeader;

public class MainLayout extends VerticalLayout implements RouterLayout {

    public MainLayout() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Header comune
        AppHeader header = new AppHeader();
        add(header);
    }
}