package br.dev.matheushenrique.floccus.dto;

public record AgenteConexaoDto(
        String host,
        int porta,
        String apiKey
) {}