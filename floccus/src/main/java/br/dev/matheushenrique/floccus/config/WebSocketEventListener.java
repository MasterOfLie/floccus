package br.dev.matheushenrique.floccus.config;

import br.dev.matheushenrique.floccus.services.ContainerLogService;
import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ContainerLogService containerLogService;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket desconectado: {}", sessionId);
        containerLogService.fecharCanaisSessao(sessionId);

    }
}
