package br.dev.matheushenrique;

import br.dev.matheushenrique.config.DockerClientConfig;
import br.dev.matheushenrique.config.FloccusConfig;
import br.dev.matheushenrique.server.GrpcServer;
import br.dev.matheushenrique.service.HeartbeatService;
import br.dev.matheushenrique.service.MetricasCollectorService;
import br.dev.matheushenrique.service.TraefikManager;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FloccusAgentServer {
    public static void main(String[] args) {
        try {
            log.info("Inicializando a aplicação...");
            FloccusConfig config = FloccusConfig.loadConfig();
            log.info("Traefik inicializado com sucesso.");
            MetricasCollectorService metricsService = new MetricasCollectorService();
            HeartbeatService heartbeatService = new HeartbeatService(config, metricsService, DockerClientConfig.getInstance());
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            log.info("Iniciando serviço de heartbeat do agente...");
            System.out.println(config.getAgenteId());
            scheduler.scheduleAtFixedRate(
                    heartbeatService::enviarHeartbeat,
                    0,
                    5,
                    TimeUnit.SECONDS
            );
            scheduler.scheduleAtFixedRate(
                    heartbeatService::enviarHeartbeatContainer,
                    0,
                    5,
                    TimeUnit.SECONDS
            );
            GrpcServer server = new GrpcServer();
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Encerrando agente...");
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                }
            }));

        } catch (Exception e) {
            log.error("Erro crítico ao iniciar o Agente: ", e);
            System.exit(1);
        }

    }
}