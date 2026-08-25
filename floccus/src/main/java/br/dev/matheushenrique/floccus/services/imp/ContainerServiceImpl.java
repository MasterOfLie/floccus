package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.dto.ContainerDTO;
import br.dev.matheushenrique.floccus.dto.RedeContainerDTO;
import br.dev.matheushenrique.floccus.services.ContainerService;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import br.dev.matheushenrique.grpc.AgenteDockerServiceGrpc;
import br.dev.matheushenrique.grpc.ContainerRequest;
import br.dev.matheushenrique.grpc.LogContainer;
import br.dev.matheushenrique.grpc.LogsContainerRequest;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerServiceImpl implements ContainerService {
    private final GrpcClientService grpcClientService;

    @Override
    public ContainerDTO iniciarContainer(String containerID) throws StatusRuntimeException {
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
        var container = grpcClientService.floccusAgente(
                agenteConexaoDto,
                AgenteDockerServiceGrpc::newBlockingStub,
                stub -> stub.iniciarContainer(
                        ContainerRequest.newBuilder().setId(containerID).build()
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
    public ContainerDTO pararContainer(String containerID) throws StatusRuntimeException {
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
        var container = grpcClientService.floccusAgente(
                agenteConexaoDto,
                AgenteDockerServiceGrpc::newBlockingStub,
                stub -> stub.pararContainer(
                        ContainerRequest.newBuilder().setId(containerID).build()
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
    public ContainerDTO reiniciarContainer(String containerID) throws StatusRuntimeException {
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
        var container = grpcClientService.floccusAgente(
                agenteConexaoDto,
                AgenteDockerServiceGrpc::newBlockingStub,
                stub -> stub.reiniciarContainer(
                        ContainerRequest.newBuilder().setId(containerID).build()
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
    public ContainerDTO detalharContainer(String containerID) throws StatusRuntimeException {
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
        var container = grpcClientService.floccusAgente(
                agenteConexaoDto,
                AgenteDockerServiceGrpc::newBlockingStub,
                stub -> stub.detalharContainer(
                        ContainerRequest.newBuilder().setId(containerID).build()
                )
        );
        Instant inicio = Instant.parse(container.getContainer().getTempoStatus());
        Duration uptime = Duration.between(inicio, Instant.now());
        long dias = uptime.toDays();
        long horas = uptime.toHours() % 24;
        long minutos = uptime.toMinutes() % 60;
        String tempoStatus = String.format(
                "%d dias, %d horas e %d minutos",
                dias,
                horas,
                minutos
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
                .tempoStatus(tempoStatus)
                .redes(redes)
                .build();
    }

}
