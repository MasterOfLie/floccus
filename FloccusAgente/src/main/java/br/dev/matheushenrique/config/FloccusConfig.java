package br.dev.matheushenrique.config;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

@Data
public class FloccusConfig {

    private int agentPort;
    private String secretKey;
    private String backendHost;
    private int backendPort;
    private String dockerHost;

    @SuppressWarnings("unchecked")
    public static FloccusConfig loadConfig() {
        FloccusConfig config = new FloccusConfig();
        try (InputStream input = FloccusConfig.class.getClassLoader().getResourceAsStream("config.yml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = yaml.load(input);

            // Navega na árvore do YAML de forma simples
            Map<String, Object> floccus = (Map<String, Object>) yamlMap.get("floccus");
            Map<String, Object> agent = (Map<String, Object>) floccus.get("agent");
            Map<String, Object> backend = (Map<String, Object>) floccus.get("backend");
            Map<String, Object> docker = (Map<String, Object>) floccus.get("docker");

            config.setAgentPort((int) agent.get("port"));
            config.setSecretKey((String) agent.get("secret-key"));
            config.setBackendHost((String) backend.get("host"));
            config.setBackendPort((int) backend.get("port"));
            config.setDockerHost((String) docker.get("host"));

        } catch (Exception e) {
            throw new RuntimeException("Falha crítica ao carregar arquivo config.yml", e);
        }
        return config;
    }
}
