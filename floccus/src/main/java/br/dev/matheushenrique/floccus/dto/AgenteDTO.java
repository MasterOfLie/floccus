package br.dev.matheushenrique.floccus.dto;

import br.dev.matheushenrique.floccus.models.Agente;

public record AgenteDTO(
        String nome,
        String id,
        String ipAgente,
        int portaAgente,
        String chaveAgente,
        String ipBackend,
        String portaBackend,
        String dockerHost
) {
    public AgenteDTO(Agente agente) {
        this(
                agente.getNome(),
                String.valueOf(agente.getId()), // ou agente.getId().toString()
                agente.getIpAgente(),
                agente.getPortaAgente(),
                agente.getChaveAgente(),
                agente.getIpBackend(),
                agente.getPortaBackend(),
                agente.getDockerHost()
        );
    }
}
