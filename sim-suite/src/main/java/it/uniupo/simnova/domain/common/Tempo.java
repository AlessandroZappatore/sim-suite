package it.uniupo.simnova.domain.common;

import lombok.Data;

import java.util.List;


/**
 * Classe che rappresenta un <strong>tempo</strong> specifico all'interno di uno scenario di simulazione avanzato.
 * Contiene i parametri vitali del paziente e altre informazioni rilevanti che definiscono lo stato
 * della simulazione in quel preciso istante o fase.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Data
public class Tempo {
    /**
     * Identificativo univoco del tempo, assegnato dal database.
     */
    private final int idTempo;
    /**
     * <strong>Identificativo</strong> dello scenario avanzato associato.
     */
    private final int advancedScenario;
    /**
     * Frequenza respiratoria del paziente (atti/min).
     */
    private final Integer RR;
    /**
     * Saturazione di ossigeno del paziente (%).
     */
    private final Integer SpO2;
    /**
     * Frazione di ossigeno inspirato dal paziente (%).
     */
    private final Integer FiO2;
    /**
     * Litri di ossigeno somministrati al paziente (L/min).
     */
    private final Float LitriO2;
    /**
     * Pressione parziale di CO2 espirata del paziente (mmHg).
     */
    private final Integer EtCO2;
    /**
     * ID del tempo a cui andare se l'azione viene eseguita (tempo "se SI").
     */
    private final int TSi;
    /**
     * ID del tempo a cui andare se l'azione non viene eseguita (tempo "se NO").
     */
    private final int TNo;
    /**
     * Altri dettagli o note rilevanti per questo tempo.
     */
    private final String altriDettagli;
    /**
     * Durata del timer associato a questo tempo, in secondi.
     */
    private final long timerTempo;
    /**
     * Ruolo del genitore associato a questo tempo, se lo scenario è pediatrico.
     * Può essere {@code null} se non applicabile o non definito.
     */
    private final String ruoloGenitore;
    /**
     * Pressione arteriosa del paziente nel formato "sistolica/diastolica" (es. "120/80").
     */
    private String PA;
    /**
     * Frequenza cardiaca del paziente (bpm).
     */
    private Integer FC;
    /**
     * Temperatura del paziente (°C).
     */
    private double T;
    /**
     * Azione o evento associato a questo tempo.
     */
    private String Azione;
    /**
     * Lista dei parametri aggiuntivi associati a questo tempo.
     * Questi parametri possono essere utilizzati per estendere le informazioni
     * disponibili per questo tempo, come ad esempio la pressione venosa centrale,
     * la glicemia, ecc.
     */
    private List<ParametroAggiuntivo> parametriAggiuntivi;

    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>Tempo</code></strong> in uno scenario avanzato.
     * Include la validazione di alcuni parametri per garantire la coerenza dei dati.
     *
     * @param idTempo          <strong>Identificativo univoco</strong> del tempo.
     * @param advancedScenario <strong>Identificativo</strong> dello scenario avanzato associato.
     * @param PA               Pressione arteriosa del paziente nel formato "sistolica/diastolica" (es. "120/80").
     * @param FC               Frequenza cardiaca del paziente. Deve essere un valore non negativo.
     * @param RR               Frequenza respiratoria del paziente. Deve essere un valore non negativo.
     * @param t                Temperatura del paziente.
     * @param spO2             Saturazione di ossigeno del paziente. Deve essere compresa tra 0 e 100.
     * @param fiO2             Frazione di ossigeno inspirato dal paziente. Deve essere compresa tra 0 e 100.
     * @param litriO2          Litri di ossigeno somministrati al paziente. Deve essere un valore non negativo.
     * @param etCO2            Pressione parziale di CO2 espirata del paziente. Deve essere un valore non negativo.
     * @param azione           Azione o evento associato a questo tempo.
     * @param TSi              ID del tempo successivo se l'azione viene eseguita (tempo "se SI").
     * @param TNo              ID del tempo successivo se l'azione NON viene eseguita (tempo "se NO").
     * @param altriDettagli    Altri dettagli rilevanti per questo tempo.
     * @param timerTempo       Durata del timer associato a questo tempo, in secondi. Deve essere un valore non negativo.
     * @param ruoloGenitore    Ruolo del genitore associato a questo tempo, se lo scenario è pediatrico.
     * @throws IllegalArgumentException se i valori di PA, FC, RR, SpO2, FiO2, LitriO2, EtCO2 o timerTempo non rispettano i criteri di validazione.
     */
    public Tempo(int idTempo, int advancedScenario, String PA, Integer FC, Integer RR, double t, Integer spO2, Integer fiO2, Float litriO2, Integer etCO2, String azione, int TSi, int TNo, String altriDettagli, long timerTempo, String ruoloGenitore) {
        this.idTempo = idTempo;
        this.advancedScenario = advancedScenario;

        // Validazione e impostazione PA
        if (PA != null) {
            String trimmedPA = PA.trim();
            if (!trimmedPA.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
                throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80')");
            }
            this.PA = trimmedPA;
        } else {
            this.PA = null;
        }

        // Validazione e impostazione FC
        if (FC != null && FC < 0) {
            throw new IllegalArgumentException("FC non può essere negativa.");
        } else {
            this.FC = FC;
        }

        // Validazione e impostazione RR
        if (RR != null && RR < 0) {
            throw new IllegalArgumentException("RR non può essere negativa.");
        } else {
            this.RR = RR;
        }

        this.T = t;

        // Validazione e impostazione SpO2
        if (spO2 != null && (spO2 < 0 || spO2 > 100)) {
            throw new IllegalArgumentException("SpO2 deve essere compresa tra 0 e 100.");
        } else {
            SpO2 = spO2;
        }

        // Validazione e impostazione FiO2
        if (fiO2 != null && (fiO2 < 0 || fiO2 > 100)) {
            throw new IllegalArgumentException("FiO2 deve essere compresa tra 0 e 100.");
        } else {
            FiO2 = fiO2;
        }

        // Validazione e impostazione LitriO2
        if (litriO2 != null && litriO2 < 0) {
            throw new IllegalArgumentException("LitriO2 non può essere negativo.");
        } else {
            LitriO2 = litriO2;
        }

        // Validazione e impostazione EtCO2
        if (etCO2 != null && etCO2 < 0) {
            throw new IllegalArgumentException("EtCO2 non può essere negativa.");
        } else {
            EtCO2 = etCO2;
        }

        Azione = azione;
        this.TSi = TSi;
        this.TNo = TNo;
        this.altriDettagli = altriDettagli;

        // Validazione e impostazione timerTempo
        if (timerTempo < 0) {
            throw new IllegalArgumentException("Il timer non può essere negativo.");
        } else {
            this.timerTempo = timerTempo;
        }
        this.ruoloGenitore = ruoloGenitore;
    }

    /**
     * Imposta la <strong>pressione arteriosa</strong> del paziente.
     *
     * @param PA La nuova pressione arteriosa nel formato "sistolica/diastolica".
     * @throws IllegalArgumentException se il formato PA non è valido.
     */
    public void setPA(String PA) {
        if (PA != null) {
            String trimmedPA = PA.trim();
            if (!trimmedPA.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
                throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80').");
            }
            this.PA = trimmedPA;
        } else {
            this.PA = null;
        }
    }

    /**
     * Imposta la <strong>frequenza cardiaca</strong> del paziente.
     *
     * @param FC La nuova frequenza cardiaca.
     * @throws IllegalArgumentException se FC è negativo.
     */
    public void setFC(Integer FC) {
        if (FC != null && FC < 0) {
            throw new IllegalArgumentException("FC non può essere negativa.");
        } else {
            this.FC = FC;
        }
    }
    /**
     * Imposta la <strong>frequenza respiratoria</strong> del paziente.
     *
     * @param RR La nuova frequenza respiratoria.
     * @throws IllegalArgumentException se RR è negativo.
     */
    public void setRR(Integer RR) {
        if (RR != null && RR < 0) {
            throw new IllegalArgumentException("RR non può essere negativa.");
        }
    }

    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto <strong><code>Tempo</code></strong>,
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID del tempo, lo scenario, i parametri vitali,
     * l'azione, le transizioni, altri dettagli e il timer.
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
                ", ruoloGenitore='" + ruoloGenitore + '\'' +
                '}';
    }
}