package it.uniupo.simnova.simcreation;

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
    private EsameFisico esame_fisico;
    private PazienteT0 paziente_t0;

    public Scenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String materiale, String moulage, String liquidi, EsameFisico esame_fisico, PazienteT0 paziente_t0) {
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
        this.esame_fisico = esame_fisico;
        this.paziente_t0 = paziente_t0;
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

    public EsameFisico getEsameFisico() {
        return esame_fisico;
    }

    public PazienteT0 getPazienteT0() {
        return paziente_t0;
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

    public void setEsameFisico(EsameFisico esame_fisico) {
        this.esame_fisico = esame_fisico;
    }

    public void setPazienteT0(PazienteT0 paziente_t0) {
        this.paziente_t0 = paziente_t0;
    }

    @Override
    public String toString() {
        return "Scenario{id=" + id + ", titolo=" + titolo + ", nome_paziente=" + nome_paziente + ", patologia=" + patologia + ", descrizione=" + descrizione + ", briefing=" + briefing + ", patto_aula=" + patto_aula + ", azione_chiave=" + azione_chiave + ", obiettivo=" + obiettivo + ", materiale=" + materiale + ", moulage=" + moulage + ", liquidi=" + liquidi + ", esame_fisico=" + esame_fisico + ", paziente_t0=" + paziente_t0 + "}";
    }
}
