package br.dev.matheushenrique.floccus.dto;

import lombok.Builder;

import java.util.List;
@Builder
public record ContainerDTO(
        String id,
        String nome,
        String image,
        String status,
        List<RedeContainerDTO> redes,
        String memoria,
        String cpu,
        String diretorio,
        String tempoStatus
) {
}
