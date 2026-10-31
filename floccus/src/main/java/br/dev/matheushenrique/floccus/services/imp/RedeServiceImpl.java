package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.dto.RedeDTO;
import br.dev.matheushenrique.floccus.dto.ResponseDTO;
import br.dev.matheushenrique.floccus.models.Agente;
import br.dev.matheushenrique.floccus.models.Rede;
import br.dev.matheushenrique.floccus.models.Usuario;
import br.dev.matheushenrique.floccus.repository.AgenteRepository;
import br.dev.matheushenrique.floccus.repository.AplicacaoRepository;
import br.dev.matheushenrique.floccus.repository.RedeRepository;
import br.dev.matheushenrique.floccus.repository.UsuarioRepository;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import br.dev.matheushenrique.floccus.services.RedeService;
import br.dev.matheushenrique.grpc.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedeServiceImpl implements RedeService {
    private final GrpcClientService grpcClientService;
    private final RedeRepository redeRepository;
    private final AgenteRepository agenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AplicacaoRepository aplicacaoRepository;

//    @Override
//    public RedeDTO criarRede(RedeDTO redeDTO) throws StatusRuntimeException {
//        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
//        var rede = grpcClientService.floccusAgente(
//                agenteConexaoDto,
//                FloccusRedeServiceGrpc::newBlockingStub,
//                stub -> stub.criarRede(
//                        CriarRedeRequest.newBuilder()
//                                .setNome("Teste")
//                                .setUsuario("Matheus")
//                        .build()
//                )
//        );
//        return RedeDTO.builder()
//                .id(rede.getId())
//                .nome(rede.getNome())
//                .gateway(rede.getGateway())
//                .subnet(rede.getSubnet())
//                .usuario(rede.getUsuario())
//                .build();
//    }

    @Override
    public RedeDTO criarRede(RedeDTO redeDTO, Authentication authentication) throws StatusRuntimeException {
        if (redeRepository.existsByNomeAndAgenteId(
                redeDTO.nome(),
                redeDTO.agenteId()
        )) {
            throw new RuntimeException(
                    "Já existe uma rede com esse nome neste agente"
            );
        }
        Agente agente = agenteRepository.findById(redeDTO.agenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        try{
            var rede = grpcClientService.floccusAgente(
                    agenteConexaoDto,
                    FloccusRedeServiceGrpc::newBlockingStub,
                    stub -> stub.criarRede(
                            CriarRedeRequest.newBuilder()
                                    .setNome(redeDTO.nome())
                                    .setUsuario(usuario.getNome() + "_" + usuario.getId())
                                    .build()
                    )
            );
            Rede redeCriada = Rede.builder()
                    .nome(rede.getNome())
                    .gateway(rede.getGateway())
                    .subnet(rede.getSubnet())
                    .agenteId(agente.getId())
                    .usuarioId(usuario.getId())
                    .redeAgenteId(rede.getId())
                    .build();
            redeRepository.save(redeCriada);
            return RedeDTO.builder()
                    .id(rede.getId())
                    .nome(rede.getNome())
                    .gateway(rede.getGateway())
                    .subnet(rede.getSubnet())
                    .build();
        }catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                throw new RuntimeException(
                        "Não foi possível conectar ao agente. Verifique se ele está online e tente novamente."
                );
            }
            throw new RuntimeException(e.getStatus().getDescription());
        }
    }

    @Override
    public void excluirRede(String redeId, Authentication authentication) {
        Rede rede = redeRepository.findById(redeId).orElseThrow(() -> new RuntimeException("Rede não encontrado"));
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!rede.getUsuarioId().equals(usuario.getId())){
            throw new RuntimeException("Essa rede não pertence ao usuario conectado");
        }
        if (rede.getRedeAgenteId() == null || rede.getRedeAgenteId().isEmpty()) {
            throw new RuntimeException("Rede não possui identificação no agente");
        }
        if (aplicacaoRepository.existsByRedeId(redeId)) {
            throw new RuntimeException("A rede está sendo utilizada por uma aplicação.");
        }
        Agente agente = agenteRepository.findById(rede.getAgenteId()).orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto(agente.getIpAgente(), agente.getPortaAgente(), agente.getChaveAgente());
                var redeExcluida = grpcClientService.floccusAgente(
                agenteConexaoDto,
                FloccusRedeServiceGrpc::newBlockingStub,
                stub -> stub.excluirRede(
                        ExcluirRedeRequest.newBuilder()
                                .setId(rede.getRedeAgenteId())
                                .build()
                )
        );
        redeRepository.delete(rede);
    }

    @Override
    public Page<RedeDTO> listarRedes(Pageable pageable, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return redeRepository.listarPorUsuario(usuario.getId(), pageable);
    }

    @Override
    public List<RedeDTO> listarRedes(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return redeRepository.listarPorUsuario(usuario.getId());
    }

//    @Override
//    public ResponseDTO excluirRede(RedeDTO redeDTO) throws StatusRuntimeException {
//        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
//        var rede = grpcClientService.floccusAgente(
//                agenteConexaoDto,
//                FloccusRedeServiceGrpc::newBlockingStub,
//                stub -> stub.excluirRede(
//                        ExcluirRedeRequest.newBuilder()
//                                .setUsuario(redeDTO.usuario())
//                                .setNome(redeDTO.nome())
//                                .setId(redeDTO.id())
//                                .build()
//                )
//        );
//        return ResponseDTO.builder()
//                .message(rede.getMensagem())
//                .status(rede.getSucesso())
//                .build();
//    }
}
