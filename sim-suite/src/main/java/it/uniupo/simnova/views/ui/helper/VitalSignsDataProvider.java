package it.uniupo.simnova.views.ui.helper;

import it.uniupo.simnova.domain.common.ParametroAggiuntivo;

import java.util.List;


public interface VitalSignsDataProvider {
    String getPA();

    Integer getFC();

    Double getT();

    Integer getRR();

    Integer getSpO2();

    Integer getFiO2();

    Float getLitriO2();

    Integer getEtCO2();

    String getAdditionalMonitorText();

    List<ParametroAggiuntivo> getAdditionalParameters();
}