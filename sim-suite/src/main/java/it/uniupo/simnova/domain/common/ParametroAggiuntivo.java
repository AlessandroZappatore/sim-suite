package it.uniupo.simnova.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parametri_aggiuntivi_id")
    private Integer id;

    @Column(name = "tempo_id")
    private Integer tempoId;

    @Column(name = "id_scenario")
    private Integer idScenario;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "valore", nullable = false)
    private String valore;

    @Column(name = "unità_misura")
    private String unitaMisura;

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
}