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
import static it.uniupo.simnova.views.constant.PdfConstant.*;
import static it.uniupo.simnova.views.constant.PdfConstant.LEADING;

public class ScenarioExam {
    private static final Logger logger = LoggerFactory.getLogger(ScenarioExam.class);

    public static void createExamsSection(Integer scenarioId, boolean esam, EsameRefertoService esameRefertoService) throws IOException {
        List<EsameReferto> esami = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);
        if (esami == null || esami.isEmpty() || !esam) {
            return;
        }

        checkForNewPage(LEADING * 5);

        drawSection("Esami e Referti", "");


        for (EsameReferto esame : esami) {
            String examType = getExamType(esame);
            checkForNewPage(LEADING * 3);

            drawSubsection(examType);

            if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, "Referto: " + esame.getRefertoTestuale());
            }

            if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                drawWrappedText(FONTREGULAR, SMALL_FONT_SIZE, MARGIN + 20, "Allegato: " + esame.getMedia());
            }

            PdfExportService.currentYPosition -= LEADING;
        }

        logger.info("Exams section created");
    }

    private static String getExamType(EsameReferto esame) {
        String examType = esame.getTipo();

        return replaceSubscriptCharacters(examType);
    }
}
