package it.uniupo.simnova.simcreation;

import java.util.ArrayList;

public class PatientSimulatedScenario extends  AdvancedScenario{
    private int id_patient_simulated_scenario;
    private String sceneggiatura;

    public PatientSimulatedScenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String materiale, String moulage, String liquidi, float timer_generale, EsameFisico esame_fisico, PazienteT0 paziente_t0, ArrayList<Tempo> tempi, int id_patient_simulated_scenario, String sceneggiatura) {
        super(id, titolo, nome_paziente, patologia, descrizione, briefing, patto_aula, azione_chiave, obiettivo, materiale, moulage, liquidi, timer_generale, esame_fisico, paziente_t0, tempi);
        this.id_patient_simulated_scenario = id_patient_simulated_scenario;
        this.sceneggiatura = sceneggiatura;
    }

    public int getId_patient_simulated_scenario() {
        return id_patient_simulated_scenario;
    }

    public void setId_patient_simulated_scenario(int id_patient_simulated_scenario) {
        this.id_patient_simulated_scenario = id_patient_simulated_scenario;
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
                "id_patient_simulated_scenario=" + id_patient_simulated_scenario +
                ", sceneggiatura='" + sceneggiatura + '\'' +
                '}';
    }
}
