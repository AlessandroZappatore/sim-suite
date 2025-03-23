package it.uniupo.simnova.simcreation;

import java.util.ArrayList;

public class PazienteT0 {
    private int id;
    private int PA;
    private int FC;
    private int RR;
    private float T;
    private int SpO2;
    private int EtCO2;
    private String Monitor;
    private boolean accesso_venoso;
    private boolean accesso_arterioso;
    private ArrayList<Accesso> accessi_venosi;
    private ArrayList<Accesso> accessi_arteriosi;

    public PazienteT0(int id, int PA, int FC, int RR, float T, int SpO2, int EtCO2, String Monitor, boolean accesso_venoso, boolean accesso_arterioso, ArrayList<Accesso> accessi_venosi, ArrayList<Accesso> accessi_arteriosi) {
        this.id = id;
        this.PA = PA;
        this.FC = FC;
        this.RR = RR;
        this.T = T;
        this.SpO2 = SpO2;
        this.EtCO2 = EtCO2;
        this.Monitor = Monitor;
        this.accesso_venoso = accesso_venoso;
        this.accesso_arterioso = accesso_arterioso;
        this.accessi_venosi = accessi_venosi;
        this.accessi_arteriosi = accessi_arteriosi;
    }

    public int getId() {
        return id;
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

    public String getMonitor() {
        return Monitor;
    }

    public boolean getAccessoVenoso() {
        return accesso_venoso;
    }

    public boolean getAccessoArterioso() {
        return accesso_arterioso;
    }

    public ArrayList<Accesso> getAccessiVenosi() {
        return accessi_venosi;
    }

    public ArrayList<Accesso> getAccessiArteriosi() {
        return accessi_arteriosi;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setMonitor(String Monitor) {
        this.Monitor = Monitor;
    }

    public void setAccessoVenoso(boolean accesso_venoso) {
        this.accesso_venoso = accesso_venoso;
    }

    public void setAccessoArterioso(boolean accesso_arterioso) {
        this.accesso_arterioso = accesso_arterioso;
    }

    public void setAccessiVenosi(ArrayList<Accesso> accessi_venosi) {
        this.accessi_venosi = accessi_venosi;
    }

    public void setAccessiArteriosi(ArrayList<Accesso> accessi_arteriosi) {
        this.accessi_arteriosi = accessi_arteriosi;
    }

    @Override
    public String toString() {
        return "PazienteT0 [id=" + id + ", PA=" + PA + ", FC=" + FC + ", RR=" + RR + ", T=" + T + ", SpO2=" + SpO2 + ", EtCO2=" + EtCO2 + ", Monitor=" + Monitor + ", accesso_venoso=" + accesso_venoso + ", accesso_arterioso=" + accesso_arterioso + ", accessi_venosi=" + accessi_venosi + ", accessi_arteriosi=" + accessi_arteriosi + "]";
    }
}
