package it.uniupo.simnova.views.support.detail;

import it.uniupo.simnova.api.model.ParametroAggiuntivo;

import java.util.List;

// Interfaccia per fornire i dati dei parametri vitali al monitor
public interface VitalSignsDataProvider {
    String getPA();

    Integer getFC();

    Double getT();

    Integer getRR();

    Integer getSpO2();

    Integer getFiO2();

    Float getLitriO2();

    Integer getEtCO2();

    String getAdditionalMonitorText(); // Per testo aggiuntivo come PazienteT0.getMonitor()

    List<ParametroAggiuntivo> getAdditionalParameters(); // Nuovo metodo
}