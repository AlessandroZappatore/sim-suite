package it.uniupo.simnova.views.ui.helper.support;

public class SanitizedFileName {
    public static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "scenario";

        // Prima sostituzione: caratteri non validi con underscore
        String sanitized = name.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");

        // Seconda sostituzione: underscore multipli con singolo underscore
        sanitized = sanitized.replaceAll("_+", "_");

        // Rimuovi eventuali underscore all'inizio o alla fine
        return sanitized.replaceAll("^_|_$", "");
    }
}
