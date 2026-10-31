package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.AtualizaUsuarioDTO;
import br.dev.matheushenrique.floccus.dto.UsuarioDTO;
import org.springframework.security.core.Authentication;

public interface UsuarioService {

    UsuarioDTO atualizarUsuario(AtualizaUsuarioDTO atualizaUsuarioDTO, Authentication authentication);
    UsuarioDTO buscarPerfil(Authentication authentication);

}
