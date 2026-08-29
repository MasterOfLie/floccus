package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.models.Agente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, String> {
    boolean existsByIpAgente(String ipAgente);
    boolean existsByIpAgenteAndIdNot(String ipAgente, String id);
}
