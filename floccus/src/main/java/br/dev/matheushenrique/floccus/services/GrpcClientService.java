package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.services.imp.GrpcClientServiceImpl;
import br.dev.matheushenrique.grpc.LogContainer;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.StreamObserver;

import java.util.function.Function;


public interface GrpcClientService {
    /**
     * Executa comandos gRPC para QUALQUER serviço/stub dinamicamente.
     *
     * @param agenteConexaoDto      Endereço, porta e chave do Agente
     * @param stubFactory  Construtor do Stub (ex: AgenteDockerServiceGrpc::newBlockingStub)
     * @param consumer     Ação gRPC a ser executada no Stub
     * @return O retorno da chamada do gRPC
     */
    public <S extends AbstractBlockingStub<S>, R> R floccusAgente (AgenteConexaoDto agenteConexaoDto, Function<Channel, S> stubFactory,
                                                                    GrpcClientServiceImpl.StubConsumer<S, R> consumer) throws StatusRuntimeException;
    /**
     * Inicia um stream de logs de um container através do Agent.
     *
     * @return o canal gRPC utilizado pelo stream
     */
    ManagedChannel streamarLogsContainer(
            AgenteConexaoDto agenteConexaoDto,
            String containerId,
            int linhas,
            StreamObserver<LogContainer> responseObserver
    );
}
