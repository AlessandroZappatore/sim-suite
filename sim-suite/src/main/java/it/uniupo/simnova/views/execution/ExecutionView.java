package it.uniupo.simnova.views.execution;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.views.home.AppHeader;

@PageTitle("Execution")
@Route("execution")
public class ExecutionView extends Composite<VerticalLayout> {

    public ExecutionView() {
        AppHeader header = new AppHeader();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        // Aggiungi la scritta grande al centro
        H1 message = new H1("Funzionalità ancora da implementare");
        message.getStyle().set("text-align", "center");

        getContent().add(header, message);
    }

}