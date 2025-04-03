package it.uniupo.simnova.api.model;

public class Tempo {
    private int idTempo;
    private int advancedScenario;
    private String PA;
    private Integer FC;
    private Integer RR;
    private Float T;
    private Integer SpO2;
    private Integer EtCO2;
    private String Azione;
    private int TSi;
    private int TNo;
    private String altriDettagli;
    private long timerTempo;

    public Tempo(int idTempo, int advancedScenario, String PA, Integer FC, Integer RR, Float t, Integer spO2, Integer etCO2, String azione, int TSi, int TNo, String altriDettagli, long timerTempo) {
        this.idTempo = idTempo;
        this.advancedScenario = advancedScenario;
        if (PA != null && !PA.matches("\\d+/\\d+")) {
            throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica'");
        }
        this.PA = PA;
        this.FC = FC;
        this.RR = RR;
        T = t;
        SpO2 = spO2;
        EtCO2 = etCO2;
        Azione = azione;
        this.TSi = TSi;
        this.TNo = TNo;
        this.altriDettagli = altriDettagli;
        this.timerTempo = timerTempo;
    }

    public Tempo() {
        // Default constructor
    }

    public int getIdTempo() {
        return idTempo;
    }

    public void setIdTempo(int idTempo) {
        this.idTempo = idTempo;
    }

    public int getAdvancedScenario() {
        return advancedScenario;
    }

    public void setAdvancedScenario(int advancedScenario) {
        this.advancedScenario = advancedScenario;
    }

    public String getPA() {
        return PA;
    }

    public void setPA(String PA) {
        if (PA != null && !PA.matches("\\d+/\\d+")) {
            throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica'");
        }
        this.PA = PA;
    }

    public Integer getFC() {
        return FC;
    }

    public void setFC(Integer FC) {
        this.FC = FC;
    }

    public Integer getRR() {
        return RR;
    }

    public void setRR(Integer RR) {
        this.RR = RR;
    }

    public Float getT() {
        return T;
    }

    public void setT(Float t) {
        T = t;
    }

    public Integer getSpO2() {
        return SpO2;
    }

    public void setSpO2(Integer spO2) {
        SpO2 = spO2;
    }

    public Integer getEtCO2() {
        return EtCO2;
    }

    public void setEtCO2(Integer etCO2) {
        EtCO2 = etCO2;
    }

    public String getAzione() {
        return Azione;
    }

    public void setAzione(String azione) {
        Azione = azione;
    }

    public int getTSi() {
        return TSi;
    }

    public void setTSi(int TSi) {
        this.TSi = TSi;
    }

    public int getTNo() {
        return TNo;
    }

    public void setTNo(int TNo) {
        this.TNo = TNo;
    }

    public String getAltriDettagli() {
        return altriDettagli;
    }

    public void setAltriDettagli(String altriDettagli) {
        this.altriDettagli = altriDettagli;
    }

    public long getTimerTempo() {
        return timerTempo;
    }

    public void setTimerTempo(long timerTempo) {
        this.timerTempo = timerTempo;
    }

    @Override
    public String toString() {
        return "Tempo{" +
                "idTempo=" + idTempo +
                ", advancedScenario=" + advancedScenario +
                ", PA=" + PA +
                ", FC=" + FC +
                ", RR=" + RR +
                ", T=" + T +
                ", SpO2=" + SpO2 +
                ", EtCO2=" + EtCO2 +
                ", Azione='" + Azione + '\'' +
                ", TSi=" + TSi +
                ", TNo=" + TNo +
                ", altriDettagli='" + altriDettagli + '\'' +
                ", timerTempo=" + timerTempo +
                '}';
    }
}
