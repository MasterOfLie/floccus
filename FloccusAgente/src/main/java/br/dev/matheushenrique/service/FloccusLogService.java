package br.dev.matheushenrique.service;
import br.dev.matheushenrique.grpc.LogContainer;
import br.dev.matheushenrique.grpc.TipoLog;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;

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

        try {

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTail(linhas)
                    .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<Frame>() {

                        @Override
                        public void onNext(Frame frame) {

                            String mensagem = new String(
                                    frame.getPayload(),
                                    StandardCharsets.UTF_8
                            );

                            TipoLog tipo = frame.getStreamType() == StreamType.STDOUT
                                    ? TipoLog.STDOUT
                                    : TipoLog.STDERR;

                            LogContainer logContainer = LogContainer.newBuilder()
                                    .setId(containerId)
                                    .setMensagem(mensagem)
                                    .setTipo(tipo)
                                    .setTimestamp(System.currentTimeMillis())
                                    .build();

                            responseObserver.onNext(logContainer);
                        }

                        @Override
                        public void onComplete() {
                            responseObserver.onCompleted();
                        }

                        @Override
                        public void onError(Throwable throwable) {

                            log.error(
                                    "[Docker Logs] Erro ao obter logs do container {}",
                                    containerId,
                                    throwable
                            );

                            responseObserver.onError(throwable);
                        }
                    });

        } catch (Exception e) {

            log.error(
                    "[Docker Logs] Erro ao iniciar stream do container {}",
                    containerId,
                    e
            );

            responseObserver.onError(e);
        }
    }
}
