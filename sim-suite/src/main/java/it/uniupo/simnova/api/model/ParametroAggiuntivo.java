package it.uniupo.simnova.api.model;

/**
 * Classe che rappresenta un parametro aggiuntivo per uno scenario di simulazione.
 * <p>
 * Questa classe modella parametri personalizzati che possono essere aggiunti
 * a specifici tempi di uno scenario per arricchire la simulazione con dati
 * aggiuntivi (es. parametri vitali personalizzati, valori di laboratorio, etc.).
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ParametroAggiuntivo {
    /**
     * Identificativo univoco del parametro.
     */
    private int id;
    /**
     * Identificativo del tempo a cui appartiene il parametro.
     */
    private int tempoId;
    /**
     * Identificativo dello scenario a cui appartiene il parametro.
     */
    private int scenarioId;
    /**
     * Nome del parametro (es. "Pressione venosa centrale").
     */
    private String nome;
    /**
     * Valore del parametro (es. "12").
     */
    private String valore;
    /**
     * Unità di misura del parametro (es. "mmHg").
     */
    private String unitaMisura;

    /**
     * Costruttore completo per creare un nuovo parametro aggiuntivo.
     *
     * @param id          identificativo univoco del parametro
     * @param tempoId     identificativo del tempo a cui appartiene il parametro
     * @param nome        nome del parametro (es. "Pressione venosa centrale")
     * @param valore      valore del parametro (es. "12")
     * @param unitaMisura unità di misura del parametro (es. "mmHg")
     */
    public ParametroAggiuntivo(int id, int tempoId, String nome, String valore, String unitaMisura) {
        this.id = id;
        this.tempoId = tempoId;
        this.nome = nome;
        this.valore = valore;
        this.unitaMisura = unitaMisura;
    }

    /**
     * Costruttore vuoto per la deserializzazione.
     */
    public ParametroAggiuntivo() {
        // Costruttore vuoto per JPA e deserializzazione
    }

    /**
     * Restituisce l'identificativo univoco del parametro.
     *
     * @return l'identificativo univoco del parametro
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'identificativo univoco del parametro.
     *
     * @param id il nuovo identificativo
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce l'identificativo del tempo a cui appartiene il parametro.
     *
     * @return l'identificativo del tempo a cui appartiene il parametro
     */
    public int getTempoId() {
        return tempoId;
    }

    /**
     * Imposta l'identificativo del tempo a cui appartiene il parametro.
     *
     * @param tempoId il nuovo identificativo del tempo
     */
    public void setTempoId(int tempoId) {
        this.tempoId = tempoId;
    }

    /**
     * Restituisce l'identificativo dello scenario a cui appartiene il parametro.
     *
     * @return l'identificativo dello scenario a cui appartiene il parametro
     */
    public int getScenarioId() {
        return scenarioId;
    }

    /**
     * Imposta l'identificativo dello scenario a cui appartiene il parametro.
     *
     * @param scenarioId il nuovo identificativo dello scenario
     */
    public void setScenarioId(int scenarioId) {
        this.scenarioId = scenarioId;
    }

    /**
     * Restituisce il nome del parametro.
     *
     * @return il nome del parametro
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome del parametro.
     *
     * @param nome il nuovo nome del parametro
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce il valore del parametro.
     *
     * @return il valore del parametro
     */
    public String getValore() {
        return valore;
    }

    /**
     * Imposta il valore del parametro.
     *
     * @param valore il nuovo valore del parametro
     */
    public void setValore(String valore) {
        this.valore = valore;
    }

    /**
     * Restituisce l'unità di misura del parametro.
     *
     * @return l'unità di misura del parametro
     */
    public String getUnitaMisura() {
        return unitaMisura;
    }

    /**
     * Imposta l'unità di misura del parametro.
     *
     * @param unitaMisura la nuova unità di misura
     */
    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }

    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
    @Override
    public String toString() {
        return "ParametroAggiuntivo [id=" + id + ", tempoId=" + tempoId + ", nome=" + nome + ", valore=" + valore
                + ", unitaMisura=" + unitaMisura + "]";
    }
}