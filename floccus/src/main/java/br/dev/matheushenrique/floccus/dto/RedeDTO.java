package br.dev.matheushenrique.floccus.dto;

import lombok.Builder;

@Builder
public record RedeDTO (
        String id,
        String nome,
        String subnet,
        String gateway,
        String agenteId,
        String nomeAgente
){ }
