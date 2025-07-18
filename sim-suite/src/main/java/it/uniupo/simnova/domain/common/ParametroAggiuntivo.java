package it.uniupo.simnova.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Classe che rappresenta un <strong>parametro aggiuntivo</strong> per uno scenario di simulazione.
 * Questa classe modella parametri personalizzati che possono essere aggiunti a specifici
 * tempi di uno scenario per arricchire la simulazione con dati extra (es. parametri vitali personalizzati, valori di laboratorio).
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class ParametroAggiuntivo {

    /**
     * Identificativo univoco del parametro, ID assegnato dal database.
     */
    private Integer id;
    /**
     * Identificativo del tempo a cui appartiene il parametro.
     */
    private Integer tempoId;
    /**
     * Identificativo dello scenario a cui appartiene il parametro.
     */
    private Integer scenarioId;
    /**
     * Nome del parametro (es. "Pressione venosa centrale", "Glicemia").
     */
    private String nome;
    /**
     * Valore del parametro, memorizzato come stringa (es. "12", "98.5").
     */
    private String valore;
    /**
     * Unità di misura del parametro (es. "mmHg", "%").
     */
    private final String unitaMisura;
    /**
     * Costruttore semplificato per creare un nuovo oggetto <strong><code>ParametroAggiuntivo</code></strong>,
     * utile quando l'ID non è ancora noto (es. prima del salvataggio nel database).
     *
     * @param nome   Il nome del parametro.
     * @param valore Il valore del parametro, come numero. Verrà convertito in stringa.
     * @param unita  L'unità di misura del parametro.
     */
    public ParametroAggiuntivo(String nome, double valore, String unita) {
        this.nome = nome;
        this.valore = String.valueOf(valore); // Converte il valore numerico in stringa.
        this.unitaMisura = unita;
    }
    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto {@code ParametroAggiuntivo},
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID, il tempo, lo scenario, il nome, il valore e l'unità di misura del parametro.
     */
    @Override
    public String toString() {
        return "ParametroAggiuntivo{" +
                "id=" + id +
                ", tempoId=" + tempoId +
                ", scenarioId=" + scenarioId +
                ", nome='" + nome + '\'' +
                ", valore='" + valore + '\'' +
                ", unitaMisura='" + unitaMisura + '\'' +
                '}';
    }
}