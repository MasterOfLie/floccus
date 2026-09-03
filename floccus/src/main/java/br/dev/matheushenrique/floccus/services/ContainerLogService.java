package br.dev.matheushenrique.floccus.services;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

public interface ContainerLogService {

    void registrarCanal(String aplicacaoId, SimpMessageHeaderAccessor headerAccessor);
    void fecharCanal (String aplicacaoId, String sessionId);
    void fecharCanaisSessao (String sessionId);
}
