package it.uniupo.simnova.simcreation;

import java.util.ArrayList;

public class AdvancedScenario extends Scenario{
    private ArrayList<TempoN> tempi;

    public AdvancedScenario(String titoloScenario, String nomePaziente, String patologiaMalattia, String nomeFile, String descrizioneScenario, String briefing, String patto, String azioniChiave, String obiettiviDidattici, String materialeNecessario, String esamiReferti, String moulage, String liquidiPresidi, int PA, int FC, int RR, float temp, int SPO2, int etCO2, boolean accessiVenosi, boolean accessiArteriosi, String monitor, String generale, String pupille, String collo, String torace, String cuore, String addome, String retto, String cute, String estremita, String neurologico, String FAST, ArrayList<TempoN> tempi) {
        super(titoloScenario, nomePaziente, patologiaMalattia, nomeFile, descrizioneScenario, briefing, patto, azioniChiave, obiettiviDidattici, materialeNecessario, esamiReferti, moulage, liquidiPresidi, PA, FC, RR, temp, SPO2, etCO2, accessiVenosi, accessiArteriosi, monitor, generale, pupille, collo, torace, cuore, addome, retto, cute, estremita, neurologico, FAST);
        this.tempi = tempi;
    }

    public ArrayList<TempoN> getTempi() {
        return tempi;
    }

    public void setTempi(ArrayList<TempoN> tempi) {
        this.tempi = tempi;
    }
}
