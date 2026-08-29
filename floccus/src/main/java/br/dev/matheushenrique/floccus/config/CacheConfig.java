package br.dev.matheushenrique.floccus.config;

import br.dev.matheushenrique.floccus.dto.AgenteMetricasDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;

@Configuration
@Slf4j
public class CacheConfig {
    @Bean
    public Cache<String, AgenteMetricasDTO> heartbeatCache(SimpMessagingTemplate messagingTemplate) {

        return Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(15)).scheduler(Scheduler.systemScheduler()).maximumSize(10_000).removalListener((String agenteId, AgenteMetricasDTO metricas, RemovalCause causa) -> {

            if (causa != RemovalCause.EXPIRED) {
                return;
            }

            AgenteMetricasDTO offline = AgenteMetricasDTO.builder().cpuUsage(0).discoTotalGb(0).online(false).ramUsadaMb(0).ramTotalMb(0).uptime(0).discoUsadoGb(0).build();

            messagingTemplate.convertAndSend("/topic/agentes/" + agenteId + "/status", offline);

            log.info("Agente {} ficou offline por timeout de heartbeat", agenteId);
        }).build();
    }
}
