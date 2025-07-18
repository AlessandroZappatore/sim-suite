package it.uniupo.simnova.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Classe che rappresenta un <strong>materiale</strong> generico utilizzato nel sistema.
 * Ogni materiale ha un identificativo univoco, un nome e una descrizione.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class Materiale {
    /**
     * Identificativo univoco del materiale, assegnato dal database.
     */
    private final Integer idMateriale;
    /**
     * Nome del materiale, ad esempio "Siringa", "Defibrillatore".
     */
    private final String nome;
    /**
     * Descrizione dettagliata del materiale, che fornisce ulteriori informazioni.
     */
    private final String descrizione;
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