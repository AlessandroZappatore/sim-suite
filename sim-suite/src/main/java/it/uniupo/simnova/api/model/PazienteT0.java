package it.uniupo.simnova.api.model;

import java.util.List;

public class PazienteT0 {
    private int idPaziente;
    private String PA;
    private Integer FC;
    private Integer RR;
    private Float T;
    private Integer SpO2;
    private Integer EtCO2;
    private String Monitor;
    private List<Accesso> accessiVenosi;
    private List<Accesso> accessiArteriosi;


    public PazienteT0(int idPaziente, String PA, Integer FC, Integer RR, Float t, Integer spO2, Integer etCO2, String monitor, List<Accesso> accessiVenosi, List<Accesso> accessiArteriosi) {
        this.idPaziente = idPaziente;
        if (PA != null) {
            String trimmedPA = PA.trim();
            if (!trimmedPA.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
                throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80')");
            }
            this.PA = trimmedPA;
        } else {
            this.PA = null;
        }
        this.FC = FC;
        this.RR = RR;
        T = t;
        SpO2 = spO2;
        EtCO2 = etCO2;
        Monitor = monitor;
        this.accessiVenosi = accessiVenosi;
        this.accessiArteriosi = accessiArteriosi;
    }

    public int getIdPaziente() {
        return idPaziente;
    }

    public void setIdPaziente(int idPaziente) {
        this.idPaziente = idPaziente;
    }

    public String getPA() {
        return PA;
    }

    public void setPA(String PA) {
        if (PA != null) {
            String trimmedPA = PA.trim();
            if (!trimmedPA.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
                throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80')");
            }
            this.PA = trimmedPA;
        } else {
            this.PA = null;
        }
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

    public String getMonitor() {
        return Monitor;
    }

    public void setMonitor(String monitor) {
        Monitor = monitor;
    }

    public List<Accesso> getAccessiVenosi() {
        return accessiVenosi;
    }

    public void setAccessiVenosi(List<Accesso> accessiVenosi) {
        this.accessiVenosi = accessiVenosi;
    }

    public List<Accesso> getAccessiArteriosi() {
        return accessiArteriosi;
    }

    public void setAccessiArteriosi(List<Accesso> accessiArteriosi) {
        this.accessiArteriosi = accessiArteriosi;
    }

    @Override
    public String toString() {
        return "PazienteT0{" +
                "idPaziente=" + idPaziente +
                ", PA=" + PA +
                ", FC=" + FC +
                ", RR=" + RR +
                ", T=" + T +
                ", SpO2=" + SpO2 +
                ", EtCO2=" + EtCO2 +
                ", Monitor='" + Monitor + '\'' +
                ", accessiVenosi=" + accessiVenosi +
                ", accessiArteriosi=" + accessiArteriosi +
                '}';
    }
}
