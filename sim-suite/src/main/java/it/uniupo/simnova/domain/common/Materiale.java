package it.uniupo.simnova.domain.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe che rappresenta un <strong>materiale</strong> generico utilizzato nel sistema.
 * Ogni materiale ha un identificativo univoco, un nome e una descrizione.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Materiale")
public class Materiale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materiale")
    private Integer idMateriale;

    @Column(name = "nome", nullable = false, unique = true)
    private String nome;

    @Column(name = "descrizione")
    private String descrizione;
}