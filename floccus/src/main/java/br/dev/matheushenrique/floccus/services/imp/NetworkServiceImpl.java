package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.dto.RedeDTO;
import br.dev.matheushenrique.floccus.dto.ResponseDTO;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import br.dev.matheushenrique.floccus.services.NetworkService;
import br.dev.matheushenrique.grpc.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NetworkServiceImpl implements NetworkService {
    private final GrpcClientService grpcClientService;
    @Override
    public RedeDTO criarRede(RedeDTO redeDTO) throws StatusRuntimeException {
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
        var rede = grpcClientService.floccusAgente(
                agenteConexaoDto,
                FloccusRedeServiceGrpc::newBlockingStub,
                stub -> stub.criarRede(
                        CriarRedeRequest.newBuilder()
                                .setNome("Teste")
                                .setUsuario("Matheus")
                        .build()
                )
        );
        return RedeDTO.builder()
                .id(rede.getId())
                .nome(rede.getNome())
                .gateway(rede.getGateway())
                .subnet(rede.getSubnet())
                .usuario(rede.getUsuario())
                .build();
    }

    @Override
    public ResponseDTO excluirRede(RedeDTO redeDTO) throws StatusRuntimeException {
        AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
        var rede = grpcClientService.floccusAgente(
                agenteConexaoDto,
                FloccusRedeServiceGrpc::newBlockingStub,
                stub -> stub.excluirRede(
                        ExcluirRedeRequest.newBuilder()
                                .setUsuario(redeDTO.usuario())
                                .setNome(redeDTO.nome())
                                .setId(redeDTO.id())
                                .build()
                )
        );
        return ResponseDTO.builder()
                .message(rede.getMensagem())
                .status(rede.getSucesso())
                .build();
    }
}
