package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.repository.EsameRefertoRepository;
import it.uniupo.simnova.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione degli esami e dei referti associati ai pazienti all'interno degli scenari.
 * Fornisce funzionalità per salvare, recuperare ed eliminare i referti degli esami,
 * inclusa la gestione dei file multimediali a essi collegati.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class EsameRefertoService {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori relativi alle operazioni su esami e referti.
     */
    private static final Logger logger = LoggerFactory.getLogger(EsameRefertoService.class);

    /**
     * Il servizio per la gestione dello storage dei file, utilizzato per eliminare i file multimediali associati ai referti.
     */
    private final FileStorageService fileStorageService;

    private final EsameRefertoRepository esameRefertoRepository;

    /**
     * Costruisce una nuova istanza di <code>EsameRefertoService</code>.
     * Inietta il servizio {@link FileStorageService} necessario per le operazioni sui file.
     *
     * @param fileStorageService Il servizio per la gestione dei file.
     */
    public EsameRefertoService(FileStorageService fileStorageService, EsameRefertoRepository esameRefertoRepository) {
        this.fileStorageService = fileStorageService;
        this.esameRefertoRepository = esameRefertoRepository;
    }

    @Transactional
    public boolean saveEsamiReferti(Integer idScenario, List<EsameReferto> esamiReferti) {
        try {
            // Prima elimina i referti esistenti per lo scenario
            List<EsameReferto> esistenti = esameRefertoRepository.findByIdScenario(idScenario);
            if (!esistenti.isEmpty()) {
                // Verifica che le entità non siano null prima di eliminarle
                esistenti.removeIf(Objects::isNull);
                esameRefertoRepository.deleteAll(esistenti);
                esameRefertoRepository.flush(); // Forza l'eliminazione prima di salvare i nuovi
            }

            // Poi salva i nuovi referti
            if (esamiReferti != null && !esamiReferti.isEmpty()) {
                // Filtra eventuali entità null
                List<EsameReferto> refertiValidi = esamiReferti.stream()
                        .filter(Objects::nonNull)
                        .filter(referto -> referto.getIdScenario() != null)
                        .collect(Collectors.toList());

                if (!refertiValidi.isEmpty()) {
                    esameRefertoRepository.saveAll(refertiValidi);
                }
                return true;
            }
        } catch (Exception e) {
            logger.error("Errore durante il salvataggio dei referti per lo scenario con ID {}: {}",
                    idScenario, e.getMessage());
            throw new RuntimeException("Errore durante il salvataggio: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Recupera tutti gli oggetti {@link EsameReferto} associati a uno scenario specifico.
     * I referti vengono ordinati per <code>id_esame</code>.
     *
     * @param scenarioId L'ID dello scenario per cui recuperare gli esami e i referti.
     * @return Una {@link List} di oggetti {@link EsameReferto} associati allo scenario.
     * Restituisce una lista vuota se non vengono trovati referti o in caso di errore.
     */
    @Transactional(readOnly = true)
    public List<EsameReferto> getEsamiRefertiByScenarioId(int scenarioId) {
        List<EsameReferto> esamiReferti = esameRefertoRepository.findByIdScenario(scenarioId);
        if (esamiReferti.isEmpty()) {
            logger.warn("Nessun referto trovato per lo scenario con ID {}.", scenarioId);
            return null;
        } else {
            logger.info("Recuperati {} referti per lo scenario con ID {}.", esamiReferti.size(), scenarioId);
            return esamiReferti;
        }
    }

    /**
     * Elimina un singolo referto di esame specifico dal database e il file multimediale a esso associato.
     *
     * @param idEsameReferto L'ID del referto dell'esame da eliminare.
     * @param scenarioId     L'ID dello scenario a cui appartiene il referto.
     * @return <code>true</code> se l'eliminazione del referto e del suo file media associato è avvenuta con successo; <code>false</code> altrimenti.
     */
    @Transactional
    public boolean deleteEsameReferto(int idEsameReferto, int scenarioId) {
        // Recupera il nome del file multimediale associato al referto prima di eliminarlo dal DB.
        String mediaFilename = getMediaFilenameByEsameId(idEsameReferto, scenarioId);

        // Se esiste un nome di file multimediale, tenta di eliminare il file.
        if (mediaFilename != null && !mediaFilename.isEmpty()) {
            fileStorageService.deleteFile(mediaFilename);
            logger.info("File media '{}' dell'esame con ID {} eliminato con successo.", mediaFilename, idEsameReferto);
        }

        return esameRefertoRepository.deleteEsameRefertoByIds(idEsameReferto, scenarioId) > 0;
    }

    /**
     * Recupera il nome del file multimediale associato a un esame specifico in un dato scenario.
     *
     * @param idEsame    L'ID dell'esame di cui si vuole recuperare il nome del file multimediale.
     * @param scenarioId L'ID dello scenario a cui l'esame appartiene.
     * @return Il nome del file multimediale (<code>String</code>) se trovato; <code>null</code> altrimenti.
     */
    private String getMediaFilenameByEsameId(int idEsame, int scenarioId) {
        Optional<String> mediaFilename = esameRefertoRepository.findMediaByIdEsameAndIdScenario(idEsame, scenarioId);
        if (mediaFilename.isPresent()) {
            logger.info("Recuperato il nome del file media '{}' per l'esame con ID {} nello scenario con ID {}.", mediaFilename.get(), idEsame, scenarioId);
            return mediaFilename.get();
        } else {
            logger.warn("Nessun file media trovato per l'esame con ID {} nello scenario con ID {}.", idEsame, scenarioId);
            return null;
        }
    }

    /**
     * Aggiorna il nome del file multimediale associato a un esame specifico in un dato scenario.
     *
     * @param idEsame          L'ID dell'esame di cui aggiornare il file multimediale.
     * @param scenarioId       L'ID dello scenario a cui l'esame appartiene.
     * @param newMediaFileName Il nuovo nome del file multimediale da associare all'esame.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    @Transactional
    public boolean updateMedia(int idEsame, Integer scenarioId, String newMediaFileName) {
        return esameRefertoRepository.updateMediaByIdEsameAndIdScenario(idEsame, scenarioId, newMediaFileName);
    }

    /**
     * Aggiorna il referto testuale di un esame specifico in un dato scenario.
     *
     * @param idEsame      L'ID dell'esame di cui aggiornare il referto testuale.
     * @param scenarioId   L'ID dello scenario a cui l'esame appartiene.
     * @param nuovoReferto Il nuovo testo del referto da salvare.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    @Transactional
    public boolean updateRefertoTestuale(int idEsame, Integer scenarioId, String nuovoReferto) {
        return esameRefertoRepository.updateRefertoTestualeByIdEsameAndIdScenario(idEsame, scenarioId, nuovoReferto);
    }
}