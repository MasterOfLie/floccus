package br.dev.matheushenrique.floccus.controllers;
import br.dev.matheushenrique.floccus.services.ContainerLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ContainerLogController {
    private final ContainerLogService containerLogService;

    @MessageMapping("/logs/start/{containerId}")
    public void iniciarStreamDeLogs(@DestinationVariable String containerId, SimpMessageHeaderAccessor headerAccessor) {
        containerLogService.registrarCanal(containerId, headerAccessor);
    }

    @MessageMapping("/logs/stop/{containerId}")
    public void pararStreamDeLogs(@DestinationVariable String containerId, SimpMessageHeaderAccessor headerAccessor) {
        containerLogService.fecharCanal(containerId, headerAccessor.getSessionId());
    }

}
