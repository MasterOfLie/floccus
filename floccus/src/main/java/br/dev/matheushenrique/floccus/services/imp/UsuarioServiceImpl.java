package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AtualizaUsuarioDTO;
import br.dev.matheushenrique.floccus.dto.LoginRequestDTO;
import br.dev.matheushenrique.floccus.dto.UsuarioDTO;
import br.dev.matheushenrique.floccus.models.Usuario;
import br.dev.matheushenrique.floccus.repository.UsuarioRepository;
import br.dev.matheushenrique.floccus.services.AuthService;
import br.dev.matheushenrique.floccus.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Override
    public UsuarioDTO atualizarUsuario(AtualizaUsuarioDTO atualizaUsuarioDTO, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));

        if (usuarioRepository.existsByEmailAndIdNot(atualizaUsuarioDTO.email(), usuario.getId())) {
            throw new RuntimeException("Este e-mail já está cadastrado.");
        }

        usuario.setNome(atualizaUsuarioDTO.nome());
        usuario.setEmail(atualizaUsuarioDTO.email());

        if (atualizaUsuarioDTO.senha() != null && !atualizaUsuarioDTO.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(atualizaUsuarioDTO.senha()));
        }
        usuarioRepository.save(usuario);
        UsuarioDTO usuarioDTO = new UsuarioDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRoles());
        return usuarioDTO;
    }

    @Override
    public UsuarioDTO buscarPerfil(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));
        UsuarioDTO usuarioDTO = new UsuarioDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRoles() );
        return usuarioDTO;
    }
}
