package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import io.grpc.*;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.MetadataUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;


@Service
public class GrpcClientServiceImpl implements GrpcClientService {

    @Override
    public <S extends AbstractBlockingStub<S>, R> R floccusAgente(AgenteConexaoDto agenteConexaoDto, Function<Channel, S> stubFactory, StubConsumer<S, R> consumer) throws StatusRuntimeException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(agenteConexaoDto.host(), agenteConexaoDto.porta())
                .usePlaintext()
                .build();
        try {
            Metadata headers = new Metadata();
            Metadata.Key<String> API_KEY_HEADER = Metadata.Key.of("x-floccus-api-key", Metadata.ASCII_STRING_MARSHALLER);
            headers.put(API_KEY_HEADER, agenteConexaoDto.apiKey());
            Channel channelWithHeaders = ClientInterceptors.intercept(
                    channel,
                    MetadataUtils.newAttachHeadersInterceptor(headers)
            );
            S stub = stubFactory.apply(channelWithHeaders)
                    .withDeadlineAfter(5, TimeUnit.SECONDS);
            return consumer.execute(stub);
        } finally {
            desconectarCanal(channel);
        }
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
}
