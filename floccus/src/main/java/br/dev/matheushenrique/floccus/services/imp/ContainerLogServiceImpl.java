package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.dto.ContainerLogConnectionDTO;
import br.dev.matheushenrique.floccus.models.Agente;
import br.dev.matheushenrique.floccus.models.Aplicacao;
import br.dev.matheushenrique.floccus.repository.AgenteRepository;
import br.dev.matheushenrique.floccus.repository.AplicacaoRepository;
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
    private final AplicacaoRepository aplicacaoRepository;
    private final AgenteRepository agenteRepository;
    private final Map<String, ContainerLogConnectionDTO> canaisAtivos = new ConcurrentHashMap<>();

    @Override
    public void registrarCanal(String aplicacaoId, SimpMessageHeaderAccessor headerAccessor) {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicação não encontrada"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        String chave = headerAccessor.getSessionId() + "_" + aplicacaoId;

        ContainerLogConnectionDTO conexaoAnterior = canaisAtivos.remove(chave);
        if (conexaoAnterior != null && conexaoAnterior.channel() != null) {
            conexaoAnterior.channel().shutdownNow();
        }
        StreamObserver<LogContainer> containerStreamObserver = new ContainerLogObserver(aplicacaoId, messagingTemplate);
        ManagedChannel channel = grpcClientService.streamarLogsContainer(agenteConexaoDto, aplicacao.getContainerId(), 100, containerStreamObserver);
        canaisAtivos.put(chave, new ContainerLogConnectionDTO(aplicacaoId, headerAccessor.getSessionId(), channel));
    }

    @Override
    public void fecharCanal(String aplicacaoId, String sessionId) {
        String chave = sessionId + "_" + aplicacaoId;
        ContainerLogConnectionDTO containerLogConnection = canaisAtivos.remove(chave);
        if (containerLogConnection == null) {
            log.info("Nenhum canal gRPC ativo encontrado para a aplicação {} da sessão {}", aplicacaoId, sessionId);
            return;
        }
        ManagedChannel channel = containerLogConnection.channel();
        if (channel != null && !channel.isShutdown()) {
            log.info("Fechando canal gRPC da aplicação {} da sessão {}", aplicacaoId, sessionId);
            try {
                channel.shutdownNow();
                if (!channel.awaitTermination(2, TimeUnit.SECONDS)) {
                    log.warn("Canal gRPC da aplicação {} não encerrou.", aplicacaoId);
                }
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
                log.info("Fechando canal gRPC do container {} da sessão {}", conexao.containerId(), sessionId);

                channel.shutdown();

                try {
                    if (!channel.awaitTermination(2, TimeUnit.SECONDS)) {
                        log.warn("Canal gRPC do container {} não encerrou. Forçando encerramento.", conexao.containerId());
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
