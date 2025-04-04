package it.uniupo.simnova.api.model;

/**
 * Classe che rappresenta un esame con referto nel sistema.
 * <p>
 * Contiene informazioni su un esame medico, inclusi:
 * - Identificativi dell'esame e dello scenario associato
 * - Tipo di esame
 * - Percorso del file multimediale (se presente)
 * - Referto testuale
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class EsameReferto {
    private int idEsame;
    private int id_scenario;
    private String tipo;
    private String media;
    private String refertoTestuale;

    /**
     * Costruttore completo per creare un nuovo esame con referto.
     *
     * @param idEsame         identificativo univoco dell'esame
     * @param scenario        identificativo dello scenario associato
     * @param tipo            tipologia dell'esame (es. "Radiografia", "Ecografia")
     * @param media           percorso del file multimediale associato (opzionale)
     * @param refertoTestuale contenuto testuale del referto
     */
    public EsameReferto(int idEsame, int scenario, String tipo, String media, String refertoTestuale) {
        this.idEsame = idEsame;
        this.id_scenario = scenario;
        this.tipo = tipo;
        this.media = media;
        this.refertoTestuale = refertoTestuale;
    }

    /**
     * Costruttore vuoto per la deserializzazione.
     */
    public EsameReferto() {
        // Costruttore vuoto per JPA e deserializzazione
    }

    // Metodi getter e setter

    /**
     * @return l'identificativo univoco dell'esame
     */
    public int getIdEsame() {
        return idEsame;
    }

    /**
     * @param idEsame il nuovo identificativo dell'esame
     */
    public void setIdEsame(int idEsame) {
        this.idEsame = idEsame;
    }

    /**
     * @return l'identificativo dello scenario associato
     */
    public int getScenario() {
        return id_scenario;
    }

    /**
     * @param id_scenario il nuovo identificativo dello scenario
     */
    public void setIdScenario(int id_scenario) {
        this.id_scenario = id_scenario;
    }

    /**
     * @return la tipologia dell'esame
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo la nuova tipologia dell'esame
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * @return il percorso del file multimediale associato
     */
    public String getMedia() {
        return media;
    }

    /**
     * @param media il nuovo percorso del file multimediale
     */
    public void setMedia(String media) {
        this.media = media;
    }

    /**
     * @return il contenuto testuale del referto
     */
    public String getRefertoTestuale() {
        return refertoTestuale;
    }

    /**
     * @param refertoTestuale il nuovo contenuto testuale del referto
     */
    public void setRefertoTestuale(String refertoTestuale) {
        this.refertoTestuale = refertoTestuale;
    }

    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
    @Override
    public String toString() {
        return "EsameReferto{" +
                "idEsame=" + idEsame +
                ", scenario=" + id_scenario +
                ", tipo='" + tipo + '\'' +
                ", media='" + media + '\'' +
                ", refertoTestuale='" + refertoTestuale + '\'' +
                '}';
    }
}