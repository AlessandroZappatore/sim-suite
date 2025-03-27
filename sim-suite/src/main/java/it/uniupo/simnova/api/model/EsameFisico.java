package it.uniupo.simnova.api.model;

import java.util.HashMap;
import java.util.Map;

public class EsameFisico {
    private Integer id;
    private Map<String, String> sections;

    public EsameFisico() {
        this.sections = new HashMap<>();
        // Initialize all sections
        sections.put("Generale", "");
        sections.put("Pupille", "");
        sections.put("Collo", "");
        sections.put("Torace", "");
        sections.put("Cuore", "");
        sections.put("Addome", "");
        sections.put("Retto", "");
        sections.put("Cute", "");
        sections.put("Estremita", "");
        sections.put("Neurologico", "");
        sections.put("FAST", "");
    }

    public EsameFisico(int id, String generale, String pupille, String collo, String torace, String cuore, String addome, String retto, String cute, String estremita, String neurologico, String fast) {
        this.id = id;

        this.sections = new HashMap<>();
        sections.put("Generale", generale);
        sections.put("Pupille", pupille);
        sections.put("Collo", collo);
        sections.put("Torace", torace);
        sections.put("Cuore", cuore);
        sections.put("Addome", addome);
        sections.put("Retto", retto);
        sections.put("Cute", cute);
        sections.put("Estremita", estremita);
        sections.put("Neurologico", neurologico);
        sections.put("FAST", fast);
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<String, String> getSections() {
        return sections;
    }

    public void setSections(Map<String, String> sections) {
        this.sections = sections;
    }

    public String getSection(String sectionName) {
        return sections.get(sectionName);
    }

    public void setSection(String sectionName, String value) {
        sections.put(sectionName, value);
    }
}