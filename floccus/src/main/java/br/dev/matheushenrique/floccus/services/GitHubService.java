package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.GitHubDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

public interface GitHubService {
    Map<String, String> conectarGitHub(Authentication authentication);
    Map<String, String> finalizarConfiguracaoGithub(String code, String state);
    void salvarInstalacao(String installationId, Authentication authentication, String githubId);
    Page<GitHubDTO> listarGithub(Authentication authentication, Pageable pageable);
    void removerGithub(Authentication authentication, String githubId);

}
