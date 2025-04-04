package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import it.uniupo.simnova.views.home.AppHeader;

/**
 * Layout principale per le viste di creazione degli scenari.
 * <p>
 * Fornisce una struttura comune a tutte le viste del flusso di creazione,
 * includendo l'header dell'applicazione. Implementa {@link RouterLayout} per
 * fungere da layout radice per le viste secondarie.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class MainLayout extends VerticalLayout implements RouterLayout {

    /**
     * Costruttore che configura il layout principale.
     * <p>
     * Inizializza la struttura base con:
     * - Dimensioni a schermo intero
     * - Nessun padding o spaziatura
     * - Header dell'applicazione
     * </p>
     */
    public MainLayout() {
        // Configurazione del layout base
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Aggiunta dell'header comune a tutte le viste
        AppHeader header = new AppHeader();
        add(header);
    }
}