package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.views.home.AppHeader;

@PageTitle("Creation")
@Route("creation")
public class CreationView extends VerticalLayout {

    public CreationView() {
        AppHeader header = new AppHeader();
        TextArea textArea = new TextArea();

        setWidth("100%");
        getStyle().set("flex-grow", "1");

        textArea.setLabel("Text area");
        textArea.setWidth("100%");

        add(header, textArea);
    }
}
