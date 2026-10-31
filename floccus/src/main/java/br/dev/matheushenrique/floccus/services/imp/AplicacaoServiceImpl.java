package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.*;
import br.dev.matheushenrique.floccus.enums.StatusAplicacao;
import br.dev.matheushenrique.floccus.grpc.CriarAplicacaoRequest;
import br.dev.matheushenrique.floccus.grpc.DeployEvento;
import br.dev.matheushenrique.floccus.grpc.FloccusContainerServiceGrpc;
import br.dev.matheushenrique.floccus.grpc.RemoverAplicacaoRequest;
import br.dev.matheushenrique.floccus.models.*;
import br.dev.matheushenrique.floccus.repository.*;
import br.dev.matheushenrique.floccus.services.AplicacaoService;
import br.dev.matheushenrique.floccus.services.GitHubService;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import br.dev.matheushenrique.floccus.services.TokenService;
import br.dev.matheushenrique.grpc.AgenteDockerServiceGrpc;
import br.dev.matheushenrique.grpc.ContainerRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AplicacaoServiceImpl implements AplicacaoService {
    private final GrpcClientService grpcClientService;
    private final AplicacaoRepository aplicacaoRepository;
    private final AgenteRepository agenteRepository;
    private final GitHubRepository gitHubRepository;
    private final TokenService tokenService;
    private final RedeRepository redeRepository;
    private final UsuarioRepository usuarioRepository;
    private final GitHubService gitHubService;
    private final VariaveisRepository variaveisRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public List<AplicacaoDTO> listarAplicacoes(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return aplicacaoRepository.buscarTodasPorUsuario(usuario.getId());
    }

    @Override
    public ContainerDTO iniciarContainer(String aplicacaoId) throws StatusRuntimeException {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicacao não encontrado"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        var container = grpcClientService.floccusAgente(agenteConexaoDto, AgenteDockerServiceGrpc::newBlockingStub, stub -> stub.iniciarContainer(ContainerRequest.newBuilder().setId(aplicacao.getContainerId()).build()));
        List<RedeContainerDTO> redes = container.getContainer().getRedesList().stream().map(redeProto -> new RedeContainerDTO(redeProto.getNome(), redeProto.getIp(), redeProto.getGateway(), redeProto.getMac())).toList();
        return ContainerDTO.builder().id(container.getContainer().getId()).nome(container.getContainer().getNome()).image(container.getContainer().getImagem()).status(container.getContainer().getStatus()).redes(redes).build();
    }

    @Override
    public ContainerDTO pararContainer(String aplicacaoId) throws StatusRuntimeException {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicacao não encontrado"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        var container = grpcClientService.floccusAgente(agenteConexaoDto, AgenteDockerServiceGrpc::newBlockingStub, stub -> stub.pararContainer(ContainerRequest.newBuilder().setId(aplicacao.getContainerId()).build()));
        List<RedeContainerDTO> redes = container.getContainer().getRedesList().stream().map(redeProto -> new RedeContainerDTO(redeProto.getNome(), redeProto.getIp(), redeProto.getGateway(), redeProto.getMac())).toList();
        return ContainerDTO.builder().id(container.getContainer().getId()).nome(container.getContainer().getNome()).image(container.getContainer().getImagem()).status(container.getContainer().getStatus()).redes(redes).build();
    }

    @Override
    public ContainerDTO reiniciarContainer(String aplicacaoId) throws StatusRuntimeException {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicacao não encontrado"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        var container = grpcClientService.floccusAgente(agenteConexaoDto, AgenteDockerServiceGrpc::newBlockingStub, stub -> stub.reiniciarContainer(ContainerRequest.newBuilder().setId(aplicacao.getContainerId()).build()));
        List<RedeContainerDTO> redes = container.getContainer().getRedesList().stream().map(redeProto -> new RedeContainerDTO(redeProto.getNome(), redeProto.getIp(), redeProto.getGateway(), redeProto.getMac())).toList();
        return ContainerDTO.builder().id(container.getContainer().getId()).nome(container.getContainer().getNome()).image(container.getContainer().getImagem()).status(container.getContainer().getStatus()).redes(redes).build();
    }

    @Override
    public AplicacaoDTO detalharContainer(String aplicacaoId) throws StatusRuntimeException {
        AplicacaoDTO aplicacao = aplicacaoRepository.buscarDTOPorId(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicacao não encontrado"));
        return aplicacao;
//        return AplicacaoDTO.builder().id(aplicacao.getId()).nome(aplicacao.getNome()).imagem(aplicacao.getImagem()).agenteId(aplicacao.getAgenteId()).containerId(aplicacao.getContainerId()).tipo(aplicacao.getTipo()).portaContainer(aplicacao.getPortaContainer()).portaExterna(aplicacao.getPortaExterna()).dominio(aplicacao.getDominio()).memoriaLimite(aplicacao.getMemoriaLimite()).cpuLimite(aplicacao.getCpuLimite()).criadoEm(aplicacao.getCriadoEm()).atualizadoEm(aplicacao.getAtualizadoEm()).status(aplicacao.getStatus()).build();
    }

    @Override
    public AplicacaoDTO criarAplicacao(CriarAplicacaoDTO aplicacaoDTO, Authentication authentication) {
        Agente agente = agenteRepository.findById(aplicacaoDTO.agenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        Rede rede = redeRepository.findById(aplicacaoDTO.redeId()).orElseThrow(() -> new RuntimeException("Rede não encontrado"));
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Aplicacao aplicacao = Aplicacao.builder()
                .nome(aplicacaoDTO.nome())
                .repositorioId(aplicacaoDTO.repositorioId())
                .imagem(aplicacaoDTO.imagem())
                .agenteId(aplicacaoDTO.agenteId())
                .redeId(aplicacaoDTO.redeId())
                .tipo(aplicacaoDTO.tipo())
                .repositorioId(aplicacaoDTO.repositorioId())
                .portaContainer(aplicacaoDTO.portaContainer())
                .portaExterna(aplicacaoDTO.portaExterna())
                .dominio(aplicacaoDTO.dominio())
                .memoriaLimite(aplicacaoDTO.memoriaLimite())
                .cpuLimite(aplicacaoDTO.cpuLimite())
                .buildTool(aplicacaoDTO.buildTool())
                .runtime(aplicacaoDTO.runtime())
                .framework(aplicacaoDTO.framework())
                .linguagem(aplicacaoDTO.linguagem())
                .branch(aplicacaoDTO.branch())
                .idGitApp(aplicacaoDTO.idGitApp())
                .usuarioId(usuario.getId())
                .status(StatusAplicacao.CRIADO)
                .build();
        Aplicacao salva = aplicacaoRepository.save(aplicacao);

        return toDTO(salva);

//        GitHub gitHub = gitHubRepository.findByUsuarioId("6b56997d-e865-48f9-9d84-3b235806fe3c").orElseThrow(() -> new RuntimeException("Nao achou"));
//        Agente agente = agenteRepository.findById("0ff9a69b-fc1e-4a4f-bbef-593ed419b80f").orElseThrow(() -> new RuntimeException("Agente não encontrado"));
//        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
//        String jwt = tokenService.gerarTokenClone(gitHub);
//        var container = grpcClientService.floccusAgente(
//                agenteConexaoDto,
//                FloccusContainerServiceGrpc::newBlockingStub,
//                stub -> stub.criarAplicacao(
//                        br.dev.matheushenrique.floccus.grpc.CriarAplicacaoRequest.newBuilder()
//                                .setNome("test")
//                                .setAplicacaoId("a")
//                                .setDiretorioArquivos("/home/matheus/floccus/apps/csx")
//                                .setRepositorioGit("https://github.com/MasterOfLie/csx-cnpj.git")
//                                .setTokenGit(jwt)
//                                .build()
//                )
//        );
//        System.out.println(container.getLinguagem());
    }

    @Async
    @Override
    public void iniciarDeploy(String aplicacaoId, Authentication authentication, boolean redeploy) {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicação não encontrada"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        Rede rede = redeRepository.findById(aplicacao.getRedeId()).orElseThrow(() -> new RuntimeException("Rede não encontrado"));
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario não encontrado"));
        GitHub gitHub = gitHubRepository.findByUsuarioIdAndId(usuario.getId(), aplicacao.getIdGitApp()).orElseThrow(() -> new RuntimeException("Sem repositorio git cadastrado"));
        String jwt = tokenService.gerarTokenClone(gitHub);
        GitHubRepositoryDTO gitHubRepositoryDTO = gitHubService.buscarRepositorioPorId(Long.valueOf(aplicacao.getRepositorioId()), authentication, aplicacao.getIdGitApp());
        List<Variaveis> variaveis = variaveisRepository.findAllByAplicacaoId(aplicacao.getId());

        var request = CriarAplicacaoRequest.newBuilder()
                .setNome(aplicacao.getNome()
                        .replace(" ", ""))
                .setAplicacaoId(aplicacao.getId())
                .setImagem(aplicacao.getImagem())
                .setBuildCommand(aplicacao.getBuildTool())
                .setStartCommand(aplicacao.getRuntime())
                .setDiretorioArquivos(
                        redeploy && aplicacao.getPath() != null ? aplicacao.getPath() : ""
                )
                .setRepositorioGit(gitHubRepositoryDTO.htmlUrl())
                .setTokenGit(jwt)
                .setRede(rede.getRedeAgenteId())
                .setMemoriaMb(aplicacao.getMemoriaLimite())
                .setBranch(aplicacao.getBranch())
                .setCpu(aplicacao.getCpuLimite())
                .setPortaContainer(aplicacao.getPortaContainer())
                .setPortaExterna(
                        aplicacao.getPortaExterna() != null
                                ? aplicacao.getPortaExterna()
                                : 0
                )
                .setRedeploy(redeploy)
                .setContainerId(
                        redeploy && aplicacao.getContainerId() != null ? aplicacao.getContainerId() : ""
                )
                .setDominio(aplicacao.getDominio())
                .addAllAmbiente(
                        variaveis.stream()
                                .map(v -> br.dev.matheushenrique.floccus.grpc.Variavel.newBuilder()
                                        .setChave(v.getChave())
                                        .setValor(v.getValor())
                                        .build())
                                .toList()
                )
                .build();
        grpcClientService.streamarDeploy(agenteConexaoDto, request, new StreamObserver<DeployEvento>() {

            @Override
            public void onNext(DeployEvento evento) {

                DeployEventoDto dto = new DeployEventoDto(evento.getAplicacaoId(), evento.getTipo(), evento.getMensagem(), evento.getContainerId());

                messagingTemplate.convertAndSend("/topic/deploy/" + evento.getAplicacaoId(), dto);

                if (evento.getTipo().equals("CONTAINER_CRIADO")) {
                    String containerId = evento.getContainerId();
                    if (!containerId.isBlank()) {
                        aplicacao.setContainerId(containerId);
                        aplicacao.setPath(evento.getDiretorioArquivos());
                        aplicacaoRepository.save(aplicacao);
                    }
                }

                if (evento.getTipo().equals("CONCLUIDO")) {
                    String containerId = evento.getContainerId();
                    aplicacao.setStatus(StatusAplicacao.ONLINE);
                    aplicacao.setContainerId(containerId);
                    aplicacao.setPath(evento.getDiretorioArquivos());
                    aplicacao.setPath(evento.getDiretorioArquivos());
                    aplicacaoRepository.save(aplicacao);
                }

                if (evento.getTipo().equals("ERRO")) {
                    String containerId = evento.getContainerId();
                    aplicacao.setStatus(StatusAplicacao.ERRO);
                    aplicacao.setContainerId(containerId);
                    aplicacao.setPath(evento.getDiretorioArquivos());
                    aplicacaoRepository.save(aplicacao);
                }
            }

            @Override
            public void onError(Throwable erro) {
                aplicacao.setStatus(StatusAplicacao.ERRO);
                aplicacaoRepository.save(aplicacao);
                DeployEventoDto eventoErro = new DeployEventoDto(aplicacaoId, "ERRO", "Falha na comunicação com o agente", "");
                messagingTemplate.convertAndSend("/topic/deploy/" + aplicacaoId, eventoErro);
            }

            @Override
            public void onCompleted() {

            }
        });
    }
    @Async
    @Override
    public void redeployWebhook(String aplicacaoId, boolean redeploy) {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicação não encontrada"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        System.out.println(aplicacao.getUsuarioId());
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        Rede rede = redeRepository.findById(aplicacao.getRedeId()).orElseThrow(() -> new RuntimeException("Rede não encontrado"));
        Usuario usuario = usuarioRepository.findById(aplicacao.getUsuarioId()).orElseThrow(() -> new RuntimeException("Usuario não encontrado"));
        GitHub gitHub = gitHubRepository.findByUsuarioIdAndId(usuario.getId(), aplicacao.getIdGitApp()).orElseThrow(() -> new RuntimeException("Sem repositorio git cadastrado"));
        String jwt = tokenService.gerarTokenClone(gitHub);
        GitHubRepositoryDTO gitHubRepositoryDTO = gitHubService.buscarRepositorioPorIdWebhook(Long.valueOf(aplicacao.getRepositorioId()), usuario, aplicacao.getIdGitApp());
        List<Variaveis> variaveis = variaveisRepository.findAllByAplicacaoId(aplicacao.getId());

        var request = CriarAplicacaoRequest.newBuilder()
                .setNome(aplicacao.getNome()
                        .replace(" ", ""))
                .setAplicacaoId(aplicacao.getId())
                .setImagem(aplicacao.getImagem())
                .setBuildCommand(aplicacao.getBuildTool())
                .setStartCommand(aplicacao.getRuntime())
                .setDiretorioArquivos(
                        redeploy && aplicacao.getPath() != null ? aplicacao.getPath() : ""
                )
                .setRepositorioGit(gitHubRepositoryDTO.htmlUrl())
                .setTokenGit(jwt)
                .setRede(rede.getRedeAgenteId())
                .setMemoriaMb(aplicacao.getMemoriaLimite())
                .setBranch(aplicacao.getBranch())
                .setCpu(aplicacao.getCpuLimite())
                .setPortaContainer(aplicacao.getPortaContainer())
                .setPortaExterna(aplicacao.getPortaExterna())
                .setRedeploy(redeploy)
                .setContainerId(
                        redeploy && aplicacao.getContainerId() != null ? aplicacao.getContainerId() : ""
                )
                .addAllAmbiente(
                        variaveis.stream()
                                .map(v -> br.dev.matheushenrique.floccus.grpc.Variavel.newBuilder()
                                        .setChave(v.getChave())
                                        .setValor(v.getValor())
                                        .build())
                                .toList()
                )
                .build();
        grpcClientService.streamarDeploy(agenteConexaoDto, request, new StreamObserver<DeployEvento>() {

            @Override
            public void onNext(DeployEvento evento) {
                DeployEventoDto dto = new DeployEventoDto(evento.getAplicacaoId(), evento.getTipo(), evento.getMensagem(), evento.getContainerId());
                messagingTemplate.convertAndSend("/topic/deploy/" + evento.getAplicacaoId(), dto);
                if (evento.getTipo().equals("CONTAINER_CRIADO")) {
                    String containerId = evento.getContainerId();
                    if (!containerId.isBlank()) {
                        aplicacao.setContainerId(containerId);
                        aplicacao.setPath(evento.getDiretorioArquivos());
                        aplicacaoRepository.save(aplicacao);
                    }
                }

                if (evento.getTipo().equals("CONCLUIDO")) {
                    String containerId = evento.getContainerId();
                    aplicacao.setStatus(StatusAplicacao.ONLINE);
                    aplicacao.setContainerId(containerId);
                    aplicacao.setPath(evento.getDiretorioArquivos());
                    aplicacao.setPath(evento.getDiretorioArquivos());
                    aplicacaoRepository.save(aplicacao);
                }

                if (evento.getTipo().equals("ERRO")) {
                    String containerId = evento.getContainerId();
                    aplicacao.setStatus(StatusAplicacao.ERRO);
                    aplicacao.setContainerId(containerId);
                    aplicacao.setPath(evento.getDiretorioArquivos());
                    aplicacaoRepository.save(aplicacao);
                }
            }

            @Override
            public void onError(Throwable erro) {
                aplicacao.setStatus(StatusAplicacao.ERRO);
                aplicacaoRepository.save(aplicacao);
                DeployEventoDto eventoErro = new DeployEventoDto(aplicacaoId, "ERRO", "Falha na comunicação com o agente", "");
                messagingTemplate.convertAndSend("/topic/deploy/" + aplicacaoId, eventoErro);
            }

            @Override
            public void onCompleted() {

            }
        });
    }

    @Override
    public AplicacaoDTO atualizaAplicacao(String aplicacaoId, AplicacaoDTO aplicacaoDTO, Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuário não autenticado.");
        }

        if (aplicacaoDTO == null
                || aplicacaoDTO.id() == null
                || aplicacaoDTO.id().isBlank()) {
            throw new RuntimeException(
                    "O ID da aplicação é obrigatório."
            );
        }

        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId)
                .orElseThrow(() -> new RuntimeException(
                        "Aplicação não encontrada: " + aplicacaoId
                ));
        if (aplicacaoDTO.nome() == null
                || aplicacaoDTO.nome().isBlank()
                || aplicacaoDTO.nome().trim().length() > 100) {
            throw new RuntimeException(
                    "O nome é obrigatório e deve ter no máximo 100 caracteres."
            );
        }

        if (aplicacaoDTO.tipo() == null) {
            throw new RuntimeException(
                    "O tipo da aplicação é obrigatório."
            );
        }

        if (aplicacaoDTO.imagem() == null
                || aplicacaoDTO.imagem().isBlank()
                || aplicacaoDTO.imagem().trim().length() > 255) {
            throw new RuntimeException(
                    "A imagem Docker é obrigatória e deve ter no máximo 255 caracteres."
            );
        }

        if (aplicacaoDTO.portaContainer() == null
                || aplicacaoDTO.portaContainer() < 1
                || aplicacaoDTO.portaContainer() > 65535) {
            throw new RuntimeException(
                    "A porta do container deve estar entre 1 e 65535."
            );
        }

        if (aplicacaoDTO.portaExterna() == null
                || aplicacaoDTO.portaExterna() < 1
                || aplicacaoDTO.portaExterna() > 65535) {
            throw new RuntimeException(
                    "A porta externa deve estar entre 1 e 65535."
            );
        }

        if (aplicacaoDTO.memoriaLimite() == null
                || aplicacaoDTO.memoriaLimite() <= 0) {
            throw new RuntimeException(
                    "O limite de memória deve ser maior que zero."
            );
        }

        if (aplicacaoDTO.cpuLimite() == null
                || aplicacaoDTO.cpuLimite() <= 0) {
            throw new RuntimeException(
                    "O limite de CPU deve ser maior que zero."
            );
        }

        if (aplicacaoDTO.linguagem() != null
                && aplicacaoDTO.linguagem().trim().length() > 100) {
            throw new RuntimeException("A linguagem deve ter no máximo 100 caracteres.");
        }

        if (aplicacaoDTO.framework() != null
                && aplicacaoDTO.framework().trim().length() > 100) {
            throw new RuntimeException("O framework deve ter no máximo 100 caracteres.");
        }

        if (aplicacaoDTO.runtime() != null
                && aplicacaoDTO.runtime().trim().length() > 255) {
            throw new RuntimeException("O runtime deve ter no máximo 255 caracteres.");
        }

        if (aplicacaoDTO.buildTool() != null
                && aplicacaoDTO.buildTool().trim().length() > 255) {
            throw new RuntimeException("A ferramenta de build deve ter no máximo 255 caracteres.");
        }

        // Atualiza somente os campos permitidos
        aplicacao.setNome(aplicacaoDTO.nome().trim());
        aplicacao.setTipo(aplicacaoDTO.tipo());
        aplicacao.setLinguagem(aplicacaoDTO.linguagem());
        aplicacao.setFramework(aplicacaoDTO.framework());
        aplicacao.setImagem(aplicacaoDTO.imagem().trim());
        aplicacao.setPortaContainer(aplicacaoDTO.portaContainer());
        aplicacao.setPortaExterna(aplicacaoDTO.portaExterna());
        aplicacao.setCpuLimite(aplicacaoDTO.cpuLimite());
        aplicacao.setMemoriaLimite(aplicacaoDTO.memoriaLimite());
        aplicacaoRepository.save(aplicacao);

        return aplicacaoRepository.buscarDTOPorId(aplicacaoId).orElseThrow(() -> new IllegalArgumentException("Aplicação não encontrada"));
    }
    @Transactional
    @Override
    public void removerAplicacao(String aplicacaoId, Authentication authentication) {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicação não encontrada"));
        Agente agente = agenteRepository.findById(aplicacao.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrada"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        try {
            var rede = grpcClientService.floccusAgente(
                    agenteConexaoDto,
                    FloccusContainerServiceGrpc::newBlockingStub,
                    stub -> stub.removerAplicacao(
                            RemoverAplicacaoRequest.newBuilder()
                                    .setAplicacaoId(aplicacao.getId())
                                    .setContainerId(aplicacao.getContainerId())
                                    .setDiretorioArquivos(aplicacao.getPath())
                                    .build()
                    )
            );
            variaveisRepository.deleteAllByAplicacaoId(aplicacao.getId());
            aplicacaoRepository.deleteById(aplicacaoId);

        }catch (StatusRuntimeException e){
            Status.Code codigo = e.getStatus().getCode();
            String mensagem = e.getStatus().getDescription();
            if (mensagem == null || mensagem.isBlank()) {
                mensagem = "Não foi possível remover a aplicação.";
            }
            HttpStatus httpStatus = switch (codigo) {
                case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
                case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
                case NOT_FOUND -> HttpStatus.NOT_FOUND;
                case UNAVAILABLE, DEADLINE_EXCEEDED ->
                        HttpStatus.SERVICE_UNAVAILABLE;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
            throw new RuntimeException(mensagem);
        }
    }

    private AplicacaoDTO toDTO(Aplicacao aplicacao) {
        return AplicacaoDTO.builder().id(aplicacao.getId()).nome(aplicacao.getNome()).repositorioId(aplicacao.getRepositorioId()).imagem(aplicacao.getImagem()).agenteId(aplicacao.getAgenteId()).redeId(aplicacao.getRedeId()).containerId(aplicacao.getContainerId()).tipo(aplicacao.getTipo()).portaContainer(aplicacao.getPortaContainer()).portaExterna(aplicacao.getPortaExterna()).dominio(aplicacao.getDominio()).memoriaLimite(aplicacao.getMemoriaLimite()).cpuLimite(aplicacao.getCpuLimite()).path(aplicacao.getPath()).linguagem(aplicacao.getLinguagem()).framework(aplicacao.getFramework()).buildTool(aplicacao.getBuildTool()).status(aplicacao.getStatus()).criadoEm(aplicacao.getCriadoEm()).atualizadoEm(aplicacao.getAtualizadoEm()).build();
    }
}
