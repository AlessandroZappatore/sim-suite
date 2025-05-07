package it.uniupo.simnova.views.home;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
     * @param iconColor Colore dell'icona (formato hex, es. "#4285F4")
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
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "8px")
                .set("margin-top", "1rem")
                .set("margin-bottom", "1rem")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0,0,0,0.1)");

        // Crea un layout dedicato per icona e titolo
        HorizontalLayout titleWithIconLayout = new HorizontalLayout();
        titleWithIconLayout.setSpacing(true);
        titleWithIconLayout.setPadding(false);
        titleWithIconLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleWithIconLayout.getStyle().set("margin-bottom", "0.5rem");

        // Stile dell'icona migliorato
        Icon iconComponent = new Icon(icon);
        iconComponent.setSize("2em");
        iconComponent.getStyle()
                .set("margin-right", "0.75em")
                .set("color", iconColor)
                .set("background", iconColor + "1A") // 10% opacità
                .set("padding", "10px")
                .set("border-radius", "50%");

        // Titolo con colore personalizzato
        headerTitle.getStyle()
                .set("color", iconColor)
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px")
                .set("text-align", "center");

        // Aggiungi titolo e icona al layout dedicato
        titleWithIconLayout.add(iconComponent, headerTitle);

        // Layout principale con centratura
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        headerLayout.add(titleWithIconLayout);

        // Contenitore per il sottotitolo per garantire il centramento
        HorizontalLayout subtitleContainer = new HorizontalLayout();
        subtitleContainer.setWidthFull();
        subtitleContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        subtitleContainer.setPadding(false);
        subtitleContainer.setSpacing(false);

        // Sottotitolo
        Paragraph subtitleParagraph = new Paragraph(subtitle);
        subtitleParagraph.addClassName(LumoUtility.Margin.Top.XSMALL);
        subtitleParagraph.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        subtitleParagraph.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("max-width", "750px")
                .set("text-align", "center")
                .set("font-weight", "400")
                .set("line-height", "1.6");

        // Aggiungi il sottotitolo al suo contenitore
        subtitleContainer.add(subtitleParagraph);

        // Aggiunta componenti nell'ordine corretto
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

    public static HorizontalLayout getFooterLayout(Button nextButton){
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle()
                .set("border-color", "var(--lumo-contrast-10pct)")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "0 -2px 10px rgba(0, 0, 0, 0.03)");

        // Aggiungere effetto hover tramite classe CSS globale
        UI.getCurrent().getPage().executeJs(
                "document.head.innerHTML += '<style>" +
                        ".hover-effect:hover { transform: translateY(-2px); }" +
                        "button:active { transform: scale(0.98); }" +
                        "</style>';"
        );

        nextButton.addClassName("hover-effect");

        CreditsComponent creditsLayout = new CreditsComponent();

        // Aggiunta dei crediti e del bottone al layout del footer
        footerLayout.add(creditsLayout, nextButton);

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

    public static VerticalLayout getMainLayout(VerticalLayout content){
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle().set("min-height", "100vh");
        content.getStyle().set("background-color", "var(--lumo-base-color)");

        return content;
    }

    public static VerticalLayout getContentLayout(){
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("850px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        return contentLayout;
    }
}