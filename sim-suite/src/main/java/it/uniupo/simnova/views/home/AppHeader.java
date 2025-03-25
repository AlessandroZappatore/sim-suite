package it.uniupo.simnova.views.home;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class AppHeader extends HorizontalLayout {

    private final Button toggleThemeButton;
    private boolean isDarkMode = false;

    public AppHeader() {
        // Configurazione layout
        addClassName(LumoUtility.Padding.SMALL);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        getStyle().set("background", "var(--lumo-primary-color-10pct)");

        // Logo/Titolo applicazione
        Div appTitle = new Div();
        appTitle.setText("SIM SUITE");
        appTitle.addClassNames(
                LumoUtility.FontSize.XLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.TextColor.PRIMARY
        );

        // Pulsante toggle tema
        toggleThemeButton = new Button();
        toggleThemeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleThemeButton.setAriaLabel("Toggle dark mode");

        // Verifica tema iniziale
        checkInitialTheme();

        toggleThemeButton.addClickListener(e -> toggleTheme());

        add(appTitle, toggleThemeButton);
    }

    private void checkInitialTheme() {
        UI.getCurrent().getPage().executeJs(
                "return document.documentElement.getAttribute('theme') || 'light';"
        ).then(result -> {
            String theme = result.asString();
            isDarkMode = "dark".equals(theme);
            updateThemeIcon();
        });
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        UI.getCurrent().getPage().executeJs(
                "document.documentElement.setAttribute('theme', $0)",
                isDarkMode ? "dark" : "light"
        );
        updateThemeIcon();

        // Forza il refresh dello stile (opzionale)
        UI.getCurrent().getPage().executeJs(
                "document.documentElement.style.setProperty('color-scheme', $0)",
                isDarkMode ? "dark" : "light"
        );
    }

    private void updateThemeIcon() {
        Icon icon = isDarkMode ?
                VaadinIcon.SUN_O.create() :
                VaadinIcon.MOON_O.create();

        icon.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("width", "var(--lumo-icon-size-m)")
                .set("height", "var(--lumo-icon-size-m)");

        toggleThemeButton.setIcon(icon);
    }
}