package it.uniupo.simnova.simcreation;

public class EsameFisico {
    private int id;
    private String generale;
    private String pupille;
    private String collo;
    private String torace;
    private String cuore;
    private String addome;
    private String retto;
    private String cute;
    private String estremita;
    private String neurologico;
    private String FAST;

    public EsameFisico(int id, String generale, String pupille, String collo, String torace, String cuore, String addome, String retto, String cute, String estremita, String neurologico, String FAST) {
        this.id = id;
        this.generale = generale;
        this.pupille = pupille;
        this.collo = collo;
        this.torace = torace;
        this.cuore = cuore;
        this.addome = addome;
        this.retto = retto;
        this.cute = cute;
        this.estremita = estremita;
        this.neurologico = neurologico;
        this.FAST = FAST;
    }

    public int getId() {
        return id;
    }

    public String getGenerale() {
        return generale;
    }

    public String getPupille() {
        return pupille;
    }

    public String getCollo() {
        return collo;
    }

    public String getTorace() {
        return torace;
    }

    public String getCuore() {
        return cuore;
    }

    public String getAddome() {
        return addome;
    }

    public String getRetto() {
        return retto;
    }

    public String getCute() {
        return cute;
    }

    public String getEstremita() {
        return estremita;
    }

    public String getNeurologico() {
        return neurologico;
    }

    public String getFAST() {
        return FAST;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGenerale(String generale) {
        this.generale = generale;
    }

    public void setPupille(String pupille) {
        this.pupille = pupille;
    }

    public void setCollo(String collo) {
        this.collo = collo;
    }

    public void setTorace(String torace) {
        this.torace = torace;
    }

    public void setCuore(String cuore) {
        this.cuore = cuore;
    }

    public void setAddome(String addome) {
        this.addome = addome;
    }

    public void setRetto(String retto) {
        this.retto = retto;
    }

    public void setCute(String cute) {
        this.cute = cute;
    }

    public void setEstremita(String estremita) {
        this.estremita = estremita;
    }

    public void setNeurologico(String neurologico) {
        this.neurologico = neurologico;
    }

    public void setFAST(String FAST) {
        this.FAST = FAST;
    }

    @Override
    public String toString() {
        return "EsameFisico [id=" + id + ", generale=" + generale + ", pupille=" + pupille + ", collo=" + collo + ", torace=" + torace + ", cuore=" + cuore + ", addome=" + addome + ", retto=" + retto + ", cute=" + cute + ", estremita=" + estremita + ", neurologico=" + neurologico + ", FAST=" + FAST + "]";
    }
}
