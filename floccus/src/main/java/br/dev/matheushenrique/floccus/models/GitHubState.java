package br.dev.matheushenrique.floccus.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "github_setup_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String state;

    @Column(nullable = false)
    private String usuarioId;

    @Column(nullable = false)
    private LocalDateTime expiracao;
}
