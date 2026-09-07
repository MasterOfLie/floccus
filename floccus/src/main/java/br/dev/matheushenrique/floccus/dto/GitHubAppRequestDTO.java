package br.dev.matheushenrique.floccus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubAppRequestDTO(
        String id,
        String name,
        String slug,

        @JsonProperty("client_id")
        String clientId,

        @JsonProperty("client_secret")
        String clientSecret,

        @JsonProperty("webhook_secret")
        String webhookSecret,

        String pem
) {
}
