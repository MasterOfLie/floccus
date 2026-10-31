package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.*;
import br.dev.matheushenrique.floccus.models.GitHub;
import br.dev.matheushenrique.floccus.models.GitHubState;
import br.dev.matheushenrique.floccus.models.Usuario;
import br.dev.matheushenrique.floccus.repository.GitHubRepository;
import br.dev.matheushenrique.floccus.repository.GitHubStateRepository;
import br.dev.matheushenrique.floccus.repository.UsuarioRepository;
import br.dev.matheushenrique.floccus.services.GitHubService;
import br.dev.matheushenrique.floccus.services.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GitHubServiceImpl implements GitHubService {


    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;
    private final GitHubStateRepository gitHubStateRepository;
    private final GitHubRepository gitHubRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final TokenService tokenService;
    @Value("${spring.application.backend-domain}")
    private String backendUrl;
    @Value("${spring.application.frontend-domain}")
    private String frontendUrl;

    @Override
    public Map<String, String> conectarGitHub(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        String state = UUID.randomUUID().toString();
        GitHubState setupState = GitHubState.builder().state(state).usuarioId(usuario.getId()).expiracao(LocalDateTime.now().plusMinutes(10)).build();
        gitHubStateRepository.save(setupState);
        String manifesto = criarManifesto(state);
        return Map.of("manifest", manifesto, "state", state);
    }

    @Override
    @Transactional
    public Map<String, String> finalizarConfiguracaoGithub(String code, String state) {
        GitHubState setupState = gitHubStateRepository.findByState(state).orElseThrow(() -> new RuntimeException("State inválido, tente conectar ao GitHub novamente"));
        if (setupState.getExpiracao().isBefore(LocalDateTime.now())) {
            gitHubStateRepository.delete(setupState);
            throw new RuntimeException("State inválido, tente conectar ao GitHub novamente");
        }
        String githubConversionUrl = "https://api.github.com/app-manifests/" + code + "/conversions";
        try {
            ResponseEntity<GitHubAppRequestDTO> response = restTemplate.postForEntity(githubConversionUrl, null, GitHubAppRequestDTO.class);
            GitHubAppRequestDTO credentials = response.getBody();
            if (credentials == null) {
                throw new RuntimeException("O GitHub não retornou as credenciais da aplicação");
            }
            GitHub github = GitHub.builder().usuarioId(setupState.getUsuarioId()).appId(credentials.id()).nomeApp(credentials.name()).clienteId(credentials.clientId()).chaveCliente(credentials.clientSecret()).webhookSecret(credentials.webhookSecret()).privateKeyPem(credentials.pem()).build();
            gitHubRepository.save(github);
            gitHubStateRepository.delete(setupState);

            String urlInstalacao = "https://github.com/apps/" + credentials.slug() + "/installations/new?state=" + github.getId();

            return Map.of("Location", urlInstalacao);

        } catch (HttpClientErrorException e) {

            throw new RuntimeException("Erro ao conectar ao GitHub: " + e.getResponseBodyAsString());

        } catch (Exception e) {

            throw new RuntimeException("Erro ao conectar ao GitHub", e);
        }
    }

    @Override
    @Transactional
    public void salvarInstalacao(String installationId, Authentication authentication, String githubId) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        GitHub github = gitHubRepository.findById(githubId).orElseThrow(() -> new RuntimeException("App do GitHub não encontrado"));

        if (!github.getUsuarioId().equals(usuario.getId())) {
            throw new RuntimeException("App do GitHub não pertence a este usuário");
        }

        github.setInstalacaoId(installationId);

        gitHubRepository.save(github);
    }

    @Override
    public Page<GitHubDTO> listarGithub(Authentication authentication, Pageable pageable) {

        return gitHubRepository.buscarPorEmail(authentication.getName(), pageable);
    }

    @Override
    public void removerGithub(Authentication authentication, String githubId) {

        String email = authentication.getName();

        if (!gitHubRepository.pertenceAoUsuario(githubId, email)) {
            throw new RuntimeException("Você não tem permissão para remover esta aplicação.");
        }

        gitHubRepository.deleteById(githubId);
    }

    @Override
    public List<GitHubRepositoryDTO> listarRepositorios(Authentication authentication, String appId) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        GitHub github = gitHubRepository.findByUsuarioIdAndId(usuario.getId(), appId).orElseThrow(() -> new RuntimeException("Nenhuma aplicação do GitHub encontrada"));

        if (github.getInstalacaoId() == null) {
            throw new RuntimeException("A aplicação do GitHub ainda não foi instalada");
        }

        String jwt = tokenService.gerarJwtGithub(github);

        String urlToken = "https://api.github.com/app/installations/" + github.getInstalacaoId() + "/access_tokens";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GitHubInstallationTokenDTO> tokenResponse = restTemplate.exchange(urlToken, HttpMethod.POST, request, GitHubInstallationTokenDTO.class);

        GitHubInstallationTokenDTO token = tokenResponse.getBody();

        if (token == null || token.token() == null) {
            throw new RuntimeException("GitHub não retornou o token da instalação");
        }

        return buscarRepositorios(token.token());
    }

    @Override
    public GitHubRepositoryDTO buscarRepositorioPorId(Long repositoryId, Authentication authentication, String idApp) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        GitHub github = gitHubRepository.findByUsuarioIdAndId(usuario.getId(), idApp)
                .orElseThrow(() -> new RuntimeException("Nenhuma aplicação do GitHub encontrada"));
        if (github.getInstalacaoId() == null) {
            throw new RuntimeException("A aplicação do GitHub ainda não foi instalada");
        }
        String jwt = tokenService.gerarJwtGithub(github);
        String urlToken = "https://api.github.com/app/installations/"
                + github.getInstalacaoId()
                + "/access_tokens";
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setBearerAuth(jwt);
        tokenHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> tokenRequest = new HttpEntity<>(tokenHeaders);
        ResponseEntity<GitHubInstallationTokenDTO> tokenResponse =
                restTemplate.exchange(
                        urlToken,
                        HttpMethod.POST,
                        tokenRequest,
                        GitHubInstallationTokenDTO.class
                );
        GitHubInstallationTokenDTO token = tokenResponse.getBody();
        if (token == null || token.token() == null) {
            throw new RuntimeException("GitHub não retornou o token da instalação");
        }
        return buscarRepositorio(repositoryId, token.token());
    }

    @Override
    public List<GitHubBranchDTO> listarBranches(Authentication authentication, String username, String repo, String appId) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        GitHub github = gitHubRepository.findByUsuarioIdAndId(usuario.getId(), appId)
                .orElseThrow(() -> new RuntimeException("Nenhuma aplicação do GitHub encontrada"));
        if (github.getInstalacaoId() == null) {
            throw new RuntimeException("A aplicação do GitHub ainda não foi instalada");
        }
        String jwt = tokenService.gerarJwtGithub(github);
        String urlToken = "https://api.github.com/app/installations/"
                + github.getInstalacaoId()
                + "/access_tokens";
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setBearerAuth(jwt);
        tokenHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> tokenRequest = new HttpEntity<>(tokenHeaders);
        ResponseEntity<GitHubInstallationTokenDTO> tokenResponse =
                restTemplate.exchange(
                        urlToken,
                        HttpMethod.POST,
                        tokenRequest,
                        GitHubInstallationTokenDTO.class
                );
        GitHubInstallationTokenDTO token = tokenResponse.getBody();
        if (token == null || token.token() == null) {
            throw new RuntimeException("GitHub não retornou o token da instalação");
        }
        return buscarBranches(token.token(), username, repo);
    }

    @Override
    public GitHubRepositoryDTO buscarRepositorioPorIdWebhook(Long repositoryId, Usuario usuario, String idApp) {
        GitHub github = gitHubRepository.findByUsuarioIdAndId(usuario.getId(), idApp)
                .orElseThrow(() -> new RuntimeException("Nenhuma aplicação do GitHub encontrada"));
        if (github.getInstalacaoId() == null) {
            throw new RuntimeException("A aplicação do GitHub ainda não foi instalada");
        }
        String jwt = tokenService.gerarJwtGithub(github);
        String urlToken = "https://api.github.com/app/installations/"
                + github.getInstalacaoId()
                + "/access_tokens";
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setBearerAuth(jwt);
        tokenHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> tokenRequest = new HttpEntity<>(tokenHeaders);
        ResponseEntity<GitHubInstallationTokenDTO> tokenResponse =
                restTemplate.exchange(
                        urlToken,
                        HttpMethod.POST,
                        tokenRequest,
                        GitHubInstallationTokenDTO.class
                );
        GitHubInstallationTokenDTO token = tokenResponse.getBody();
        if (token == null || token.token() == null) {
            throw new RuntimeException("GitHub não retornou o token da instalação");
        }
        return buscarRepositorio(repositoryId, token.token());
    }

    private List<GitHubBranchDTO> buscarBranches(String token, String owner, String repo) {

        String url = "https://api.github.com/repos/"
                + owner + "/" + repo + "/branches";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GitHubBranchDTO[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                GitHubBranchDTO[].class
        );

        GitHubBranchDTO[] body = response.getBody();

        if (body == null) {
            return List.of();
        }

        return Arrays.asList(body);
    }

    private GitHubRepositoryDTO buscarRepositorio(Long repositoryId, String token) {
        String url = "https://api.github.com/repositories/" + repositoryId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<GitHubRepositoryDTO> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        GitHubRepositoryDTO.class
                );
        GitHubRepositoryDTO repository = response.getBody();
        if (repository == null) {
            throw new RuntimeException("Repositório não encontrado");
        }
        return repository;
    }

    private String criarManifesto(String state) {
        Map<String, Object> manifesto = new HashMap<>();
        manifesto.put("name", "Floccus PaaS " + UUID.randomUUID().toString().substring(0, 8));
        manifesto.put("url", frontendUrl);
        manifesto.put("hook_attributes", Map.of("url", backendUrl + "/api/github/webhook"));
        manifesto.put("setup_url", frontendUrl + "/git/github");
        manifesto.put("redirect_url", backendUrl + "/api/github/setup");
        manifesto.put("public", false);
        manifesto.put("default_permissions", Map.of("contents", "read", "metadata", "read"));
        manifesto.put("default_events", List.of("push"));
        try {
            return objectMapper.writeValueAsString(manifesto);
        } catch (JacksonException e) {
            throw new RuntimeException("Erro ao criar manifesto", e);
        }
    }

    private List<GitHubRepositoryDTO> buscarRepositorios(String token) {
        String url = "https://api.github.com/installation/repositories";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<GitHubRepositoriesResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, request, GitHubRepositoriesResponseDTO.class);
        GitHubRepositoriesResponseDTO body = response.getBody();
        if (body == null || body.repositories() == null) {
            return List.of();
        }
        return body.repositories();
    }
}