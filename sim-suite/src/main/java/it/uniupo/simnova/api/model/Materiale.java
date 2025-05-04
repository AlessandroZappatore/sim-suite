package it.uniupo.simnova.api.model;

public class Materiale {
    private int idMateriale;
    private String nome;
    private String descrizione;

    public Materiale(int idMateriale, String nome, String descrizione) {
        this.idMateriale = idMateriale;
        this.nome = nome;
        this.descrizione = descrizione;
    }

    public Integer getId() {
        return idMateriale;
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setId(int idMateriale) {
        this.idMateriale = idMateriale;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    @Override
    public String toString() {
        return "Materiale{" +
                "idMateriale=" + idMateriale +
                ", nome='" + nome + '\'' +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}
