package br.dev.matheushenrique.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class DockerClientConfig {
    private static DockerClient instance;
    private DockerClientConfig() {}
    public static synchronized DockerClient getInstance() {
        if (instance == null) {
            log.info("[Docker API] Inicializando instância única do DockerClient...");
            DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("unix:///var/run/docker.sock")
                    .build();
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100) // Permite múltiplas requisições gRPC simultâneas ao Docker
                    .connectionTimeout(Duration.ofSeconds(5))
                    .responseTimeout(Duration.ofSeconds(30))
                    .build();
            instance = DockerClientImpl.getInstance(config, httpClient);
            log.info("[Docker API] DockerClient Singleton criado com sucesso via Unix Socket.");
        }
        return instance;
    }
}