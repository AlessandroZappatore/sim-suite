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
    private int idAccesso;
    private String tipologia;
    private String posizione;

    /**
     * Costruttore per creare un nuovo accesso venoso.
     *
     * @param idAccesso identificativo univoco dell'accesso
     * @param tipologia tipologia dell'accesso (es. "CVC", "Agocannula")
     * @param posizione posizione anatomica (es. "Giugulare destra", "Cubitale sinistro")
     */
    public Accesso(int idAccesso, String tipologia, String posizione) {
        this.idAccesso = idAccesso;
        this.tipologia = tipologia;
        this.posizione = posizione;
    }

    /**
     * @return l'identificativo univoco dell'accesso
     */
    public int getId() {
        return idAccesso;
    }

    /**
     * @return la tipologia dell'accesso
     */
    public String getTipologia() {
        return tipologia;
    }

    /**
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

    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
    @Override
    public String toString() {
        return "Accesso [idAccesso=" + idAccesso + ", tipologia=" + tipologia + ", posizione=" + posizione + "]";
    }
}