package br.dev.matheushenrique.config;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Data
public class FloccusConfig {

    private int agentPort;
    private String secretKey;
    private String backendHost;
    private int backendPort;
    private String dockerHost;
    private String agenteId;

    @SuppressWarnings("unchecked")
    public static FloccusConfig loadConfig() {
        FloccusConfig config = new FloccusConfig();
        Path configPath = Paths.get("config.yml");
        System.out.println("Working directory: " +
                Paths.get("").toAbsolutePath());

        System.out.println("Config: " +
                configPath.toAbsolutePath());
        try{
            if (Files.notExists(configPath)) {
                createDefaultConfigFile(configPath);
            }
            try (InputStream input = Files.newInputStream(configPath)) {
                Yaml yaml = new Yaml();
                Map<String, Object> yamlMap = yaml.load(input);
                Map<String, Object> floccus = (Map<String, Object>) yamlMap.get("floccus");
                Map<String, Object> agent = (Map<String, Object>) floccus.get("agent");
                Map<String, Object> backend = (Map<String, Object>) floccus.get("backend");
                Map<String, Object> docker = (Map<String, Object>) floccus.get("docker");

                config.setAgentPort((int) agent.get("port"));
                config.setAgenteId((String) agent.get("agent-id"));
                config.setSecretKey((String) agent.get("secret-key"));
                config.setBackendHost((String) backend.get("host"));
                config.setBackendPort((int) backend.get("port"));
                config.setDockerHost((String) docker.get("host"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha crítica ao carregar arquivo config.yml", e);
        }
        return config;
    }
    private static void createDefaultConfigFile(Path path) throws IOException {
        String defaultConfig =
                "floccus:\n" +
                        "  agent:\n" +
                        "    agent-id: \"seu-id-aqui\"\n" +
                        "    port: 50051\n" +
                        "    secret-key: \"floccus-super-secret-token-token-xyz\"\n" +
                        "  backend:\n" +
                        "    host: \"localhost\"\n" +
                        "    port: 8080\n" +
                        "  docker:\n" +
                        "    host: \"unix:///var/run/docker.sock\"\n";

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, defaultConfig);
    }
}
