package it.uniupo.simnova.views.constant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe di supporto contenente i parametri aggiuntivi da utilizzare nei vari tempi.
 */
public class AdditionParametersConst {
    /**
     * Mappa dei parametri aggiuntivi predefiniti.
     * La chiave è il nome del parametro, il valore è la sua descrizione.
     */
    public static final Map<String, String> ADDITIONAL_PARAMETERS = new LinkedHashMap<>();
    /**
     * Chiave per i parametri personalizzati.
     */
    public static final String CUSTOM_PARAMETER_KEY = "CUSTOM";

    static {
        // Popolamento della mappa dei parametri aggiuntivi predefiniti
        // (Cardiologia / Monitor Multiparametrico)
        ADDITIONAL_PARAMETERS.put("PVC", "Pressione Venosa Centrale (cmH₂O)");
        ADDITIONAL_PARAMETERS.put("QTc", "QT/QTc (ms)");
        ADDITIONAL_PARAMETERS.put("ST", "Segmento ST (mV)");
        ADDITIONAL_PARAMETERS.put("SI", "Indice di Shock (FC/PA sistolica)");

        // (Pneumologia / Ventilazione)
        ADDITIONAL_PARAMETERS.put("PIP", "Pressione Inspiratoria Positiva (cmH₂O)");
        ADDITIONAL_PARAMETERS.put("VT", "Volume Corrente (mL/kg)");
        ADDITIONAL_PARAMETERS.put("COMP", "Compliance Polmonare (mL/cmH₂O)");
        ADDITIONAL_PARAMETERS.put("RAW", "Resistenza Vie Aeree (cmH₂O/L/s)");
        ADDITIONAL_PARAMETERS.put("RSBI", "Indice di Tobin (atti/min/L)");

        // (Neurologia / Neuro Monitoraggio)
        ADDITIONAL_PARAMETERS.put("GCS", "Scala di Glasgow (3-15)");
        ADDITIONAL_PARAMETERS.put("ICP", "Pressione Intracranica (mmHg)");
        ADDITIONAL_PARAMETERS.put("PRx", "Indice di Pressione Cerebrale"); // Unità? Aggiungere se nota
        ADDITIONAL_PARAMETERS.put("BIS", "Bispectral Index (0-100)");
        ADDITIONAL_PARAMETERS.put("TOF", "Train of Four (%)");

        // (Emodinamica / Terapia Intensiva)
        ADDITIONAL_PARAMETERS.put("CO", "Gittata Cardiaca (L/min)");
        ADDITIONAL_PARAMETERS.put("CI", "Indice Cardiaco (L/min/m²)");
        ADDITIONAL_PARAMETERS.put("PCWP", "Pressione Capillare Polmonare (mmHg)");
        ADDITIONAL_PARAMETERS.put("SvO2", "Saturazione Venosa Mista (%)");
        ADDITIONAL_PARAMETERS.put("SVR", "Resistenza Vascolare Sistemica (dyn·s·cm⁻⁵)");

        // (Metabolismo / Elettroliti)
        ADDITIONAL_PARAMETERS.put("GLY", "Glicemia (mg/dL)");
        ADDITIONAL_PARAMETERS.put("LAC", "Lattati (mmol/L)");
        ADDITIONAL_PARAMETERS.put("NA", "Sodio (mmol/L)");
        ADDITIONAL_PARAMETERS.put("K", "Potassio (mmol/L)");
        ADDITIONAL_PARAMETERS.put("CA", "Calcio ionizzato (mmol/L)");

        // (Nefrologia / Diuresi)
        ADDITIONAL_PARAMETERS.put("UO", "Diuresi oraria (mL/h)");
        ADDITIONAL_PARAMETERS.put("CR", "Creatinina (mg/dL)");
        ADDITIONAL_PARAMETERS.put("BUN", "Azotemia (mg/dL)");

        // (Infettivologia / Stato Infettivo)
        ADDITIONAL_PARAMETERS.put("WBC", "Globuli Bianchi (10³/μL)");
        ADDITIONAL_PARAMETERS.put("qSOFA", "qSOFA (0-4)"); // Unità? Scala numerica

        // (Coagulazione / Ematologia)
        ADDITIONAL_PARAMETERS.put("INR", "INR"); // Adimensionale
        ADDITIONAL_PARAMETERS.put("PTT", "PTT (sec)");
        ADDITIONAL_PARAMETERS.put("PLT", "Piastrine (10³/μL)");

        //(Altri Monitoraggi Specializzati)
        ADDITIONAL_PARAMETERS.put("pCO₂ cutanea", "pCO₂ cutanea (mmHg)");
        ADDITIONAL_PARAMETERS.put("NIRS", "Ossimetria cerebrale (%)");
    }
}
