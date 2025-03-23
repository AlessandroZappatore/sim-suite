package it.uniupo.simnova.simcreation;

import java.util.ArrayList;

public class AdvancedScenario extends  ScenarioTemporizzato{
    private int id_advanced;

    public AdvancedScenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String materiale, String moulage, String liquidi, EsameFisico esame_fisico, PazienteT0 paziente_t0, ArrayList<Tempo> tempi, int id_advanced) {
        super(id, titolo, nome_paziente, patologia, descrizione, briefing, patto_aula, azione_chiave, obiettivo, materiale, moulage, liquidi, esame_fisico, paziente_t0, tempi);
        this.id_advanced = id_advanced;
    }

    public int getId_advanced() {
        return id_advanced;
    }

    public void setId_advanced(int id_advanced) {
        this.id_advanced = id_advanced;
    }

    @Override
    public String toString() {
        return "AdvancedScenario{" +
                "id_advanced=" + id_advanced +
                '}';
    }
}
