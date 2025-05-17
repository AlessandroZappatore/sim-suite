package it.uniupo.simnova.service.scenario.operations;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class UnZipScenarioService {

    public static final String SCENARIO_JSON_FILENAME = "scenario.json";
    public static final String MEDIA_FOLDER_PREFIX = "esami/";

    /**
         * Contiene i dati estratti da un file ZIP dello scenario.
         */
        public record UnzippedScenarioData(byte[] scenarioJson, Map<String, byte[]> mediaFiles) {
    }

    /**
     * Decomprime il file ZIP fornito come InputStream ed estrae il file scenario.json
     * e i file contenuti nella cartella media/.
     *
     * @param zipInputStream L'InputStream del file ZIP.
     * @return Un oggetto UnzippedScenarioData contenente i dati dello scenario.json e i file multimediali.
     * @throws IOException Se si verifica un errore durante la lettura dello ZIP.
     * @throws IllegalArgumentException Se il file scenario.json non viene trovato nello ZIP.
     */
    public UnzippedScenarioData unzipScenario(InputStream zipInputStream) throws IOException {
        byte[] scenarioJson = null;
        Map<String, byte[]> mediaFiles = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    if (SCENARIO_JSON_FILENAME.equalsIgnoreCase(zipEntry.getName())) {
                        scenarioJson = readEntryData(zis);
                    } else if (zipEntry.getName().toLowerCase().startsWith(MEDIA_FOLDER_PREFIX)) {
                        String mediaFileName = zipEntry.getName().substring(MEDIA_FOLDER_PREFIX.length());
                        if (!mediaFileName.isEmpty()) {
                            mediaFiles.put(mediaFileName, readEntryData(zis));
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        if (scenarioJson == null) {
            throw new IllegalArgumentException("File '" + SCENARIO_JSON_FILENAME + "' non trovato nell'archivio ZIP.");
        }

        return new UnzippedScenarioData(scenarioJson, mediaFiles);
    }

    private byte[] readEntryData(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }
}
