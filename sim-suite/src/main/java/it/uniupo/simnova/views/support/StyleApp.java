package it.uniupo.simnova.views.support;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class StyleApp extends HorizontalLayout {
    public StyleApp() {
    }

    /**
     * Crea e restituisce un pulsante "Indietro" stilizzato.
     *
     * @return Un'istanza di Button configurata con stile "indietro"
     */
    public static Button getBackButton() {
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle()
                .set("margin-right", "auto")
                .set("transition", "all 0.2s ease")
                .set("font-weight", "500");
        backButton.addClassName("hover-effect");

        return backButton;
    }

    /**
     * Crea un header completo con pulsante indietro, titolo, sottotitolo e icona.
     *
     * @param title     Titolo da mostrare nell'header
     * @param subtitle  Sottotitolo da mostrare nell'header
     * @param icon      Icona Vaadin da utilizzare
     * @param iconColor Colore dell'icona (formato hex, es. "#4285F4", o CSS variable es. "var(--lumo-primary-color)")
     * @return Layout completo dell'header
     */
    public static VerticalLayout getTitleSubtitle(String title, String subtitle, VaadinIcon icon, String iconColor) {
        H2 headerTitle = new H2(title.toUpperCase());
        headerTitle.addClassName(LumoUtility.Margin.Bottom.NONE);
        headerTitle.addClassName(LumoUtility.Margin.Top.NONE);

        VerticalLayout headerSection = new VerticalLayout();
        headerSection.setPadding(true);
        headerSection.setSpacing(false);
        headerSection.setWidthFull();
        headerSection.setAlignItems(FlexComponent.Alignment.CENTER); // Allineamento centrale per tutti gli elementi
        headerSection.getStyle()
                .set("background", "var(--lumo-base-color)") // Already good: uses Lumo variable
                .set("border-radius", "8px")
                .set("margin-top", "1rem")
                .set("margin-bottom", "1rem")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0,0,0,0.1)");


        HorizontalLayout titleWithIconLayout = new HorizontalLayout();
        titleWithIconLayout.setSpacing(true);
        titleWithIconLayout.setPadding(false);
        titleWithIconLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleWithIconLayout.getStyle().set("margin-bottom", "0.5rem");

        Icon iconComponent = new Icon(icon);
        iconComponent.setSize("3em");
        iconComponent.getStyle()
                .set("margin-right", "0.25em")
                .set("color", iconColor)
                .set("background", iconColor + "1A")
                .set("padding", "10px")
                .set("border-radius", "50%");

        headerTitle.getStyle()
                .set("color", iconColor) // Depends on iconColor parameter
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px")
                .set("text-align", "center");

        titleWithIconLayout.add(iconComponent, headerTitle);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        headerLayout.add(titleWithIconLayout);

        HorizontalLayout subtitleContainer = new HorizontalLayout();
        subtitleContainer.setWidthFull();
        subtitleContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        subtitleContainer.setPadding(false);
        subtitleContainer.setSpacing(false);

        Paragraph subtitleParagraph = new Paragraph(subtitle);
        subtitleParagraph.addClassName(LumoUtility.Margin.Top.XSMALL);
        subtitleParagraph.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        subtitleParagraph.getStyle()
                .set("color", "var(--lumo-secondary-text-color)") // Good: uses Lumo variable
                .set("max-width", "750px")
                .set("text-align", "center")
                .set("font-weight", "400")
                .set("line-height", "1.6");

        subtitleContainer.add(subtitleParagraph);
        headerSection.add(headerLayout, subtitleContainer);
        return headerSection;
    }

    public static HorizontalLayout getCustomHeader(Button backButton, AppHeader header) {
        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);
        customHeader.getStyle().set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)");
        return customHeader;
    }

    public static HorizontalLayout getFooterLayout(Button nextButton) {
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle()
                .set("border-color", "var(--lumo-contrast-10pct)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("box-shadow", "0 -2px 10px rgba(0, 0, 0, 0.03)");

        UI.getCurrent().getPage().executeJs(
                "if (!document.getElementById('custom-hover-active-styles')) {" +
                        "  const styleElement = document.createElement('style');" +
                        "  styleElement.id = 'custom-hover-active-styles';" +
                        "  styleElement.innerHTML = '" +
                        ".hover-effect:hover { transform: translateY(-2px); }" +
                        "button:active { transform: scale(0.98); }" +
                        "  ';" +
                        "  document.head.appendChild(styleElement);" +
                        "}"
        );

        CreditsComponent creditsLayout = new CreditsComponent();

        if (nextButton != null) {
            nextButton.addClassName("hover-effect");
            footerLayout.add(creditsLayout, nextButton);
        } else {
            footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            footerLayout.add(creditsLayout);
        }

        return footerLayout;
    }

    public static Button getNextButton() {
        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px");
        nextButton.getStyle()
                .set("border-radius", "30px")
                .set("font-weight", "600")
                .set("transition", "transform 0.2s ease");
        return nextButton;
    }

    public static Button getButton(String label, VaadinIcon icon, ButtonVariant variant, String iconColor) {
        Button editButton = new Button(label, new Icon(icon));
        editButton.addThemeVariants(variant);
        editButton.setMinWidth("150px");
        editButton.setMaxWidth("250px");
        editButton.getStyle()
                .set("border-radius", "30px")
                .set("font-weight", "600")
                .set("transition", "all 0.2s ease")
                .set("background-color", "var(" + iconColor + "-10pct)")
                .set("color", "var(" + iconColor + ")")
                .set("border", "1px solid var(" + iconColor + "-50pct)")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.05)");

        // Aggiungo effetto hover utilizzando il colore passato come parametro
        editButton.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.backgroundColor = 'var(" + iconColor + "-20pct)'; " +
                        "  this.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.1)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.backgroundColor = 'var(" + iconColor + "-10pct)'; " +
                        "  this.style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.05)'; " +
                        "});"
        );

        editButton.addClassName("hover-effect");
        return editButton;
    }

    public static VerticalLayout getMainLayout(VerticalLayout content) {
        content.setSizeUndefined();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("min-height", "100vh")
                .set("background", "var(--lumo-contrast-5pct)");
        return content;
    }

    public static VerticalLayout getContentLayout() {
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");
        return contentLayout;
    }

    public static Button getSaveEditButton() {
        Button saveButton = new Button("Salva Modifiche", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidth("200px");  // Larghezza adeguata al testo più lungo
        saveButton.getStyle()
                .set("border-radius", "30px")
                .set("font-weight", "600")
                .set("transition", "transform 0.2s ease")
                .set("background-color", "var(--lumo-success-color)")
                .set("color", "var(--lumo-base-color)")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.05)")
        ;

        // Aggiungo effetto hover
        saveButton.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.backgroundColor = 'var(--lumo-success-text-color)'; " +
                        "  this.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.1)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.backgroundColor = 'var(--lumo-success-color)'; " +
                        "  this.style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.05)'; " +
                        "});"
        );

        saveButton.addClassName("hover-effect");
        return saveButton;
    }

    public static Button getScrollButton() {
        Button scrollToTopButton = new Button(VaadinIcon.ARROW_UP.create());
        scrollToTopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
        scrollToTopButton.setAriaLabel("Torna all'inizio della pagina");
        scrollToTopButton.setTooltipText("Torna all'inizio della pagina");
        scrollToTopButton.getStyle()
                .set("position", "fixed")
                .set("bottom", "20px")
                .set("right", "20px")
                .set("z-index", "1000");
        scrollToTopButton.addClickListener(e ->
                UI.getCurrent().getPage().executeJs("window.scrollTo(0, 0);")
        );

        return scrollToTopButton;
    }

    public static void styleDetailsSummary(Details details) {
        if (details != null && details.getSummary() != null) {
            details.getSummary().getStyle()
                    .set("font-size", "var(--lumo-font-size-xl)")
                    .set("font-weight", "600")
                    .set("padding-top", "var(--lumo-space-s)")
                    .set("padding-bottom", "var(--lumo-space-s)");
        }
    }
}