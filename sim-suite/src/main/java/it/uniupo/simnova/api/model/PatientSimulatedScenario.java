package it.uniupo.simnova.api.model;

import java.util.ArrayList;

/**
 * Classe che rappresenta uno scenario simulato con paziente.
 * <p>
 * Estende la classe {@link AdvancedScenario} aggiungendo informazioni specifiche
 * per gli scenari simulati con paziente, come la sceneggiatura.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class PatientSimulatedScenario extends AdvancedScenario {
    /**
     * Identificativo univoco dello scenario simulato con paziente.
     */
    private int idPatientSimulatedScenario;
    /**
     * Identificativo dello scenario avanzato associato.
     */
    private int advancedScenario;
    /**
     * Sceneggiatura dello scenario simulato con paziente.
     */
    private String sceneggiatura;

    /**
     * Costruttore completo per creare un nuovo scenario simulato con paziente.
     *
     * @param id                         l'identificativo univoco dello scenario
     * @param titolo                     il titolo dello scenario
     * @param nome_paziente              il nome del paziente
     * @param patologia                  la patologia del paziente
     * @param descrizione                la descrizione dello scenario
     * @param briefing                   il briefing dello scenario
     * @param patto_aula                 il patto d'aula dello scenario
     * @param obiettivo                  l'obiettivo dello scenario
     * @param moulage                    il moulage dello scenario
     * @param liquidi                    i liquidi dello scenario
     * @param timer_generale             il timer generale dello scenario
     * @param id_advanced_scenario       l'identificativo dello scenario avanzato associato
     * @param tempi                      la lista dei tempi dello scenario
     * @param idPatientSimulatedScenario l'identificativo univoco dello scenario simulato con paziente
     * @param advancedScenario           l'identificativo dello scenario avanzato associato
     * @param sceneggiatura              la sceneggiatura dello scenario
     */
    public PatientSimulatedScenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String obiettivo, String moulage, String liquidi, float timer_generale, String autori, String tipologia, String target, String infoGenitore, int id_advanced_scenario, ArrayList<Tempo> tempi, int idPatientSimulatedScenario, int advancedScenario, String sceneggiatura) {
        super(id, titolo, nome_paziente, patologia, descrizione, briefing, patto_aula, obiettivo, moulage, liquidi, timer_generale, id_advanced_scenario, tempi,  autori, tipologia, infoGenitore, target);
        this.idPatientSimulatedScenario = idPatientSimulatedScenario;
        this.advancedScenario = advancedScenario;
        this.sceneggiatura = sceneggiatura;
    }

    /**
     * Restituisce l'identificativo univoco dello scenario simulato con paziente.
     *
     * @return l'identificativo univoco dello scenario simulato con paziente
     */
    public int getIdPatientSimulatedScenario() {
        return idPatientSimulatedScenario;
    }

    /**
     * Imposta l'identificativo univoco dello scenario simulato con paziente.
     *
     * @param idPatientSimulatedScenario il nuovo identificativo
     */
    public void setIdPatientSimulatedScenario(int idPatientSimulatedScenario) {
        this.idPatientSimulatedScenario = idPatientSimulatedScenario;
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
     * Restituisce la sceneggiatura dello scenario.
     *
     * @return la sceneggiatura dello scenario
     */
    public String getSceneggiatura() {
        return sceneggiatura;
    }

    /**
     * Imposta la sceneggiatura dello scenario.
     *
     * @param sceneggiatura la nuova sceneggiatura
     */
    public void setSceneggiatura(String sceneggiatura) {
        this.sceneggiatura = sceneggiatura;
    }

    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
    @Override
    public String toString() {
        return super.toString() + "PatientSimulatedScenario{" +
                "idPatientSimulatedScenario=" + idPatientSimulatedScenario +
                ", advancedScenario=" + advancedScenario +
                ", sceneggiatura='" + sceneggiatura + '\'' +
                '}';
    }
}