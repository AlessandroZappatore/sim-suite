package it.uniupo.simnova.views.common.utils;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;

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
     * @param title         Titolo da mostrare nell'header
     * @param subtitle      Sottotitolo da mostrare nell'header
     * @param iconComponent Icona Vaadin da utilizzare
     * @param iconColor     Colore dell'icona (formato hex, es. "#4285F4", o CSS variable es. "var(--lumo-primary-color)")
     * @return Layout completo dell'header
     */
    public static VerticalLayout getTitleSubtitle(String title, String subtitle, Icon iconComponent, String iconColor) {
        H2 headerTitle = new H2(title.toUpperCase());
        headerTitle.addClassName(LumoUtility.Margin.Bottom.NONE);
        headerTitle.addClassName(LumoUtility.Margin.Top.NONE);

        VerticalLayout headerSection = new VerticalLayout();
        headerSection.setPadding(true);
        headerSection.setSpacing(false);
        headerSection.setWidthFull();
        headerSection.setAlignItems(FlexComponent.Alignment.CENTER);
        headerSection.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "8px")
                .set("margin-top", "1rem")
                .set("margin-bottom", "1rem")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0,0,0,0.1)");


        HorizontalLayout titleWithIconLayout = new HorizontalLayout();
        titleWithIconLayout.setSpacing(true);
        titleWithIconLayout.setPadding(false);
        titleWithIconLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleWithIconLayout.getStyle().set("margin-bottom", "0.5rem");

        iconComponent.setSize("3em");
        iconComponent.getStyle()
                .set("margin-right", "0.25em")
                .set("color", iconColor)
                .set("background", iconColor + "1A")
                .set("padding", "10px")
                .set("border-radius", "50%");

        headerTitle.getStyle()
                .set("color", iconColor)
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
                .set("color", "var(--lumo-secondary-text-color)")
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
        Button newButton;
        if (icon != null) {
            newButton = new Button(label, new Icon(icon));
        } else {
            newButton = new Button(label);
        }

        newButton.addThemeVariants(variant);
        newButton.setMaxWidth("280px");
        newButton.getStyle()
                .set("border-radius", "30px")
                .set("font-weight", "600")
                .set("transition", "all 0.2s ease")
                .set("background-color", "var(" + iconColor + "-10pct)")
                .set("color", "var(" + iconColor + ")")
                .set("border", "1px solid var(" + iconColor + "-50pct)")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.05)");

        newButton.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.backgroundColor = 'var(" + iconColor + "-20pct)'; " +
                        "  this.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.1)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.backgroundColor = 'var(" + iconColor + "-10pct)'; " +
                        "  this.style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.05)'; " +
                        "});"
        );

        newButton.addClassName("hover-effect");
        return newButton;
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

    public static Button getScrollButton() {
        Button scrollToTopButton = new Button(FontAwesome.Solid.ARROW_TURN_UP.create());
        scrollToTopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
        scrollToTopButton.setAriaLabel("Torna all'inizio della pagina");
        scrollToTopButton.setTooltipText("Torna all'inizio della pagina");
        scrollToTopButton.getStyle()
                .set("position", "fixed")
                .set("top", "calc(50% - 50px)")
                .set("right", "20px")
                .set("z-index", "1000")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");
        scrollToTopButton.addClickListener(e ->
                UI.getCurrent().getPage().executeJs("window.scrollTo({top: 0, behavior: 'smooth'});")
        );

        return scrollToTopButton;
    }

    public static Button getScrollDownButton() {
        Button scrollToBottomButton = new Button(FontAwesome.Solid.ARROW_TURN_DOWN.create());
        scrollToBottomButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
        scrollToBottomButton.setAriaLabel("Vai alla fine della pagina");
        scrollToBottomButton.setTooltipText("Vai alla fine della pagina");
        scrollToBottomButton.getStyle()
                .set("position", "fixed")
                .set("top", "calc(50% + 10px)")
                .set("right", "20px")
                .set("z-index", "1000")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");
        scrollToBottomButton.addClickListener(e ->
                UI.getCurrent().getPage().executeJs(
                        "window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});"
                )
        );

        return scrollToBottomButton;
    }

    public static void createConfirmDialog(String title, String message,
                                           String confirmText, String cancelText,
                                           Runnable confirmAction) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        H3 titleComponent = new H3(title);
        titleComponent.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-m)");

        Paragraph messageComponent = new Paragraph(message);
        messageComponent.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "var(--lumo-space-l)");

        Button confirmButton = new Button(confirmText);
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.getStyle().set("margin-right", "var(--lumo-space-s)");
        confirmButton.addClickListener(e -> {
            dialog.close();
            if (confirmAction != null) {
                confirmAction.run();
            }
        });

        Button cancelButton = new Button(cancelText);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, confirmButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        VerticalLayout mainLayout = new VerticalLayout(titleComponent, messageComponent, buttonLayout);
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        dialog.add(mainLayout);
        dialog.open();

    }
}
