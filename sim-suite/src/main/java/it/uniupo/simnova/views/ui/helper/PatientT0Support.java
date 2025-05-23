package it.uniupo.simnova.views.ui.helper;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.components.PresidiService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.vaadin.tinymce.TinyMce;

import java.util.List;
import java.util.Map;

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

            // Usa il nuovo MonitorSupport con l'adattatore
            VitalSignsDataProvider t0DataProvider = new PazienteT0VitalSignsAdapter(paziente);
            Component vitalSignsMonitor = MonitorSupport.createVitalSignsMonitor(t0DataProvider, scenarioId, true, presidiService, pazienteT0Service, advancedScenarioService, null);
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

            addSectionIfNotEmpty(examLayout, "Generale", sections.get("Generale"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "Pupille", sections.get("Pupille"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "Collo", sections.get("Collo"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "Torace", sections.get("Torace"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "Cuore", sections.get("Cuore"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "Addome", sections.get("Addome"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "Retto", sections.get("Retto"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "Cute", sections.get("Cute"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "Estremità", sections.get("Estremità"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "Neurologico", sections.get("Neurologico"), esameFisicoService, scenarioId);
            addSectionIfNotEmpty(examLayout, "FAST", sections.get("FAST"), esameFisicoService, scenarioId);

            if (examLayout.getComponentCount() > 0) {
                examCard.add(examLayout);
                layout.add(examCard);
            }
        }
        return layout;
    }

    private static void addSectionIfNotEmpty(VerticalLayout content, String title, String value, EsameFisicoService esameFisicoService, Integer scenarioId) {
        if (value != null && !value.trim().isEmpty()) {
            Icon sectionIcon = getSectionIcon(title);
            // Layout principale della sezione
            VerticalLayout sectionLayout = new VerticalLayout();
            sectionLayout.setPadding(false);
            sectionLayout.setSpacing(false);
            sectionLayout.setWidthFull();

            // Header con icona, titolo e pulsante modifica
            HorizontalLayout headerRow = new HorizontalLayout();
            headerRow.setWidthFull();
            headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
            headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            HorizontalLayout titleGroup = new HorizontalLayout();
            titleGroup.setAlignItems(FlexComponent.Alignment.CENTER);
            sectionIcon.addClassName(LumoUtility.TextColor.PRIMARY);
            sectionIcon.getStyle()
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("padding", "var(--lumo-space-s)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("font-size", "var(--lumo-icon-size-m)")
                    .set("margin-right", "var(--lumo-space-xs)");

            H4 titleLabel = new H4(title);
            titleLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
            titleLabel.getStyle().set("font-weight", "600");
            titleGroup.add(sectionIcon, titleLabel);

            Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SMALL, "var(--lumo-base-color");
            editButton.setTooltipText("Modifica " + title);
            editButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

            headerRow.add(titleGroup, editButton);
            sectionLayout.add(headerRow);

            // Contenuto visualizzato
            Div contentDisplay = new Div();
            contentDisplay.getStyle()
                    .set("font-family", "var(--lumo-font-family)")
                    .set("line-height", "var(--lumo-line-height-m)")
                    .set("color", "var(--lumo-body-text-color)")
                    .set("white-space", "pre-wrap")
                    .set("padding", "var(--lumo-space-xs) 0 var(--lumo-space-s) calc(var(--lumo-icon-size-m) + var(--lumo-space-m))")
                    .set("width", "100%")
                    .set("box-sizing", "border-box");
            contentDisplay.getElement().setProperty("innerHTML", value.replace("\n", "<br />"));
            sectionLayout.add(contentDisplay);

            // Editor TinyMCE - inizialmente nascosto
            TinyMce contentEditor = TinyEditor.getEditor();
            contentEditor.setValue(value);
            contentEditor.setVisible(false);
            // Pulsanti Salva/Annulla per l'editor - inizialmente nascosti
            Button saveButton = new Button("Salva");
            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            Button cancelButton = new Button("Annulla");
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            HorizontalLayout editorActions = new HorizontalLayout(saveButton, cancelButton);
            editorActions.setVisible(false);
            editorActions.getStyle()
                    .set("margin-top", "var(--lumo-space-xs)")
                    .set("padding-left", "calc(var(--lumo-icon-size-m) + var(--lumo-space-m))");

            sectionLayout.add(contentEditor, editorActions);

            // Logica dei pulsanti
            editButton.addClickListener(e -> {
                contentDisplay.setVisible(false);
                contentEditor.setValue(contentDisplay.getElement().getProperty("innerHTML").replace("<br />", "\n").replace("<br>", "\n"));
                contentEditor.setVisible(true);
                editorActions.setVisible(true);
                editButton.setVisible(false);
            });

            saveButton.addClickListener(e -> {
                String newContent = contentEditor.getValue();
                contentDisplay.getElement().setProperty("innerHTML", newContent.replace("\n", "<br />"));
                esameFisicoService.updateSingleEsameFisico(scenarioId, title, newContent);
                contentEditor.setVisible(false);
                editorActions.setVisible(false);
                contentDisplay.setVisible(true);
                editButton.setVisible(true);
                Notification.show("Sezione "+title+" aggiornata.", 3000, Notification.Position.BOTTOM_CENTER);

            });

            cancelButton.addClickListener(e -> {
                contentEditor.setVisible(false);
                editorActions.setVisible(false);
                contentDisplay.setVisible(true);
                editButton.setVisible(true);
            });

            content.add(sectionLayout);
        }
    }

    private static Icon getSectionIcon(String sectionTitle) {
        return switch (sectionTitle) {
            case "Generale" -> new Icon(VaadinIcon.CLIPBOARD_PULSE);
            case "Pupille" -> new Icon(VaadinIcon.EYE);
            case "Collo" -> new Icon(VaadinIcon.USER);
            case "Torace" -> FontAwesome.Solid.LUNGS.create();
            case "Cuore" -> new Icon(VaadinIcon.HEART);
            case "Addome" -> FontAwesome.Solid.A.create();
            case "Retto" -> FontAwesome.Solid.POOP.create();
            case "Cute" -> FontAwesome.Solid.HAND_DOTS.create();
            case "Estremità" -> FontAwesome.Solid.HANDS.create();
            case "Neurologico" -> FontAwesome.Solid.BRAIN.create();
            case "FAST" -> new Icon(VaadinIcon.AMBULANCE);
            default -> new Icon(VaadinIcon.INFO);
        };
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
}

