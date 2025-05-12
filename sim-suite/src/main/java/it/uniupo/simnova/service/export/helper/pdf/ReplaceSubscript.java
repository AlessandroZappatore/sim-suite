package it.uniupo.simnova.service.export.helper.pdf;

public class ReplaceSubscript {
    public static String replaceSubscriptCharacters(String text) {
        if (text == null) return null;

        return text.replace('₁', '1')
                .replace('₂', '2')
                .replace('₃', '3')
                .replace('₄', '4')
                .replace('₅', '5')
                .replace('₆', '6')
                .replace('₇', '7')
                .replace('₈', '8')
                .replace('₉', '9')
                .replace('₀', '0')
                .replace('⁰', '0')
                .replace('¹', '1')
                .replace('²', '2')
                .replace('³', '3')
                .replace('⁴', '4')
                .replace('⁵', '5')
                .replace('⁶', '6')
                .replace('⁷', '7')
                .replace('⁸', '8')
                .replace('⁹', '9')
                .replace('⁻', '-')
                .replace('⁺', '+');
    }
}
