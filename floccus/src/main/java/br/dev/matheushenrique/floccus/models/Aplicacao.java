package br.dev.matheushenrique.floccus.models;

import br.dev.matheushenrique.floccus.enums.TipoAplicacao;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aplicacoes")
@Data
public class Aplicacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String imagem;

    @Column(name = "agente_id", nullable = false)
    private String agenteId;

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

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}
