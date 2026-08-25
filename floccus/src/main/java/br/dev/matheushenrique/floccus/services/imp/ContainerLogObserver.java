package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.grpc.LogContainer;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
public class ContainerLogObserver implements StreamObserver<LogContainer> {
    private final String containerId;
    private final SimpMessagingTemplate messagingTemplate;

    public ContainerLogObserver(String containerId, SimpMessagingTemplate messagingTemplate) {
        this.containerId = containerId;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onNext(LogContainer logContainer) {
        messagingTemplate.convertAndSend(
                "/topic/logs/" + containerId,
                logContainer.getMensagem()
        );
    }

    @Override
    public void onError(Throwable t) {
        log.error("Erro no stream gRPC para o container {}: {}", containerId, t.getMessage());
        String msgErro = "\r\n\u001B[31m[Erro] Conexão com o agente perdida: " + t.getMessage() + "\u001B[0m\r\n";
        messagingTemplate.convertAndSend("/topic/logs/" + containerId, msgErro);
    }

    @Override
    public void onCompleted() {
        log.info("Stream gRPC finalizado para o container {}", containerId);
        String msgFim = "\r\n\u001B[33m[Floccus] Stream de logs finalizado pelo servidor.\u001B[0m\r\n";
        messagingTemplate.convertAndSend("/topic/logs/" + containerId, msgFim);
    }

}
