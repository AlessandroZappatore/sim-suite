package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.PdfExportService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;

import java.io.IOException;
import java.util.List;

import static it.uniupo.simnova.service.export.PdfExportService.*;
import static it.uniupo.simnova.service.export.helper.pdf.ReplaceSubscript.replaceSubscriptCharacters;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.*;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.*;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.LEADING;

/**
 * Classe di utilità per la generazione della sezione "Timeline" nel PDF.
 * Permette di esportare la sequenza temporale (tempi) di uno scenario, con i relativi parametri e dettagli.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioTimeline {
    /**
     * Crea la sezione "Timeline" nel PDF, includendo tutti i tempi dello scenario e i relativi parametri.
     * Ogni tempo viene presentato con i parametri vitali, eventuali parametri aggiuntivi, dettagli e azioni.
     *
     * @param scenario                Scenario di riferimento.
     * @param advancedScenarioService Service per recuperare i dati avanzati dello scenario.
     * @param scenarioService         Service per informazioni aggiuntive sullo scenario (es. se pediatrico).
     * @throws IOException In caso di errore nella scrittura del PDF.
     */
    public static void createTimelineSection(Scenario scenario, AdvancedScenarioService advancedScenarioService, ScenarioService scenarioService) throws IOException {
        // Recupera la lista dei tempi associati allo scenario
        List<Tempo> tempi = advancedScenarioService.getTempiByScenarioId(scenario.getId());
        // Se non ci sono tempi, esce senza fare nulla
        if (tempi.isEmpty()) {
            return;
        }

        float spazioTitoloSezione = LEADING * 3; // Spazio stimato per il titolo della sezione
        float spazioTitoloPrimoTempo = LEADING * 3; // Spazio stimato per il titolo del primo tempo
        // Verifica se c'è spazio sufficiente per la sezione e il primo tempo
        checkForNewPage(spazioTitoloSezione + spazioTitoloPrimoTempo);

        // Scrive il titolo della sezione "Timeline"
        drawSection("Timeline", "");
        // Ciclo su tutti i tempi della timeline
        for (int i = 0; i < tempi.size(); i++) {
            Tempo tempo = tempi.get(i);

            // Costruisce il titolo del tempo (es. "Tempo 1 (2.0 min)")
            String title = String.format("Tempo %d (%.1f min)",
                    tempo.getIdTempo(),
                    tempo.getTimerTempo() / 60.0
            );
            // Verifica spazio per il titolo del tempo
            checkForNewPage(LEADING * 3);
            // Scrive il titolo del tempo
            drawSubsection(title);

            float paramsIndent = MARGIN + 20; // Indentazione per i parametri

            // Parametri vitali principali
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

            // FiO2 (solo se presente e > 0)
            Number fio2 = tempo.getFiO2();
            if (fio2 != null && fio2.doubleValue() > 0) {
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("FiO2: %.0f%%", fio2.doubleValue()));
            }

            // Litri O2 (solo se presente e > 0)
            Number litriO2 = tempo.getLitriO2();
            if (litriO2 != null && litriO2.doubleValue() > 0) {
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("Litri O2: %.1f L/min", litriO2.doubleValue()));
            }

            // EtCO2
            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("EtCO2: %d mmHg", tempo.getEtCO2()));

            // Parametri aggiuntivi (se presenti)
            List<ParametroAggiuntivo> parametriAggiuntivo = advancedScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenario.getId());
            if (!parametriAggiuntivo.isEmpty()) {
                for (ParametroAggiuntivo parametro : parametriAggiuntivo) {
                    checkForNewPage(LEADING * 2);
                    // Sostituisce eventuali caratteri di pedice nel nome e nell'unità di misura
                    String parametroNome = replaceSubscriptCharacters(parametro.getNome());
                    String parametroUnita = replaceSubscriptCharacters(parametro.getUnitaMisura());
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent,
                            String.format("%s: %s %s", parametroNome, parametro.getValore(), parametroUnita));
                }
            }
            // Spazio dopo i parametri
            PdfExportService.currentYPosition -= LEADING;

            float detailsLabelIndent = MARGIN + 20; // Indentazione per le etichette dei dettagli
            float detailsTextIndent = MARGIN + 30; // Indentazione per il testo dei dettagli

            // Se sono presenti dettagli aggiuntivi, li stampa
            if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Dettagli:");
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, detailsTextIndent, tempo.getAltriDettagli());
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            // Se lo scenario è pediatrico e c'è un ruolo del genitore, lo stampa
            if (scenarioService.isPediatric(scenario.getId()) && tempo.getRuoloGenitore() != null && !tempo.getRuoloGenitore().isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Ruolo del genitore:");
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, detailsTextIndent, tempo.getRuoloGenitore());
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            // Se sono presenti azioni, le stampa con il riferimento al tempo successivo
            String azione = tempo.getAzione();
            if (azione != null && !azione.isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Azioni da svolgere per passare a → T" + tempo.getTSi() + ":");
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, detailsTextIndent, azione);
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            // Se è definito il tempo alternativo (TNo), lo stampa
            if (tempo.getTNo() >= 0) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Se non vengono svolte le azioni passare a → T" + tempo.getTNo());
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            // Spazio tra un tempo e il successivo
            if (i < tempi.size() - 1) {
                checkForNewPage(LEADING * 2);
                PdfExportService.currentYPosition -= LEADING;
            }
        }
    }
}
