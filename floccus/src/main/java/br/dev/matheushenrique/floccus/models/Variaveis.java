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
public class Variaveis {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String aplicacaoId;
    private String chave;
    private String valor;
    private boolean secreta;
}
