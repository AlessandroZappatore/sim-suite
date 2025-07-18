package it.uniupo.simnova.domain.paziente;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Classe che rappresenta un <strong>esame con referto</strong> nel sistema.
 * Contiene informazioni su un esame medico, inclusi:
 * <ul>
 * <li><strong>Identificativi</strong> dell'esame e dello scenario associato</li>
 * <li><strong>Tipo di esame</strong> (es. Radiografia, Ecografia)</li>
 * <li><strong>Percorso del file multimediale</strong> (se presente)</li>
 * <li><strong>Referto testuale</strong></li>
 * </ul>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class EsameReferto {

    /**
     * <strong>Identificativo univoco</strong> dell'esame e referto, assegnato dal database.
     */
    private final Integer idEsame;
    /**
     * Identificativo dello scenario associato all'esame.
     */
    private final Integer id_scenario;
    /**
     * Tipologia dell'esame, ad esempio "Radiografia", "Ecografia".
     */
    private String tipo;
    /**
     * Percorso del file multimediale associato all'esame.
     */
    private String media;
    /**
     * Contenuto testuale del referto dell'esame.
     */
    private String refertoTestuale;
    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto {@code EsameReferto},
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID dell'esame, lo scenario, il tipo, il media e il referto testuale.
     */
    @Override
    public String toString() {
        return "EsameReferto{" +
                "id_esame=" + idEsame +
                ", id_scenario=" + id_scenario +
                ", tipo='" + tipo + '\'' +
                ", media='" + media + '\'' +
                ", refertoTestuale='" + refertoTestuale + '\'' +
                '}';
    }
}