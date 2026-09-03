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

    @MessageMapping("/logs/start/{aplicacaoId}")
    public void iniciarStreamDeLogs(@DestinationVariable String aplicacaoId, SimpMessageHeaderAccessor headerAccessor) {
        containerLogService.registrarCanal(aplicacaoId, headerAccessor);
    }

    @MessageMapping("/logs/stop/{aplicacaoId}")
    public void pararStreamDeLogs(@DestinationVariable String aplicacaoId, SimpMessageHeaderAccessor headerAccessor) {
        containerLogService.fecharCanal(aplicacaoId, headerAccessor.getSessionId());
    }

}
