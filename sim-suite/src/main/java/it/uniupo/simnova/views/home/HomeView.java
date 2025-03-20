package it.uniupo.simnova.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.icon.VaadinIcon;

@PageTitle("Home")
@Route("")
public class HomeView extends Composite<VerticalLayout> {

    public HomeView() {
        H1 title = new H1("SIM SUITE");
        H3 subtitle = new H3("Benvenuto in SIM SUITE, il software per la creazione ed esecuzione di scenari di simulazione.");

        // Creazione dei bottoni
        Button buttonPrimary = new Button("SIM CREATION");
        Button buttonPrimary2 = new Button("SIM EXECUTION");
        Button toggleThemeButton = new Button();

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

        // Imposta l'icona iniziale del pulsante di toggle
        setThemeIcon(toggleThemeButton);

        // Aggiunta del titolo, sottotitolo, bottoni e pulsante di toggle al layout
        getContent().add(title, subtitle, buttonPrimary, buttonPrimary2);

        // Posiziona il pulsante di toggle in alto a destra
        toggleThemeButton.getStyle().set("position", "fixed");
        toggleThemeButton.getStyle().set("top", "10px");
        toggleThemeButton.getStyle().set("right", "10px");
        toggleThemeButton.getStyle().set("z-index", "1000");
        getContent().add(toggleThemeButton);

        // Aggiunta dei listener per la navigazione
        buttonPrimary.addClickListener(e -> buttonPrimary.getUI().ifPresent(ui -> ui.navigate("creation")));
        buttonPrimary2.addClickListener(e -> buttonPrimary2.getUI().ifPresent(ui -> ui.navigate("execution")));

        // Aggiunta del listener per il cambio di tema
        toggleThemeButton.addClickListener(e -> toggleTheme(toggleThemeButton));
    }

    private void toggleTheme(Button toggleThemeButton) {
        // Esegui JavaScript per cambiare il tema
        UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', document.documentElement.getAttribute('theme') === 'dark' ? 'light' : 'dark');");

        // Cambia l'icona del pulsante in base al tema
        setThemeIcon(toggleThemeButton);
    }

    private void setThemeIcon(Button button) {
        UI.getCurrent().getPage().executeJs("return document.documentElement.getAttribute('theme') || 'light';")
                .then(result -> {
                    String theme = result.asString();
                    if ("dark".equals(theme)) {
                        button.setIcon(VaadinIcon.SUN_O.create());
                    } else {
                        button.setIcon(VaadinIcon.MOON.create());
                    }
                });
    }

}
