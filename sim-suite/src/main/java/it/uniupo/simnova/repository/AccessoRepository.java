package it.uniupo.simnova.repository;

import it.uniupo.simnova.domain.common.Accesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessoRepository extends JpaRepository<Accesso, Integer> {
}
