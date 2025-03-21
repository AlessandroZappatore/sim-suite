package it.uniupo.simnova.views.execution;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.views.home.AppHeader;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Execution")
@Route("execution")
public class ExecutionView extends Composite<VerticalLayout> {

    public ExecutionView() {
        AppHeader header = new AppHeader();
        Checkbox checkbox = new Checkbox("Checkbox");
        MultiSelectComboBox<SampleItem> multiSelectComboBox = new MultiSelectComboBox<>();

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        multiSelectComboBox.setLabel("Multi-Select Combo Box");
        multiSelectComboBox.setWidth("min-content");

        setMultiSelectComboBoxSampleData(multiSelectComboBox);

        getContent().add(header, checkbox, multiSelectComboBox);
    }

    record SampleItem(String value, String label, Boolean disabled) {
    }

    private void setMultiSelectComboBoxSampleData(MultiSelectComboBox<SampleItem> multiSelectComboBox) {
        List<SampleItem> sampleItems = new ArrayList<>();
        sampleItems.add(new SampleItem("first", "First", null));
        sampleItems.add(new SampleItem("second", "Second", null));
        sampleItems.add(new SampleItem("third", "Third", Boolean.TRUE));
        sampleItems.add(new SampleItem("fourth", "Fourth", null));
        multiSelectComboBox.setItems(sampleItems);
        multiSelectComboBox.setItemLabelGenerator(SampleItem::label);
    }
}
