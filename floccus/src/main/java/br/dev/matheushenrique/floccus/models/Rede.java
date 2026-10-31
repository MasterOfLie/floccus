package br.dev.matheushenrique.floccus.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "redes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rede {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String agenteId;

    private String subnet;

    private String gateway;

    private String usuarioId;

    @Column(nullable = false)
    private String redeAgenteId;

}
