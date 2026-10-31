package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.models.GitHub;
import br.dev.matheushenrique.floccus.models.Usuario;
import br.dev.matheushenrique.floccus.services.TokenService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
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

    @Override
    public String gerarJwtGithub(GitHub github) {
        try {
            Algorithm algorithm = Algorithm.RSA256(
                    null,
                    carregarPrivateKey(github.getPrivateKeyPem())
            );
            Instant agora = Instant.now();
            return JWT.create()
                    .withIssuer(github.getAppId())
                    .withIssuedAt(agora)
                    .withExpiresAt(
                            agora.plusSeconds(9 * 60)
                    )
                    .sign(algorithm);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar JWT da GitHub App: " + e.getMessage(), e);
        }
    }

    @Override
    public String gerarTokenClone(GitHub github) {
        try {
            String jwt = gerarJwtGithub(github);

            String url = "https://api.github.com/app/installations/"
                    + github.getInstalacaoId()
                    + "/access_tokens";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + jwt)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException(
                        "Erro ao gerar token de clone GitHub: "
                                + response.statusCode()
                                + " - "
                                + response.body()
                );
            }

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode json = objectMapper.readTree(response.body());

            return json.get("token").asText();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar token de clone GitHub",
                    e
            );
        }
    }

    private RSAPrivateKey carregarPrivateKey(String pem)
            throws Exception {
        String chave = pem
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] der = Base64.getDecoder().decode(chave);
        ByteArrayInputStream input = new ByteArrayInputStream(der);
        readTag(input, 0x30);
        readLength(input);
        readInteger(input);
        BigInteger modulus = readInteger(input);
        BigInteger publicExponent = readInteger(input);
        BigInteger privateExponent = readInteger(input);
        BigInteger primeP = readInteger(input);
        BigInteger primeQ = readInteger(input);
        BigInteger primeExponentP = readInteger(input);
        BigInteger primeExponentQ = readInteger(input);
        BigInteger crtCoefficient = readInteger(input);
        RSAPrivateCrtKeySpec keySpec =
                new RSAPrivateCrtKeySpec(
                        modulus,
                        publicExponent,
                        privateExponent,
                        primeP,
                        primeQ,
                        primeExponentP,
                        primeExponentQ,
                        crtCoefficient
                );

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    private static BigInteger readInteger(
            ByteArrayInputStream input) throws IOException {
        readTag(input, 0x02);
        int length = readLength(input);
        byte[] value = input.readNBytes(length);
        if (value.length != length) {
            throw new IOException("DER inválido: dados incompletos");
        }
        return new BigInteger(value);
    }

    private static void readTag(
            ByteArrayInputStream input,
            int expectedTag) throws IOException {
        int tag = input.read();
        if (tag != expectedTag) {
            throw new IOException(
                    String.format(
                            "DER inválido: esperava 0x%02X, recebeu 0x%02X",
                            expectedTag,
                            tag
                    )
            );
        }
    }

    private static int readLength(
            ByteArrayInputStream input) throws IOException {
        int first = input.read();
        if (first < 0) {
            throw new IOException("DER inválido: tamanho ausente");
        }
        if ((first & 0x80) == 0) {
            return first;
        }
        int numberOfBytes = first & 0x7F;
        if (numberOfBytes == 0 || numberOfBytes > 4) {
            throw new IOException("DER inválido: tamanho inválido");
        }
        int length = 0;
        for (int i = 0; i < numberOfBytes; i++) {
            int value = input.read();
            if (value < 0) {
                throw new IOException("DER inválido: tamanho incompleto");
            }
            length = (length << 8) | value;
        }
        return length;
    }
}
