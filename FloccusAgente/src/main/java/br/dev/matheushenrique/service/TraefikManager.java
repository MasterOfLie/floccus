
package br.dev.matheushenrique.service;

import br.dev.matheushenrique.config.FloccusConfig;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;

public class TraefikManager {

    private static final String CONTAINER_NAME = "floccus-traefik";
    private static final String NETWORK_NAME = "floccus-proxy";
    private static final String IMAGE = "traefik:v3.6";

    private final DockerClient dockerClient;
    private final Path dataDirectory;
    private final FloccusConfig floccusConfig;

    public TraefikManager(DockerClient dockerClient, Path dataDirectory, FloccusConfig floccusConfig) {
        this.dockerClient = dockerClient;
        this.dataDirectory = dataDirectory;
        this.floccusConfig = floccusConfig;
    }

    public synchronized void garantirTraefik() throws Exception {
        String email = floccusConfig.getEmailSsl();

        if (email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "A variável de ambiente ACME_EMAIL não foi configurada."
            );
        }

        Files.createDirectories(dataDirectory);

        Path letsEncryptDirectory = dataDirectory.resolve("letsencrypt");
        Files.createDirectories(letsEncryptDirectory);

        Path acmeFile = letsEncryptDirectory.resolve("acme.json");

        if (Files.notExists(acmeFile)) {
            Files.createFile(acmeFile);
        }

        // O ACME exige armazenamento privado para os certificados.
        try {
            Files.setPosixFilePermissions(
                    acmeFile,
                    Set.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE
                    )
            );
        } catch (UnsupportedOperationException ignored) {
            // Sistemas de arquivos sem suporte a permissões POSIX.
        }

        garantirRede();
        garantirContainer(email);
    }

    private void garantirRede() {
        List<Network> redes = dockerClient
                .listNetworksCmd()
                .withNameFilter(NETWORK_NAME)
                .exec();

        boolean existe = redes.stream()
                .anyMatch(rede -> NETWORK_NAME.equals(rede.getName()));

        if (!existe) {
            dockerClient.createNetworkCmd()
                    .withName(NETWORK_NAME)
                    .withDriver("bridge")
                    .exec();
        }
    }

    private void garantirContainer(String email) throws Exception {
        List<Container> containers = dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .withNameFilter(List.of(CONTAINER_NAME))
                .exec();

        Container existente = containers.stream()
                .filter(c -> c.getNames() != null)
                .filter(c -> java.util.Arrays.stream(c.getNames())
                        .anyMatch(n -> n.equals("/" + CONTAINER_NAME)))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            if (!"running".equalsIgnoreCase(existente.getState())) {
                dockerClient.startContainerCmd(existente.getId()).exec();
            }

            return;
        }

        dockerClient.pullImageCmd("traefik")
                .withTag("v3.6")
                .start()
                .awaitCompletion();

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withNetworkMode(NETWORK_NAME)
                .withRestartPolicy(RestartPolicy.unlessStoppedRestart())
                .withPortBindings(
                        new PortBinding(
                                Ports.Binding.bindPort(80),
                                new ExposedPort(80)
                        ),
                        new PortBinding(
                                Ports.Binding.bindPort(443),
                                new ExposedPort(443)
                        )
                )
                .withBinds(
                        new Bind(
                                "/var/run/docker.sock",
                                new Volume("/var/run/docker.sock"),
                                AccessMode.ro
                        ),
                        new Bind(
                                dataDirectory.resolve("letsencrypt")
                                        .toAbsolutePath().toString(),
                                new Volume("/letsencrypt")
                        )
                );

        CreateContainerResponse container = dockerClient
                .createContainerCmd(IMAGE)
                .withName(CONTAINER_NAME)
                .withExposedPorts(
                        new ExposedPort(80),
                        new ExposedPort(443)
                )
                .withHostConfig(hostConfig)
                .withCmd(
                        "--providers.docker=true",
                        "--providers.docker.exposedbydefault=false",
                        "--providers.docker.network=" + NETWORK_NAME,

                        "--entrypoints.web.address=:80",
                        "--entrypoints.websecure.address=:443",

                        "--entrypoints.web.http.redirections.entrypoint.to=websecure",
                        "--entrypoints.web.http.redirections.entrypoint.scheme=https",

                        "--certificatesresolvers.letsencrypt.acme.email=" + email,
                        "--certificatesresolvers.letsencrypt.acme.storage=/letsencrypt/acme.json",
                        "--certificatesresolvers.letsencrypt.acme.httpchallenge=true",
                        "--certificatesresolvers.letsencrypt.acme.httpchallenge.entrypoint=web"
                )
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
    }
    public synchronized void conectarRedeAplicacao(String networkId) {
        boolean conectado = dockerClient
                .inspectNetworkCmd()
                .withNetworkId(networkId)
                .exec()
                .getContainers()
                .values()
                .stream()
                .anyMatch(c -> CONTAINER_NAME.equals(c.getName()));

        if (!conectado) {
            dockerClient.connectToNetworkCmd()
                    .withNetworkId(networkId)
                    .withContainerId(CONTAINER_NAME)
                    .exec();
        }
    }
}
