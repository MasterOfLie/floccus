package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.models.Usuario;
import br.dev.matheushenrique.floccus.services.TokenService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenServiceImpl implements TokenService {

    @Value("${spring.application.jwt-secret}")
    private String secret;

    @Override
    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            List<String> roles = usuario.getRoles().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            return JWT.create()
                    .withIssuer("floccus-backend")
                    .withSubject(usuario.getEmail())
                    .withClaim("roles", roles)
                    .withExpiresAt(gerarDataExpiracao())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT");
        }
    }

    @Override
    public DecodedJWT validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("floccus-backend")
                    .build()
                    .verify(token);
        } catch (Exception exception) {
            throw new RuntimeException("Erro ao validar token, porfavor faça login novamente");
        }
    }

    private Instant gerarDataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
