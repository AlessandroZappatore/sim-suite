package it.uniupo.simnova.views.ui.helper.support;

public class SanitizedFileName {
    public static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "scenario";
        return name.replaceAll("[\\\\/:*?\"<>|\\s]+", "_").replaceAll("_+", "_");
    }
}
