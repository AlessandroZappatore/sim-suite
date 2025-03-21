package it.uniupo.simnova.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Home")
@Route("")
public class HomeView extends Composite<VerticalLayout> {

    public HomeView() {
        AppHeader header = new AppHeader();
        H1 title = new H1("SIM SUITE");
        H3 subtitle = new H3("Benvenuto in SIM SUITE, il software per la creazione ed esecuzione di scenari di simulazione.");

        // Creazione dei bottoni
        Button buttonPrimary = new Button("SIM CREATION");
        Button buttonPrimary2 = new Button("SIM EXECUTION");

        // Impostazione delle dimensioni e degli stili
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonPrimary2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Aggiunta degli stili per adattare i bottoni alla dimensione dello schermo
        buttonPrimary.getStyle().set("width", "80%");
        buttonPrimary.getStyle().set("height", "200px");
        buttonPrimary.getStyle().set("font-size", "32px");

        buttonPrimary2.getStyle().set("width", "80%");
        buttonPrimary2.getStyle().set("height", "200px");
        buttonPrimary2.getStyle().set("font-size", "32px");

        // Configurazione del layout
        getContent().setWidthFull();
        getContent().setHeightFull();
        getContent().getStyle().set("display", "flex");
        getContent().getStyle().set("flex-direction", "column");
        getContent().getStyle().set("justify-content", "center");
        getContent().getStyle().set("align-items", "center");

        // Aggiunta del titolo, sottotitolo, bottoni e header al layout
        getContent().add(header, title, subtitle, buttonPrimary, buttonPrimary2);

        // Aggiunta dei listener per la navigazione
        buttonPrimary.addClickListener(e -> buttonPrimary.getUI().ifPresent(ui -> ui.navigate("creation")));
        buttonPrimary2.addClickListener(e -> buttonPrimary2.getUI().ifPresent(ui -> ui.navigate("execution")));
    }
}
