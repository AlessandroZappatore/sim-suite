package it.uniupo.simnova.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Classe che rappresenta un <strong>accesso venoso o arterioso</strong> nel sistema.
 * Definisce le proprietà chiave di un accesso, come tipologia, posizione, lato e misura.
 *
 * @author Alessandro Zappatore
 * @version 2.0
 */
@Data
@AllArgsConstructor
public class Accesso {
    /**
     * Identificativo univoco dell'accesso, assegnato dal database.
     */
    private final Integer idAccesso;
    /**
     * Tipologia dell'accesso (es. "CVC", "Agocannula" per venoso; "Radiale" per arterioso).
     */
    private String tipologia;
    /**
     * Posizione anatomica dell'accesso (es. "Giugulare destra", "Cubitale sinistro").
     */
    private String posizione;
    /**
     * Lato dell'accesso, che può essere "DX" (destro) o "SX" (sinistro).
     */
    private String lato;
    /**
     * Misura dell'accesso, espressa in Gauge (es. 14G, 16G).
     */
    private Integer misura;
    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto {@code Accesso},
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID, la tipologia, la posizione, il lato e la misura dell'accesso.
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