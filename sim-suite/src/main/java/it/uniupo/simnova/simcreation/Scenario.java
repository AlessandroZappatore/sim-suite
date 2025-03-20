package it.uniupo.simnova.simcreation;

public class Scenario {
    private String TitoloScenario;
    private String NomePaziente;
    private String PatologiaMalattia;
    private String NomeFile;

    private String DescrizioneScenario;
    private String Briefing;
    private String Patto;
    private String AzioniChiave;
    private String ObiettiviDidattici;
    private String MaterialeNecessario;
    private String EsamiReferti;
    private String Moulage;
    private String LiquidiPresidi;

    private int PA;
    private int FC;
    private int RR;
    private float Temp;
    private int SPO2;
    private int EtCO2;
    private boolean AccessiVenosi;
    private boolean AccessiArteriosi;
    private String Monitor;
    private String Generale;
    private String Pupille;
    private String Collo;
    private String Torace;
    private String Cuore;
    private String Addome;
    private String Retto;
    private String Cute;
    private String Estremita;
    private String Neurologico;
    private String FAST;

    public Scenario(String titoloScenario, String nomePaziente, String patologiaMalattia, String nomeFile, String descrizioneScenario, String briefing, String patto, String azioniChiave, String obiettiviDidattici, String materialeNecessario, String esamiReferti, String moulage, String liquidiPresidi, int PA, int FC, int RR, float temp, int SPO2, int etCO2, boolean accessiVenosi, boolean accessiArteriosi, String monitor, String generale, String pupille, String collo, String torace, String cuore, String addome, String retto, String cute, String estremita, String neurologico, String FAST) {
        TitoloScenario = titoloScenario;
        NomePaziente = nomePaziente;
        PatologiaMalattia = patologiaMalattia;
        NomeFile = nomeFile;
        DescrizioneScenario = descrizioneScenario;
        Briefing = briefing;
        Patto = patto;
        AzioniChiave = azioniChiave;
        ObiettiviDidattici = obiettiviDidattici;
        MaterialeNecessario = materialeNecessario;
        EsamiReferti = esamiReferti;
        Moulage = moulage;
        LiquidiPresidi = liquidiPresidi;
        this.PA = PA;
        this.FC = FC;
        this.RR = RR;
        Temp = temp;
        this.SPO2 = SPO2;
        EtCO2 = etCO2;
        AccessiVenosi = accessiVenosi;
        AccessiArteriosi = accessiArteriosi;
        Monitor = monitor;
        Generale = generale;
        Pupille = pupille;
        Collo = collo;
        Torace = torace;
        Cuore = cuore;
        Addome = addome;
        Retto = retto;
        Cute = cute;
        Estremita = estremita;
        Neurologico = neurologico;
        this.FAST = FAST;
    }

    public String getTitoloScenario() {
        return TitoloScenario;
    }

    public void setTitoloScenario(String titoloScenario) {
        TitoloScenario = titoloScenario;
    }

    public String getNomePaziente() {
        return NomePaziente;
    }

    public void setNomePaziente(String nomePaziente) {
        NomePaziente = nomePaziente;
    }

    public String getPatologiaMalattia() {
        return PatologiaMalattia;
    }

    public void setPatologiaMalattia(String patologiaMalattia) {
        PatologiaMalattia = patologiaMalattia;
    }

    public String getNomeFile() {
        return NomeFile;
    }

    public void setNomeFile(String nomeFile) {
        NomeFile = nomeFile;
    }

    public String getDescrizioneScenario() {
        return DescrizioneScenario;
    }

    public void setDescrizioneScenario(String descrizioneScenario) {
        DescrizioneScenario = descrizioneScenario;
    }

    public String getBriefing() {
        return Briefing;
    }

    public void setBriefing(String briefing) {
        Briefing = briefing;
    }

    public String getPatto() {
        return Patto;
    }

    public void setPatto(String patto) {
        Patto = patto;
    }

    public String getAzioniChiave() {
        return AzioniChiave;
    }

    public void setAzioniChiave(String azioniChiave) {
        AzioniChiave = azioniChiave;
    }

    public String getObiettiviDidattici() {
        return ObiettiviDidattici;
    }

    public void setObiettiviDidattici(String obiettiviDidattici) {
        ObiettiviDidattici = obiettiviDidattici;
    }

    public String getMaterialeNecessario() {
        return MaterialeNecessario;
    }

    public void setMaterialeNecessario(String materialeNecessario) {
        MaterialeNecessario = materialeNecessario;
    }

    public String getEsamiReferti() {
        return EsamiReferti;
    }

    public void setEsamiReferti(String esamiReferti) {
        EsamiReferti = esamiReferti;
    }

    public String getMoulage() {
        return Moulage;
    }

    public void setMoulage(String moulage) {
        Moulage = moulage;
    }

    public String getLiquidiPresidi() {
        return LiquidiPresidi;
    }

    public void setLiquidiPresidi(String liquidiPresidi) {
        LiquidiPresidi = liquidiPresidi;
    }

    public int getPA() {
        return PA;
    }

    public void setPA(int PA) {
        this.PA = PA;
    }

    public int getFC() {
        return FC;
    }

    public void setFC(int FC) {
        this.FC = FC;
    }

    public int getRR() {
        return RR;
    }

    public void setRR(int RR) {
        this.RR = RR;
    }

    public float getTemp() {
        return Temp;
    }

    public void setTemp(float temp) {
        Temp = temp;
    }

    public int getSPO2() {
        return SPO2;
    }

    public void setSPO2(int SPO2) {
        this.SPO2 = SPO2;
    }

    public int getEtCO2() {
        return EtCO2;
    }

    public void setEtCO2(int etCO2) {
        EtCO2 = etCO2;
    }

    public boolean isAccessiVenosi() {
        return AccessiVenosi;
    }

    public void setAccessiVenosi(boolean accessiVenosi) {
        AccessiVenosi = accessiVenosi;
    }

    public boolean isAccessiArteriosi() {
        return AccessiArteriosi;
    }

    public void setAccessiArteriosi(boolean accessiArteriosi) {
        AccessiArteriosi = accessiArteriosi;
    }

    public String getMonitor() {
        return Monitor;
    }

    public void setMonitor(String monitor) {
        Monitor = monitor;
    }

    public String getGenerale() {
        return Generale;
    }

    public void setGenerale(String generale) {
        Generale = generale;
    }

    public String getPupille() {
        return Pupille;
    }

    public void setPupille(String pupille) {
        Pupille = pupille;
    }

    public String getCollo() {
        return Collo;
    }

    public void setCollo(String collo) {
        Collo = collo;
    }

    public String getTorace() {
        return Torace;
    }

    public void setTorace(String torace) {
        Torace = torace;
    }

    public String getCuore() {
        return Cuore;
    }

    public void setCuore(String cuore) {
        Cuore = cuore;
    }

    public String getAddome() {
        return Addome;
    }

    public void setAddome(String addome) {
        Addome = addome;
    }

    public String getRetto() {
        return Retto;
    }

    public void setRetto(String retto) {
        Retto = retto;
    }

    public String getCute() {
        return Cute;
    }

    public void setCute(String cute) {
        Cute = cute;
    }

    public String getEstremita() {
        return Estremita;
    }

    public void setEstremita(String estremita) {
        Estremita = estremita;
    }

    public String getNeurologico() {
        return Neurologico;
    }

    public void setNeurologico(String neurologico) {
        Neurologico = neurologico;
    }

    public String getFAST() {
        return FAST;
    }

    public void setFAST(String FAST) {
        this.FAST = FAST;
    }
}
