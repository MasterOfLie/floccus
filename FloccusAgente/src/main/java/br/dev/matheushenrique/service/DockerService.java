package br.dev.matheushenrique.service;

import br.dev.matheushenrique.floccus.grpc.CriarAplicacaoRequest;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.RestartPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class DockerService {

    private final DockerClient dockerClient;

    public String criarContainer(br.dev.matheushenrique.floccus.grpc.CriarAplicacaoRequest request) {
        ExposedPort portaContainer = criarPortaExposta(request);
        HostConfig hostConfig = HostConfig.newHostConfig();
        if (request.getMemoriaMb() > 0) {

            long memoriaBytes =
                    request.getMemoriaMb() * 1024L * 1024L;

            hostConfig.withMemory(memoriaBytes);
        }
        if (request.getCpu() > 0) {

            long nanoCpus =
                    (long) (request.getCpu() * 1_000_000_000L);

            hostConfig.withNanoCPUs(nanoCpus);
        }
        if (!request.getRede().isBlank()) {
            hostConfig.withNetworkMode(request.getRede());
        }
        if (portaContainer != null) {
            Ports portBindings = new Ports();
            Ports.Binding binding =
                    Ports.Binding.bindPort(
                            request.getPortaExterna()
                    );

            portBindings.bind(
                    portaContainer,
                    binding
            );
            hostConfig.withPortBindings(portBindings);
        }
        hostConfig.withRestartPolicy(
                RestartPolicy.unlessStoppedRestart()
        );
        List<String> ambiente = new ArrayList<>();
        request.getAmbienteList().forEach(
                variavel -> ambiente.add(
                        variavel.getChave()
                                + "="
                                + variavel.getValor()
                )
        );

        Map<String, String> labels =
                new HashMap<>();

        labels.put(
                "floccus.aplicacao",
                "true"
        );

        labels.put(
                "floccus.aplicacao.id",
                request.getAplicacaoId()
        );

        var comando = dockerClient
                .createContainerCmd(request.getImagem())
                .withName(request.getNome())
                .withLabels(labels)
                .withHostConfig(hostConfig);


        if (!ambiente.isEmpty()) {
            comando.withEnv(ambiente);
        }
        if (portaContainer != null) {
            comando.withExposedPorts(portaContainer);
        }
        System.out.println("Labels enviados para Docker: " + labels);

        CreateContainerResponse container =
                comando.exec();

        String containerId = container.getId();

        var inspect = dockerClient
                .inspectContainerCmd(containerId)
                .exec();

        System.out.println(
                "Labels retornados pelo Docker: "
                        + inspect.getConfig().getLabels()
        );
        if (!request.getRede().isBlank()) {
            dockerClient
                    .connectToNetworkCmd()
                    .withNetworkId(request.getRede())
                    .withContainerId(containerId)
                    .exec();
        }
        dockerClient
                .startContainerCmd(containerId)
                .exec();

        return containerId;
    }

    private ExposedPort criarPortaExposta(
            CriarAplicacaoRequest request) {
        if (request.getPortaContainer() <= 0) {
            return null;
        }
        int porta = request.getPortaContainer();
        return switch (request.getTipo()) {
            case UDP ->
                    ExposedPort.udp(porta);
            case TCP, HTTP ->
                    ExposedPort.tcp(porta);
            default ->
                    ExposedPort.tcp(porta);
        };
    }

}
