package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.dto.RedeDTO;
import br.dev.matheushenrique.floccus.models.Rede;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RedeRepository extends JpaRepository<Rede, String> {
    List<Rede> findByAgenteId(String agenteId);
    boolean existsByNomeAndAgenteId(String nome, String agenteId);
    boolean existsBySubnetAndAgenteId(String subnet, String agenteId);
    @Query("""
        SELECT new br.dev.matheushenrique.floccus.dto.RedeDTO(
            r.id,
            r.nome,
            r.subnet,
            r.gateway,
            r.agenteId,
            a.nome
        )
        FROM Rede r
        JOIN Agente a ON a.id = r.agenteId
        WHERE r.usuarioId = :usuarioId
    """)
    Page<RedeDTO> listarPorUsuario(
            @Param("usuarioId") String usuarioId,
            Pageable pageable
    );
    @Query("""
        SELECT new br.dev.matheushenrique.floccus.dto.RedeDTO(
            r.id,
            r.nome,
            r.subnet,
            r.gateway,
            r.agenteId,
            a.nome
        )
        FROM Rede r
        JOIN Agente a ON a.id = r.agenteId
        WHERE r.usuarioId = :usuarioId
    """)
    List<RedeDTO> listarPorUsuario(@Param("usuarioId") String usuarioId);
}
