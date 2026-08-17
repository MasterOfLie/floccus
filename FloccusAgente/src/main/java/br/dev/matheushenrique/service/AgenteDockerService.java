package br.dev.matheushenrique.service;
import  br.dev.matheushenrique.grpc.*;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import com.github.dockerjava.api.model.Container;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class AgenteDockerService extends AgenteDockerServiceGrpc.AgenteDockerServiceImplBase{

    private final DockerClient dockerClient;
    @Override
    public void listarContainers(ListarContainersInt request, StreamObserver<ListarContainersOut> responseObserver) {
        log.info("[Docker API] Listando containers...");
        try {
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(request.getMostrarTodos())
                    .exec();

            List<ContainerModel> listaModels = new ArrayList<>();

            for (Container c : containers) {
                String nomeFormatado = (c.getNames() != null && c.getNames().length > 0)
                        ? c.getNames()[0].replace("/", "")
                        : "desconhecido";

                listaModels.add(ContainerModel.newBuilder()
                        .setId(c.getId().substring(0, 12))
                        .setNome(nomeFormatado)
                        .setImagem(c.getImage())
                        .setStatus(c.getState())
                        .build());
            }

            ListarContainersOut resposta = ListarContainersOut.newBuilder()
                    .addAllContainers(listaModels)
                    .build();
            System.out.println(containers);
            responseObserver.onNext(resposta);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("[Docker API] Erro ao listar containers: ", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erro na comunicação interna com o Docker.")
                    .asRuntimeException());
        }
    }

    @Override
    public void iniciarContainer(IniciarContainerRequest request, StreamObserver<IniciarContainerResponse> responseObserver) {
        try {
            dockerClient.startContainerCmd(request.getId()).exec();
            InspectContainerResponse containerCmd = dockerClient.inspectContainerCmd(request.getId()).exec();
            List<FloccusNetwork> redes = new ArrayList<>();
            var networkSettings = containerCmd.getNetworkSettings();
            if (networkSettings != null && networkSettings.getNetworks() != null) {
                networkSettings.getNetworks().forEach((nomeRede, dadosRede) -> {
                    FloccusNetwork floccusNetwork = FloccusNetwork.newBuilder()
                            .setNome(nomeRede) // O nome da rede é a chave do Map
                            .setIp(dadosRede.getIpAddress() != null ? dadosRede.getIpAddress() : "")
                            .setGateway(dadosRede.getGateway() != null ? dadosRede.getGateway() : "")
                            .setMac(dadosRede.getMacAddress() != null ? dadosRede.getMacAddress() : "")
                            .build();
                    redes.add(floccusNetwork);
                });
            }
            IniciarContainerResponse iniciarContainerResponse =
                    IniciarContainerResponse.newBuilder()
                            .setNome(containerCmd.getName())
                            .setId(containerCmd.getId())
                            .setImagem(containerCmd.getConfig().getImage())
                            .setStatus(containerCmd.getState().getStatus())
                            .addAllRedes(redes)
                            .build();
            responseObserver.onNext(iniciarContainerResponse);
            responseObserver.onCompleted();
        } catch (NotModifiedException e){
            log.warn("Container " + request.getId() + " já em execução");
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Container " + request.getId() + " já em execução")
                    .asRuntimeException());
        } catch (NotFoundException e){
            log.warn(e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Erro ao iniciar container " + request.getId())
                    .asRuntimeException());
        } catch (Exception e ) {
            System.out.println(e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erro na comunicação interna com o Docker.")
                    .asRuntimeException());
        }
    }

    @Override
    public void pararContainer(PararContainerRequest request, StreamObserver<PararContainerResponse> responseObserver) {
        try {
            dockerClient.stopContainerCmd(request.getId()).exec();
            InspectContainerResponse containerCmd = dockerClient.inspectContainerCmd(request.getId()).exec();
            List<FloccusNetwork> redes = new ArrayList<>();
            var networkSettings = containerCmd.getNetworkSettings();
            if (networkSettings != null && networkSettings.getNetworks() != null) {
                networkSettings.getNetworks().forEach((nomeRede, dadosRede) -> {
                    FloccusNetwork floccusNetwork = FloccusNetwork.newBuilder()
                            .setNome(nomeRede) // O nome da rede é a chave do Map
                            .setIp(dadosRede.getIpAddress() != null ? dadosRede.getIpAddress() : "")
                            .setGateway(dadosRede.getGateway() != null ? dadosRede.getGateway() : "")
                            .setMac(dadosRede.getMacAddress() != null ? dadosRede.getMacAddress() : "")
                            .build();
                    redes.add(floccusNetwork);
                });
            }
            PararContainerResponse pararContainerResponse =
                    PararContainerResponse.newBuilder()
                            .setNome(containerCmd.getName())
                            .setId(containerCmd.getId())
                            .setImagem(containerCmd.getConfig().getImage())
                            .setStatus(containerCmd.getState().getStatus())
                            .addAllRedes(redes)
                            .build();
            responseObserver.onNext(pararContainerResponse);
            responseObserver.onCompleted();
        } catch (NotModifiedException e){
            log.warn("Container " + request.getId() + " já em parado");
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Container " + request.getId() + " já em parado")
                    .asRuntimeException());
        } catch (Exception e ) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Erro na comunicação interna com o Docker.")
                    .asRuntimeException());
        }
    }

}
