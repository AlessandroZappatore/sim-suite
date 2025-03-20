package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.views.home.AppHeader;
package it.uniupo.simnova.views.home.AppHeader;
@PageTitle("Creation")
@Route("creation")
public class CreationView extends Composite<VerticalLayout> {

    public CreationView() {
        AppHeader header = new AppHeader();
        TextArea textArea = new TextArea();

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        textArea.setLabel("Text area");
        textArea.setWidth("100%");

        getContent().add(header, textArea);
    }
}
