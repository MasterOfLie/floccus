package br.dev.matheushenrique.floccus.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(schema = "agente")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Agente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String ipAgente;
    private int portaAgente;
    private String chaveAgente;
    private String ipBackend;
    private String portaBackend;
    private String dockerHost;

}
