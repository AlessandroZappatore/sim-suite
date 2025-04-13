package it.uniupo.simnova.views.home;

        import com.vaadin.flow.component.UI;
        import com.vaadin.flow.component.button.Button;
        import com.vaadin.flow.component.button.ButtonVariant;
        import com.vaadin.flow.component.html.Div;
        import com.vaadin.flow.component.html.Image;
        import com.vaadin.flow.component.icon.Icon;
        import com.vaadin.flow.component.icon.VaadinIcon;
        import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
        import com.vaadin.flow.component.shared.Tooltip;
        import com.vaadin.flow.theme.lumo.LumoUtility;

        /**
         * Componente per l'header dell'applicazione.
         * <p>
         * Contiene il logo, il titolo dell'applicazione e un pulsante per cambiare il tema (light/dark mode).
         * Implementato come HorizontalLayout per una disposizione orizzontale degli elementi.
         * </p>
         *
         * @author Alessandro Zappatore
         * @version 1.0
         */
        public class AppHeader extends HorizontalLayout {

            /**
             * Pulsante per il cambio tema.
             * <p>
             * Utilizza le icone di Vaadin per rappresentare il tema corrente.
             * </p>
             */
            private final Button toggleThemeButton;
            /**
             * Variabile per tenere traccia dello stato del tema (dark/light).
             * <p>
             * Inizialmente impostato su false (light mode).
             * </p>
             */
            private boolean isDarkMode = false;

            /**
             * Costruttore che inizializza l'header con:
             * - Logo dell'applicazione
             * - Titolo dell'applicazione
             * - Pulsante per il cambio tema
             */
            public AppHeader() {
                // Configurazione layout
                addClassName(LumoUtility.Padding.SMALL);
                setWidthFull();
                setAlignItems(Alignment.CENTER);
                setJustifyContentMode(JustifyContentMode.BETWEEN);
                getStyle().set("background", "var(--lumo-primary-color-10pct)");

                // Logo dell'applicazione
                Image logo = new Image("icons/LogoSimsuiteNoSlogan.png", "SIM SUITE Logo");
                logo.setHeight("40px");
                logo.getStyle().set("cursor", "pointer");
                logo.addClickListener(e -> UI.getCurrent().navigate(""));

                // Aggiungi tooltip personalizzato
                Tooltip.forComponent(logo)
                        .withText("Torna alla Home")
                        .withPosition(Tooltip.TooltipPosition.BOTTOM);
                // Titolo applicazione
                HorizontalLayout logoAndTitle = getHorizontalLayout(logo);

                // Pulsante toggle tema
                toggleThemeButton = new Button();
                toggleThemeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                toggleThemeButton.setAriaLabel("Toggle dark mode");

                // Verifica tema iniziale
                checkInitialTheme();

                toggleThemeButton.addClickListener(e -> toggleTheme());

                add(logoAndTitle, toggleThemeButton);
            }

            /**
             * Crea un layout orizzontale per il logo e il titolo dell'applicazione.
             * <p>
             * Utilizza un Div per il titolo e lo stile LumoUtility per la formattazione.
             * </p>
             *
             * @param logo Il logo dell'applicazione
             * @return Un layout orizzontale contenente il logo e il titolo
             */
            private static HorizontalLayout getHorizontalLayout(Image logo) {
                Div appTitle = new Div();
                appTitle.setText("SIM SUITE");
                appTitle.addClassNames(
                        LumoUtility.FontSize.XLARGE,
                        LumoUtility.FontWeight.BOLD,
                        LumoUtility.TextColor.PRIMARY
                );

                // Layout per logo e titolo
                HorizontalLayout logoAndTitle = new HorizontalLayout(logo, appTitle);
                logoAndTitle.setSpacing(true);
                logoAndTitle.setAlignItems(Alignment.CENTER);
                return logoAndTitle;
            }

            /**
             * Verifica il tema iniziale dell'applicazione.
             * <p>
             * Esegue uno script JS per determinare se il tema iniziale è dark o light
             * e aggiorna di conseguenza lo stato e l'icona del pulsante.
             * </p>
             */
            private void checkInitialTheme() {
                UI.getCurrent().getPage().executeJs(
                        "return document.documentElement.getAttribute('theme') || 'light';"
                ).then(result -> {
                    String theme = result.asString();
                    isDarkMode = "dark".equals(theme);
                    updateThemeIcon();
                });
            }

            /**
             * Cambia il tema tra light e dark mode.
             * <p>
             * Modifica l'attributo 'theme' del documento HTML e aggiorna lo stato interno.
             * </p>
             */
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

            /**
             * Aggiorna l'icona del pulsante in base al tema corrente.
             * <p>
             * Mostra l'icona del sole per il dark mode e l'icona della luna per il light mode.
             * </p>
             */
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