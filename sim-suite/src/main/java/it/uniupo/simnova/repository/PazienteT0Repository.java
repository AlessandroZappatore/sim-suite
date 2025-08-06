package it.uniupo.simnova.repository;

import it.uniupo.simnova.domain.paziente.PazienteT0;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PazienteT0Repository extends JpaRepository<PazienteT0, Integer> {

}
