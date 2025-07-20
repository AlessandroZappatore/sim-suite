package it.uniupo.simnova.service.scenario.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class TimelineConfiguration {

    @Getter
    private final AdvancedScenarioService service;
    private final Integer scenarioId;
    @Getter
    private boolean editable;
    @Getter
    private boolean showAddButton;
    @Getter
    private boolean showDeleteButton;
    @Setter
    @Getter
    private BiConsumer<VerticalLayout, Tempo> cardCustomizer;

    @Setter
    @Getter
    private BiFunction<Tempo, TimelineConfiguration, Component> headerGenerator;


    public TimelineConfiguration(AdvancedScenarioService service, Integer scenarioId, boolean editable) {
        if (!editable) {
            this.editable = false;
            this.showAddButton = false;
            this.showDeleteButton = false;
        } else {
            this.editable = true;
            this.showAddButton = true;
            this.showDeleteButton = true;
        }
        this.service = service;
        this.scenarioId = scenarioId;
    }

    public int getScenarioId() {
        return scenarioId;
    }

}
