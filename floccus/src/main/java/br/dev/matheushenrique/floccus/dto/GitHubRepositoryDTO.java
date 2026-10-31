package br.dev.matheushenrique.floccus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepositoryDTO(
        Long id,
        String name,
        @JsonProperty("full_name")
        String fullName,
        @JsonProperty("html_url")
        String htmlUrl,
        @JsonProperty("git_url")
        String gitUrl,
        @JsonProperty("default_branch")
        String defaultBranch,
        @JsonProperty("private")
        boolean privateRepository) {
}
