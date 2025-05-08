package it.uniupo.simnova.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.views.support.AppHeader;
import jakarta.servlet.http.HttpServletResponse;

/**
 * La classe ErrorView rappresenta una vista personalizzata per gestire gli errori di pagina non trovata (404).
 * Implementa l'interfaccia HasErrorParameter per gestire gli errori di tipo NotFoundException.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Pagina non trovata")
public class ErrorView extends Composite<VerticalLayout>
        implements HasErrorParameter<NotFoundException> {

    /**
     * Messaggio di errore visualizzato nella pagina.
     */
    private final Paragraph message;

    /**
     * Costruttore della classe ErrorView.
     * Configura il layout della pagina di errore con un header, un logo, un titolo, un messaggio e un pulsante per tornare alla home.
     */
    public ErrorView(FileStorageService fileStorageService) {
        // Header dell'applicazione
        AppHeader header = new AppHeader(fileStorageService);
        header.getElement().getStyle()
                .set("position", "fixed")
                .set("top", "0")
                .set("width", "100%")
                .set("z-index", "1000");

        // Contenitore principale
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.getStyle()
                .set("min-height", "100vh")
                .set("padding-top", "80px")
                .set("box-sizing", "border-box")
                .set("margin", "0")
                .set("padding", "0");

        // Logo
        Image logo = new Image("icons/404logo.png", "404 logo");
        logo.setWidth("280px");
        logo.setHeight("280px");
        logo.getStyle()
                .set("margin-bottom", "10px")
                .set("filter", "drop-shadow(0 4px 8px rgba(0,0,0,0.1))")
                .set("animation", "float 3s ease-in-out infinite");

        // CSS per l'animazione
        logo.getElement().executeJs("document.head.insertAdjacentHTML('beforeend', " +
                "'<style>@keyframes float {0%, 100% {transform: translateY(0);} 50% {transform: translateY(-10px);}}</style>')");

        // Titolo
        H1 title = new H1("Ops! Pagina non trovata");
        title.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.FontSize.XXXLARGE,
                LumoUtility.FontWeight.BOLD
        );
        title.getStyle()
                .set("color", "#2c3e50")
                .set("text-shadow", "1px 1px 2px rgba(0,0,0,0.1)")
                .set("margin", "5px 0 10px 0");

        // Messaggio di errore
        message = new Paragraph("Sembra che la pagina che stai cercando non esista o sia stata spostata.");
        message.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.FontSize.LARGE
        );
        message.getStyle()
                .set("color", "#7f8c8d")
                .set("max-width", "500px")
                .set("margin", "5px auto 20px");

        // Bottone
        Button homeButton = new Button("Torna alla Home");
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.getStyle()
                .set("padding", "10px 20px")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("transition", "all 0.3s ease")
                .set("margin-top", "10px");

        homeButton.addClickListener(e -> homeButton.getUI().ifPresent(ui -> ui.navigate("")));

        // Layout del contenuto
        contentLayout.setSizeFull();
        contentLayout.setPadding(false);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        contentLayout.add(logo, title, message, homeButton);

        // Layout principale
        getContent().removeAll();
        getContent().setSizeFull();
        getContent().setPadding(false);
        getContent().setSpacing(false);
        getContent().add(header, contentLayout);
    }

    /**
     * Gestisce il parametro di errore per la rotta non trovata.
     *
     * @param event     l'evento di navigazione
     * @param parameter il parametro di errore
     * @return il codice di stato HTTP 404 (Pagina non trovata)
     */
    @Override
    public int setErrorParameter(BeforeEnterEvent event,
                                 ErrorParameter<NotFoundException> parameter) {
        String customMessage = parameter.getCustomMessage();
        if (customMessage != null && !customMessage.isEmpty()) {
            message.setText(customMessage);
        }
        return HttpServletResponse.SC_NOT_FOUND;
    }
}