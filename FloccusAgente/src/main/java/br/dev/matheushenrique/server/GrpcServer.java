package br.dev.matheushenrique.server;

import br.dev.matheushenrique.config.DockerClientConfig;
import br.dev.matheushenrique.config.FloccusConfig;
import br.dev.matheushenrique.security.AuthInterceptor;
import br.dev.matheushenrique.service.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcServer {

    public void start(){

        try {
            FloccusConfig config = FloccusConfig.loadConfig();
            System.out.println(config.getAgenteId());
            log.info("Configurações carregadas com sucesso!");
            log.info("Chave Secreta do Agente: [PROTEGIDA]");
            log.info("Conectando ao Docker Engine via: {}", config.getDockerHost());
            log.info("IP mapeado do Painel Central (Spring): {}:{}", config.getBackendHost(), config.getBackendPort());
            FloccusLogService floccusLogService = new FloccusLogService(DockerClientConfig.getInstance());
            // 2. Inicializa o servidor gRPC nativo na porta configurada no .yml
            Server server = ServerBuilder.forPort(config.getAgentPort())
                    .addService(ServerInterceptors.intercept(
                            new FloccusStatusService(),
                            new AuthInterceptor(config.getSecretKey())))
                    .addService(ServerInterceptors.intercept(
                    new AgenteDockerService(DockerClientConfig.getInstance(), floccusLogService),
                    new AuthInterceptor(config.getSecretKey())))
                    .addService(ServerInterceptors.intercept(
                            new AplicacaoService(DockerClientConfig.getInstance())))
                    .addService(ServerInterceptors.intercept(
                            new FloccusRedeService(DockerClientConfig.getInstance(), config),
                            new AuthInterceptor(config.getSecretKey())))

                    .build();


            log.info("Servidor gRPC pronto e escutando na porta: {}", config.getAgentPort());
            server.start();
            server.awaitTermination();
        } catch (Exception e) {
            log.error("Erro fatal na execução do servidor gRPC do agente: ", e);
            System.exit(1);
        }
    }

}
