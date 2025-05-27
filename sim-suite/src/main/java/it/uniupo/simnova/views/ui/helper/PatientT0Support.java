package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.components.PresidiService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.views.common.utils.StyleApp;

import java.util.List;

public class PatientT0Support {

    public static VerticalLayout createPatientContent(PazienteT0 paziente,
                                                      EsameFisico esame,
                                                      Integer scenarioId,
                                                      EsameFisicoService esameFisicoService,
                                                      PazienteT0Service pazienteT0Service,
                                                      PresidiService presidiService,
                                                      AdvancedScenarioService advancedScenarioService) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        if (paziente != null) {
            Div patientCard = new Div();
            patientCard.addClassName("info-card");
            patientCard.getStyle()
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("box-shadow", "var(--lumo-box-shadow-xs)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("margin-bottom", "var(--lumo-space-m)")
                    .set("transition", "box-shadow 0.3s ease-in-out")
                    .set("width", "80%")
                    .set("max-width", "800px");

            patientCard.getElement().executeJs(
                    "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                            "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
            );

            VitalSignsDataProvider t0DataProvider = new PazienteT0VitalSignsAdapter(paziente);
            Component vitalSignsMonitor = MonitorSupport.createVitalSignsMonitor(t0DataProvider, scenarioId, true, presidiService, pazienteT0Service, advancedScenarioService, null);
            patientCard.add(vitalSignsMonitor);


            Div accessCard = AccessSupport.getAccessoCard(pazienteT0Service, scenarioId);
            patientCard.add(accessCard);

            layout.add(patientCard);
        } else {
            Div noDataCard = EmptySupport.createErrorContent("Nessun dato paziente disponibile");
            layout.add(noDataCard);
            HorizontalLayout buttonContainer = new HorizontalLayout();
            buttonContainer.setWidthFull();
            buttonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

            Button creatT0Button = StyleApp.getButton("Aggiungi i dati per T0", VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
            creatT0Button.addThemeVariants(ButtonVariant.LUMO_LARGE);
            creatT0Button.getStyle().set("background-color", "var(--lumo-success-color");
            creatT0Button.addClickListener(ev -> UI.getCurrent().navigate("pazienteT0/" + scenarioId + "/edit"));

            buttonContainer.add(creatT0Button);
            layout.add(buttonContainer);
        }

        Div examCard = PhysicalExamSupport.getExamCard(esame, esameFisicoService, scenarioId);
        layout.add(examCard);

        return layout;
    }


    private record PazienteT0VitalSignsAdapter(PazienteT0 paziente) implements VitalSignsDataProvider {

        @Override
        public String getPA() {
            return paziente.getPA();
        }

        @Override
        public Integer getFC() {
            return paziente.getFC();
        }

        @Override
        public Double getT() {
            return paziente.getT();
        }

        @Override
        public Integer getRR() {
            return paziente.getRR();
        }

        @Override
        public Integer getSpO2() {
            return paziente.getSpO2();
        }

        @Override
        public Integer getFiO2() {
            return paziente.getFiO2();
        }

        @Override
        public Float getLitriO2() {
            return paziente.getLitriO2();
        }

        @Override
        public Integer getEtCO2() {
            return paziente.getEtCO2();
        }

        @Override
        public String getAdditionalMonitorText() {
            return paziente.getMonitor();
        }

        @Override
        public List<ParametroAggiuntivo> getAdditionalParameters() {
            return List.of();
        }
    }
}

