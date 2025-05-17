package it.uniupo.simnova.views.execution;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;

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
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "SIM ECECUTION",
                "Funzionalità non implementata",
                VaadinIcon.BUILDING.create(),
                "var(--lumo-primary-color)"
        );

        contentLayout.add(headerSection);

        HorizontalLayout footerSection = StyleApp.getFooterLayout(null);

        mainLayout.add(customHeader, contentLayout, footerSection);
    }
}

/*
  Note per l'implementazione futura:

  1. Per visualizzare parametri e informazioni temporali:
     - Utilizzare l'implementazione esistente in detailView (vedere @MonitorSupport in ui.helper)

  2. Per l'editor di note:
     - Utilizzare il componente text editor già disponibile (@TinyEditor in views.utils)

  3. Per il timer dei vari tempi:
     - Si può utilizzare un addon di Vaadin (https://vaadin.com/directory/component/simple-timer-addon)
 */