package br.dev.matheushenrique.floccus.dto;

import lombok.*;

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