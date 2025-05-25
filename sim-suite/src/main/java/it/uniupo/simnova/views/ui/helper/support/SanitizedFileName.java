package it.uniupo.simnova.views.ui.helper.support;

public class SanitizedFileName {
    public static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "scenario";


        String sanitized = name.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");


        sanitized = sanitized.replaceAll("_+", "_");


        return sanitized.replaceAll("^_|_$", "");
    }
}
