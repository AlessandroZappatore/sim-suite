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
    /**
     * Identificativo univoco del paziente.
     */
    private int idPaziente;
    /**
     * Pressione arteriosa del paziente.
     */
    private String PA;
    /**
     * Frequenza cardiaca del paziente.
     */
    private Integer FC;
    /**
     * Frequenza respiratoria del paziente.
     */
    private Integer RR;
    /**
     * Temperatura del paziente.
     */
    private double T;
    /**
     * Saturazione di ossigeno del paziente.
     */
    private Integer SpO2;
    /**
     * Percentuale di ossigeno somministrato al paziente.
     */
    private Integer FiO2;
    /**
     * Litri di ossigeno somministrati al paziente.
     */
    private Float LitriO2;
    /**
     * Pressione parziale di CO2 espirata del paziente.
     */
    private Integer EtCO2;
    /**
     * Monitoraggio del paziente.
     */
    private String Monitor;
    /**
     * Lista degli accessi venosi del paziente.
     */
    private List<Accesso> accessiVenosi;
    /**
     * Lista degli accessi arteriosi del paziente.
     */
    private List<Accesso> accessiArteriosi;
    /**
     * Presidi al T0.
     */
    private String presidi;


    public PazienteT0(int idPaziente, String PA, Integer FC, Integer RR, double t, Integer spO2, Integer fiO2, Float litriO2, Integer etCO2,
                      String monitor, List<Accesso> accessiVenosi, List<Accesso> accessiArteriosi, String presidi) {
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

        if (FC < 0) {
            throw new IllegalArgumentException("FC non può essere negativa");
        } else this.FC = FC;

        if (RR < 0) {
            throw new IllegalArgumentException("RR non può essere negativa");
        } else this.RR = RR;

        this.T = t;

        if (spO2 < 0 || spO2 > 100) {
            throw new IllegalArgumentException("SpO2 deve essere compresa tra 0 e 100");
        } else SpO2 = spO2;

        if (fiO2 < 0 || fiO2 > 100) {
            throw new IllegalArgumentException("FiO2 deve essere compresa tra 0 e 100");
        } else FiO2 = fiO2;

        if (litriO2 < 0) {
            throw new IllegalArgumentException("LitriO2 non può essere negativo");
        } else LitriO2 = litriO2;

        if (etCO2 < 0) {
            throw new IllegalArgumentException("EtCO2 non può essere negativa");
        } else EtCO2 = etCO2;

        Monitor = monitor;
        this.accessiVenosi = accessiVenosi;
        this.accessiArteriosi = accessiArteriosi;
        this.presidi = presidi;
    }

    /**
     * Restituisce l'identificativo univoco del paziente.
     *
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
     * Restituisce la pressione arteriosa del paziente.
     *
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
     * Restituisce la frequenza cardiaca del paziente.
     *
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
        if (FC < 0) {
            throw new IllegalArgumentException("FC non può essere negativa");
        } else this.FC = FC;
    }

    /**
     * Restituisce la frequenza respiratoria del paziente.
     *
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
        if (RR < 0) {
            throw new IllegalArgumentException("RR non può essere negativa");
        } else this.RR = RR;
    }

    /**
     * Restituisce la temperatura del paziente.
     *
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
     * Restituisce la saturazione di ossigeno del paziente.
     *
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
        if (spO2 < 0 || spO2 > 100) {
            throw new IllegalArgumentException("SpO2 deve essere compresa tra 0 e 100");
        } else SpO2 = spO2;
    }

    /**
     * Restituisce la pressione parziale di CO2 espirata del paziente.
     *
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
        if (etCO2 < 0) {
            throw new IllegalArgumentException("EtCO2 non può essere negativa");
        } else EtCO2 = etCO2;
    }

    /**
     * Restituisce il monitoraggio del paziente.
     *
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
     * Restituisce la lista degli accessi venosi del paziente.
     *
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
     * Restituisce la lista degli accessi arteriosi del paziente.
     *
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

    public String getPresidi() {
        return presidi;
    }

    public void setPresidi(String presidi) {
        this.presidi = presidi;
    }

    public Integer getFiO2() {
        return FiO2;
    }

    public void setFiO2(Integer fiO2) {
        if (fiO2 < 0 || fiO2 > 100) {
            throw new IllegalArgumentException("FiO2 deve essere compresa tra 0 e 100");
        } else FiO2 = fiO2;
    }

    public Float getLitriO2() {
        return LitriO2;
    }

    public void setLitriO2(Float litriO2) {
        if (litriO2 < 0) {
            throw new IllegalArgumentException("LitriO2 non può essere negativo");
        } else LitriO2 = litriO2;
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
                ", PA='" + PA + '\'' +
                ", FC=" + FC +
                ", RR=" + RR +
                ", T=" + T +
                ", SpO2=" + SpO2 +
                ", FiO2=" + FiO2 +
                ", LitriO2=" + LitriO2 +
                ", EtCO2=" + EtCO2 +
                ", Monitor='" + Monitor + '\'' +
                ", accessiVenosi=" + accessiVenosi +
                ", accessiArteriosi=" + accessiArteriosi +
                ", presidi='" + presidi + '\'' +
                '}';
    }
}