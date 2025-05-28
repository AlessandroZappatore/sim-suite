package it.uniupo.simnova.domain.common;

/**
 * Classe che rappresenta un <strong>materiale</strong> generico utilizzato nel sistema.
 * Ogni materiale ha un identificativo univoco, un nome e una descrizione.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class Materiale {

    private final int idMateriale;   // Identificativo univoco del materiale, assegnato dal database.
    private final String nome;       // Il nome del materiale (es. "Siringa", "Defibrillatore").
    private final String descrizione; // Una descrizione dettagliata del materiale.

    /**
     * Costruttore per creare un nuovo oggetto <strong><code>Materiale</code></strong>.
     *
     * @param idMateriale L'identificativo univoco del materiale.
     * @param nome        Il nome del materiale.
     * @param descrizione La descrizione del materiale.
     */
    public Materiale(int idMateriale, String nome, String descrizione) {
        this.idMateriale = idMateriale;
        this.nome = nome;
        this.descrizione = descrizione;
    }

    /**
     * Restituisce l'<strong>identificativo univoco</strong> del materiale.
     *
     * @return L'ID del materiale.
     */
    public Integer getId() {
        return idMateriale;
    }

    /**
     * Restituisce il <strong>nome</strong> del materiale.
     *
     * @return Il nome del materiale.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce la <strong>descrizione</strong> del materiale.
     *
     * @return La descrizione del materiale.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto <strong><code>Materiale</code></strong>,
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID, il nome e la descrizione del materiale.
     */
    @Override
    public String toString() {
        return "Materiale{" +
                "idMateriale=" + idMateriale +
                ", nome='" + nome + '\'' +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}