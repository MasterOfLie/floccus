package br.dev.matheushenrique.floccus.dto;

public record RegisterRequestDTO(
        String nome,
        String email,
        String senha
) {
}
