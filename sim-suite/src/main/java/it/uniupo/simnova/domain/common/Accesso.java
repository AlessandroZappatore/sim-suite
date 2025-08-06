package it.uniupo.simnova.domain.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe che rappresenta un <strong>accesso venoso o arterioso</strong> nel sistema.
 * Definisce le proprietà chiave di un accesso, come tipologia, posizione, lato e misura.
 *
 * @author Alessandro Zappatore
 * @version 2.0
 */
@Entity
@Data
@Table(name = "PazienteAccesso")
@AllArgsConstructor
@NoArgsConstructor
public class Accesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paziente_accesso")
    private Integer id;

    @Column(name = "paziente_t0_id")
    private Integer pazienteT0Id;

    @Column(name = "nome_accesso")
    private String nomeAccesso;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "posizione")
    private String posizione;

    @Column(name = "lato")
    private String lato;

    @Column(name = "misura")
    private Integer misura;
}