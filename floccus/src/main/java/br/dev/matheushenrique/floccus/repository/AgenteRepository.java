package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.models.Agente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, String> {
    boolean existsByIpAgente(String ipAgente);
    boolean existsByIpAgenteAndIdNot(String ipAgente, String id);

    @Query("SELECT a.chaveAgente FROM Agente a WHERE a.id = :id")
    Optional<String> findChaveAgenteById(@Param("id") String id);
}
