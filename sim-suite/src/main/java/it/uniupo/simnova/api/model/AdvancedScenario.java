package it.uniupo.simnova.api.model;

import java.util.ArrayList;

/**
 * Classe che rappresenta uno scenario avanzato nel sistema.
 * <p>
 * Estende la classe base {@link Scenario} aggiungendo la gestione di più tempi
 * di simulazione. Ogni tempo rappresenta una fase distinta dello scenario con
 * le proprie caratteristiche e parametri.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class AdvancedScenario extends Scenario {
    /**
     * Identificativo specifico per lo scenario avanzato.
     */
    private int id_advanced_scenario;
    /**
     * Lista dei tempi/fasi dello scenario.
     * Ogni tempo rappresenta una fase distinta dello scenario con le proprie
     * caratteristiche e parametri.
     */
    private ArrayList<Tempo> tempi;

    /**
     * Costruttore completo per creare un nuovo scenario avanzato.
     *
     * @param id                   identificativo univoco dello scenario
     * @param titolo               titolo dello scenario
     * @param nome_paziente        nome del paziente nello scenario
     * @param patologia            patologia principale del paziente
     * @param descrizione          descrizione dettagliata dello scenario
     * @param briefing             testo introduttivo per i discenti
     * @param patto_aula           accordi iniziali con la classe
     * @param azione_chiave        azioni principali da valutare
     * @param obiettivo            obiettivi didattici dello scenario
     * @param materiale            materiale necessario per la simulazione
     * @param moulage              trucco/effetti speciali per il paziente
     * @param liquidi              liquidi e presidi disponibili
     * @param timer_generale       durata complessiva dello scenario
     * @param id_advanced_scenario identificativo specifico per scenario avanzato
     * @param tempi                lista dei tempi/fasi dello scenario
     */
    public AdvancedScenario(int id, String titolo, String nome_paziente, String patologia,
                            String descrizione, String briefing, String patto_aula,
                            String azione_chiave, String obiettivo, String materiale,
                            String moulage, String liquidi, float timer_generale,
                            int id_advanced_scenario, ArrayList<Tempo> tempi) {
        super(id, titolo, nome_paziente, patologia, descrizione, briefing, patto_aula,
                azione_chiave, obiettivo, materiale, moulage, liquidi, timer_generale);
        this.id_advanced_scenario = id_advanced_scenario;
        this.tempi = tempi;
    }

    /**
     * Restituisce l'identificativo specifico dello scenario avanzato.
     *
     * @return l'identificativo specifico dello scenario avanzato
     */
    public int getId_advanced_scenario() {
        return id_advanced_scenario;
    }

    /**
     * Imposta l'identificativo specifico dello scenario avanzato.
     *
     * @param id_advanced_scenario il nuovo identificativo
     */
    public void setId_advanced_scenario(int id_advanced_scenario) {
        this.id_advanced_scenario = id_advanced_scenario;
    }

    /**
     * Restituisce la lista dei tempi/fasi dello scenario.
     *
     * @return la lista dei tempi/fasi dello scenario
     */
    public ArrayList<Tempo> getTempi() {
        return tempi;
    }

    /**
     * Imposta la lista dei tempi/fasi dello scenario.
     *
     * @param tempi la nuova lista di tempi
     */
    public void setTempi(ArrayList<Tempo> tempi) {
        this.tempi = tempi;
    }

    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori dei campi specifici dello scenario avanzato
     */
    @Override
    public String toString() {
        return super.toString() + "AdvancedScenario{" +
                "id_advanced_scenario=" + id_advanced_scenario +
                ", tempi=" + tempi +
                '}';
    }
}