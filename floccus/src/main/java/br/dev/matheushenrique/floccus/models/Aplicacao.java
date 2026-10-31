package br.dev.matheushenrique.floccus.models;

import br.dev.matheushenrique.floccus.enums.StatusAplicacao;
import br.dev.matheushenrique.floccus.enums.TipoAplicacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aplicacoes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Aplicacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nome;

    private String repositorioId;

    @Column(nullable = false)
    private String imagem;

    @Column(name = "agente_id", nullable = false)
    private String agenteId;

    @Column(name = "rede_id", nullable = false)
    private String redeId;

    @Column(name = "container_id")
    private String containerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAplicacao tipo;

    @Column(name = "porta_container")
    private Integer portaContainer;

    @Column(name = "porta_externa")
    private Integer portaExterna;

    private String dominio;

    @Column(name = "memoria_limite")
    private Long memoriaLimite;

    @Column(name = "cpu_limite")
    private Long cpuLimite;

    private String path;

    private String linguagem;

    private String framework;

    private String branch;

    private String idGitApp;

    private String usuarioId;

    @Column(name = "build_tool")
    private String buildTool;

    private String runtime;

    @Enumerated(EnumType.STRING)
    private StatusAplicacao status;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}
