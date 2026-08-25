package br.dev.matheushenrique.floccus.dto;

import io.grpc.ManagedChannel;

public record ContainerLogConnectionDTO(
        String containerId,
        String sessionId,
        ManagedChannel channel) {
}