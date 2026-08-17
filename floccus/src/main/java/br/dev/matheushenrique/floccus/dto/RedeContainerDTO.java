package br.dev.matheushenrique.floccus.dto;

import lombok.Builder;

@Builder
public record RedeContainerDTO(
        String nome,
        String ip,
        String gateway,
        String mac
) {}
