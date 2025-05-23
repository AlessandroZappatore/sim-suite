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
 * Vista di esecuzione dell'applicazione SIM SUITE.
 * <p>
 * Fornisce la struttura della pagina di esecuzione, con header, pulsante di ritorno e messaggio centrale.
 * Attualmente la funzionalità non è implementata e viene mostrato un messaggio informativo.
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
     * Inizializza la pagina con header, pulsante di ritorno e messaggio centrale.
     * </p>
     *
     * @param fileStorageService servizio per la gestione dei file, utilizzato dall'header
     */
    public ExecutionView(FileStorageService fileStorageService) {
        // Layout principale della pagina
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // Header dell'applicazione
        AppHeader header = new AppHeader(fileStorageService);

        // Pulsante per tornare alla home
        Button backButton = StyleApp.getBackButton();
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));

        // Header personalizzato con pulsante e header
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // Layout del contenuto centrale
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Sezione con titolo, sottotitolo e icona
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "SIM ECECUTION",
                "Funzionalità non implementata",
                VaadinIcon.BUILDING.create(),
                "var(--lumo-primary-color)"
        );

        contentLayout.add(headerSection);

        // Footer della pagina
        HorizontalLayout footerSection = StyleApp.getFooterLayout(null);

        // Composizione finale del layout
        mainLayout.add(customHeader, contentLayout, footerSection);
    }
}

/*
 * Note per l'implementazione futura:
 *
 * 1. Per visualizzare parametri e informazioni temporali:
 *    - Utilizzare l'implementazione esistente in detailView (vedere @MonitorSupport in ui.helper)
 *
 * 2. Per l'editor di note:
 *    - Utilizzare il componente text editor già disponibile (@TinyEditor in views.utils)
 *
 * 3. Per il timer dei vari tempi:
 *    - Si può utilizzare un addon di Vaadin (https://vaadin.com/directory/component/simple-timer-addon)
 */