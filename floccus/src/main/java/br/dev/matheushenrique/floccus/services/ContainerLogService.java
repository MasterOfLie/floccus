package br.dev.matheushenrique.floccus.services;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

public interface ContainerLogService {

    void registrarCanal(String containerId, SimpMessageHeaderAccessor headerAccessor);
    void fecharCanal (String containerId, String sessionId);
    void fecharCanaisSessao (String sessionId);
}
