package br.dev.matheushenrique.floccus.dto;

import br.dev.matheushenrique.floccus.enums.StatusAplicacao;
import br.dev.matheushenrique.floccus.enums.TipoAplicacao;
import br.dev.matheushenrique.floccus.models.Aplicacao;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AplicacaoDTO(
        String id,
        String nome,
        String repositorioId,
        String imagem,
        String agenteId,
        String redeId,
        String containerId,
        TipoAplicacao tipo,
        Integer portaContainer,
        Integer portaExterna,
        String dominio,
        Long memoriaLimite,
        Long cpuLimite,
        String path,
        String linguagem,
        String framework,
        String branch,
        String idGitApp,
        String buildTool,
        StatusAplicacao status,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        String nomeAgente,
        String nomeRede,
        String runtime
)
{ }