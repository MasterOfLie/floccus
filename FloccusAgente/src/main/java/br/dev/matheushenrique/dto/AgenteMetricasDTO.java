package br.dev.matheushenrique.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AgenteMetricasDTO(
        double cpuUsage,
        double ramUsadaMb,
        double ramTotalMb,
        double discoUsadoGb,
        double discoTotalGb,
        long uptime,
        boolean online,
        Instant ultimaAtualizacao
) { }