package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Creation")
@Route("creation")
public class CreationView extends Composite<VerticalLayout> {

    public CreationView() {
        Button toggleThemeButton = new Button();
        Button backButton = new Button("Back to Home", VaadinIcon.ARROW_LEFT.create());

        TextArea textArea = new TextArea();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        textArea.setLabel("Text area");
        textArea.setWidth("100%");
        getContent().add(textArea, backButton);

        setThemeIcon(toggleThemeButton);

        // Posiziona il pulsante di toggle in alto a destra
        toggleThemeButton.getStyle().set("position", "fixed");
        toggleThemeButton.getStyle().set("top", "10px");
        toggleThemeButton.getStyle().set("right", "10px");
        toggleThemeButton.getStyle().set("z-index", "1000");
        getContent().add(toggleThemeButton);

        // Aggiunta del listener per il cambio di tema
        toggleThemeButton.addClickListener(e -> toggleTheme(toggleThemeButton));

        // Aggiunta del listener per tornare alla home page
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("")));
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
