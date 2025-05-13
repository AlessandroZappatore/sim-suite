package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.PdfExportService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static it.uniupo.simnova.service.export.PdfExportService.checkForNewPage;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSection;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.renderHtmlWithFormatting;
import static it.uniupo.simnova.views.constant.PdfConstant.*;

public class ScenarioSceneggiatura {
    private static final Logger logger = LoggerFactory.getLogger(ScenarioSceneggiatura.class);

    public static void createSceneggiaturaSection(Scenario scenario, boolean scen, PatientSimulatedScenarioService patientSimulatedScenarioService) throws IOException {
        String sceneggiatura = patientSimulatedScenarioService.getSceneggiatura(scenario.getId());
        if (sceneggiatura == null || sceneggiatura.isEmpty() || !scen) {
            return;
        }

        checkForNewPage(LEADING * 5);

        drawSection("Sceneggiatura", "");
        renderHtmlWithFormatting(sceneggiatura, MARGIN + 20);
        PdfExportService.currentYPosition -= LEADING;

        logger.info("Sceneggiatura section created");
    }
}
