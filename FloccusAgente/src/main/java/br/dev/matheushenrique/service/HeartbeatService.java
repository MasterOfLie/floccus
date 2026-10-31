package br.dev.matheushenrique.service;

import br.dev.matheushenrique.config.FloccusConfig;
import br.dev.matheushenrique.dto.AgenteMetricasDTO;
import br.dev.matheushenrique.dto.ContainerHeartbeat;
import br.dev.matheushenrique.dto.ContainerMonitoramentoDTO;
import com.github.dockerjava.api.DockerClient;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Slf4j
public class HeartbeatService {

    private final FloccusConfig config;
    private final MetricasCollectorService metricsService;
    private final HttpClient httpClient;
    private final DockerClient dockerClient;

    public HeartbeatService(FloccusConfig config, MetricasCollectorService metricsService, DockerClient dockerClient) {
        this.config = config;
        this.metricsService = metricsService;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        this.dockerClient = dockerClient;
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
                    """.formatted(metricas.cpuUsage(), metricas.ramUsadaMb(), metricas.ramTotalMb(), metricas.discoUsadoGb(), metricas.discoTotalGb(), metricas.uptime(), true);

            String url = String.format("http://%s:%d/api/agentes/%s/heartbeat", config.getBackendHost(), config.getBackendPort(), config.getAgenteId());

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5))
//                    .header("Authorization", "Bearer " + config.getSecretKey())
                    .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {

            }).exceptionally(error -> {

                System.err.println("Erro ao enviar heartbeat: " + error.getMessage());

                return null;
            });

        } catch (Exception e) {
            System.err.println("Erro ao preparar heartbeat: " + e.getMessage());
        }
    }

    public void enviarHeartbeatContainer() {
        try {
            MonitoramentoService monitoramentoService = new MonitoramentoService(dockerClient);
            List<ContainerMonitoramentoDTO> containers = monitoramentoService.monitorarContainer();

            ContainerHeartbeat containerHeartbeat = new ContainerHeartbeat(config.getAgenteId(), System.currentTimeMillis(), containers);
            String json = """
                    {
                        "agenteId": "%s",
                        "timestamp": %d,
                        "containers": %s
                    }
                    """.formatted(containerHeartbeat.agenteId(), containerHeartbeat.timestamp(), converterContainersParaJson(containers));

            String url = String.format("http://%s:%d/api/heartbeat", config.getBackendHost(), config.getBackendPort());

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();

            log.debug("Enviando monitoramento de {} containers para {}", containers.size(), url);

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {

                if (response.statusCode() >= 200 && response.statusCode() < 300) {

                    log.debug("Monitoramento enviado com sucesso");

                } else {

                    log.warn("Erro no monitoramento. HTTP {} - {}", response.statusCode(), response.body());
                }

            }).exceptionally(error -> {

                log.error("Erro ao enviar monitoramento: {}", error.getMessage());

                return null;
            });

        } catch (Exception e) {

            log.error("Erro ao preparar monitoramento", e);
        }

    }

    private String converterContainersParaJson(List<ContainerMonitoramentoDTO> containers) {

        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < containers.size(); i++) {

            ContainerMonitoramentoDTO container = containers.get(i);

            json.append("""
                    {
                        "aplicacaoId": "%s",
                        "containerId": "%s",
                        "status": "%s",
                        "cpuPercentual": %s,
                        "memoriaUso": %d,
                        "memoriaLimite": %d,
                        "memoriaPercentual": %s,
                        "redeRecebida": %d,
                        "redeEnviada": %d
                    }
                    """.formatted(escaparJson(container.aplicacaoId()), escaparJson(container.containerId()), escaparJson(container.status()), container.cpuPercentual(), container.memoriaUso(), container.memoriaLimite(), container.memoriaPercentual(), container.redeRecebida(), container.redeEnviada()));

            if (i < containers.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        return json.toString();
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
