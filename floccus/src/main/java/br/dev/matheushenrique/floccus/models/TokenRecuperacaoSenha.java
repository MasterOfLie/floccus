package br.dev.matheushenrique.floccus.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tokens_recuperacao_senha",
        indexes = @Index(
                name = "idx_token_recuperacao_hash",
                columnList = "token_hash",
                unique = true
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    private String usuarioId;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    @Column(nullable = false)
    private boolean usado;
}
