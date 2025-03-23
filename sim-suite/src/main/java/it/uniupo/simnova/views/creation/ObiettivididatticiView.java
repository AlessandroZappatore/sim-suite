package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Obiettivididattici")
@Route("obiettivididattici")
@Menu(order = 7, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class ObiettivididatticiView extends Composite<VerticalLayout> {

    public ObiettivididatticiView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        Icon icon = new Icon();
        HorizontalLayout layoutRow3 = new HorizontalLayout();
        Icon icon2 = new Icon();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        TextArea textArea = new TextArea();
        HorizontalLayout layoutRow4 = new HorizontalLayout();
        HorizontalLayout layoutRow5 = new HorizontalLayout();
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
        layoutRow3.setHeightFull();
        layoutRow2.setFlexGrow(1.0, layoutRow3);
        layoutRow3.addClassName(Gap.MEDIUM);
        layoutRow3.setWidth("100%");
        layoutRow3.getStyle().set("flex-grow", "1");
        layoutRow3.setAlignItems(Alignment.START);
        layoutRow3.setJustifyContentMode(JustifyContentMode.END);
        icon2.setIcon("lumo:user");
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn3.setWidthFull();
        layoutColumn2.setFlexGrow(1.0, layoutColumn3);
        layoutColumn3.setWidth("100%");
        layoutColumn3.getStyle().set("flex-grow", "1");
        layoutColumn3.setJustifyContentMode(JustifyContentMode.CENTER);
        layoutColumn3.setAlignItems(Alignment.CENTER);
        textArea.setLabel("OBIETTIVI DIDATTICI");
        layoutColumn3.setAlignSelf(Alignment.CENTER, textArea);
        textArea.setWidth("90%");
        textArea.setHeight("300px");
        layoutRow4.addClassName(Gap.MEDIUM);
        layoutRow4.setWidth("100%");
        layoutRow4.setHeight("min-content");
        layoutRow5.addClassName(Gap.MEDIUM);
        layoutRow5.setWidth("100%");
        layoutRow5.setHeight("min-content");
        buttonPrimary.setText("Indietro");
        buttonPrimary.setWidth("10%");
        buttonPrimary.setHeight("50px");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        textSmall.setText("Sviluppato e creato da Alessandro Zappatore");
        layoutRow5.setAlignSelf(Alignment.CENTER, textSmall);
        textSmall.setWidth("100%");
        textSmall.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        buttonPrimary2.setText("Avanti");
        buttonPrimary2.setWidth("10%");
        buttonPrimary2.setHeight("50px");
        buttonPrimary2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.add(icon);
        layoutRow2.add(layoutRow3);
        layoutRow3.add(icon2);
        getContent().add(layoutColumn2);
        layoutColumn2.add(layoutColumn3);
        layoutColumn3.add(textArea);
        getContent().add(layoutRow4);
        layoutRow4.add(layoutRow5);
        layoutRow5.add(buttonPrimary);
        layoutRow5.add(textSmall);
        layoutRow5.add(buttonPrimary2);

        buttonPrimary.addClickListener(e -> {
            buttonPrimary.getUI().ifPresent(ui -> ui.navigate("azionechiave"));
        });

        buttonPrimary2.addClickListener(e -> {
            buttonPrimary2.getUI().ifPresent(ui -> ui.navigate("materialenecessario"));
        });
    }
}
