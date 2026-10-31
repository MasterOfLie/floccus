package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.AuthResponseDTO;
import br.dev.matheushenrique.floccus.dto.LoginRequestDTO;
import br.dev.matheushenrique.floccus.dto.RegisterRequestDTO;

public interface AuthService {

    AuthResponseDTO registrarConta(RegisterRequestDTO registerRequestDTO);
    AuthResponseDTO login(LoginRequestDTO loginRequestDTO);
    void solicitarRecuperacao(String email);
    void redefinirSenha(String token, String novaSenha);
}
