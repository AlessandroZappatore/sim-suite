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

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "SIM ECECUTION",
                "Funzionalità non implementata",
                VaadinIcon.BUILDING,
                "var(--lumo-primary-color)"
        );

        HorizontalLayout footerSection = StyleApp.getFooterLayout(null);

        mainLayout.add(headerSection, customHeader, contentLayout, footerSection);
    }

}