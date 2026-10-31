package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.dto.AplicacaoDTO;
import br.dev.matheushenrique.floccus.models.Aplicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AplicacaoRepository extends JpaRepository<Aplicacao, String> {
    List<Aplicacao> findByIdGitAppAndRepositorioIdAndBranch(
            String idGitApp,
            String repositorioId,
            String branch
    );
    boolean existsByRedeId(String redeId);
    boolean existsByAgenteId(String agenteId);
    @Query("""
    SELECT new br.dev.matheushenrique.floccus.dto.AplicacaoDTO(
        a.id,
        a.nome,
        a.repositorioId,
        a.imagem,
        a.agenteId,
        a.redeId,
        a.containerId,
        a.tipo,
        a.portaContainer,
        a.portaExterna,
        a.dominio,
        a.memoriaLimite,
        a.cpuLimite,
        a.path,
        a.linguagem,
        a.framework,
        a.branch,
        a.idGitApp,
        a.buildTool,
        a.status,
        a.criadoEm,
        a.atualizadoEm,
        ag.nome,
        r.nome,
        a.runtime
    )
    FROM Aplicacao a
    LEFT JOIN Agente ag ON ag.id = a.agenteId
    LEFT JOIN Rede r ON r.id = a.redeId
    WHERE a.id = :id
""")
    Optional<AplicacaoDTO> buscarDTOPorId(@Param("id") String id);

    @Query("""
    SELECT new br.dev.matheushenrique.floccus.dto.AplicacaoDTO(
        a.id,
        a.nome,
        a.repositorioId,
        a.imagem,
        a.agenteId,
        a.redeId,
        a.containerId,
        a.tipo,
        a.portaContainer,
        a.portaExterna,
        a.dominio,
        a.memoriaLimite,
        a.cpuLimite,
        a.path,
        a.linguagem,
        a.framework,
        a.branch,
        a.idGitApp,
        a.buildTool,
        a.status,
        a.criadoEm,
        a.atualizadoEm,
        ag.nome,
        r.nome,
        a.runtime
    )
    FROM Aplicacao a
    LEFT JOIN Agente ag ON ag.id = a.agenteId
    LEFT JOIN Rede r ON r.id = a.redeId
    WHERE a.usuarioId = :usuarioId
    """)
    List<AplicacaoDTO> buscarTodasPorUsuario(
            @Param("usuarioId") String usuarioId
    );
}
