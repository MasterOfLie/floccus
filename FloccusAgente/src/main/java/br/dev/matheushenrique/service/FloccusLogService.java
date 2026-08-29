package br.dev.matheushenrique.service;

import br.dev.matheushenrique.grpc.LogContainer;
import br.dev.matheushenrique.grpc.TipoLog;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class FloccusLogService {

    private final DockerClient dockerClient;

    public FloccusLogService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public void streamLogs(
            String containerId,
            int linhas,
            StreamObserver<LogContainer> responseObserver
    ) {

        ServerCallStreamObserver<LogContainer> serverObserver =
                (ServerCallStreamObserver<LogContainer>) responseObserver;

        AtomicBoolean cancelado = new AtomicBoolean(false);

        final ResultCallback.Adapter<Frame>[] callbackHolder = new ResultCallback.Adapter[1];

        serverObserver.setOnCancelHandler(() -> {

            cancelado.set(true);

            log.info(
                    "[Docker Logs] Cliente desconectou do container {}",
                    containerId
            );

            ResultCallback.Adapter<Frame> callback = callbackHolder[0];

            if (callback != null) {
                try {
                    callback.close();
                } catch (Exception e) {
                    log.debug(
                            "[Docker Logs] Erro ao fechar stream do container {}",
                            containerId,
                            e
                    );
                }
            }
        });

        try {

            ResultCallback.Adapter<Frame> callback =
                    new ResultCallback.Adapter<Frame>() {

                        @Override
                        public void onNext(Frame frame) {

                            // Cliente já desconectou.
                            if (cancelado.get() || serverObserver.isCancelled()) {
                                return;
                            }

                            String mensagem = new String(
                                    frame.getPayload(),
                                    StandardCharsets.UTF_8
                            );

                            TipoLog tipo =
                                    frame.getStreamType() == StreamType.STDOUT
                                            ? TipoLog.STDOUT
                                            : TipoLog.STDERR;

                            LogContainer logContainer =
                                    LogContainer.newBuilder()
                                            .setId(containerId)
                                            .setMensagem(mensagem)
                                            .setTipo(tipo)
                                            .setTimestamp(System.currentTimeMillis())
                                            .build();

                            try {
                                responseObserver.onNext(logContainer);
                            } catch (Exception e) {

                                cancelado.set(true);

                                log.debug(
                                        "[Docker Logs] Stream gRPC já foi encerrado para o container {}",
                                        containerId
                                );
                            }
                        }

                        @Override
                        public void onComplete() {

                            if (cancelado.get() || serverObserver.isCancelled()) {
                                return;
                            }

                            responseObserver.onCompleted();
                        }

                        @Override
                        public void onError(Throwable throwable) {

                            if (cancelado.get() || serverObserver.isCancelled()) {
                                log.debug(
                                        "[Docker Logs] Stream cancelado para o container {}",
                                        containerId
                                );
                                return;
                            }

                            log.error(
                                    "[Docker Logs] Erro ao obter logs do container {}",
                                    containerId,
                                    throwable
                            );

                            responseObserver.onError(throwable);
                        }
                    };

            callbackHolder[0] = callback;

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTail(linhas)
                    .exec(callback);

        } catch (Exception e) {

            if (cancelado.get() || serverObserver.isCancelled()) {
                return;
            }

            log.error(
                    "[Docker Logs] Erro ao iniciar stream do container {}",
                    containerId,
                    e
            );

            responseObserver.onError(e);
        }
    }
}