package it.uniupo.simnova.api.model;

public class Scenario {
    private int id;
    private String titolo;
    private String nome_paziente;
    private String patologia;
    private String descrizione;
    private String briefing;
    private String patto_aula;
    private String azione_chiave;
    private String obiettivo;
    private String materiale;
    private String moulage;
    private String liquidi;
    private float timer_generale;

    public Scenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String materiale, String moulage, String liquidi, float timer_generale) {
        this.id = id;
        this.titolo = titolo;
        this.nome_paziente = nome_paziente;
        this.patologia = patologia;
        this.descrizione = descrizione;
        this.briefing = briefing;
        this.patto_aula = patto_aula;
        this.azione_chiave = azione_chiave;
        this.obiettivo = obiettivo;
        this.materiale = materiale;
        this.moulage = moulage;
        this.liquidi = liquidi;
        if(timer_generale<=0){
            this.timer_generale = 0;
        }
        else{
            this.timer_generale = timer_generale;
        }
    }

    public Scenario(int id, String titolo, String nome_paziente, String patologia, float timer_generale) {
        this.id = id;
        this.titolo = titolo;
        this.nome_paziente = nome_paziente;
        this.patologia = patologia;
        this.timer_generale = timer_generale;
    }

    public Scenario(){

    }

    public int getId() {
        return id;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getNomePaziente() {
        return nome_paziente;
    }

    public String getPatologia() {
        return patologia;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getBriefing() {
        return briefing;
    }

    public String getPattoAula() {
        return patto_aula;
    }

    public String getAzioneChiave() {
        return azione_chiave;
    }

    public String getObiettivo() {
        return obiettivo;
    }

    public String getMateriale() {
        return materiale;
    }

    public String getMoulage() {
        return moulage;
    }

    public String getLiquidi() {
        return liquidi;
    }

    public float getTimerGenerale(){
        return timer_generale;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public void setNomePaziente(String nome_paziente) {
        this.nome_paziente = nome_paziente;
    }

    public void setPatologia(String patologia) {
        this.patologia = patologia;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }

    public void setPattoAula(String patto_aula) {
        this.patto_aula = patto_aula;
    }

    public void setAzioneChiave(String azione_chiave) {
        this.azione_chiave = azione_chiave;
    }

    public void setObiettivo(String obiettivo) {
        this.obiettivo = obiettivo;
    }

    public void setMateriale(String materiale) {
        this.materiale = materiale;
    }

    public void setMoulage(String moulage) {
        this.moulage = moulage;
    }

    public void setLiquidi(String liquidi) {
        this.liquidi = liquidi;
    }

    @Override
    public String toString() {
        return "Scenario{id=" + id + ", titolo=" + titolo + ", nome_paziente=" + nome_paziente + ", patologia=" + patologia + ", descrizione=" + descrizione + ", briefing=" + briefing + ", patto_aula=" + patto_aula + ", azione_chiave=" + azione_chiave + ", obiettivo=" + obiettivo + ", materiale=" + materiale + ", moulage=" + moulage + ", liquidi=" + liquidi + "}";
    }
}
