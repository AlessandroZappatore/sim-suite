package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.service.export.PdfExportService;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static it.uniupo.simnova.service.export.PdfExportService.FONTREGULAR;
import static it.uniupo.simnova.service.export.PdfExportService.checkForNewPage;
import static it.uniupo.simnova.service.export.helper.pdf.ReplaceSubscript.replaceSubscriptCharacters;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.*;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawWrappedText;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.*;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.LEADING;

/**
 * Classe per la creazione della sezione Esami e Referti nel PDF.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioExam {
    /**
     * Logger per la classe ScenarioExam.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioExam.class);

    /**
     * Crea la sezione Esami e Referti nel PDF.
     *
     * @param scenarioId          L'ID dello scenario.
     * @param esam                true se la sezione deve essere creata, false altrimenti.
     * @param esameRefertoService Il servizio per recuperare gli esami e referti.
     * @throws IOException Se si verifica un errore durante la scrittura nel PDF.
     */
    public static void createExamsSection(Integer scenarioId, boolean esam, EsameRefertoService esameRefertoService) throws IOException {
        List<EsameReferto> esami = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);
        if (esami == null || esami.isEmpty() || !esam) {
            return;
        }

        // Controlla se lo spazio è sufficiente per la sezione
        checkForNewPage(LEADING * 5);

        // Imposta il titolo della sezione
        drawSection("Esami e Referti", "");


        for (EsameReferto esame : esami) {
            String examType = getExamType(esame);
            checkForNewPage(LEADING * 3);

            // Aggiunge la tipologia dell'esame
            drawSubsection(examType);

            // Aggiunge il referto testuale
            if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, "Referto: " + esame.getRefertoTestuale());
            }

            // Aggiunge il nome del media allegato
            if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                drawWrappedText(FONTREGULAR, SMALL_FONT_SIZE, MARGIN + 20, "Allegato: " + esame.getMedia());
            }

            // Sposta la posizione corrente per il prossimo esame
            PdfExportService.currentYPosition -= LEADING;
        }

        logger.info("Exams section created");
    }

    /**
     * Restituisce il tipo di esame con i caratteri in apice e pedice sostituiti.
     *
     * @param esame L'oggetto EsameReferto da cui estrarre il tipo di esame.
     * @return Il tipo di esame con i caratteri in apice e pedice sostituiti.
     */
    private static String getExamType(EsameReferto esame) {
        String examType = esame.getTipo();

        return replaceSubscriptCharacters(examType);
    }
}
