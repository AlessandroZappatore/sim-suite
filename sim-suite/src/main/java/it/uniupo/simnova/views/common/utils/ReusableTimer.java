package it.uniupo.simnova.views.common.utils;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

@CssImport("./themes/sim.suite/views/timer-styles.css")
public class ReusableTimer extends Composite<Div> {
    private final SimpleTimer timer;
    private final Button toggleButton;
    private final String titleText;
    private final Notification.Position notificationPosition;

    public ReusableTimer(String title, Integer durationInMinutes, Notification.Position notificationPosition) {
        this.titleText = title;
        this.notificationPosition = notificationPosition;

        int totalSeconds = durationInMinutes * 60;

        Div root = getContent();
        root.addClassName("card-style");
        root.setWidthFull();
        root.setMaxWidth("250px");

        timer = new SimpleTimer();
        timer.setStartTime(totalSeconds);
        timer.setMinutes(true);
        timer.setFractions(false);
        timer.getStyle().set("font-size", "var(--lumo-font-size-xxl)").set("font-weight", "600");

        toggleButton = new Button(VaadinIcon.PLAY.create());
        toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleButton.setTooltipText("Fai partire il timer");

        Button restartButton = new Button(VaadinIcon.REFRESH.create());
        restartButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        restartButton.setTooltipText("Riavvia il timer");

        addListeners(restartButton);

        Span titleSpan = new Span(this.titleText);
        titleSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        HorizontalLayout buttonsLayout = new HorizontalLayout(toggleButton, restartButton);
        VerticalLayout contentWrapper = new VerticalLayout(titleSpan, timer, buttonsLayout);
        contentWrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        contentWrapper.setPadding(false);
        contentWrapper.setSpacing(false);

        root.add(contentWrapper);
    }

    private void addListeners(Button restartButton) {
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
            getContent().removeClassName("timer-ended-blinking");
            timer.reset();
            toggleButton.setIcon(VaadinIcon.PLAY.create());
            toggleButton.setTooltipText("Fai partire il timer");
        });

        timer.addTimerEndEvent(e -> {
            String message = String.format("Il timer '%s' è terminato.", this.titleText);
            Notification.show(message, 3000, this.notificationPosition)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            getContent().addClassName("timer-ended-blinking");
        });
    }
}