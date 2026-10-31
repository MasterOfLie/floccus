package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.services.GitHubWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubWebhookController {

    private final GitHubWebhookService webhookService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> receberWebhook(@RequestHeader("X-GitHub-Event") String evento, @RequestHeader("X-Hub-Signature-256") String assinatura, @RequestHeader("X-GitHub-Delivery") String deliveryId,
            @RequestBody byte[] payload) {

        webhookService.processar(evento, assinatura, deliveryId, payload);
        return ResponseEntity.accepted().build();
    }
}
