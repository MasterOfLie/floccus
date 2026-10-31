package br.dev.matheushenrique.service;

import br.dev.matheushenrique.floccus.grpc.*;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AplicacaoService extends FloccusContainerServiceGrpc.FloccusContainerServiceImplBase {

    private final DockerClient dockerClient;
    private final GitService gitService;

    public AplicacaoService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
        this.gitService = new GitService();
    }

    @Override
    public void criarAplicacao(CriarAplicacaoRequest request, StreamObserver<DeployEvento> responseObserver) {
        String diretorio = null;
        try {
            enviarEvento(responseObserver, request, "INICIANDO", "Iniciando deploy da aplicação", "", "");

            validarRequest(request);

            enviarEvento(responseObserver, request, "CLONANDO", "Baixando arquivos do repositório", "", "");

            diretorio = "/home/matheus/floccus/apps/floccus_" + gerarIdentificador();
            gitService.baixarRepositorio(request.getRepositorioGit(), request.getTokenGit(), diretorio, request.getBranch());

            enviarEvento(responseObserver, request, "PREPARANDO", "Preparando configuração do container", "", diretorio);

            int portaContainer = request.getPortaContainer();
            int portaExterna = request.getPortaExterna();

            ExposedPort exposedPort = ExposedPort.tcp(portaContainer);
            Ports portBindings = new Ports();

            if (portaExterna > 0) {
                portBindings.bind(exposedPort, Ports.Binding.bindPort(portaExterna));
            }

            List<String> envs = new ArrayList<>();
            for (Variavel var : request.getAmbienteList()) {
                envs.add(var.getChave() + "=" + var.getValor());
            }

            long memoryBytes = request.getMemoriaMb() * 1024L * 1024L;
            long cpuQuota = (long) (request.getCpu() * 100000L);

            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withPortBindings(portBindings)
                    .withMemory(memoryBytes)
                    .withCpuQuota(cpuQuota)
                    .withCpuPeriod(100000L)
                    .withBinds(new Bind(diretorio, new Volume("/app")));

            if (!request.getRede().isBlank()) {
                hostConfig.withNetworkMode(request.getRede());
            }

            List<String> cmdList = new ArrayList<>();
            cmdList.add("sh");
            cmdList.add("-c");

            StringBuilder comandoFinal = new StringBuilder();
            if (!request.getBuildCommand().isBlank()) {
                comandoFinal.append(request.getBuildCommand()).append(" && ");
            }

            if (!request.getStartCommand().isBlank()) {
                comandoFinal.append(request.getStartCommand());
            } else {
                comandoFinal.append("tail -f /dev/null");
            }

            cmdList.add(comandoFinal.toString());

            String imagem = request.getImagem();

            try {
                dockerClient.inspectImageCmd(imagem).exec();
            } catch (NotFoundException e) {
                dockerClient.pullImageCmd(imagem).start().awaitCompletion();
            }

            List<Network> redes = dockerClient.listNetworksCmd()
                    .exec();

            Network rede = redes.stream()
                    .filter(r -> request.getRede().equals(r.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Rede Docker não encontrada para o ID: " + request.getRede()
                    ));


            Map<String, String> labels = Map.of(
                    "floccus.aplicacao", "true",
                    "floccus.aplicacao.id", request.getAplicacaoId(),

                    "traefik.enable", "true",
                    "traefik.docker.network", rede.getName(),

                    "traefik.http.routers." + request.getAplicacaoId() + ".rule",
                    "Host(`"  + request.getDominio()+ "`)",

                    "traefik.http.routers." + request.getAplicacaoId() + ".entrypoints",
                    "websecure",

                    "traefik.http.routers." + request.getAplicacaoId() + ".tls",
                    "true",

                    "traefik.http.routers." + request.getAplicacaoId() + ".tls.certresolver",
                    "letsencrypt",

                    "traefik.http.services." + request.getAplicacaoId() + ".loadbalancer.server.port",
                    String.valueOf(request.getPortaContainer())
            );


            CreateContainerResponse containerResponse = dockerClient.createContainerCmd(imagem)
                    .withName(request.getNome() + "_" + gerarIdentificador())
                    .withWorkingDir("/app")
                    .withExposedPorts(exposedPort)
                    .withEnv(envs)
                    .withHostConfig(hostConfig)
                    .withCmd(cmdList)
                    .withLabels(labels)
                    .exec();

            String containerId = containerResponse.getId();

            enviarEvento(responseObserver, request, "INICIANDO_CONTAINER", "Preparando container", containerId, diretorio);
            String diretorioAntigo = request.getDiretorioArquivos();
            if (request.getRedeploy() && !request.getContainerId().isBlank()) {
                try {
                    dockerClient.stopContainerCmd(request.getContainerId()).exec();
                    dockerClient.removeContainerCmd(request.getContainerId()).withForce(true).exec();
                } catch (Exception e) {
                    log.warn("Aviso ao remover container antigo: {}", e.getMessage());
                }
            }
            dockerClient.startContainerCmd(containerId).exec();
            if (request.getRedeploy() && diretorioAntigo != null && !diretorioAntigo.isBlank()) {
                deletarDiretorioSeExistir(diretorioAntigo);
            }

            enviarEvento(responseObserver, request, "CONCLUIDO", "Aplicação iniciada com sucesso", containerId, diretorio);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Erro ao realizar deploy da aplicação {}", request.getNome(), e);
            if (diretorio != null) {
                deletarDiretorioSeExistir(diretorio);
            }

            enviarEvento(responseObserver, request, "ERRO", e.getMessage() != null ? e.getMessage() : "Erro durante o deploy", "", "");
            responseObserver.onCompleted();
        }
    }
    @Override
    public void removerAplicacao(RemoverAplicacaoRequest request, StreamObserver<RemoverAplicacaoResponse> responseObserver) {
        String aplicacaoId = request.getAplicacaoId();
        String containerId = request.getContainerId();
        try {
            if (aplicacaoId.isBlank() || containerId.isBlank()) {
                throw new IllegalArgumentException("ID da aplicação e do container são obrigatórios.");
            }
            var container = dockerClient
                    .inspectContainerCmd(containerId)
                    .exec();
            var config = container.getConfig();
            var labels = config != null ? config.getLabels() : null;
            if (labels == null
                    || !"true".equals(labels.get("floccus.aplicacao"))
                    || !aplicacaoId.equals(labels.get("floccus.aplicacao.id"))) {
                throw new SecurityException(
                        "O container não é uma aplicação Floccus válida "
                                + "ou não pertence à aplicação informada."
                );
            }
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .withRemoveVolumes(false)
                    .exec();
            deletarDiretorioSeExistir(request.getDiretorioArquivos());
            responseObserver.onNext(
                    RemoverAplicacaoResponse.newBuilder()
                            .setSucesso(true)
                            .setAplicacaoId(aplicacaoId)
                            .setContainerId(containerId)
                            .setMensagem("Container removido com sucesso.")
                            .build()
            );

            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (SecurityException e) {
            responseObserver.onError(
                    io.grpc.Status.PERMISSION_DENIED
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (io.grpc.StatusRuntimeException e) {
            responseObserver.onError(e);
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Erro ao remover aplicação.")
                            .asRuntimeException()
            );
        }
    }
    private void validarRequest(CriarAplicacaoRequest request) {

        if (request.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome da aplicação não informado");
        }

        if (request.getAplicacaoId().isBlank()) {
            throw new IllegalArgumentException("ID da aplicação não informado");
        }

        if (request.getRepositorioGit().isBlank()) {
            throw new IllegalArgumentException("Repositório Git não informado");
        }
    }

    private void enviarEvento(StreamObserver<DeployEvento> responseObserver, CriarAplicacaoRequest request, String tipo, String mensagem, String containerId, String diretorio) {

        responseObserver.onNext(DeployEvento.newBuilder().setAplicacaoId(request.getAplicacaoId()).setTipo(tipo).setMensagem(mensagem).setContainerId(containerId != null ? containerId : "").setDiretorioArquivos(diretorio).build());
    }

    private static String gerarIdentificador() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder resultado = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            resultado.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return resultado.toString();
    }
    private void deletarDiretorioSeExistir(String caminhoStr) {
        if (caminhoStr == null || caminhoStr.isBlank()) return;

        Path caminho = Paths.get(caminhoStr);
        if (Files.exists(caminho)) {
            try {
                Files.walkFileTree(caminho, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
                log.info("Diretório removido com sucesso: {}", caminhoStr);
            } catch (IOException e) {
                log.warn("Não foi possível remover o diretório {}: {}", caminhoStr, e.getMessage());
            }
        }
    }
}
