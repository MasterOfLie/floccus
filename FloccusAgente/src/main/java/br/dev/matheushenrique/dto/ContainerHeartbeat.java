package br.dev.matheushenrique.dto;

import java.util.List;

public record ContainerHeartbeat(
        String agenteId,
        long timestamp,
        List<ContainerMonitoramentoDTO> containers
) {
}
