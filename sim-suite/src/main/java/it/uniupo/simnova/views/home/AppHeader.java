    package it.uniupo.simnova.views.home;

    import com.vaadin.flow.component.Composite;
    import com.vaadin.flow.component.UI;
    import com.vaadin.flow.component.button.Button;
    import com.vaadin.flow.component.icon.VaadinIcon;
    import com.vaadin.flow.component.orderedlayout.FlexComponent;
    import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
    import com.vaadin.flow.router.RouterLink;

    public class AppHeader extends Composite<HorizontalLayout> {

        public AppHeader() {
            Button toggleThemeButton = new Button();
            Button backButton = new Button("Back to Home", VaadinIcon.ARROW_LEFT.create());

            // Imposta l'icona iniziale del pulsante di toggle
            setThemeIcon(toggleThemeButton);

            // Posiziona il pulsante di toggle in alto a destra
            toggleThemeButton.getStyle().set("position", "fixed");
            toggleThemeButton.getStyle().set("top", "10px");
            toggleThemeButton.getStyle().set("right", "10px");
            toggleThemeButton.getStyle().set("z-index", "1000");

            // Aggiunta del listener per il cambio di tema
            toggleThemeButton.addClickListener(e -> toggleTheme(toggleThemeButton));

            // Aggiungi gli elementi al layout
            HorizontalLayout layout = new HorizontalLayout(backButton, toggleThemeButton);
            layout.setWidthFull();
            layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            layout.getStyle().set("padding", "10px");
            layout.getStyle().set("box-sizing", "border-box");

            setCompositionRoot(layout);
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
