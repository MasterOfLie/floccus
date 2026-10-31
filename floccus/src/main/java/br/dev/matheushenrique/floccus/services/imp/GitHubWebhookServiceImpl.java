package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.models.Aplicacao;
import br.dev.matheushenrique.floccus.models.GitHub;
import br.dev.matheushenrique.floccus.repository.AplicacaoRepository;
import br.dev.matheushenrique.floccus.repository.GitHubRepository;
import br.dev.matheushenrique.floccus.services.AplicacaoService;
import br.dev.matheushenrique.floccus.services.GitHubWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubWebhookServiceImpl implements GitHubWebhookService {

    private final GitHubRepository gitHubRepository;
    private final AplicacaoRepository aplicacaoRepository;
    private final ObjectMapper objectMapper;
    private final AplicacaoService aplicacaoService;


    @Override
    public void processar(
            String evento,
            String assinatura,
            String deliveryId,
            byte[] payload) {

        if (!"push".equals(evento)) {
            return;
        }

        try {
            JsonNode json = objectMapper.readTree(payload);

            String installationId = json
                    .path("installation")
                    .path("id")
                    .asText();

            String repositoryId = json
                    .path("repository")
                    .path("id")
                    .asText();

            String ref = json.path("ref").asText();

            boolean excluido = json.path("deleted").asBoolean(false);

            if (installationId.isBlank()
                    || repositoryId.isBlank()
                    || ref.isBlank()
                    || !ref.startsWith("refs/heads/")) {
                throw new IllegalArgumentException(
                        "Payload de push inválido"
                );
            }

            if (excluido) {
                return;
            }

            String branch = ref.substring("refs/heads/".length());

            // Localiza a GitHub App pela instalação.
            GitHub github = gitHubRepository
                    .findByInstalacaoId(installationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Instalação GitHub não cadastrada"
                    ));

            // Valida a assinatura antes de processar os dados do evento.
            boolean assinaturaValida = validarAssinatura(
                    payload,
                    assinatura,
                    github.getWebhookSecret()
            );

            if (!assinaturaValida) {
                throw new SecurityException(
                        "Assinatura do webhook inválida"
                );
            }

            // Busca aplicações vinculadas à App, ao repositório e à branch.
            List<Aplicacao> aplicacoes =
                    aplicacaoRepository
                            .findByIdGitAppAndRepositorioIdAndBranch(
                                    github.getId(),
                                    repositoryId,
                                    branch
                            );

            if (aplicacoes.isEmpty()) {
                return;
            }

            for (Aplicacao aplicacao : aplicacoes) {
                redeployWebhook(
                        aplicacao.getId(),
                        true
                );
            }

        } catch (SecurityException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao processar webhook do GitHub",
                    e
            );
        }
    }

    private boolean validarAssinatura(
            byte[] payload,
            String assinaturaRecebida,
            String segredo) {

        if (payload == null
                || assinaturaRecebida == null
                || segredo == null
                || segredo.isBlank()
                || !assinaturaRecebida.startsWith("sha256=")) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(
                    segredo.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            byte[] hash = mac.doFinal(payload);

            String assinaturaEsperada =
                    "sha256=" + HexFormat.of().formatHex(hash);

            return MessageDigest.isEqual(
                    assinaturaEsperada.getBytes(StandardCharsets.UTF_8),
                    assinaturaRecebida.getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível validar a assinatura do webhook",
                    e
            );
        }
    }

    @Async("redeployExecutor")
    private void redeployWebhook(String aplicacaoId, boolean redeploy) {
        try {
             aplicacaoService.redeployWebhook(aplicacaoId, redeploy);
        } catch (Exception e) {
            log.error("Erro ao iniciar redeploy da aplicação " + aplicacaoId + ": " + e.getMessage());
        }
    }
}
