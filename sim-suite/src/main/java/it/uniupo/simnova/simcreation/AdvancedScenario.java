package it.uniupo.simnova.simcreation;

import java.util.ArrayList;

public class AdvancedScenario extends Scenario {
    private int id;
    private ArrayList<Tempo> tempi;

    public AdvancedScenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String materiale, String moulage, String liquidi, float timer_generale, EsameFisico esame_fisico, PazienteT0 paziente_t0, ArrayList<Tempo> tempi) {
        super(id, titolo, nome_paziente, patologia, descrizione, briefing, patto_aula, azione_chiave, obiettivo, materiale, moulage, liquidi, timer_generale, esame_fisico, paziente_t0);
        this.tempi = tempi;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
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
                "id=" + id +
                ", tempi=" + tempi +
                '}';
    }
}
