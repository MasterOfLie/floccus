package br.dev.matheushenrique;

import br.dev.matheushenrique.server.GrpcServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FloccusAgentServer {
    public static void main(String[] args) {
        try {
            log.info("Inicializando a aplicação...");
            GrpcServer server = new GrpcServer();
            server.start();
        } catch (Exception e) {
            log.error("Erro crítico ao iniciar o Agente: ", e);
            System.exit(1);
        }
    }
}