package it.uniupo.simnova.views.execution;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;

@PageTitle("Execution")
@Route("execution")
@CssImport("./themes/sim.suite/views/header-style.css")
public class ExecutionView extends Composite<VerticalLayout> {

    public ExecutionView(FileStorageService fileStorageService) {

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());
        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));

        SimpleTimer timer = new SimpleTimer();
        timer.setStartTime(60);
        timer.setFractions(false);
        timer.getStyle().set("font-size", "var(--lumo-font-size-xxl)").set("font-weight", "600");

        Button toggleButton = new Button(VaadinIcon.PLAY.create());
        toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleButton.setTooltipText("Fai partire il timer");

        Button restartButton = new Button(VaadinIcon.REFRESH.create());
        restartButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        restartButton.setTooltipText("Riavvia il timer");

        Div timerContainer = new Div();
        timerContainer.addClassName("card-style");

        toggleButton.addClickListener(e -> {
            if (timer.isRunning()) {
                timer.pause();
                toggleButton.setIcon(VaadinIcon.PLAY.create());
                toggleButton.setTooltipText("Riprendi");
            } else {
                timer.start();
                toggleButton.setIcon(VaadinIcon.PAUSE.create());
                toggleButton.setTooltipText("Metti in pausa");
            }
        });

        restartButton.addClickListener(e -> {
            timerContainer.getStyle().remove("background");

            timer.reset();
            toggleButton.setIcon(VaadinIcon.PLAY.create());
            toggleButton.setTooltipText("Fai partire il timer");
        });

        timer.addTimerEndEvent(e -> {
            Notification.show("Il timer generale è terminato.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);

            timerContainer.getStyle().set("background", "var(--lumo-error-color-50pct)");
        });

        Span title = new Span("Timer Generale");
        title.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        HorizontalLayout buttonsLayout = new HorizontalLayout(toggleButton, restartButton);
        VerticalLayout contentWrapper = new VerticalLayout(title, timer, buttonsLayout);
        contentWrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        contentWrapper.setPadding(false);
        contentWrapper.setSpacing(false);

        timerContainer.add(contentWrapper);

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);
        customHeader.add(timerContainer);
        customHeader.expand(header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "SIM EXECUTION",
                "Funzionalità non implementata",
                VaadinIcon.BUILDING.create(),
                "var(--lumo-primary-color)"
        );

        contentLayout.add(headerSection);
        HorizontalLayout footerSection = StyleApp.getFooterLayout(null);
        mainLayout.add(customHeader, contentLayout, footerSection);
    }
}