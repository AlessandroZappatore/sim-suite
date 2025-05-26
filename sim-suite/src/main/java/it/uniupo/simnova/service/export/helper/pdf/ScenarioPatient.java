package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.export.PdfExportService;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static it.uniupo.simnova.service.export.PdfExportService.*;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.*;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSubsection;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawWrappedText;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.renderHtmlWithFormatting;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.*;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.LEADING;

/**
 * Classe di utilità per la generazione della sezione "Stato Paziente" nel PDF.
 * Permette di esportare i parametri vitali, accessi venosi/arteriosi ed esame fisico
 * del paziente associato a uno scenario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioPatient {
    /**
     * Logger per la classe ScenarioPatient.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioPatient.class);

    /**
     * Crea la sezione "Stato Paziente" nel PDF, includendo parametri vitali,
     * accessi venosi/arteriosi ed esame fisico a seconda dei flag forniti.
     *
     * @param scenarioId         ID dello scenario di riferimento.
     * @param param              true per includere i parametri vitali.
     * @param acces              true per includere gli accessi venosi/arteriosi.
     * @param fisic              true per includere l'esame fisico.
     * @param pazienteT0Service  Service per recuperare i dati del paziente.
     * @param esameFisicoService Service per recuperare l'esame fisico.
     * @throws IOException In caso di errore nella scrittura del PDF.
     */
    public static void createPatientSection(Integer scenarioId, boolean param, boolean acces, boolean fisic, PazienteT0Service pazienteT0Service, EsameFisicoService esameFisicoService) throws IOException {
        // Se tutte le sezioni sono false, non fare nulla
        if (!param && !acces && !fisic) {
            return;
        }

        // Verifica se c'è spazio sufficiente per la sezione
        checkForNewPage(LEADING * 3); // Spazio stimato per drawSection

        // Titolo principale della sezione
        drawSection("Stato Paziente", "");

        // Recupera i dati del paziente T0
        PazienteT0 paziente = pazienteT0Service.getPazienteT0ById(scenarioId);
        if (paziente != null) {

            // Se richiesto, stampa i parametri vitali
            if (param) {
                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Parametri Vitali");

                // PA
                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("PA: %s mmHg", paziente.getPA()));

                // FC
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("FC: %d bpm", paziente.getFC()));

                // RR
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("RR: %d atti/min", paziente.getRR()));

                // Temperatura
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("Temperatura: %.1f °C", paziente.getT()));

                // SpO2
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("SpO2: %d%%", paziente.getSpO2()));

                // FiO2 (solo se > 0)
                if (paziente.getFiO2() > 0) {
                    checkForNewPage(LEADING * 2);
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("FiO2: %d%%", paziente.getFiO2()));
                }

                // Litri O2 (solo se > 0)
                if (paziente.getLitriO2() > 0) {
                    checkForNewPage(LEADING * 2);
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("Litri O2: %.1f L/min", paziente.getLitriO2()));
                }

                // EtCO2
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("EtCO2: %d mmHg", paziente.getEtCO2()));

                // Monitor (solo se presente)
                if (paziente.getMonitor() != null && !paziente.getMonitor().isEmpty()) {
                    checkForNewPage(LEADING * 2);
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("Monitor: %s", paziente.getMonitor()));
                }
                PdfExportService.currentYPosition -= LEADING; // Spazio dopo il blocco parametri vitali
            }

            // Accessi venosi
            List<Accesso> accessiVenosi = paziente.getAccessiVenosi();
            if (accessiVenosi != null && !accessiVenosi.isEmpty() && acces) {
                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Accessi Venosi");

                for (Accesso accesso : accessiVenosi) {
                    checkForNewPage(LEADING * 2); // Spazio stimato per una riga
                    // Descrizione accesso venoso
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, "• " + accesso.getTipologia() + " - " + accesso.getPosizione() + " (" + accesso.getLato() + ") - " + accesso.getMisura() + "G");
                }
                PdfExportService.currentYPosition -= LEADING; // Spazio dopo la sezione
            }

            // Accessi arteriosi
            List<Accesso> accessiArteriosi = paziente.getAccessiArteriosi();
            if (accessiArteriosi != null && !accessiArteriosi.isEmpty() && acces) {
                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Accessi Arteriosi");

                for (Accesso accesso : accessiArteriosi) {
                    checkForNewPage(LEADING * 2); // Spazio stimato per una riga
                    // Descrizione accesso arterioso
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, "• " + accesso.getTipologia() + " - " + accesso.getPosizione() + " (" + accesso.getLato() + ") - " + accesso.getMisura() + "G");
                }
                PdfExportService.currentYPosition -= LEADING; // Spazio dopo la sezione
            }
        }

        // Esame fisico
        EsameFisico esame = esameFisicoService.getEsameFisicoById(scenarioId);
        if (esame != null && esame.getSections() != null && !esame.getSections().isEmpty() && fisic) {
            // Verifica se tutte le sezioni sono vuote
            boolean allSectionsEmpty = esame.getSections().values().stream()
                .allMatch(value -> value == null || value.trim().isEmpty());

            if (!allSectionsEmpty) {
                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Esame Fisico");

                Map<String, String> sections = esame.getSections();

                for (Map.Entry<String, String> entry : sections.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    // Solo se chiave e valore sono valorizzati
                    if (key != null && !key.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                        checkForNewPage(LEADING * 4);

                        // Titolo della sottosezione (es. "Torace:")
                        drawWrappedText(FONTBOLD, BODY_FONT_SIZE, MARGIN + 20, key + ":");

                        // Testo formattato della sezione (può contenere HTML)
                        renderHtmlWithFormatting(value, MARGIN + 40);

                        PdfExportService.currentYPosition -= LEADING; // Spazio tra le entry
                    }
                }
                PdfExportService.currentYPosition -= LEADING; // Spazio dopo la sezione
            }
        }

        logger.info("Patient section creata con page break granulare e vitali riga per riga.");
    }
}
