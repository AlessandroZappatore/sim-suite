package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("StartCreation")
@Route("startCreation")
@Menu(order = 2, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class StartCreationView extends Composite<VerticalLayout> {

    public StartCreationView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        Icon icon = new Icon();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        TextField textField = new TextField();
        TextField textField2 = new TextField();
        TextField textField3 = new TextField();
        HorizontalLayout layoutRow3 = new HorizontalLayout();
        HorizontalLayout layoutRow4 = new HorizontalLayout();
        Button buttonPrimary = new Button();
        Paragraph textSmall = new Paragraph();
        Button buttonPrimary2 = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        layoutRow2.setHeightFull();
        layoutRow.setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.getStyle().set("flex-grow", "1");
        layoutRow2.setAlignItems(Alignment.START);
        layoutRow2.setJustifyContentMode(JustifyContentMode.END);
        icon.setIcon("lumo:user");
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn3.setWidthFull();
        layoutColumn2.setFlexGrow(1.0, layoutColumn3);
        layoutColumn3.setWidth("100%");
        layoutColumn3.getStyle().set("flex-grow", "1");
        textField.setLabel("TITOLO SCENARIO");
        layoutColumn3.setAlignSelf(FlexComponent.Alignment.CENTER, textField);
        textField.setWidth("50%");
        textField.setHeight("100px");
        textField2.setLabel("NOME PAZIENTE");
        layoutColumn3.setAlignSelf(FlexComponent.Alignment.CENTER, textField2);
        textField2.setWidth("50%");
        textField2.setHeight("100px");
        textField3.setLabel("PATOLOGIA/MALATTIA");
        layoutColumn3.setAlignSelf(FlexComponent.Alignment.CENTER, textField3);
        textField3.setWidth("50%");
        textField3.setHeight("100px");
        layoutRow3.addClassName(Gap.MEDIUM);
        layoutRow3.setWidth("100%");
        layoutRow3.setHeight("min-content");
        layoutRow4.addClassName(Gap.MEDIUM);
        layoutRow4.setWidth("100%");
        layoutRow4.setHeight("min-content");
        buttonPrimary.setText("Indietro");
        buttonPrimary.setWidth("10%");
        buttonPrimary.setHeight("50px");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        textSmall.setText("Sviluppato e creato da Alessandro Zappatore");
        layoutRow4.setAlignSelf(FlexComponent.Alignment.CENTER, textSmall);
        textSmall.setWidth("100%");
        textSmall.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        buttonPrimary2.setText("Avanti");
        buttonPrimary2.setWidth("10%");
        buttonPrimary2.setHeight("50px");
        buttonPrimary2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.add(icon);
        getContent().add(layoutColumn2);
        layoutColumn2.add(layoutColumn3);
        layoutColumn3.add(textField);
        layoutColumn3.add(textField2);
        layoutColumn3.add(textField3);
        getContent().add(layoutRow3);
        layoutRow3.add(layoutRow4);
        layoutRow4.add(buttonPrimary);
        layoutRow4.add(textSmall);
        layoutRow4.add(buttonPrimary2);

        buttonPrimary2.addClickListener(e -> {
            buttonPrimary2.getUI().ifPresent(ui -> ui.navigate("descrizione"));
        });

        buttonPrimary.addClickListener(e -> {
            buttonPrimary.getUI().ifPresent(ui -> ui.navigate("creation"));
        });
    }
}
