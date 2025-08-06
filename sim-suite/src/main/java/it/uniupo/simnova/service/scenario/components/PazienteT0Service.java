package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.repository.PazienteT0Repository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servizio per la gestione dei dati del paziente al tempo zero (T0) all'interno di uno scenario.
 * Gestisce i parametri vitali del paziente e gli accessi vascolari (venosi e arteriosi).
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class PazienteT0Service {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori relativi alle operazioni sui dati del paziente T0.
     */
    private static final Logger logger = LoggerFactory.getLogger(PazienteT0Service.class);

    private final PazienteT0Repository pazienteT0Repository;

    @Autowired
    public PazienteT0Service(PazienteT0Repository pazienteT0Repository) {
        this.pazienteT0Repository = pazienteT0Repository;
    }

    /**
     * Recupera un oggetto {@link PazienteT0} dal database, inclusi i suoi accessi venosi e arteriosi,
     * basandosi sull'ID dello scenario a cui è associato.
     *
     * @param scenarioId L'ID dello scenario per il quale recuperare i dati del paziente T0.
     * @return L'oggetto {@link PazienteT0} corrispondente all'ID dello scenario, o <code>null</code> se non trovato
     * o in caso di errore SQL.
     */
    @Transactional(readOnly = true)
    public PazienteT0 getPazienteT0ById(Integer scenarioId) {
        Optional<PazienteT0> pazienteT0Optional = pazienteT0Repository.findById(scenarioId);
        if (pazienteT0Optional.isEmpty()) {
            logger.warn("Nessun paziente T0 trovato per lo scenario con ID {}.", scenarioId);
            return null;
        }
        return pazienteT0Optional.get();
    }

    @Transactional
    public PazienteT0 savePazienteT0(PazienteT0 pazienteT0) {
        logger.info("Salvataggio o aggiornamento del paziente T0 con ID {}", pazienteT0.getId());
        PazienteT0 savedPaziente = pazienteT0Repository.save(pazienteT0);
        logger.info("Paziente T0 con ID {} salvato con successo.", savedPaziente.getId());
        return savedPaziente;
    }

    @Transactional
    public void updateMonitor(int scenarioId, String monitor) {
        logger.info("Aggiornamento del monitor per il paziente T0 con ID {}", scenarioId);
        PazienteT0 paziente = getPazienteT0ById(scenarioId);
        paziente.setMonitor(monitor);
        pazienteT0Repository.save(paziente);
        logger.info("Monitor per il paziente T0 con ID {} aggiornato a: '{}'", scenarioId, monitor);
    }

    @Transactional
    public void deleteAccesso(int scenarioId, int accessoId) {
        logger.info("Tentativo di eliminare l'accesso con ID {} dal paziente T0 con ID {}", accessoId, scenarioId);
        PazienteT0 paziente = getPazienteT0ById(scenarioId);

        boolean removed = paziente.getAccessi().removeIf(accesso -> accesso.getId().equals(accessoId));

        if (removed) {
            pazienteT0Repository.save(paziente);
            logger.info("Accesso con ID {} eliminato con successo dal paziente T0 con ID {}.", accessoId, scenarioId);
        } else {
            logger.warn("Nessun accesso con ID {} trovato per il paziente T0 con ID {}. Nessuna eliminazione eseguita.", accessoId, scenarioId);
            throw new EntityNotFoundException("Accesso non trovato con ID: " + accessoId + " per il paziente: " + scenarioId);
        }
    }

    @Transactional
    public void addAccesso(int scenarioId, Accesso accesso) {
        logger.info("Aggiunta di un accesso di tipo '{}' al paziente T0 con ID {}", accesso.getTipo(), scenarioId);
        PazienteT0 paziente = getPazienteT0ById(scenarioId); // Riutilizza il metodo di recupero
        paziente.getAccessi().add(accesso);
        pazienteT0Repository.save(paziente);
        logger.info("Accesso con ID {} aggiunto con successo al paziente T0 con ID {}", accesso.getId(), scenarioId);
    }
}