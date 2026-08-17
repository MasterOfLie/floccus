package br.dev.matheushenrique.floccus.dto;

import lombok.Builder;

@Builder
public record ResponseDTO(
        String message,
        boolean status
) {}
