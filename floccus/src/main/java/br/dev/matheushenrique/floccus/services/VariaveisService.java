package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.VariaveisRequest;
import br.dev.matheushenrique.floccus.models.Variaveis;
import org.springframework.security.core.Authentication;

import java.util.List;


public interface VariaveisService {

    List<Variaveis> salvarTodas(VariaveisRequest request, String aplicacaoId, Authentication authentication);
    List<Variaveis> atualizarTodas(VariaveisRequest request, String aplicacaoId, Authentication authentication);
    VariaveisRequest listarVariaveis(String aplicacaoId, Authentication authentication);
}
