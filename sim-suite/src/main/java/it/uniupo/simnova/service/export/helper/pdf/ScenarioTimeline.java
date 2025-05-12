package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.PdfExportService;
import it.uniupo.simnova.service.scenario.ScenarioService;

import java.io.IOException;
import java.util.List;

import static it.uniupo.simnova.service.export.PdfExportService.*;
import static it.uniupo.simnova.service.export.helper.pdf.ReplaceSubscript.replaceSubscriptCharacters;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.*;
import static it.uniupo.simnova.views.constant.PdfConstant.*;
import static it.uniupo.simnova.views.constant.PdfConstant.LEADING;

public class ScenarioTimeline {
    public static void createTimelineSection(Scenario scenario, ScenarioService scenarioService) throws IOException {
        List<Tempo> tempi = scenarioService.getTempiByScenarioId(scenario.getId());
        if (tempi.isEmpty()) {
            return;
        }

        float spazioTitoloSezione = LEADING * 3; // Spazio stimato per drawSection("Timeline", "")
        float spazioTitoloPrimoTempo = LEADING * 3; // Spazio stimato per drawSubsection del primo tempo
        checkForNewPage(spazioTitoloSezione + spazioTitoloPrimoTempo);

        drawSection("Timeline", "");
        for (int i = 0; i < tempi.size(); i++) {
            Tempo tempo = tempi.get(i);

            String title = String.format("Tempo %d (%.1f min)",
                    tempo.getIdTempo(),
                    tempo.getTimerTempo() / 60.0
            );
            checkForNewPage(LEADING * 3);
            drawSubsection(title);

            float paramsIndent = MARGIN + 20;

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("PA: %s mmHg", tempo.getPA()));

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("FC: %d bpm", tempo.getFC()));

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("RR: %d atti/min", tempo.getRR()));

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("Temperatura: %.1f °C", tempo.getT()));

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("SpO2: %d%%", tempo.getSpO2()));


            Number fio2 = tempo.getFiO2();
            if (fio2 != null && fio2.doubleValue() > 0) {
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("FiO2: %.0f%%", fio2.doubleValue()));
            }


            Number litriO2 = tempo.getLitriO2();
            if (litriO2 != null && litriO2.doubleValue() > 0) {
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("Litri O2: %.1f L/min", litriO2.doubleValue()));
            }

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("EtCO2: %d mmHg", tempo.getEtCO2()));

            List<ParametroAggiuntivo> parametriAggiuntivo = ScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenario.getId());
            if (!parametriAggiuntivo.isEmpty()) {
                for (ParametroAggiuntivo parametro : parametriAggiuntivo) {
                    checkForNewPage(LEADING * 2);
                    String parametroNome = replaceSubscriptCharacters(parametro.getNome());
                    String parametroUnita = replaceSubscriptCharacters(parametro.getUnitaMisura());
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent,
                            String.format("%s: %s %s", parametroNome, parametro.getValore(), parametroUnita));
                }
            }
            PdfExportService.currentYPosition -= LEADING;

            float detailsLabelIndent = MARGIN + 20;
            float detailsTextIndent = MARGIN + 30;

            if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Dettagli:");
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, detailsTextIndent, tempo.getAltriDettagli());
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            if (scenarioService.isPediatric(scenario.getId()) && tempo.getRuoloGenitore() != null && !tempo.getRuoloGenitore().isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Ruolo del genitore:");
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, detailsTextIndent, tempo.getRuoloGenitore());
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            String azione = tempo.getAzione();
            if (azione != null && !azione.isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Azioni da svolgere per passare a → T" + tempo.getTSi() + ":");
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, detailsTextIndent, azione);
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            if (tempo.getTNo() >= 0) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Se non vengono svolte le azioni passare a → T" + tempo.getTNo());
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            if (i < tempi.size() - 1) {
                checkForNewPage(LEADING * 2);
                PdfExportService.currentYPosition -= LEADING;
            }
        }
    }
}
