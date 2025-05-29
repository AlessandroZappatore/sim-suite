package it.uniupo.simnova.domain.paziente;

/**
 * Classe che rappresenta un <strong>esame con referto</strong> nel sistema.
 * Contiene informazioni su un esame medico, inclusi:
 * <ul>
 * <li><strong>Identificativi</strong> dell'esame e dello scenario associato</li>
 * <li><strong>Tipo di esame</strong> (es. Radiografia, Ecografia)</li>
 * <li><strong>Percorso del file multimediale</strong> (se presente)</li>
 * <li><strong>Referto testuale</strong></li>
 * </ul>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class EsameReferto {

    /**
     * <strong>Identificativo univoco</strong> dell'esame e referto, assegnato dal database.
     */
    private final int idEsame;
    /**
     * Identificativo dello scenario associato all'esame.
     */
    private final int id_scenario;
    /**
     * Tipologia dell'esame, ad esempio "Radiografia", "Ecografia".
     */
    private String tipo;
    /**
     * Percorso del file multimediale associato all'esame.
     */
    private String media;
    /**
     * Contenuto testuale del referto dell'esame.
     */
    private String refertoTestuale;

    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>EsameReferto</code></strong>.
     *
     * @param idEsame         <strong>Identificativo univoco</strong> dell'esame.
     * @param scenario        <strong>Identificativo</strong> dello scenario associato.
     * @param tipo            <strong>Tipologia</strong> dell'esame (es. "Radiografia", "Ecografia").
     * @param media           <strong>Percorso del file multimediale</strong> associato (opzionale).
     * @param refertoTestuale <strong>Contenuto testuale</strong> del referto.
     */
    public EsameReferto(int idEsame, int scenario, String tipo, String media, String refertoTestuale) {
        this.idEsame = idEsame;
        this.id_scenario = scenario;
        this.tipo = tipo;
        this.media = media;
        this.refertoTestuale = refertoTestuale;
    }

    /**
     * Restituisce l'<strong>identificativo univoco</strong> dell'esame.
     *
     * @return L'identificativo univoco dell'esame.
     */
    public int getIdEsame() {
        return idEsame;
    }

    /**
     * Restituisce l'<strong>identificativo dello scenario</strong> associato all'esame.
     *
     * @return L'identificativo dello scenario associato.
     */
    public int getScenario() {
        return id_scenario;
    }

    /**
     * Restituisce la <strong>tipologia</strong> dell'esame.
     *
     * @return La tipologia dell'esame.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Imposta una nuova <strong>tipologia</strong> per l'esame.
     *
     * @param tipo La nuova tipologia dell'esame.
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Restituisce il <strong>percorso del file multimediale</strong> associato all'esame.
     *
     * @return Il percorso del file multimediale associato.
     */
    public String getMedia() {
        return media;
    }

    /**
     * Imposta un nuovo <strong>percorso per il file multimediale</strong> associato all'esame.
     *
     * @param media Il nuovo percorso del file multimediale.
     */
    public void setMedia(String media) {
        this.media = media;
    }

    /**
     * Restituisce il <strong>contenuto testuale</strong> del referto.
     *
     * @return Il contenuto testuale del referto.
     */
    public String getRefertoTestuale() {
        return refertoTestuale;
    }

    /**
     * Imposta un nuovo <strong>contenuto testuale</strong> per il referto.
     *
     * @param refertoTestuale Il nuovo contenuto testuale del referto.
     */
    public void setRefertoTestuale(String refertoTestuale) {
        this.refertoTestuale = refertoTestuale;
    }

    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto {@code EsameReferto},
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID dell'esame, lo scenario, il tipo, il media e il referto testuale.
     */
    @Override
    public String toString() {
        return "EsameReferto{" +
                "id_esame=" + idEsame +
                ", id_scenario=" + id_scenario +
                ", tipo='" + tipo + '\'' +
                ", media='" + media + '\'' +
                ", refertoTestuale='" + refertoTestuale + '\'' +
                '}';
    }
}