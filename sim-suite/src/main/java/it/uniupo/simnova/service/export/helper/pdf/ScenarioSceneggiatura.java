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
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.*;

/**
 * Classe di utilità per la generazione della sezione "Sceneggiatura" nel PDF.
 * Permette di esportare la sceneggiatura associata a uno scenario, se presente.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioSceneggiatura {
    /**
     * Logger per la classe ScenarioSceneggiatura.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioSceneggiatura.class);

    /**
     * Crea la sezione "Sceneggiatura" nel PDF, se abilitata e se la sceneggiatura è presente.
     * Recupera la sceneggiatura tramite il service e la inserisce nel PDF con formattazione HTML.
     *
     * @param scenario                        Scenario di riferimento.
     * @param scen                            true per includere la sezione sceneggiatura.
     * @param patientSimulatedScenarioService Service per recuperare la sceneggiatura.
     * @throws IOException In caso di errore nella scrittura del PDF.
     */
    public static void createSceneggiaturaSection(Scenario scenario, boolean scen, PatientSimulatedScenarioService patientSimulatedScenarioService) throws IOException {
        // Recupera la sceneggiatura associata allo scenario tramite il service
        String sceneggiatura = patientSimulatedScenarioService.getSceneggiatura(scenario.getId());
        // Se la sceneggiatura è nulla, vuota o la sezione non è richiesta, esce senza fare nulla
        if (sceneggiatura == null || sceneggiatura.isEmpty() || !scen) {
            return;
        }

        // Verifica se c'è spazio sufficiente per la sezione, altrimenti va a nuova pagina
        checkForNewPage(LEADING * 5);

        // Scrive il titolo della sezione "Sceneggiatura"
        drawSection("Sceneggiatura", "");
        // Inserisce la sceneggiatura con formattazione HTML e margine a sinistra
        renderHtmlWithFormatting(sceneggiatura, MARGIN + 20);
        // Aggiunge uno spazio verticale dopo la sezione
        PdfExportService.currentYPosition -= LEADING;

        // Logga la creazione della sezione per debug/tracciamento
        logger.info("Sceneggiatura section created");
    }
}

