package br.dev.matheushenrique.floccus.config;

import br.dev.matheushenrique.floccus.services.AgenteService;
import br.dev.matheushenrique.floccus.services.ContainerLogService;
import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ContainerLogService containerLogService;
    private final AgenteService agenteService;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket desconectado: {}", sessionId);
        containerLogService.fecharCanaisSessao(sessionId);

    }

    @EventListener
    public void handleWebSocketSubscribe(SessionSubscribeEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        if (destination.startsWith("/topic/agentes/")
                && destination.endsWith("/status")) {
            String agenteId = destination
                    .substring("/topic/agentes/".length())
                    .replace("/status", "");

            agenteService.republicarMetricas(agenteId);
        }
    }
}
