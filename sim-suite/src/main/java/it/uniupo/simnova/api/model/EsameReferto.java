package it.uniupo.simnova.api.model;

public class EsameReferto {
    private int idEsame;
    private int id_scenario;
    private String tipo;
    private String media;
    private String refertoTestuale;

    public EsameReferto(int idEsame, int scenario, String tipo, String media, String refertoTestuale) {
        this.idEsame = idEsame;
        this.id_scenario = scenario;
        this.tipo = tipo;
        this.media = media;
        this.refertoTestuale = refertoTestuale;
    }

    public EsameReferto() {

    }

    public int getIdEsame() {
        return idEsame;
    }

    public void setIdEsame(int idEsame) {
        this.idEsame = idEsame;
    }

    public int getScenario() {
        return id_scenario;
    }

    public void setIdScenario(int id_scenario) {
        this.id_scenario = id_scenario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getRefertoTestuale() {
        return refertoTestuale;
    }

    public void setRefertoTestuale(String refertoTestuale) {
        this.refertoTestuale = refertoTestuale;
    }

    @Override
    public String toString() {
        return "EsameReferto{" +
                "idEsame=" + idEsame +
                ", scenario=" + id_scenario +
                ", tipo='" + tipo + '\'' +
                ", media='" + media + '\'' +
                ", refertoTestuale='" + refertoTestuale + '\'' +
                '}';
    }
}
