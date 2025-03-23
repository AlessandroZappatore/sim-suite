package it.uniupo.simnova.simcreation;

public class Accesso {
    private int id;
    private String tipologia;
    private String posizione;

    public Accesso(int id, String tipologia, String posizione) {
        this.id = id;
        this.tipologia = tipologia;
        this.posizione = posizione;
    }

    public int getId() {
        return id;
    }

    public String getTipologia() {
        return tipologia;
    }

    public String getPosizione() {
        return posizione;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public void setPosizione(String posizione) {
        this.posizione = posizione;
    }

    @Override
    public String toString() {
        return "Accesso [id=" + id + ", tipologia=" + tipologia + ", posizione=" + posizione + "]";
    }
}
