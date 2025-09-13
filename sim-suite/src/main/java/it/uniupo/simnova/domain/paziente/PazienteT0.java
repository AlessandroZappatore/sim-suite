package it.uniupo.simnova.domain.paziente;

import it.uniupo.simnova.domain.common.Accesso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta i <strong>parametri del paziente al tempo T0</strong> (iniziale) di uno scenario di simulazione.
 * Contiene i parametri vitali principali e le liste degli accessi venosi e arteriosi.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Entity
@Table(name = "Paziente_T0")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PazienteT0 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paziente")
    private Integer id;

    @Column(name = "pa_sistolica")
    private Integer paSistolica;

    @Column(name = "pa_diastolica")
    private Integer paDiastolica;

    @Column(name = "fc")
    private Integer fc;

    @Column(name = "rr")
    private Integer rr;

    @Column(name = "t")
    private Float t;

    @Column(name = "spo2")
    private Integer spo2;

    @Column(name = "fio2")
    private Integer fio2;

    @Column(name = "litri_ossigeno")
    private Float litriOssigeno;

    @Column(name = "etco2")
    private Integer etco2;

    @Column(name = "monitor")
    private String monitor;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "paziente_t0_id", referencedColumnName = "id_paziente")
    private List<Accesso> accessi = new ArrayList<>();
}