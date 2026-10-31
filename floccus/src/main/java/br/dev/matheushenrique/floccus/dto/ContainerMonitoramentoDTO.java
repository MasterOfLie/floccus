package br.dev.matheushenrique.floccus.dto;

public record ContainerMonitoramentoDTO(
        String aplicacaoId,
        String containerId,
        String status,
        double cpuPercentual,
        long memoriaUso,
        long memoriaLimite,
        double memoriaPercentual,
        long redeRecebida,
        long redeEnviada

) {
}
