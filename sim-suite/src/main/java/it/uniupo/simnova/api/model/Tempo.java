package it.uniupo.simnova.api.model;

import java.util.List;


/**
 * Classe che rappresenta un tempo in uno scenario avanzato.
 * <p>
 * Contiene i parametri vitali del paziente e altre informazioni rilevanti per un determinato tempo.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class Tempo {
    /**
     * Identificativo univoco del tempo.
     */
    private int idTempo;
    /**
     * Identificativo dello scenario avanzato associato.
     */
    private int advancedScenario;
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

    private Integer FiO2;

    private Float LitriO2;
    /**
     * Pressione parziale di CO2 espirata del paziente.
     */
    private Integer EtCO2;
    /**
     * Azione associata a questo tempo.
     */
    private String Azione;
    /**
     * Tempo di simulazione in secondi.
     */
    private int TSi;
    /**
     * Tempo di non simulazione in secondi.
     */
    private int TNo;
    /**
     * Altri dettagli rilevanti per questo tempo.
     */
    private String altriDettagli;
    /**
     * Timer del tempo in millisecondi.
     */
    private long timerTempo;
    /**
     * Lista dei parametri aggiuntivi associati a questo tempo.
     */
    private List<ParametroAggiuntivo> parametriAggiuntivi;

    private String ruoloGenitore;


    public Tempo(int idTempo, int advancedScenario, String PA, Integer FC, Integer RR, double t, Integer spO2, Integer fiO2, Float litriO2, Integer etCO2, String azione, int TSi, int TNo, String altriDettagli, long timerTempo, String ruoloGenitore) {
        this.idTempo = idTempo;
        this.advancedScenario = advancedScenario;

        if (PA != null) {
            String trimmedPA = PA.trim();
            if (!trimmedPA.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
                throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80')");
            }
            this.PA = trimmedPA;
        } else {
            this.PA = null;
        }

        if (FC != null && FC < 0) {
            throw new IllegalArgumentException("FC non può essere negativa");
        } else this.FC = FC;

        if (RR != null && RR < 0) {
            throw new IllegalArgumentException("RR non può essere negativa");
        } else this.RR = RR;

        this.T = t;

        if (spO2 != null && (spO2 < 0 || spO2 > 100)) {
            throw new IllegalArgumentException("SpO2 deve essere compresa tra 0 e 100");
        } else SpO2 = spO2;

        if (fiO2 != null && (fiO2 < 0 || fiO2 > 100)) {
            throw new IllegalArgumentException("FiO2 deve essere compresa tra 0 e 100");
        } else FiO2 = fiO2;

        if (litriO2 != null && litriO2 < 0) {
            throw new IllegalArgumentException("LitriO2 non può essere negativo");
        } else LitriO2 = litriO2;

        if (etCO2 != null && etCO2 < 0) {
            throw new IllegalArgumentException("EtCO2 non può essere negativa");
        } else EtCO2 = etCO2;

        Azione = azione;
        this.TSi = TSi;
        this.TNo = TNo;
        this.altriDettagli = altriDettagli;
        if(timerTempo < 0){
            throw new IllegalArgumentException("Il timer non può essere negativo");
        } else this.timerTempo = timerTempo;
        this.ruoloGenitore = ruoloGenitore;
    }

    /**
     * Restituisce l'identificativo univoco del tempo.
     *
     * @return l'identificativo univoco del tempo
     */
    public int getIdTempo() {
        return idTempo;
    }

    /**
     * Imposta l'identificativo univoco del tempo.
     *
     * @param idTempo il nuovo identificativo
     */
    public void setIdTempo(int idTempo) {
        this.idTempo = idTempo;
    }

    /**
     * Restituisce l'identificativo dello scenario avanzato associato.
     *
     * @return l'identificativo dello scenario avanzato associato
     */
    public int getAdvancedScenario() {
        return advancedScenario;
    }

    /**
     * Imposta l'identificativo dello scenario avanzato associato.
     *
     * @param advancedScenario il nuovo identificativo
     */
    public void setAdvancedScenario(int advancedScenario) {
        this.advancedScenario = advancedScenario;
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
        if (FC != null && FC < 0) {
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
        if (RR != null && RR < 0) {
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
        if (spO2 != null && (spO2 < 0 || spO2 > 100)) {
            throw new IllegalArgumentException("SpO2 deve essere compresa tra 0 e 100");
        } else SpO2 = spO2;
    }

    public Integer getFiO2() {
        return FiO2;
    }

    public Float getLitriO2() {
        return LitriO2;
    }

    public void setFiO2(Integer fiO2) {
        if (fiO2 != null && (fiO2 < 0 || fiO2 > 100)) {
            throw new IllegalArgumentException("FiO2 deve essere compresa tra 0 e 100");
        } else FiO2 = fiO2;
    }

    public void setLitriO2(Float litriO2) {
        if (litriO2 != null && litriO2 < 0) {
            throw new IllegalArgumentException("LitriO2 non può essere negativo");
        } else LitriO2 = litriO2;
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
        if (etCO2 != null && etCO2 < 0) {
            throw new IllegalArgumentException("EtCO2 non può essere negativa");
        } else EtCO2 = etCO2;
    }

    /**
     * Restituisce l'azione associata a questo tempo.
     *
     * @return l'azione associata a questo tempo
     */
    public String getAzione() {
        return Azione;
    }

    /**
     * Imposta l'azione associata a questo tempo.
     *
     * @param azione la nuova azione
     */
    public void setAzione(String azione) {
        Azione = azione;
    }

    /**
     * Restituisce il tempo di simulazione in secondi.
     *
     * @return il tempo di simulazione in secondi
     */
    public int getTSi() {
        return TSi;
    }

    /**
     * Imposta il tempo di simulazione in secondi.
     *
     * @param TSi il nuovo tempo di simulazione
     */
    public void setTSi(int TSi) {
        this.TSi = TSi;
    }

    /**
     * Restituisce il tempo di non simulazione in secondi.
     *
     * @return il tempo di non simulazione in secondi
     */
    public int getTNo() {
        return TNo;
    }

    /**
     * Imposta il tempo di non simulazione in secondi.
     *
     * @param TNo il nuovo tempo di non simulazione
     */
    public void setTNo(int TNo) {
        this.TNo = TNo;
    }

    /**
     * Restituisce altri dettagli rilevanti per questo tempo.
     *
     * @return altri dettagli rilevanti per questo tempo
     */
    public String getAltriDettagli() {
        return altriDettagli;
    }

    /**
     * Imposta altri dettagli rilevanti per questo tempo.
     *
     * @param altriDettagli i nuovi dettagli
     */
    public void setAltriDettagli(String altriDettagli) {
        this.altriDettagli = altriDettagli;
    }

    /**
     * Restituisce il timer del tempo in millisecondi.
     *
     * @return il timer del tempo in millisecondi
     */
    public long getTimerTempo() {
        return timerTempo;
    }

    /**
     * Imposta il timer del tempo in millisecondi.
     *
     * @param timerTempo il nuovo timer
     */
    public void setTimerTempo(long timerTempo) {
        this.timerTempo = timerTempo;
    }

    /**
     * Restituisce la lista dei parametri aggiuntivi associati a questo tempo.
     *
     * @return la lista dei parametri aggiuntivi
     */
    public List<ParametroAggiuntivo> getParametriAggiuntivi() {
        return parametriAggiuntivi;
    }

    /**
     * Imposta la lista dei parametri aggiuntivi associati a questo tempo.
     *
     * @param parametriAggiuntivi la nuova lista di parametri aggiuntivi
     */
    public void setParametriAggiuntivi(List<ParametroAggiuntivo> parametriAggiuntivi) {
        this.parametriAggiuntivi = parametriAggiuntivi;
    }

    public String getRuoloGenitore() {
        return ruoloGenitore;
    }

    public void setRuoloGenitore(String ruoloGenitore) {
        this.ruoloGenitore = ruoloGenitore;
    }

    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
    @Override
    public String toString() {
        return "Tempo{" +
                "idTempo=" + idTempo +
                ", advancedScenario=" + advancedScenario +
                ", PA='" + PA + '\'' +
                ", FC=" + FC +
                ", RR=" + RR +
                ", T=" + T +
                ", SpO2=" + SpO2 +
                ", FiO2=" + FiO2 +
                ", LitriO2=" + LitriO2 +
                ", EtCO2=" + EtCO2 +
                ", Azione='" + Azione + '\'' +
                ", TSi=" + TSi +
                ", TNo=" + TNo +
                ", altriDettagli='" + altriDettagli + '\'' +
                ", timerTempo=" + timerTempo +
                ", parametriAggiuntivi=" + parametriAggiuntivi +
                '}';
    }
}
