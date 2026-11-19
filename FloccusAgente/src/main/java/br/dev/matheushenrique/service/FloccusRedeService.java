package br.dev.matheushenrique.service;

import br.dev.matheushenrique.config.FloccusConfig;
import br.dev.matheushenrique.grpc.*;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.model.Network;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@RequiredArgsConstructor
public class FloccusRedeService extends FloccusRedeServiceGrpc.FloccusRedeServiceImplBase {
    private final DockerClient dockerClient;
    private final FloccusConfig floccusConfig;

    @Override
    public void criarRede(CriarRedeRequest request, StreamObserver<RedeResponse> responseObserver) {
        try {
            CreateNetworkCmd cmd = dockerClient.createNetworkCmd()
                    .withName(request.getNome())
                    .withDriver("bridge")
                    .withLabels(Map.of(
                            "floccus.owner", request.getUsuario(),
                            "floccus.managed-by", "floccus-agent",
                            "created-at", String.valueOf(System.currentTimeMillis())
                    ));
            if (request.hasSubnet() && !request.getSubnet().isBlank()) {
                cmd.withIpam(new Network.Ipam().withConfig(
                        new Network.Ipam.Config().withSubnet(request.getSubnet())
                ));
            }
            Network network = dockerClient.inspectNetworkCmd()
                    .withNetworkId(cmd.exec().getId())
                    .exec();
            Network.Ipam.Config config = network.getIpam() != null
                    && network.getIpam().getConfig() != null
                    && !network.getIpam().getConfig().isEmpty()
                    ? network.getIpam().getConfig().getFirst()
                    : null;
            floccusConfig.getTraefikManager().conectarRedeAplicacao(network.getId());
            responseObserver.onNext(RedeResponse.newBuilder()
                    .setId(network.getId())
                    .setNome(network.getName())
                    .setSubnet(config != null && config.getSubnet() != null ? config.getSubnet() : "")
                    .setGateway(config != null && config.getGateway() != null ? config.getGateway() : "")
                    .setUsuario(network.getLabels() != null
                            ? network.getLabels().getOrDefault("floccus.owner", "")
                            : "")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Erro ao criar rede: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void excluirRede(
            ExcluirRedeRequest request,
            StreamObserver<ExcluirRedeResponse> responseObserver) {

        try {
            if (request.getId().isBlank()) {
                throw new IllegalArgumentException(
                        "O ID da rede não pode ser vazio."
                );
            }

            Network network = dockerClient.inspectNetworkCmd()
                    .withNetworkId(request.getId())
                    .exec();

            if (network.getContainers() != null
                    && !network.getContainers().isEmpty()) {

                ExcluirRedeResponse response = ExcluirRedeResponse.newBuilder()
                        .setSucesso(false)
                        .setMensagem("Não é possível remover a rede: existem containers conectados.")
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            dockerClient.removeNetworkCmd(request.getId()).exec();

            ExcluirRedeResponse response = ExcluirRedeResponse.newBuilder()
                    .setSucesso(true)
                    .setMensagem("Rede " + request.getId() + " removida com sucesso.")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            ExcluirRedeResponse errorResponse = ExcluirRedeResponse.newBuilder()
                    .setSucesso(false)
                    .setMensagem("Falha ao remover a rede: " + e.getMessage())
                    .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

}
