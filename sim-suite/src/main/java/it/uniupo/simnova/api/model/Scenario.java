package it.uniupo.simnova.api.model;

/**
 * Classe che rappresenta uno scenario di simulazione.
 * <p>
 * Contiene i dettagli principali dello scenario come titolo, nome del paziente, patologia, descrizione, briefing, patto d'aula, azione chiave, obiettivo, materiale, moulage, liquidi e timer generale.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class Scenario {
    private int id;
    private String titolo;
    private String nome_paziente;
    private String patologia;
    private String descrizione;
    private String briefing;
    private String patto_aula;
    private String azione_chiave;
    private String obiettivo;
    private String materiale;
    private String moulage;
    private String liquidi;
    private float timer_generale;

    /**
     * Costruttore completo per creare un nuovo scenario.
     *
     * @param id             l'identificativo univoco dello scenario
     * @param titolo         il titolo dello scenario
     * @param nome_paziente  il nome del paziente
     * @param patologia      la patologia del paziente
     * @param descrizione    la descrizione dello scenario
     * @param briefing       il briefing dello scenario
     * @param patto_aula     il patto d'aula dello scenario
     * @param azione_chiave  l'azione chiave dello scenario
     * @param obiettivo      l'obiettivo dello scenario
     * @param materiale      il materiale necessario per lo scenario
     * @param moulage        il moulage dello scenario
     * @param liquidi        i liquidi dello scenario
     * @param timer_generale il timer generale dello scenario
     */
    public Scenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String materiale, String moulage, String liquidi, float timer_generale) {
        this.id = id;
        this.titolo = titolo;
        this.nome_paziente = nome_paziente;
        this.patologia = patologia;
        this.descrizione = descrizione;
        this.briefing = briefing;
        this.patto_aula = patto_aula;
        this.azione_chiave = azione_chiave;
        this.obiettivo = obiettivo;
        this.materiale = materiale;
        this.moulage = moulage;
        this.liquidi = liquidi;
        if (timer_generale <= 0) {
            this.timer_generale = 0;
        } else {
            this.timer_generale = timer_generale;
        }
    }

    /**
     * Costruttore parziale per creare uno scenario con i campi principali.
     *
     * @param id             l'identificativo univoco dello scenario
     * @param titolo         il titolo dello scenario
     * @param nome_paziente  il nome del paziente
     * @param patologia      la patologia del paziente
     * @param timer_generale il timer generale dello scenario
     */
    public Scenario(int id, String titolo, String nome_paziente, String patologia, float timer_generale) {
        this.id = id;
        this.titolo = titolo;
        this.nome_paziente = nome_paziente;
        this.patologia = patologia;
        this.timer_generale = timer_generale;
    }

    /**
     * Costruttore vuoto per la deserializzazione.
     */
    public Scenario() {

    }

    /**
     * @return l'identificativo univoco dello scenario
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'identificativo univoco dello scenario.
     *
     * @param id il nuovo identificativo
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return il titolo dello scenario
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Imposta il titolo dello scenario.
     *
     * @param titolo il nuovo titolo
     */
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    /**
     * @return il nome del paziente
     */
    public String getNomePaziente() {
        return nome_paziente;
    }

    /**
     * Imposta il nome del paziente.
     *
     * @param nome_paziente il nuovo nome del paziente
     */
    public void setNomePaziente(String nome_paziente) {
        this.nome_paziente = nome_paziente;
    }

    /**
     * @return la patologia del paziente
     */
    public String getPatologia() {
        return patologia;
    }

    /**
     * Imposta la patologia del paziente.
     *
     * @param patologia la nuova patologia
     */
    public void setPatologia(String patologia) {
        this.patologia = patologia;
    }

    /**
     * @return la descrizione dello scenario
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la descrizione dello scenario.
     *
     * @param descrizione la nuova descrizione
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * @return il briefing dello scenario
     */
    public String getBriefing() {
        return briefing;
    }

    /**
     * Imposta il briefing dello scenario.
     *
     * @param briefing il nuovo briefing
     */
    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }

    /**
     * @return il patto d'aula dello scenario
     */
    public String getPattoAula() {
        return patto_aula;
    }

    /**
     * Imposta il patto d'aula dello scenario.
     *
     * @param patto_aula il nuovo patto d'aula
     */
    public void setPattoAula(String patto_aula) {
        this.patto_aula = patto_aula;
    }

    /**
     * @return l'azione chiave dello scenario
     */
    public String getAzioneChiave() {
        return azione_chiave;
    }

    /**
     * Imposta l'azione chiave dello scenario.
     *
     * @param azione_chiave la nuova azione chiave
     */
    public void setAzioneChiave(String azione_chiave) {
        this.azione_chiave = azione_chiave;
    }

    /**
     * @return l'obiettivo dello scenario
     */
    public String getObiettivo() {
        return obiettivo;
    }

    /**
     * Imposta l'obiettivo dello scenario.
     *
     * @param obiettivo il nuovo obiettivo
     */
    public void setObiettivo(String obiettivo) {
        this.obiettivo = obiettivo;
    }

    /**
     * @return il materiale necessario per lo scenario
     */
    public String getMateriale() {
        return materiale;
    }

    /**
     * Imposta il materiale necessario per lo scenario.
     *
     * @param materiale il nuovo materiale
     */
    public void setMateriale(String materiale) {
        this.materiale = materiale;
    }

    /**
     * @return il moulage dello scenario
     */
    public String getMoulage() {
        return moulage;
    }

    /**
     * Imposta il moulage dello scenario.
     *
     * @param moulage il nuovo moulage
     */
    public void setMoulage(String moulage) {
        this.moulage = moulage;
    }

    /**
     * @return i liquidi dello scenario
     */
    public String getLiquidi() {
        return liquidi;
    }

    /**
     * Imposta i liquidi dello scenario.
     *
     * @param liquidi i nuovi liquidi
     */
    public void setLiquidi(String liquidi) {
        this.liquidi = liquidi;
    }

    /**
     * @return il timer generale dello scenario
     */
    public float getTimerGenerale() {
        return timer_generale;
    }

    /**
     * Imposta il timer generale dello scenario.
     *
     * @param timer_generale il nuovo timer generale
     */
    public void setTimerGenerale(float timer_generale) {
        this.timer_generale = timer_generale;
    }

    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
    @Override
    public String toString() {
        return "Scenario{id=" + id + ", titolo=" + titolo + ", nome_paziente=" + nome_paziente + ", patologia=" + patologia + ", descrizione=" + descrizione + ", briefing=" + briefing + ", patto_aula=" + patto_aula + ", azione_chiave=" + azione_chiave + ", obiettivo=" + obiettivo + ", materiale=" + materiale + ", moulage=" + moulage + ", liquidi=" + liquidi + "}";
    }
}