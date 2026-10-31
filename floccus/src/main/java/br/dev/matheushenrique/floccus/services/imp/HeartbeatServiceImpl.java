package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteMetricasDTO;
import br.dev.matheushenrique.floccus.dto.ContainerHeartbeat;
import br.dev.matheushenrique.floccus.services.HeartbeatService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class HeartbeatServiceImpl implements HeartbeatService {

    private final Cache<String, ContainerHeartbeat> monitoramentoCache;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void heartbeatContainer(ContainerHeartbeat heartbeat) {
        monitoramentoCache.put(
                heartbeat.agenteId(),
                heartbeat
        );

        messagingTemplate.convertAndSend(
                "/topic/heartbeat/"+ heartbeat.agenteId() + "/containers",
                heartbeat
        );
    }

    @Override
    public Map<String, ContainerHeartbeat> listarTodos() {
        return monitoramentoCache.asMap();
    }
}
