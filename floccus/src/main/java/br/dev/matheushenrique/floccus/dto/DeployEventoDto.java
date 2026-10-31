package br.dev.matheushenrique.floccus.dto;

public record DeployEventoDto(
        String aplicacaoId,
        String tipo,
        String mensagem,
        String containerId
) {
}
