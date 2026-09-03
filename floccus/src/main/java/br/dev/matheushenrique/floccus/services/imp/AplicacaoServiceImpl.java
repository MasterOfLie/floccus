package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.dto.AplicacaoDTO;
import br.dev.matheushenrique.floccus.dto.ContainerDTO;
import br.dev.matheushenrique.floccus.dto.RedeContainerDTO;
import br.dev.matheushenrique.floccus.models.Agente;
import br.dev.matheushenrique.floccus.models.Aplicacao;
import br.dev.matheushenrique.floccus.repository.AgenteRepository;
import br.dev.matheushenrique.floccus.repository.AplicacaoRepository;
import br.dev.matheushenrique.floccus.services.AplicacaoService;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import br.dev.matheushenrique.grpc.AgenteDockerServiceGrpc;
import br.dev.matheushenrique.grpc.ContainerRequest;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AplicacaoServiceImpl implements AplicacaoService {
    private final GrpcClientService grpcClientService;
    private final AplicacaoRepository aplicacaoRepository;
    private final AgenteRepository agenteRepository;

    @Override
    public ContainerDTO iniciarContainer(String aplicacaoId) throws StatusRuntimeException {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicacao não encontrado"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        var container = grpcClientService.floccusAgente(
                agenteConexaoDto,
                AgenteDockerServiceGrpc::newBlockingStub,
                stub -> stub.iniciarContainer(
                        ContainerRequest.newBuilder().setId(aplicacao.getContainerId()).build()
                )
        );
        List<RedeContainerDTO> redes = container.getContainer().getRedesList().stream()
                .map(redeProto -> new RedeContainerDTO(
                        redeProto.getNome(),
                        redeProto.getIp(),
                        redeProto.getGateway(),
                        redeProto.getMac()
                ))
                .toList();
        return ContainerDTO.builder()
                .id(container.getContainer().getId())
                .nome(container.getContainer().getNome())
                .image(container.getContainer().getImagem())
                .status(container.getContainer().getStatus())
                .redes(redes)
                .build();
    }

    @Override
    public ContainerDTO pararContainer(String aplicacaoId) throws StatusRuntimeException {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicacao não encontrado"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        var container = grpcClientService.floccusAgente(
                agenteConexaoDto,
                AgenteDockerServiceGrpc::newBlockingStub,
                stub -> stub.pararContainer(
                        ContainerRequest.newBuilder().setId(aplicacao.getContainerId()).build()
                )
        );
        List<RedeContainerDTO> redes = container.getContainer().getRedesList().stream()
                .map(redeProto -> new RedeContainerDTO(
                        redeProto.getNome(),
                        redeProto.getIp(),
                        redeProto.getGateway(),
                        redeProto.getMac()
                ))
                .toList();
        return ContainerDTO.builder()
                .id(container.getContainer().getId())
                .nome(container.getContainer().getNome())
                .image(container.getContainer().getImagem())
                .status(container.getContainer().getStatus())
                .redes(redes)
                .build();
    }

    @Override
    public ContainerDTO reiniciarContainer(String aplicacaoId) throws StatusRuntimeException {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicacao não encontrado"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        var container = grpcClientService.floccusAgente(
                agenteConexaoDto,
                AgenteDockerServiceGrpc::newBlockingStub,
                stub -> stub.reiniciarContainer(
                        ContainerRequest.newBuilder().setId(aplicacao.getContainerId()).build()
                )
        );
        List<RedeContainerDTO> redes = container.getContainer().getRedesList().stream()
                .map(redeProto -> new RedeContainerDTO(
                        redeProto.getNome(),
                        redeProto.getIp(),
                        redeProto.getGateway(),
                        redeProto.getMac()
                ))
                .toList();
        return ContainerDTO.builder()
                .id(container.getContainer().getId())
                .nome(container.getContainer().getNome())
                .image(container.getContainer().getImagem())
                .status(container.getContainer().getStatus())
                .redes(redes)
                .build();
    }

    @Override
    public AplicacaoDTO detalharContainer(String aplicacaoId) throws StatusRuntimeException {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicacao não encontrado"));
//        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
//        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
//        var container = grpcClientService.floccusAgente(
//                agenteConexaoDto,
//                AgenteDockerServiceGrpc::newBlockingStub,
//                stub -> stub.detalharContainer(
//                        ContainerRequest.newBuilder().setId(aplicacao.getContainerId()).build()
//                )
//        );
//        Instant inicio = Instant.parse(container.getContainer().getTempoStatus());
//        Duration uptime = Duration.between(inicio, Instant.now());
//        long dias = uptime.toDays();
//        long horas = uptime.toHours() % 24;
//        long minutos = uptime.toMinutes() % 60;
//        String tempoStatus = String.format(
//                "%d dias, %d horas e %d minutos",
//                dias,
//                horas,
//                minutos
//        );
//        List<RedeContainerDTO> redes = container.getContainer().getRedesList().stream()
//                .map(redeProto -> new RedeContainerDTO(
//                        redeProto.getNome(),
//                        redeProto.getIp(),
//                        redeProto.getGateway(),
//                        redeProto.getMac()
//                ))
//                .toList();
        return AplicacaoDTO.builder()
                .id(aplicacao.getId())
                .nome(aplicacao.getNome())
                .imagem(aplicacao.getImagem())
                .agenteId(aplicacao.getAgenteId())
                .containerId(aplicacao.getContainerId())
                .tipo(aplicacao.getTipo())
                .portaContainer(aplicacao.getPortaContainer())
                .portaExterna(aplicacao.getPortaExterna())
                .dominio(aplicacao.getDominio())
                .memoriaLimite(aplicacao.getMemoriaLimite())
                .cpuLimite(aplicacao.getCpuLimite())
                .criadoEm(aplicacao.getCriadoEm())
                .atualizadoEm(aplicacao.getAtualizadoEm())
                .build();
    }

}
