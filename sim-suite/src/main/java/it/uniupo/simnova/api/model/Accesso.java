package it.uniupo.simnova.api.model;

public class Accesso {
    private int idAccesso;
    private String tipologia;
    private String posizione;

    public Accesso(int idAccesso, String tipologia, String posizione) {
        this.idAccesso = idAccesso;
        this.tipologia = tipologia;
        this.posizione = posizione;
    }

    public int getId() {
        return idAccesso;
    }

    public String getTipologia() {
        return tipologia;
    }

    public String getPosizione() {
        return posizione;
    }

    public void setId(int idAccesso) {
        this.idAccesso = idAccesso;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public void setPosizione(String posizione) {
        this.posizione = posizione;
    }

    @Override
    public String toString() {
        return "Accesso [idAccesso=" + idAccesso + ", tipologia=" + tipologia + ", posizione=" + posizione + "]";
    }
}
