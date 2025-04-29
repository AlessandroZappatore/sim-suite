package it.uniupo.simnova.views.execution;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.views.home.AppHeader;

/**
 * Classe che rappresenta la vista di esecuzione dell'applicazione.
 * <p>
 * Questa classe estende Composite e rappresenta una vista di esecuzione
 * con un'intestazione e un messaggio centrale.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Execution")
@Route("execution")
public class ExecutionView extends Composite<VerticalLayout> {

    /**
     * Costruttore della vista di esecuzione.
     * <p>
     * Inizializza l'intestazione e aggiunge un messaggio centrale alla vista.
     * </p>
     */
    public ExecutionView(FileStorageService fileStorageService) {
        AppHeader header = new AppHeader(fileStorageService);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        // Aggiungi la scritta grande al centro
        H1 message = new H1("Funzionalità ancora da implementare");
        message.getStyle().set("text-align", "center");

        getContent().add(header, message);
    }

}