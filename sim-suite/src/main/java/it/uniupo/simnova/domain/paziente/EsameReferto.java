package it.uniupo.simnova.domain.paziente;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Entity
@Data
@Table(name = "Esame_Referto")
@AllArgsConstructor
@NoArgsConstructor
public class EsameReferto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_esame")
    private Integer idEsame;

    @Column(name = "id_scenario")
    private Integer idScenario;

    @Column(name = "tipo")
    private String tipoEsame;

    @Column(name = "media")
    private String media;

    @Column(name = "referto_testuale")
    private String referto;

}