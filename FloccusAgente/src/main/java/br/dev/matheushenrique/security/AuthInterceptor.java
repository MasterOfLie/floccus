package br.dev.matheushenrique.security;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthInterceptor implements ServerInterceptor {

    private final String chaveSecretaEsperada;

    private static final Metadata.Key<String> AUTH_KEY =
            Metadata.Key.of("x-floccus-api-key", Metadata.ASCII_STRING_MARSHALLER);
    public AuthInterceptor(String chaveSecretaEsperada) {
        this.chaveSecretaEsperada = chaveSecretaEsperada;
    }
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        String tokenRecebido = headers.get(AUTH_KEY);
        if (tokenRecebido != null && tokenRecebido.equals(chaveSecretaEsperada)) {
            return next.startCall(call, headers);
        }
        log.warn("Tentativa de acesso não autorizada recusada de: {}", call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR));
        call.close(Status.UNAUTHENTICATED.withDescription("Chave secreta inválida ou ausente."), new Metadata());
        return new ServerCall.Listener<ReqT>() {};
    }
}