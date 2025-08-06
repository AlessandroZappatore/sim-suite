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
@Table(name = "PazienteT0")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PazienteT0 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paziente")
    private Integer id;

    @Column(name = "PA_sistolica")
    private Integer paSistolica;

    @Column(name = "PA_diastolica")
    private Integer paDiastolica;

    @Column(name = "FC")
    private Integer fc;

    @Column(name = "RR")
    private Integer rr;

    @Column(name = "T")
    private Float t;

    @Column(name = "SpO2")
    private Integer spo2;

    @Column(name = "FiO2")
    private Integer fio2;

    @Column(name = "LitriOssigeno")
    private Float litriOssigeno;

    @Column(name = "EtCO2")
    private Integer etco2;

    @Column(name = "Monitor")
    private String monitor;

    private List<Accesso> accessi = new ArrayList<>();

}