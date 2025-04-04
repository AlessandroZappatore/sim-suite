package it.uniupo.simnova.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.servlet.http.HttpServletResponse;

@PageTitle("Errore")
public class ErrorView extends Composite<VerticalLayout>
        implements HasErrorParameter<NotFoundException> {

    public ErrorView() {
        H1 title = new H1("Pagina non trovata");
        title.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.FontSize.XXXLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.Margin.Bottom.SMALL
        );

        Paragraph message = new Paragraph("La pagina che stai cercando non esiste.");
        message.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.Bottom.XLARGE
        );

        Button homeButton = new Button("Torna alla Home");
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.addClickListener(e -> homeButton.getUI().ifPresent(ui -> ui.navigate("")));

        getContent().setSizeFull();
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
        getContent().setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getContent().add(title, message, homeButton);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event,
                                 ErrorParameter<NotFoundException> parameter) {
        return HttpServletResponse.SC_NOT_FOUND; // 404
    }
}