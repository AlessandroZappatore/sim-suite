package it.uniupo.simnova.simcreation;

public class EsameReferto {
    private int id;
    private int id_scenario;
    private String tipo;
    private String media;
    private String referto_testuale;

    public EsameReferto(int id, int id_scenario, String tipo, String media, String referto_testuale) {
        this.id = id;
        this.id_scenario = id_scenario;
        this.tipo = tipo;
        this.media = media;
        this.referto_testuale = referto_testuale;
    }

    public int getId() {
        return id;
    }

    public int getIdScenario() {
        return id_scenario;
    }

    public String getTipo() {
        return tipo;
    }

    public String getMedia() {
        return media;
    }

    public String getRefertoTestuale() {
        return referto_testuale;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIdScenario(int id_scenario) {
        this.id_scenario = id_scenario;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setRefertoTestuale(String referto_testuale) {
        this.referto_testuale = referto_testuale;
    }

    @Override
    public String toString() {
        return "EsameReferto [id=" + id + ", id_scenario=" + id_scenario + ", tipo=" + tipo + ", media=" + media + ", referto_testuale=" + referto_testuale + "]";
    }
}
