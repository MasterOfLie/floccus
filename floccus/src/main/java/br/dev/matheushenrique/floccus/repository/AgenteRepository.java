package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.dto.AgenteDTO;
import br.dev.matheushenrique.floccus.models.Agente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, String> {
    boolean existsByIpAgente(String ipAgente);
    boolean existsByIpAgenteAndIdNot(String ipAgente, String id);
    @Query("""
    SELECT a FROM Agente a
    WHERE LOWER(a.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
       OR LOWER(a.ipAgente) LIKE LOWER(CONCAT('%', :busca, '%'))
    """)
    Page<Agente> buscarPaginado(@Param("busca") String busca, Pageable pageable);
    @Query("SELECT a.chaveAgente FROM Agente a WHERE a.id = :id")
    Optional<String> findChaveAgenteById(@Param("id") String id);

    @Query("""
        SELECT new br.dev.matheushenrique.floccus.dto.AgenteDTO(
            a.nome,
            a.id,
            a.ipAgente,
            a.portaAgente,
            a.chaveAgente,
            a.ipBackend,
            a.portaBackend,
            a.dockerHost
        )
        FROM Agente a
    """)
    List<AgenteDTO> listarTodosDTO();
}
