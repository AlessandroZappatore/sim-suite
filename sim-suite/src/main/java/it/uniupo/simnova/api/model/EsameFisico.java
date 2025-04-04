package it.uniupo.simnova.api.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe che rappresenta un esame fisico completo di un paziente.
 * <p>
 * Contiene i risultati di un esame fisico organizzato per sezioni anatomiche.
 * Ogni sezione può contenere una descrizione testuale dei risultati.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class EsameFisico {
    private int idEsameFisico;
    private int scenario;
    private Map<String, String> sections;

    /**
     * Costruttore vuoto che inizializza un esame fisico con tutte le sezioni vuote.
     */
    public EsameFisico() {
        this.sections = new HashMap<>();
        // Inizializza tutte le sezioni con stringhe vuote
        sections.put("Generale", "");
        sections.put("Pupille", "");
        sections.put("Collo", "");
        sections.put("Torace", "");
        sections.put("Cuore", "");
        sections.put("Addome", "");
        sections.put("Retto", "");
        sections.put("Cute", "");
        sections.put("Estremita", "");
        sections.put("Neurologico", "");
        sections.put("FAST", "");
    }

    /**
     * Costruttore completo per creare un esame fisico con tutti i valori.
     *
     * @param idEsameFisico identificativo univoco dell'esame fisico
     * @param scenario      identificativo dello scenario associato
     * @param generale      risultati sezione generale
     * @param pupille       risultati esame pupille
     * @param collo         risultati esame collo
     * @param torace        risultati esame torace
     * @param cuore         risultati esame cuore
     * @param addome        risultati esame addome
     * @param retto         risultati esame rettale (se eseguito)
     * @param cute          risultati esame cute
     * @param estremita     risultati esame estremità
     * @param neurologico   risultati esame neurologico
     * @param fast          risultati FAST exam (Focused Assessment with Sonography for Trauma)
     */
    public EsameFisico(int idEsameFisico, int scenario, String generale, String pupille,
                       String collo, String torace, String cuore, String addome,
                       String retto, String cute, String estremita,
                       String neurologico, String fast) {
        this.idEsameFisico = idEsameFisico;
        this.scenario = scenario;

        this.sections = new HashMap<>();
        sections.put("Generale", generale);
        sections.put("Pupille", pupille);
        sections.put("Collo", collo);
        sections.put("Torace", torace);
        sections.put("Cuore", cuore);
        sections.put("Addome", addome);
        sections.put("Retto", retto);
        sections.put("Cute", cute);
        sections.put("Estremita", estremita);
        sections.put("Neurologico", neurologico);
        sections.put("FAST", fast);
    }

    /**
     * @return l'identificativo univoco dell'esame fisico
     */
    public Integer getIdEsameFisico() {
        return idEsameFisico;
    }

    /**
     * Imposta l'identificativo univoco dell'esame fisico.
     *
     * @param idEsameFisico il nuovo identificativo
     */
    public void setIdEsameFisico(Integer idEsameFisico) {
        this.idEsameFisico = idEsameFisico;
    }

    /**
     * @return la mappa completa di tutte le sezioni dell'esame con i relativi risultati
     */
    public Map<String, String> getSections() {
        return sections;
    }

    /**
     * Imposta una nuova mappa di sezioni per l'esame fisico.
     *
     * @param sections la nuova mappa di sezioni
     */
    public void setSections(Map<String, String> sections) {
        this.sections = sections;
    }

    /**
     * Recupera il risultato di una specifica sezione dell'esame.
     *
     * @param sectionName il nome della sezione da recuperare
     * @return il risultato testuale della sezione, o null se la sezione non esiste
     */
    public String getSection(String sectionName) {
        return sections.get(sectionName);
    }

    /**
     * Imposta il risultato per una specifica sezione dell'esame.
     *
     * @param sectionName il nome della sezione da modificare
     * @param value       il nuovo valore testuale per la sezione
     */
    public void setSection(String sectionName, String value) {
        sections.put(sectionName, value);
    }
}