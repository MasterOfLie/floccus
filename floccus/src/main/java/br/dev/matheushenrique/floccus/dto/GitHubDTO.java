package br.dev.matheushenrique.floccus.dto;

public record GitHubDTO(
        String id,
        String usuarioId,
        String appId,
        String nomeApp,
        String clienteId,
        String instalacaoId,
        String chaveCliente,
        String webhookSecret,
        String privateKeyPem,
        String nomeUsuario) {
}
