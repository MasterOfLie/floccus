package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.models.GitHubState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitHubStateRepository extends JpaRepository<GitHubState, String> {
    Optional<GitHubState> findByState(String state);
}
