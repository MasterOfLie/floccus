package br.dev.matheushenrique.floccus.dto;

import br.dev.matheushenrique.floccus.enums.TipoAplicacao;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AplicacaoDTO(String id, String nome,
                           String imagem,
                           String agenteId,
                           String containerId,
                           TipoAplicacao tipo,
                           Integer portaContainer,
                           Integer portaExterna, String dominio,
                           Long memoriaLimite,
                           Long cpuLimite,
                           LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
}
