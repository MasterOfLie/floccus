package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.ContainerHeartbeat;

import java.util.Map;

public interface HeartbeatService {

    void heartbeatContainer(ContainerHeartbeat heartbeat);
    Map<String, ContainerHeartbeat> listarTodos();

}
