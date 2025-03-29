package it.uniupo.simnova.api.model;

import java.util.ArrayList;

public class AdvancedScenario extends Scenario {
    private int id_advanced_scenario;
    private ArrayList<Tempo> tempi;

    public AdvancedScenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String materiale, String moulage, String liquidi, float timer_generale, int id_advanced_scenario, ArrayList<Tempo> tempi) {
        super(id, titolo, nome_paziente, patologia, descrizione, briefing, patto_aula, azione_chiave, obiettivo, materiale, moulage, liquidi, timer_generale);
        this.id_advanced_scenario = id_advanced_scenario;
        this.tempi = tempi;
    }

    public AdvancedScenario(){

    }

    public int getId_advanced_scenario() {
        return id_advanced_scenario;
    }

    public void setId_advanced_scenario(int id_advanced_scenario) {
        this.id_advanced_scenario = id_advanced_scenario;
    }

    public ArrayList<Tempo> getTempi() {
        return tempi;
    }

    public void setTempi(ArrayList<Tempo> tempi) {
        this.tempi = tempi;
    }

    @Override
    public String toString() {
        return "AdvancedScenario{" +
                "id_advanced_scenario=" + id_advanced_scenario +
                ", tempi=" + tempi +
                '}';
    }
}
