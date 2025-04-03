package it.uniupo.simnova.api.model;

public class ParametroAggiuntivo {
    private int id;
    private int tempoId;
    private int scenarioId;
    private String nome;
    private String valore;
    private String unitaMisura;

    public ParametroAggiuntivo(int id, int tempoId, String nome, String valore, String unitaMisura) {
        this.id = id;
        this.tempoId = tempoId;
        this.nome = nome;
        this.valore = valore;
        this.unitaMisura = unitaMisura;
    }

    public ParametroAggiuntivo() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTempoId() {
        return tempoId;
    }

    public void setTempoId(int tempoId) {
        this.tempoId = tempoId;
    }

    public int getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(int scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getValore() {
        return valore;
    }

    public void setValore(String valore) {
        this.valore = valore;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }

    @Override
    public String toString() {
        return "ParametroAggiuntivo [id=" + id + ", tempoId=" + tempoId + ", nome=" + nome + ", valore=" + valore
                + ", unitaMisura=" + unitaMisura + "]";
    }
}
