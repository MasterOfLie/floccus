package br.dev.matheushenrique.service;

import br.dev.matheushenrique.config.FloccusConfig;
import br.dev.matheushenrique.dto.AgenteMetricasDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HeartbeatService {

    private final FloccusConfig config;
    private final MetricasCollectorService metricsService;
    private final HttpClient httpClient;

    public HeartbeatService(FloccusConfig config, MetricasCollectorService metricsService) {
        this.config = config;
        this.metricsService = metricsService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public void enviarHeartbeat() {
        try {
            AgenteMetricasDTO metricas = metricsService.coletarMetricas();

            String json = """
                    {
                        "cpuUsage": %s,
                        "ramUsadaMb": %s,
                        "ramTotalMb": %s,
                        "discoUsadoGb": %s,
                        "discoTotalGb": %s,
                        "uptime": %s,
                        "online": %s
                    }
                    """.formatted(
                    metricas.cpuUsage(),
                    metricas.ramUsadaMb(),
                    metricas.ramTotalMb(),
                    metricas.discoUsadoGb(),
                    metricas.discoTotalGb(),
                    metricas.uptime(),
                    true
            );

            String url = String.format(
                    "http://%s:%d/api/agentes/%s/heartbeat",
                    config.getBackendHost(),
                    config.getBackendPort(),
                    config.getAgenteId()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
//                    .header("Authorization", "Bearer " + config.getSecretKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            System.out.println("enviado requisicao " +  url);
            httpClient.sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    )
                    .thenAccept(response -> {

                        System.out.println(
                                "Heartbeat HTTP Status: "
                                        + response.statusCode()
                        );

                        System.out.println(
                                "Resposta: "
                                        + response.body()
                        );

                    })
                    .exceptionally(error -> {

                        System.err.println(
                                "Erro ao enviar heartbeat: "
                                        + error.getMessage()
                        );

                        return null;
                    });

        } catch (Exception e) {
            System.err.println("Erro ao preparar heartbeat: " + e.getMessage());
        }
    }
}
