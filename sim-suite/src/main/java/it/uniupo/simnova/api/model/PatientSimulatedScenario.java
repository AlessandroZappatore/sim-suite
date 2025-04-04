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
    private int idPatientSimulatedScenario;
    private int advancedScenario;
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
     * @param azione_chiave              l'azione chiave dello scenario
     * @param obiettivo                  l'obiettivo dello scenario
     * @param materiale                  il materiale necessario per lo scenario
     * @param moulage                    il moulage dello scenario
     * @param liquidi                    i liquidi dello scenario
     * @param timer_generale             il timer generale dello scenario
     * @param id_advanced_scenario       l'identificativo dello scenario avanzato associato
     * @param tempi                      la lista dei tempi dello scenario
     * @param idPatientSimulatedScenario l'identificativo univoco dello scenario simulato con paziente
     * @param advancedScenario           l'identificativo dello scenario avanzato associato
     * @param sceneggiatura              la sceneggiatura dello scenario
     */
    public PatientSimulatedScenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String materiale, String moulage, String liquidi, float timer_generale, int id_advanced_scenario, ArrayList<Tempo> tempi, int idPatientSimulatedScenario, int advancedScenario, String sceneggiatura) {
        super(id, titolo, nome_paziente, patologia, descrizione, briefing, patto_aula, azione_chiave, obiettivo, materiale, moulage, liquidi, timer_generale, id_advanced_scenario, tempi);
        this.idPatientSimulatedScenario = idPatientSimulatedScenario;
        this.advancedScenario = advancedScenario;
        this.sceneggiatura = sceneggiatura;
    }

    /**
     * Costruttore vuoto per la deserializzazione.
     */
    public PatientSimulatedScenario() {

    }

    /**
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
        return "PatientSimulatedScenario{" +
                "idPatientSimulatedScenario=" + idPatientSimulatedScenario +
                ", advancedScenario=" + advancedScenario +
                ", sceneggiatura='" + sceneggiatura + '\'' +
                '}';
    }
}