package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.models.GitHub;
import br.dev.matheushenrique.floccus.models.Usuario;
import com.auth0.jwt.interfaces.DecodedJWT;

public interface TokenService {
    public String gerarToken(Usuario usuario);
    public DecodedJWT validarToken(String token);
    public String gerarJwtGithub(GitHub github);
    public String gerarTokenClone(GitHub github);
}
