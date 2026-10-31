package br.dev.matheushenrique.floccus.dto;

import java.util.List;

public record GitHubRepositoriesResponseDTO(
        List<GitHubRepositoryDTO> repositories
) {
}
