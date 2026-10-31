package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.grpc.CriarAplicacaoRequest;
import br.dev.matheushenrique.floccus.grpc.DeployEvento;
import br.dev.matheushenrique.floccus.grpc.FloccusContainerServiceGrpc;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import br.dev.matheushenrique.grpc.AgenteDockerServiceGrpc;
import br.dev.matheushenrique.grpc.LogContainer;
import br.dev.matheushenrique.grpc.LogsContainerRequest;
import io.grpc.*;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;


@Service
public class GrpcClientServiceImpl implements GrpcClientService {

    @Override
    public <S extends AbstractBlockingStub<S>, R> R floccusAgente(
            AgenteConexaoDto agenteConexaoDto,
            Function<Channel, S> stubFactory,
            StubConsumer<S, R> consumer) throws StatusRuntimeException {

        System.out.println("=== INICIANDO GRPC ===");

        System.out.println(
                "Conectando em: "
                        + agenteConexaoDto.host()
                        + ":"
                        + agenteConexaoDto.porta()
        );

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(
                        agenteConexaoDto.host(),
                        agenteConexaoDto.porta()
                )
                .usePlaintext()
                .build();

        System.out.println("Canal criado");

        try {

            Metadata headers = new Metadata();

            Metadata.Key<String> API_KEY_HEADER =
                    Metadata.Key.of(
                            "x-floccus-api-key",
                            Metadata.ASCII_STRING_MARSHALLER
                    );

            headers.put(
                    API_KEY_HEADER,
                    agenteConexaoDto.apiKey()
            );

            System.out.println("API Key configurada");

            Channel channelWithHeaders =
                    ClientInterceptors.intercept(
                            channel,
                            MetadataUtils.newAttachHeadersInterceptor(headers)
                    );

            System.out.println("Interceptor configurado");

            S stub = stubFactory
                    .apply(channelWithHeaders)
                    .withDeadlineAfter(
                            120,
                            TimeUnit.SECONDS
                    );

            System.out.println("Stub criado");

            System.out.println("Executando chamada gRPC...");

            R response = consumer.execute(stub);

            System.out.println("Resposta recebida");

            return response;

        } catch (StatusRuntimeException e) {

            System.err.println("=== ERRO GRPC ===");
            System.err.println("Código: " + e.getStatus().getCode());
            System.err.println("Descrição: " + e.getStatus().getDescription());

            e.printStackTrace();

            throw e;

        } catch (Exception e) {

            System.err.println("=== ERRO INESPERADO GRPC ===");
            e.printStackTrace();

            throw e;

        } finally {

            System.out.println("Encerrando canal...");

            desconectarCanal(channel);

            System.out.println("Canal encerrado");
        }
    }
    @Override
    public ManagedChannel streamarLogsContainer(AgenteConexaoDto agenteConexaoDto, String containerId, int linhas, StreamObserver<LogContainer> responseObserver) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(agenteConexaoDto.host(), agenteConexaoDto.porta())
                .usePlaintext()
                .build();

        Metadata headers = new Metadata();
        Metadata.Key<String> API_KEY_HEADER = Metadata.Key.of("x-floccus-api-key", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(API_KEY_HEADER, agenteConexaoDto.apiKey());
        Channel channelWithHeaders = ClientInterceptors.intercept(
                channel,
                MetadataUtils.newAttachHeadersInterceptor(headers)
        );
        AgenteDockerServiceGrpc.AgenteDockerServiceStub asyncStub = AgenteDockerServiceGrpc.newStub(channelWithHeaders);
        LogsContainerRequest request = LogsContainerRequest.newBuilder()
                .setId(containerId)
                .setLinhas(linhas)
                .build();
        asyncStub.streamarLogsContainer(request, responseObserver);
        return channel;
    }

    private void desconectarCanal(ManagedChannel channel) {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    @FunctionalInterface
    public interface StubConsumer<S extends AbstractBlockingStub<S>, R> {
        R execute(S stub) throws StatusRuntimeException;
    }
    @Override
    public ManagedChannel streamarDeploy(
            AgenteConexaoDto agenteConexaoDto,
            CriarAplicacaoRequest request,
            StreamObserver<DeployEvento> responseObserver
    ) {

        System.out.println("=== INICIANDO STREAMING DO DEPLOY ===");

        System.out.println(
                "Conectando em: "
                        + agenteConexaoDto.host()
                        + ":"
                        + agenteConexaoDto.porta()
        );

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(
                        agenteConexaoDto.host(),
                        agenteConexaoDto.porta()
                )
                .usePlaintext()
                .build();

        Metadata headers = new Metadata();

        Metadata.Key<String> API_KEY_HEADER =
                Metadata.Key.of(
                        "x-floccus-api-key",
                        Metadata.ASCII_STRING_MARSHALLER
                );

        headers.put(
                API_KEY_HEADER,
                agenteConexaoDto.apiKey()
        );

        Channel channelWithHeaders =
                ClientInterceptors.intercept(
                        channel,
                        MetadataUtils.newAttachHeadersInterceptor(headers)
                );

        FloccusContainerServiceGrpc.FloccusContainerServiceStub asyncStub =
                FloccusContainerServiceGrpc
                        .newStub(channelWithHeaders)
                        .withDeadlineAfter(
                                120,
                                TimeUnit.SECONDS
                        );

        asyncStub.criarAplicacao(
                request,
                new StreamObserver<DeployEvento>() {

                    @Override
                    public void onNext(DeployEvento evento) {
                        responseObserver.onNext(evento);
                    }

                    @Override
                    public void onError(Throwable erro) {
                        responseObserver.onError(erro);
                        desconectarCanal(channel);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                        desconectarCanal(channel);
                    }
                }
        );

        return channel;
    }
}
