package it.uniupo.simnova.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScenarioDetailsDto {
    private String title;
    private String patientName;
    private String pathology;
    private String author;
    private Integer duration;
    private String type;
}
