package br.dev.matheushenrique.floccus.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_apps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHub {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String usuarioId;

    @Column(nullable = false)
    private String appId;

    @Column(nullable = false)
    private String nomeApp;

    @Column(nullable = false)
    private String clienteId;

    @Column(name = "instalacao_id")
    private String instalacaoId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String chaveCliente;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String webhookSecret;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String privateKeyPem;

}
