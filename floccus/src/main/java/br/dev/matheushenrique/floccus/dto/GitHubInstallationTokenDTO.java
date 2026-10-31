package br.dev.matheushenrique.floccus.dto;

public record GitHubInstallationTokenDTO(
        String token,
        String expires_at
) {
}
