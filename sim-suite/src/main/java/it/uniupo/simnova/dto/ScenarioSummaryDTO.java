package it.uniupo.simnova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioSummaryDTO {
    Integer id;
    String titolo;
    String autori;
    String patologia;
    String descrizione;
    String tipologiaPaziente;
    String tipoScenario;
}
