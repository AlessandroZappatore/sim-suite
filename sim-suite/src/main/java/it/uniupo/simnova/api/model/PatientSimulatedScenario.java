package it.uniupo.simnova.api.model;

import java.util.ArrayList;

public class PatientSimulatedScenario extends AdvancedScenario {
    private int idPatientSimulatedScenario;
    private int advancedScenario;
    private String sceneggiatura;

    public PatientSimulatedScenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String materiale, String moulage, String liquidi, float timer_generale, int id_advanced_scenario, ArrayList<Tempo> tempi, int idPatientSimulatedScenario, int advancedScenario, String sceneggiatura) {
        super(id, titolo, nome_paziente, patologia, descrizione, briefing, patto_aula, azione_chiave, obiettivo, materiale, moulage, liquidi, timer_generale, id_advanced_scenario, tempi);
        this.idPatientSimulatedScenario = idPatientSimulatedScenario;
        this.advancedScenario = advancedScenario;
        this.sceneggiatura = sceneggiatura;
    }

    public PatientSimulatedScenario() {

    }

    public int getIdPatientSimulatedScenario() {
        return idPatientSimulatedScenario;
    }

    public void setIdPatientSimulatedScenario(int idPatientSimulatedScenario) {
        this.idPatientSimulatedScenario = idPatientSimulatedScenario;
    }

    public int getAdvancedScenario() {
        return advancedScenario;
    }

    public void setAdvancedScenario(int advancedScenario) {
        this.advancedScenario = advancedScenario;
    }

    public String getSceneggiatura() {
        return sceneggiatura;
    }

    public void setSceneggiatura(String sceneggiatura) {
        this.sceneggiatura = sceneggiatura;
    }

    @Override
    public String toString() {
        return "PatientSimulatedScenario{" +
                "idPatientSimulatedScenario=" + idPatientSimulatedScenario +
                ", advancedScenario=" + advancedScenario +
                ", sceneggiatura='" + sceneggiatura + '\'' +
                '}';
    }
}
