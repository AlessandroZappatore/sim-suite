package it.uniupo.simnova.views.execution;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.ReusableTimer;
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

        ReusableTimer generalTimer = new ReusableTimer(
                "Timer Generale",
                20,
                Notification.Position.MIDDLE
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);
        customHeader.add(generalTimer);
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