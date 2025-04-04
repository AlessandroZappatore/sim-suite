package it.uniupo.simnova.api.model;

import java.util.List;

/**
 * Classe che rappresenta i parametri del paziente in T0.
 * <p>
 * Contiene i parametri vitali principali e gli accessi venosi e arteriosi.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class PazienteT0 {
    private int idPaziente;
    private String PA;
    private Integer FC;
    private Integer RR;
    private double T;
    private Integer SpO2;
    private Integer EtCO2;
    private String Monitor;
    private List<Accesso> accessiVenosi;
    private List<Accesso> accessiArteriosi;

    /**
     * Costruttore completo per creare un nuovo paziente T0.
     *
     * @param idPaziente       l'identificativo univoco del paziente
     * @param PA               la pressione arteriosa del paziente
     * @param FC               la frequenza cardiaca del paziente
     * @param RR               la frequenza respiratoria del paziente
     * @param t                la temperatura del paziente
     * @param spO2             la saturazione di ossigeno del paziente
     * @param etCO2            la pressione parziale di CO2 espirata del paziente
     * @param monitor          il monitoraggio del paziente
     * @param accessiVenosi    la lista degli accessi venosi del paziente
     * @param accessiArteriosi la lista degli accessi arteriosi del paziente
     */
    public PazienteT0(int idPaziente, String PA, Integer FC, Integer RR, double t, Integer spO2, Integer etCO2, String monitor, List<Accesso> accessiVenosi, List<Accesso> accessiArteriosi) {
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

    /**
     * @return l'identificativo univoco del paziente
     */
    public int getIdPaziente() {
        return idPaziente;
    }

    /**
     * Imposta l'identificativo univoco del paziente.
     *
     * @param idPaziente il nuovo identificativo
     */
    public void setIdPaziente(int idPaziente) {
        this.idPaziente = idPaziente;
    }

    /**
     * @return la pressione arteriosa del paziente
     */
    public String getPA() {
        return PA;
    }

    /**
     * Imposta la pressione arteriosa del paziente.
     *
     * @param PA la nuova pressione arteriosa
     */
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

    /**
     * @return la frequenza cardiaca del paziente
     */
    public Integer getFC() {
        return FC;
    }

    /**
     * Imposta la frequenza cardiaca del paziente.
     *
     * @param FC la nuova frequenza cardiaca
     */
    public void setFC(Integer FC) {
        this.FC = FC;
    }

    /**
     * @return la frequenza respiratoria del paziente
     */
    public Integer getRR() {
        return RR;
    }

    /**
     * Imposta la frequenza respiratoria del paziente.
     *
     * @param RR la nuova frequenza respiratoria
     */
    public void setRR(Integer RR) {
        this.RR = RR;
    }

    /**
     * @return la temperatura del paziente
     */
    public double getT() {
        return T;
    }

    /**
     * Imposta la temperatura del paziente.
     *
     * @param t la nuova temperatura
     */
    public void setT(double t) {
        T = t;
    }

    /**
     * @return la saturazione di ossigeno del paziente
     */
    public Integer getSpO2() {
        return SpO2;
    }

    /**
     * Imposta la saturazione di ossigeno del paziente.
     *
     * @param spO2 la nuova saturazione di ossigeno
     */
    public void setSpO2(Integer spO2) {
        SpO2 = spO2;
    }

    /**
     * @return la pressione parziale di CO2 espirata del paziente
     */
    public Integer getEtCO2() {
        return EtCO2;
    }

    /**
     * Imposta la pressione parziale di CO2 espirata del paziente.
     *
     * @param etCO2 la nuova pressione parziale di CO2 espirata
     */
    public void setEtCO2(Integer etCO2) {
        EtCO2 = etCO2;
    }

    /**
     * @return il monitoraggio del paziente
     */
    public String getMonitor() {
        return Monitor;
    }

    /**
     * Imposta il monitoraggio del paziente.
     *
     * @param monitor il nuovo monitoraggio
     */
    public void setMonitor(String monitor) {
        Monitor = monitor;
    }

    /**
     * @return la lista degli accessi venosi del paziente
     */
    public List<Accesso> getAccessiVenosi() {
        return accessiVenosi;
    }

    /**
     * Imposta la lista degli accessi venosi del paziente.
     *
     * @param accessiVenosi la nuova lista di accessi venosi
     */
    public void setAccessiVenosi(List<Accesso> accessiVenosi) {
        this.accessiVenosi = accessiVenosi;
    }

    /**
     * @return la lista degli accessi arteriosi del paziente
     */
    public List<Accesso> getAccessiArteriosi() {
        return accessiArteriosi;
    }

    /**
     * Imposta la lista degli accessi arteriosi del paziente.
     *
     * @param accessiArteriosi la nuova lista di accessi arteriosi
     */
    public void setAccessiArteriosi(List<Accesso> accessiArteriosi) {
        this.accessiArteriosi = accessiArteriosi;
    }

    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
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