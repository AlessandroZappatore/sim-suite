package it.uniupo.simnova.api.model;

/**
 * Classe che rappresenta un accesso venoso nel sistema.
 * <p>
 * Contiene informazioni su un accesso venoso, inclusi:
 * - Identificativo univoco
 * - Tipologia dell'accesso
 * - Posizione anatomica
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class Accesso {
    /**
     * Identificativo univoco dell'accesso venoso.
     */
    private int idAccesso;
    /**
     * Tipologia dell'accesso venoso (es. CVC, Agocannula).
     */
    private String tipologia;
    /**
     * Posizione anatomica dell'accesso venoso (es. Giugulare destra, Cubitale sinistro).
     */
    private String posizione;

    private String lato;

    private Integer misura;
    /**
     * Costruttore per creare un nuovo accesso venoso.
     *
     * @param idAccesso identificativo univoco dell'accesso
     * @param tipologia tipologia dell'accesso (es. "CVC", "Agocannula")
     * @param posizione posizione anatomica (es. "Giugulare destra", "Cubitale sinistro")
     */
    public Accesso(int idAccesso, String tipologia, String posizione, String lato, Integer misura) {
        this.idAccesso = idAccesso;
        this.tipologia = tipologia;
        this.posizione = posizione;
        this.lato = lato;
        this.misura = misura;
    }

    /**
     * Restituisce l'identificativo univoco dell'accesso.
     *
     * @return l'identificativo univoco dell'accesso
     */
    public int getId() {
        return idAccesso;
    }

    /**
     * Restituisce la tipologia dell'accesso.
     *
     * @return la tipologia dell'accesso
     */
    public String getTipologia() {
        return tipologia;
    }

    /**
     * Restituisce la posizione anatomica dell'accesso.
     *
     * @return la posizione anatomica dell'accesso
     */
    public String getPosizione() {
        return posizione;
    }

    /**
     * Imposta un nuovo identificativo per l'accesso.
     *
     * @param idAccesso il nuovo identificativo
     */
    public void setId(int idAccesso) {
        this.idAccesso = idAccesso;
    }

    /**
     * Imposta una nuova tipologia per l'accesso.
     *
     * @param tipologia la nuova tipologia
     */
    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    /**
     * Imposta una nuova posizione per l'accesso.
     *
     * @param posizione la nuova posizione anatomica
     */
    public void setPosizione(String posizione) {
        this.posizione = posizione;
    }

    public String getLato() {
        return lato;
    }

    public void setLato(String lato) {
        this.lato = lato;
    }

    public Integer getMisura() {
        return misura;
    }

    public void setMisura(Integer misura) {
        this.misura = misura;
    }

    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
    @Override
    public String toString() {
        return "Accesso{" +
                "idAccesso=" + idAccesso +
                ", tipologia='" + tipologia + '\'' +
                ", posizione='" + posizione + '\'' +
                ", lato='" + lato + '\'' +
                ", misura=" + misura +
                '}';
    }
}