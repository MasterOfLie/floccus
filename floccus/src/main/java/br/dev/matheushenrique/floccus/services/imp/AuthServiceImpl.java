package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AuthResponseDTO;
import br.dev.matheushenrique.floccus.dto.LoginRequestDTO;
import br.dev.matheushenrique.floccus.dto.RegisterRequestDTO;
import br.dev.matheushenrique.floccus.enums.Role;
import br.dev.matheushenrique.floccus.models.Usuario;
import br.dev.matheushenrique.floccus.repository.UsuarioRepository;
import br.dev.matheushenrique.floccus.services.AuthService;
import br.dev.matheushenrique.floccus.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    public AuthResponseDTO registrarConta(RegisterRequestDTO registerRequestDTO) {
        if (usuarioRepository.findByEmail(registerRequestDTO.email()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado na plataforma.");
        }

        Usuario novoUsuario = Usuario.builder().nome(registerRequestDTO.nome()).email(registerRequestDTO.email()).senha(passwordEncoder.encode(registerRequestDTO.senha())).roles(Set.of(Role.ROLE_USER)).build();

        novoUsuario = usuarioRepository.save(novoUsuario);

        String token = tokenService.gerarToken(novoUsuario);

        return AuthResponseDTO.builder().token(token).id(novoUsuario.getId()).nome(novoUsuario.getNome()).email(novoUsuario.getEmail()).permissoes(novoUsuario.getRoles()).build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO) {
        var usuarioLogin = usuarioRepository.findByEmail(loginRequestDTO.email()).orElseThrow(() -> new RuntimeException("Email ou Senha invalidos"));

        if (passwordEncoder.matches(loginRequestDTO.senha(), usuarioLogin.getSenha())) {

            String token = tokenService.gerarToken(usuarioLogin);

            return AuthResponseDTO.builder().token(token).id(usuarioLogin.getId()).nome(usuarioLogin.getNome()).email(usuarioLogin.getEmail()).permissoes(usuarioLogin.getRoles()).build();
        }
        throw new RuntimeException("Email ou Senha invalidos");
    }
}
