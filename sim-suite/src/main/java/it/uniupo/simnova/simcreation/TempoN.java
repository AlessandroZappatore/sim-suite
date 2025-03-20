package it.uniupo.simnova.simcreation;

public class TempoN {
    public String nome;
    private int PA;
    private int FC;
    private int RR;
    private float Temp;
    private int SPO2;
    private int EtCO2;
    private String Dettagli;
    private String Azione;
    private TempoN Positivo;
    private TempoN Negativo;

    public TempoN(String nome, int PA, int FC, int RR, float Temp, int SPO2, int EtCO2, String Dettagli, String Azione, TempoN Positivo, TempoN Negativo) {
        this.nome = nome;
        this.PA = PA;
        this.FC = FC;
        this.RR = RR;
        this.Temp = Temp;
        this.SPO2 = SPO2;
        this.EtCO2 = EtCO2;
        this.Dettagli = Dettagli;
        this.Azione = Azione;
        this.Positivo = Positivo;
        this.Negativo = Negativo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getPA() {
        return PA;
    }

    public void setPA(int PA) {
        this.PA = PA;
    }

    public int getFC() {
        return FC;
    }

    public void setFC(int FC) {
        this.FC = FC;
    }

    public int getRR() {
        return RR;
    }

    public void setRR(int RR) {
        this.RR = RR;
    }

    public float getTemp() {
        return Temp;
    }

    public void setTemp(float temp) {
        Temp = temp;
    }

    public int getSPO2() {
        return SPO2;
    }

    public void setSPO2(int SPO2) {
        this.SPO2 = SPO2;
    }

    public int getEtCO2() {
        return EtCO2;
    }

    public void setEtCO2(int etCO2) {
        EtCO2 = etCO2;
    }

    public String getDettagli() {
        return Dettagli;
    }

    public void setDettagli(String dettagli) {
        Dettagli = dettagli;
    }

    public String getAzione() {
        return Azione;
    }

    public void setAzione(String azione) {
        Azione = azione;
    }

    public TempoN getPositivo() {
        return Positivo;
    }

    public void setPositivo(TempoN positivo) {
        Positivo = positivo;
    }

    public TempoN getNegativo() {
        return Negativo;
    }

    public void setNegativo(TempoN negativo) {
        Negativo = negativo;
    }
}
