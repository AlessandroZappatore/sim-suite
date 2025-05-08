package it.uniupo.simnova.views.support.detail;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import it.uniupo.simnova.api.model.Accesso;
import it.uniupo.simnova.api.model.EsameFisico;
import it.uniupo.simnova.api.model.ParametroAggiuntivo;
import it.uniupo.simnova.api.model.PazienteT0;
import it.uniupo.simnova.service.ScenarioService;

import java.util.List;
import java.util.Map;

public class PatientT0Support {

    // Adattatore da PazienteT0 a VitalSignsDataProvider
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

    public static VerticalLayout createPatientContent(ScenarioService scenarioService, int scenarioId) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        PazienteT0 paziente = scenarioService.getPazienteT0ById(scenarioId);
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

            // Usa il nuovo MonitorSupport con l'adattatore
            VitalSignsDataProvider t0DataProvider = new PazienteT0VitalSignsAdapter(paziente);
            Component vitalSignsMonitor = MonitorSupport.createVitalSignsMonitor(t0DataProvider);
            patientCard.add(vitalSignsMonitor);

            // Accessi venosi e arteriosi
            if (!paziente.getAccessiVenosi().isEmpty() || !paziente.getAccessiArteriosi().isEmpty()) {
                Div accessesCard = new Div();
                accessesCard.getStyle()
                        .set("margin-top", "var(--lumo-space-m)")
                        .set("padding-top", "var(--lumo-space-s)")
                        .set("border-top", "1px solid var(--lumo-contrast-10pct)")
                        .set("width", "100%");

                if (!paziente.getAccessiVenosi().isEmpty()) {
                    HorizontalLayout accessVenosiTitleLayout = new HorizontalLayout();
                    accessVenosiTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                    accessVenosiTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                    accessVenosiTitleLayout.setWidthFull();
                    accessVenosiTitleLayout.setSpacing(true);

                    Icon accessVenosiIcon = new Icon(VaadinIcon.LINES);
                    accessVenosiIcon.getStyle()
                            .set("color", "var(--lumo-primary-color)")
                            .set("background-color", "var(--lumo-primary-color-10pct)")
                            .set("padding", "var(--lumo-space-xs)")
                            .set("border-radius", "50%");

                    H4 accessVenosiTitle = new H4("Accessi Venosi");
                    accessVenosiTitle.getStyle()
                            .set("margin", "0")
                            .set("font-weight", "500");

                    accessVenosiTitleLayout.add(accessVenosiIcon, accessVenosiTitle);
                    accessesCard.add(accessVenosiTitleLayout);

                    Grid<Accesso> accessiVenosiGrid = createAccessiGrid(paziente.getAccessiVenosi());
                    accessiVenosiGrid.getStyle()
                            .set("border", "none")
                            .set("box-shadow", "none")
                            .set("margin-top", "var(--lumo-space-s)")
                            .set("margin-bottom", "var(--lumo-space-s)")
                            .set("margin-left", "auto")
                            .set("margin-right", "auto")
                            .set("max-width", "600px");
                    accessesCard.add(accessiVenosiGrid);
                }

                if (!paziente.getAccessiArteriosi().isEmpty()) {
                    HorizontalLayout accessArtTitleLayout = new HorizontalLayout();
                    accessArtTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                    accessArtTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                    accessArtTitleLayout.setWidthFull();
                    accessArtTitleLayout.setSpacing(true);

                    Icon accessArtIcon = new Icon(VaadinIcon.LINES);
                    accessArtIcon.getStyle()
                            .set("color", "var(--lumo-success-color)")
                            .set("background-color", "var(--lumo-success-color-10pct)")
                            .set("padding", "var(--lumo-space-xs)")
                            .set("border-radius", "50%");

                    H4 accessArtTitle = new H4("Accessi Arteriosi");
                    accessArtTitle.getStyle()
                            .set("margin", "0")
                            .set("font-weight", "500");

                    accessArtTitleLayout.add(accessArtIcon, accessArtTitle);
                    accessesCard.add(accessArtTitleLayout);

                    Grid<Accesso> accessiArtGrid = createAccessiGrid(paziente.getAccessiArteriosi());
                    accessiArtGrid.getStyle()
                            .set("border", "none")
                            .set("box-shadow", "none")
                            .set("margin-top", "var(--lumo-space-s)")
                            .set("margin-left", "auto")
                            .set("margin-right", "auto")
                            .set("max-width", "600px");
                    accessesCard.add(accessiArtGrid);
                }
                patientCard.add(accessesCard);
            }
            layout.add(patientCard);
        } else {
            Div noDataCard = EmptySupport.createErrorContent("Nessun dato paziente disponibile");
            layout.add(noDataCard);
        }

        // Sezione Esame Fisico
        EsameFisico esame = scenarioService.getEsameFisicoById(scenarioId);
        if (esame != null && !esame.getSections().isEmpty()) {
            Div examCard = new Div();
            examCard.addClassName("info-card");
            examCard.getStyle()
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("box-shadow", "var(--lumo-box-shadow-xs)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("transition", "box-shadow 0.3s ease-in-out")
                    .set("width", "80%")
                    .set("max-width", "800px");

            examCard.getElement().executeJs(
                    "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                            "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
            );

            HorizontalLayout examTitleLayout = new HorizontalLayout();
            examTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            examTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            examTitleLayout.setWidthFull();
            examTitleLayout.setSpacing(true);

            Icon examIcon = new Icon(VaadinIcon.STETHOSCOPE);
            examIcon.getStyle()
                    .set("color", "var(--lumo-primary-color)")
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("padding", "var(--lumo-space-xs)")
                    .set("border-radius", "50%");

            H3 examTitle = new H3("Esame Fisico");
            examTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "600")
                    .set("color", "var(--lumo-primary-text-color)");

            examTitleLayout.add(examIcon, examTitle);
            examCard.add(examTitleLayout);

            Map<String, String> sections = esame.getSections();
            VerticalLayout examLayout = new VerticalLayout();
            examLayout.setPadding(false);
            examLayout.setSpacing(true);
            examLayout.getStyle().set("margin-top", "var(--lumo-space-m)");
            examLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            addSectionIfNotEmpty(examLayout, "Generale", sections.get("Generale"));
            addSectionIfNotEmpty(examLayout, "Pupille", sections.get("Pupille"));
            addSectionIfNotEmpty(examLayout, "Collo", sections.get("Collo"));
            addSectionIfNotEmpty(examLayout, "Torace", sections.get("Torace"));
            addSectionIfNotEmpty(examLayout, "Cuore", sections.get("Cuore"));
            addSectionIfNotEmpty(examLayout, "Addome", sections.get("Addome"));
            addSectionIfNotEmpty(examLayout, "Retto", sections.get("Retto"));
            addSectionIfNotEmpty(examLayout, "Cute", sections.get("Cute"));
            addSectionIfNotEmpty(examLayout, "Estremità", sections.get("Estremità"));
            addSectionIfNotEmpty(examLayout, "Neurologico", sections.get("Neurologico"));
            addSectionIfNotEmpty(examLayout, "FAST", sections.get("FAST"));

            if (examLayout.getComponentCount() > 0) {
                examCard.add(examLayout);
                layout.add(examCard);
            }
        }
        return layout;
    }

    private static void addSectionIfNotEmpty(VerticalLayout content, String title, String value) {
        if (value != null && !value.trim().isEmpty()) {
            content.add(InfoItemSupport.createInfoItem(title, value, new Icon(VaadinIcon.INFO)));
        }
    }

    private static Grid<Accesso> createAccessiGrid(List<Accesso> accessi) {
        Grid<Accesso> grid = new Grid<>();
        grid.setItems(accessi);
        grid.addColumn(Accesso::getTipologia).setHeader("Tipologia").setAutoWidth(true);
        grid.addColumn(Accesso::getPosizione).setHeader("Posizione").setAutoWidth(true);
        grid.addColumn(Accesso::getLato).setHeader("Lato").setAutoWidth(true);
        grid.addColumn(Accesso::getMisura).setHeader("Misura").setAutoWidth(true);
        grid.setAllRowsVisible(true);
        return grid;
    }
}