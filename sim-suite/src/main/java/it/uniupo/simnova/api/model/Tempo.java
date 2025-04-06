package it.uniupo.simnova.api.model;

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
     * Costruttore completo per creare un nuovo tempo.
     *
     * @param idTempo          l'identificativo univoco del tempo
     * @param advancedScenario l'identificativo dello scenario avanzato associato
     * @param PA               la pressione arteriosa del paziente
     * @param FC               la frequenza cardiaca del paziente
     * @param RR               la frequenza respiratoria del paziente
     * @param t                la temperatura del paziente
     * @param spO2             la saturazione di ossigeno del paziente
     * @param etCO2            la pressione parziale di CO2 espirata del paziente
     * @param azione           l'azione associata a questo tempo
     * @param TSi              il tempo di simulazione in secondi
     * @param TNo              il tempo di non simulazione in secondi
     * @param altriDettagli    altri dettagli rilevanti per questo tempo
     * @param timerTempo       il timer del tempo in millisecondi
     */
    public Tempo(int idTempo, int advancedScenario, String PA, Integer FC, Integer RR, double t, Integer spO2, Integer etCO2, String azione, int TSi, int TNo, String altriDettagli, long timerTempo) {
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

    /**
     * Costruttore vuoto per la deserializzazione.
     */
    public Tempo() {
        // Default constructor
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
        if (PA != null && !PA.matches("\\d+/\\d+")) {
            throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica'");
        }
        this.PA = PA;
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
        this.FC = FC;
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
        this.RR = RR;
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
        SpO2 = spO2;
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
        EtCO2 = etCO2;
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
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
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