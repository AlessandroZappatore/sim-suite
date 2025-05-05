package it.uniupo.simnova.api.model;

/**
 * Classe che rappresenta uno scenario di simulazione.
 * <p>
 * Contiene i dettagli principali dello scenario come titolo, nome del paziente, patologia, descrizione, briefing, patto d'aula, azione chiave, obiettivo, materiale, moulage, liquidi e timer generale.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class Scenario {
    /**
     * Identificativo univoco dello scenario.
     */
    private int id;
    /**
     * Titolo dello scenario.
     */
    private String titolo;
    /**
     * Nome del paziente.
     */
    private String nome_paziente;
    /**
     * Patologia del paziente.
     */
    private String patologia;
    /**
     * Descrizione dello scenario.
     */
    private String descrizione;
    /**
     * Briefing dello scenario.
     */
    private String briefing;
    /**
     * Patto d'aula dello scenario.
     */
    private String patto_aula;
    /**
     * Azione chiave dello scenario.
     */
    private String azione_chiave;
    /**
     * Obiettivo dello scenario.
     */
    private String obiettivo;
    /**
     * Moulage dello scenario.
     */
    private String moulage;
    /**
     * Liquidi dello scenario.
     */
    private String liquidi;
    /**
     * Timer generale dello scenario.
     */
    private float timer_generale;
    /**
     * Autori dello scenario.
     */
    private String autori;
    /**
     * Tipologia dello scenario.
     */
    private String tipologia;
    /**
     * Informazioni del genitore dello scenario.
     */
    private String infoGenitore;
    /**
     * Informazioni sul target dello scenario.
     */
    private String target;


    public Scenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String azione_chiave, String obiettivo, String moulage, String liquidi, float timer_generale, String autori, String tipologia, String infoGenitore, String target) {
        this.id = id;
        this.titolo = titolo;
        this.nome_paziente = nome_paziente;
        this.patologia = patologia;
        this.descrizione = descrizione;
        this.briefing = briefing;
        this.patto_aula = patto_aula;
        this.azione_chiave = azione_chiave;
        this.obiettivo = obiettivo;
        this.moulage = moulage;
        this.liquidi = liquidi;
        if (timer_generale < 0) {
            this.timer_generale = 0;
        } else {
            this.timer_generale = timer_generale;
        }
        this.autori = autori;
        this.tipologia = tipologia;
        if(tipologia.equals("Pediatrico"))
            this.infoGenitore = infoGenitore;
        else
            this.infoGenitore = null;
        this.target = target;
    }

    /**
     * Costruttore parziale per creare uno scenario con i campi principali.
     *
     * @param id             l'identificativo univoco dello scenario
     * @param titolo         il titolo dello scenario
     * @param patologia      la patologia del paziente
     * @param descrizione    la descrizione dello scenario
     */
    public Scenario(int id, String titolo, String autori, String patologia, String descrizione) {
        this.id = id;
        this.titolo = titolo;
        this.autori = autori;
        this.patologia = patologia;
        this.descrizione = descrizione;
    }

    /**
     * Restituisce l'identificativo univoco dello scenario.
     *
     * @return l'identificativo univoco dello scenario
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'identificativo univoco dello scenario.
     *
     * @param id il nuovo identificativo
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il titolo dello scenario.
     *
     * @return il titolo dello scenario
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Imposta il titolo dello scenario.
     *
     * @param titolo il nuovo titolo
     */
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    /**
     * Restituisce il nome del paziente.
     *
     * @return il nome del paziente
     */
    public String getNomePaziente() {
        return nome_paziente;
    }

    /**
     * Imposta il nome del paziente.
     *
     * @param nome_paziente il nuovo nome del paziente
     */
    public void setNomePaziente(String nome_paziente) {
        this.nome_paziente = nome_paziente;
    }

    /**
     * Restituisce la patologia del paziente.
     *
     * @return la patologia del paziente
     */
    public String getPatologia() {
        return patologia;
    }

    /**
     * Imposta la patologia del paziente.
     *
     * @param patologia la nuova patologia
     */
    public void setPatologia(String patologia) {
        this.patologia = patologia;
    }

    /**
     * Restituisce la descrizione dello scenario.
     *
     * @return la descrizione dello scenario
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la descrizione dello scenario.
     *
     * @param descrizione la nuova descrizione
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce il briefing dello scenario.
     *
     * @return il briefing dello scenario
     */
    public String getBriefing() {
        return briefing;
    }

    /**
     * Imposta il briefing dello scenario.
     *
     * @param briefing il nuovo briefing
     */
    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }

    /**
     * Restituisce il patto d'aula dello scenario.
     *
     * @return il patto d'aula dello scenario
     */
    public String getPattoAula() {
        return patto_aula;
    }

    /**
     * Imposta il patto d'aula dello scenario.
     *
     * @param patto_aula il nuovo patto d'aula
     */
    public void setPattoAula(String patto_aula) {
        this.patto_aula = patto_aula;
    }

    /**
     * Restituisce l'azione chiave dello scenario.
     *
     * @return l'azione chiave dello scenario
     */
    public String getAzioneChiave() {
        if (azione_chiave == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        String[] azioni = azione_chiave.split(";");

        for (int i = 0; i < azioni.length; i++) {
            sb.append(azioni[i].trim());
            if (i < azioni.length - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public String getAzione_chiave_raw() {
        return azione_chiave;
    }

    /**
     * Imposta l'azione chiave dello scenario.
     *
     * @param azione_chiave la nuova azione chiave
     */
    public void setAzioneChiave(String azione_chiave) {
        this.azione_chiave = azione_chiave;
    }

    /**
     * Restituisce l'obiettivo dello scenario.
     *
     * @return l'obiettivo dello scenario
     */
    public String getObiettivo() {
        return obiettivo;
    }

    /**
     * Imposta l'obiettivo dello scenario.
     *
     * @param obiettivo il nuovo obiettivo
     */
    public void setObiettivo(String obiettivo) {
        this.obiettivo = obiettivo;
    }

    /**
     * Restituisce il moulage dello scenario.
     *
     * @return il moulage dello scenario
     */
    public String getMoulage() {
        return moulage;
    }

    /**
     * Imposta il moulage dello scenario.
     *
     * @param moulage il nuovo moulage
     */
    public void setMoulage(String moulage) {
        this.moulage = moulage;
    }

    /**
     * Restituisce i liquidi dello scenario.
     *
     * @return i liquidi dello scenario
     */
    public String getLiquidi() {
        return liquidi;
    }

    /**
     * Imposta i liquidi dello scenario.
     *
     * @param liquidi i nuovi liquidi
     */
    public void setLiquidi(String liquidi) {
        this.liquidi = liquidi;
    }

    /**
     * Restituisce il timer generale dello scenario.
     *
     * @return il timer generale dello scenario
     */
    public float getTimerGenerale() {
        return timer_generale;
    }

    /**
     * Imposta il timer generale dello scenario.
     *
     * @param timer_generale il nuovo timer generale
     */
    public void setTimerGenerale(float timer_generale) {
        this.timer_generale = timer_generale;
    }

    public String getAutori() {
        return autori;
    }

    public void setAutori(String autori) {
        this.autori = autori;
    }

    public String getTipologia() {
        return tipologia;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public String getInfoGenitore() {
        return infoGenitore;
    }

    public void setInfoGenitore(String infoGenitore) {
        if(tipologia.equals("Pediatrico"))
            this.infoGenitore = infoGenitore;
        else
            this.infoGenitore = null;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    /**
     * Restituisce una rappresentazione stringa dell'oggetto.
     *
     * @return stringa con i valori di tutti i campi
     */
    @Override
    public String toString() {
        return "Scenario{" +
                "id=" + id +
                ", titolo='" + titolo + '\'' +
                ", nome_paziente='" + nome_paziente + '\'' +
                ", patologia='" + patologia + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", briefing='" + briefing + '\'' +
                ", patto_aula='" + patto_aula + '\'' +
                ", azione_chiave='" + azione_chiave + '\'' +
                ", obiettivo='" + obiettivo + '\'' +
                ", moulage='" + moulage + '\'' +
                ", liquidi='" + liquidi + '\'' +
                ", timer_generale=" + timer_generale +
                ", autori='" + autori + '\'' +
                ", tipologia='" + tipologia + '\'' +
                ", infoGenitore='" + infoGenitore + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}