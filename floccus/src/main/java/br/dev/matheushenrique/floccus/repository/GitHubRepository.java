package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.dto.GitHubDTO;
import br.dev.matheushenrique.floccus.models.GitHub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface GitHubRepository extends JpaRepository<GitHub, String> {
    Optional<GitHub> findByUsuarioIdAndId(String idUsuario, String idApp);
    Optional<GitHub> findByInstalacaoId(String instalacaoId);
    @Query("""
    SELECT new br.dev.matheushenrique.floccus.dto.GitHubDTO(
        g.id,
        g.usuarioId,
        g.appId,
        g.nomeApp,
        g.clienteId,
        g.instalacaoId,
        g.chaveCliente,
        g.webhookSecret,
        g.privateKeyPem,
        u.nome
    )
    FROM GitHub g
    JOIN Usuario u ON u.id = g.usuarioId
    WHERE u.email = :email
    """)
    Page<GitHubDTO> buscarPorEmail(
            String email,
            Pageable pageable
    );
    @Query("""
    SELECT COUNT(g) > 0
    FROM GitHub g
    JOIN Usuario u ON u.id = g.usuarioId
    WHERE g.id = :githubId
      AND u.email = :email
    """)
    boolean pertenceAoUsuario(String githubId, String email);
}
