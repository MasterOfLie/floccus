package br.dev.matheushenrique.floccus.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String linguagem;
    private String imagem;
    private String framework;
    private String runtime;
    private String buildTool;
    private String versao;
    private String buildCommand;
    private String startCommand;

}
