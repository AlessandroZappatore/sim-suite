package it.uniupo.simnova.domain.scenario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe che rappresenta uno <strong>scenario di simulazione</strong> come entità JPA.
 * Mappa la tabella 'Scenario' del database.
 *
 * @author Alessandro Zappatore (versione rifattorizzata per JPA)
 * @version 2.0
 */
@Entity
@Table(name = "Scenario") // Specifica il nome della tabella nel database
@Data // Genera getter, setter, toString, equals, hashCode
@NoArgsConstructor // Necessario per JPA: genera un costruttore senza argomenti
@AllArgsConstructor // Opzionale: genera un costruttore con tutti i campi
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Per ID auto-incrementanti (comune con SQLite, MySQL, etc.)
    @Column(name = "id_scenario") // Mappa questo campo alla colonna 'id_scenario'
    private Integer id;

    @Column(name = "titolo")
    private String titolo;

    @Column(name = "nome_paziente")
    private String nomePaziente;

    @Column(name = "patologia")
    private String patologia;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "briefing")
    private String briefing;

    @Column(name = "patto_aula")
    private String pattoAula;

    @Column(name = "obiettivo")
    private String obiettivo;

    @Column(name = "moulage")
    private String moulage;

    @Column(name = "liquidi")
    private String liquidi;

    @Column(name = "timer_generale")
    private Float timerGenerale;

    @Column(name = "autori")
    private String autori;

    @Column(name = "tipologia_paziente") // Ho rinominato il campo in 'tipologiaPaziente' ma mappato alla vecchia colonna
    private String tipologiaPaziente;

    @Column(name = "info_genitore")
    private String infoGenitore;

    @Column(name = "target")
    private String target;

    public Scenario(Integer id, String titolo, String autori, String patologia, String descrizione, String tipologiaPaziente) {
        this.id = id;
        this.titolo = titolo;
        this.autori = autori;
        this.patologia = patologia;
        this.descrizione = descrizione;
        this.tipologiaPaziente = tipologiaPaziente;
    }

    /**
     * Getter personalizzato per la tipologia, per evitare valori null.
     * Questo sovrascrive il getter generato da Lombok.
     *
     * @return La tipologia dello scenario (es. "Adulto", "Pediatrico"). Restituisce una stringa vuota se è null.
     */
    public String getTipologia() {
        return tipologiaPaziente != null ? tipologiaPaziente : "";
    }
}