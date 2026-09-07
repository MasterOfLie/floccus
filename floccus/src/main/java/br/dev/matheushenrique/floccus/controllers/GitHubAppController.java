package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.services.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/github")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GitHubAppController {

    private final GitHubService gitHubService;

    @GetMapping
    public ResponseEntity<?> listarGithub(
            Authentication authentication,
            Pageable pageable) {

        return ResponseEntity.ok(
                gitHubService.listarGithub(authentication, pageable)
        );
    }

    @GetMapping("/setup")
    public ResponseEntity<?> finalizarConfiguracaoApp(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {

        Map<String, String> response =
                gitHubService.finalizarConfiguracaoGithub(code, state);

        return ResponseEntity
                .status(302)
                .header("Location", response.get("Location"))
                .build();
    }
    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> conectarGitHub(
            Authentication authentication) {

        Map<String, String> response =
                gitHubService.conectarGitHub(authentication);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/install")
    public ResponseEntity<?> salvarInstalacao(
            @RequestParam("installation_id") String installationId,
            @RequestParam("githubId") String githubId,
            Authentication authentication) {

        gitHubService.salvarInstalacao(
                installationId,
                authentication,
                githubId
        );

        return ResponseEntity.ok(
                Map.of("message", "Instalação vinculada com sucesso!")
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(Authentication authentication, @PathVariable String id) {
        gitHubService.removerGithub(authentication, id);

        return ResponseEntity.ok(
                Map.of("message", "Aplicação removida com sucesso!")
        );
    }

}
