package br.dev.matheushenrique.floccus.dto;

import br.dev.matheushenrique.floccus.models.Variaveis;

import java.util.List;

public record VariaveisRequest(
        List<Variaveis> variaveis
) {
}
