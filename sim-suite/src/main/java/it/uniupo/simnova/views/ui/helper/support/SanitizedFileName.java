package it.uniupo.simnova.views.ui.helper.support;

import lombok.NoArgsConstructor;

/**
 * Utility class per la sanificazione dei nomi di file.
 * Questo assicura che i nomi dei file siano validi e sicuri per l'utilizzo nei sistemi operativi.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@NoArgsConstructor
public class SanitizedFileName {
    /**
     * Sanifica una stringa rimuovendo o sostituendo caratteri non validi per i nomi di file.
     * I caratteri invalidi includono: \, /, :, *, ?, ", &lt;, &gt;, |, e spazi.
     * Vengono sostituiti con underscore e gli underscore multipli vengono ridotti a uno singolo.
     * Gli underscore all'inizio o alla fine del nome vengono rimossi.
     *
     * @param name Il nome del file da sanificare.
     * @return Il nome del file sanificato. Restituisce "scenario" se il nome è null o vuoto.
     */
    public static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "scenario";
        }

        // Sostituisce i caratteri speciali e gli spazi con underscore
        String sanitized = name.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");

        // Rimuove underscore consecutivi
        sanitized = sanitized.replaceAll("_+", "_");

        // Rimuove underscore all'inizio o alla fine della stringa
        return sanitized.replaceAll("^_|_$", "");
    }
}