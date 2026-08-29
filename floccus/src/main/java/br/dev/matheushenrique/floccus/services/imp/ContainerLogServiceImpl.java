package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.dto.ContainerLogConnectionDTO;
import br.dev.matheushenrique.floccus.services.ContainerLogService;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import br.dev.matheushenrique.grpc.LogContainer;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class ContainerLogServiceImpl implements ContainerLogService {

    private final GrpcClientService grpcClientService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, ContainerLogConnectionDTO> canaisAtivos = new ConcurrentHashMap<>();

    @Override
    public void registrarCanal(String containerId, SimpMessageHeaderAccessor headerAccessor) {
        AgenteConexaoDto agente = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
        StreamObserver<LogContainer> containerStreamObserver = new ContainerLogObserver(containerId, messagingTemplate);
        ManagedChannel channel = grpcClientService.streamarLogsContainer(
                agente,
                containerId,
                100,
                containerStreamObserver
        );
        ContainerLogConnectionDTO conexaoAnterior = canaisAtivos.remove(headerAccessor.getSessionId() + "_" + containerId);
        canaisAtivos.put(headerAccessor.getSessionId() + "_" + containerId, new ContainerLogConnectionDTO(containerId, headerAccessor.getSessionId(), channel));
    }

    @Override
    public void fecharCanal(String containerId, String sessionId) {
        ContainerLogConnectionDTO containerLogConnection = canaisAtivos.remove(sessionId + "_" + containerId);
        ManagedChannel channel = containerLogConnection.channel();
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdownNow().awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void fecharCanaisSessao(String sessionId) {
        canaisAtivos.entrySet().removeIf(entry -> {
            ContainerLogConnectionDTO conexao = entry.getValue();
            if (!conexao.sessionId().equals(sessionId)) {
                return false;
            }
            ManagedChannel channel = conexao.channel();
            if (channel != null && !channel.isShutdown()) {
                log.info(
                        "Fechando canal gRPC do container {} da sessão {}",
                        conexao.containerId(),
                        sessionId
                );

                channel.shutdown();

                try {
                    if (!channel.awaitTermination(2, TimeUnit.SECONDS)) {
                        log.warn(
                                "Canal gRPC do container {} não encerrou. Forçando encerramento.",
                                conexao.containerId()
                        );
                        channel.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    channel.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            return true;
        });
    }
}
