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
import static it.uniupo.simnova.views.constant.PdfConstant.*;
import static it.uniupo.simnova.views.constant.PdfConstant.LEADING;

public class ScenarioPatient {
    private static final Logger logger = LoggerFactory.getLogger(ScenarioPatient.class);

    public static void createPatientSection(Integer scenarioId, boolean param, boolean acces, boolean fisic, PazienteT0Service pazienteT0Service, EsameFisicoService esameFisicoService) throws IOException {
        if (!param && !acces && !fisic) {
            return; // Se tutte le sezioni sono false, non fare nulla
        }

        checkForNewPage(LEADING * 3); // Spazio stimato per drawSection

        drawSection("Stato Paziente", "");

        PazienteT0 paziente = pazienteT0Service.getPazienteT0ById(scenarioId);
        if (paziente != null) {


            if (param) {
                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Parametri Vitali");

                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("PA: %s mmHg", paziente.getPA()));

                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("FC: %d bpm", paziente.getFC()));

                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("RR: %d atti/min", paziente.getRR()));

                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("Temperatura: %.1f °C", paziente.getT()));

                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("SpO2: %d%%", paziente.getSpO2()));


                if (paziente.getFiO2() > 0) {
                    checkForNewPage(LEADING * 2); // Spazio per una riga

                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("FiO2: %d%%", paziente.getFiO2())); // Esempio: formatta come intero %
                }


                if (paziente.getLitriO2() > 0) {
                    checkForNewPage(LEADING * 2); // Spazio per una riga
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("Litri O2: %.1f L/min", paziente.getLitriO2())); // Esempio: formatta con un decimale L/min
                }

                checkForNewPage(LEADING * 2); // Spazio per una riga
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("EtCO2: %d mmHg", paziente.getEtCO2()));

                if (paziente.getMonitor() != null && !paziente.getMonitor().isEmpty()) {
                    checkForNewPage(LEADING * 2); // Spazio per una riga
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("Monitor: %s", paziente.getMonitor()));
                }
                PdfExportService.currentYPosition -= LEADING; // Aggiungi un piccolo spazio dopo il blocco dei parametri vitali
            }

            List<Accesso> accessiVenosi = paziente.getAccessiVenosi();
            if (accessiVenosi != null && !accessiVenosi.isEmpty() && acces) {

                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Accessi Venosi");

                for (Accesso accesso : accessiVenosi) {

                    checkForNewPage(LEADING * 2); // Spazio stimato per una riga
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, "• " + accesso.getTipologia() + " - " + accesso.getPosizione() + " (" + accesso.getLato() + ") - " + accesso.getMisura() + "G");
                }
                PdfExportService.currentYPosition -= LEADING; // Aggiungi spazio dopo la sezione
            }

            List<Accesso> accessiArteriosi = paziente.getAccessiArteriosi();
            if (accessiArteriosi != null && !accessiArteriosi.isEmpty() && acces) {

                checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
                drawSubsection("Accessi Arteriosi");

                for (Accesso accesso : accessiArteriosi) {

                    checkForNewPage(LEADING * 2); // Spazio stimato per una riga
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, "• " + accesso.getTipologia() + " - " + accesso.getPosizione() + " (" + accesso.getLato() + ") - " + accesso.getMisura() + "G");
                }
                PdfExportService.currentYPosition -= LEADING; // Aggiungi spazio dopo la sezione
            }
        }

        EsameFisico esame = esameFisicoService.getEsameFisicoById(scenarioId);
        if (esame != null && esame.getSections() != null && !esame.getSections().isEmpty() && fisic) {

            checkForNewPage(LEADING * 3); // Spazio stimato per drawSubsection
            drawSubsection("Esame Fisico");

            Map<String, String> sections = esame.getSections();

            for (Map.Entry<String, String> entry : sections.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key != null && !key.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                    checkForNewPage(LEADING * 4);

                    drawWrappedText(FONTBOLD, BODY_FONT_SIZE, MARGIN + 20, key + ":");

                    renderHtmlWithFormatting(value, MARGIN + 40);

                    PdfExportService.currentYPosition -= LEADING; // Aggiungi piccolo spazio tra le entry
                }
            }
            PdfExportService.currentYPosition -= LEADING; // Aggiungi spazio dopo la sezione
        }

        logger.info("Patient section creata con page break granulare e vitali riga per riga.");
    }
}
