package it.uniupo.simnova.simcreation;

public class Tempo {
    private int id;
    private int id_scenario;
    private int PA;
    private int FC;
    private int RR;
    private float T;
    private int SpO2;
    private int EtCO2;
    private String Azione;
    private Tempo TSi;
    private Tempo TNo;
    private String altri_dettagli;
    private float timer_tempo;

    public Tempo(int id, int id_scenario, int PA, int FC, int RR, float T, int SpO2, int EtCO2, String Azione, Tempo TSi, Tempo TNo, String altri_dettagli, float timer_tempo) {
        this.id = id;
        this.id_scenario = id_scenario;
        this.PA = PA;
        this.FC = FC;
        this.RR = RR;
        this.T = T;
        this.SpO2 = SpO2;
        this.EtCO2 = EtCO2;
        this.Azione = Azione;
        this.TSi = TSi;
        this.TNo = TNo;
        this.altri_dettagli = altri_dettagli;
        if(timer_tempo<=0){
            this.timer_tempo = 0;
        }
        else{
            this.timer_tempo = timer_tempo;
        }
    }

    public int getId() {
        return id;
    }

    public int getIdScenario() {
        return id_scenario;
    }

    public int getPA() {
        return PA;
    }

    public int getFC() {
        return FC;
    }

    public int getRR() {
        return RR;
    }

    public float getT() {
        return T;
    }

    public int getSpO2() {
        return SpO2;
    }

    public int getEtCO2() {
        return EtCO2;
    }

    public String getAzione() {
        return Azione;
    }

    public Tempo getTSi() {
        return TSi;
    }

    public Tempo getTNo() {
        return TNo;
    }

    public String getAltriDettagli() {
        return altri_dettagli;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIdScenario(int id_scenario) {
        this.id_scenario = id_scenario;
    }

    public void setPA(int PA) {
        this.PA = PA;
    }

    public void setFC(int FC) {
        this.FC = FC;
    }

    public void setRR(int RR) {
        this.RR = RR;
    }

    public void setT(float T) {
        this.T = T;
    }

    public void setSpO2(int SpO2) {
        this.SpO2 = SpO2;
    }

    public void setEtCO2(int EtCO2) {
        this.EtCO2 = EtCO2;
    }

    public void setAzione(String Azione) {
        this.Azione = Azione;
    }

    public void setTSi(Tempo TSi) {
        this.TSi = TSi;
    }

    public void setTNo(Tempo TNo) {
        this.TNo = TNo;
    }

    public void setAltriDettagli(String altri_dettagli) {
        this.altri_dettagli = altri_dettagli;
    }

    @Override
    public String toString() {
        return "Tempo [id=" + id + ", id_scenario=" + id_scenario + ", PA=" + PA + ", FC=" + FC + ", RR=" + RR + ", T=" + T + ", SpO2=" + SpO2 + ", EtCO2=" + EtCO2 + ", Azione=" + Azione + ", TSi=" + TSi + ", TNo=" + TNo + ", altri_dettagli=" + altri_dettagli + "]";
    }
}
