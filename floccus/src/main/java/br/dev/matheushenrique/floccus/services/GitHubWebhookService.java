package br.dev.matheushenrique.floccus.services;

public interface GitHubWebhookService {
    void processar(String evento, String assinatura, String deliveryId, byte[] payload);
}
