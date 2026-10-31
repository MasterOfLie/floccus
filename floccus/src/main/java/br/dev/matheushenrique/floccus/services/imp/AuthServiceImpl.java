package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AuthResponseDTO;
import br.dev.matheushenrique.floccus.dto.LoginRequestDTO;
import br.dev.matheushenrique.floccus.dto.RegisterRequestDTO;
import br.dev.matheushenrique.floccus.enums.Role;
import br.dev.matheushenrique.floccus.models.TokenRecuperacaoSenha;
import br.dev.matheushenrique.floccus.models.Usuario;
import br.dev.matheushenrique.floccus.repository.TokenRecuperacaoSenhaRepository;
import br.dev.matheushenrique.floccus.repository.UsuarioRepository;
import br.dev.matheushenrique.floccus.services.AuthService;
import br.dev.matheushenrique.floccus.services.EmailService;
import br.dev.matheushenrique.floccus.services.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final EmailService emailService;

    @Value("${spring.application.frontend-domain}")
    private String frontendUrl;

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
    @Transactional
    @Override
    public void solicitarRecuperacao(String email) {
        var usuario = usuarioRepository.findByEmail(email.trim());
        if (usuario.isEmpty()) return;
        Usuario usuarioRecuperacao = usuario.get();

        tokenRecuperacaoSenhaRepository.excluirTokensDoUsuario(usuario.get().getId());
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        TokenRecuperacaoSenha registro = TokenRecuperacaoSenha.builder()
                .tokenHash(hashToken(token))
                .usuarioId(usuario.get().getId())
                .expiraEm(LocalDateTime.now().plusMinutes(15))
                .usado(false)
                .build();

        tokenRecuperacaoSenhaRepository.save(registro);

        String link = frontendUrl
                + "/redefinir-senha?token="
                + token;

        emailService.enviarRecuperacaoSenha(usuario.get().getEmail(), link);
    }

    @Transactional
    @Override
    public void redefinirSenha(String token, String novaSenha) {
        String hash = hashToken(token);

        TokenRecuperacaoSenha registro = tokenRecuperacaoSenhaRepository
                .findByTokenHash(hash)
                .orElseThrow(() -> new RuntimeException("Token inválido ou expirado."));

        if (registro.isUsado()
                || !registro.getExpiraEm().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Token inválido ou expirado.");
        }

        Usuario usuario = usuarioRepository.findById(registro.getUsuarioId()).orElseThrow(()-> new RuntimeException("Usuario não encontrado"));
        usuario.setSenha(passwordEncoder.encode(novaSenha));

        usuarioRepository.save(usuario);

        registro.setUsado(true);
        tokenRecuperacaoSenhaRepository.save(registro);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (Exception e)
        {throw new RuntimeException(
                    "Não foi possível processar o token.", e
            );
        }
    }
}
