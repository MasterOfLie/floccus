package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.VariaveisRequest;
import br.dev.matheushenrique.floccus.models.Aplicacao;
import br.dev.matheushenrique.floccus.models.Usuario;
import br.dev.matheushenrique.floccus.models.Variaveis;
import br.dev.matheushenrique.floccus.repository.AplicacaoRepository;
import br.dev.matheushenrique.floccus.repository.UsuarioRepository;
import br.dev.matheushenrique.floccus.repository.VariaveisRepository;
import br.dev.matheushenrique.floccus.services.VariaveisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariaveisServiceImpl implements VariaveisService {

    private final VariaveisRepository variaveisRepository;
    private final AplicacaoRepository aplicacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public List<Variaveis> salvarTodas(VariaveisRequest request, String aplicacaoId, Authentication authentication) {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId).orElseThrow(() -> new RuntimeException("Aplicação não encontrado"));
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        List<Variaveis> variaveis = request.variaveis().stream().map(item -> Variaveis.builder().aplicacaoId(aplicacaoId).chave(item.getChave()).valor(item.getValor()).secreta(item.isSecreta()).build()).toList();
        return variaveisRepository.saveAll(variaveis);
    }

    @Transactional
    @Override
    public List<Variaveis> atualizarTodas(VariaveisRequest request, String aplicacaoId, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuário não autenticado.");
        }
        if (request == null || request.variaveis() == null) {
            throw new RuntimeException("Lista de variáveis obrigatória.");
        }
        aplicacaoRepository.findById(aplicacaoId)
                .orElseThrow(() ->
                        new RuntimeException("Aplicação não encontrada."));

        usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado."));
        List<Variaveis> existentes =
                variaveisRepository.findByAplicacaoId(aplicacaoId);

        Map<String, Variaveis> porId = existentes.stream()
                .collect(Collectors.toMap(
                        Variaveis::getId,
                        Function.identity()
                ));

        Set<String> idsRecebidos = new HashSet<>();
        Set<String> chavesRecebidas = new HashSet<>();
        List<Variaveis> paraSalvar = new ArrayList<>();

        for (Variaveis item : request.variaveis()) {

            if (item.getChave() == null || item.getChave().isBlank()) {
                throw new RuntimeException("A chave é obrigatória.");
            }

            if (!chavesRecebidas.add(item.getChave())) {
                throw new RuntimeException(
                        "Chave duplicada: " + item.getChave());
            }

            Variaveis variavel;

            if (item.getId() != null && !item.getId().isBlank()) {

                if (!idsRecebidos.add(item.getId())) {
                    throw new RuntimeException(
                            "Variável duplicada na requisição.");
                }

                variavel = porId.get(item.getId());

                if (variavel == null) {
                    throw new RuntimeException(
                            "Variável não encontrada nesta aplicação.");
                }

                variavel.setChave(item.getChave());

                if (item.isSecreta()) {
                    variavel.setSecreta(true);

                    // Null significa que o usuário não alterou o segredo.
                    if (item.getValor() != null) {
                        variavel.setValor(item.getValor());
                    }
                } else {
                    variavel.setSecreta(false);

                    if (item.getValor() == null) {
                        throw new RuntimeException(
                                "Informe o valor de " + item.getChave());
                    }

                    variavel.setValor(item.getValor());
                }

            } else {
                // Uma variável nova precisa receber um valor.
                if (item.getValor() == null) {
                    throw new RuntimeException(
                            "Informe o valor da nova variável "
                                    + item.getChave());
                }

                variavel = Variaveis.builder()
                        .aplicacaoId(aplicacaoId)
                        .chave(item.getChave())
                        .valor(item.getValor())
                        .secreta(item.isSecreta())
                        .build();
            }

            paraSalvar.add(variavel);
        }
        List<Variaveis> paraExcluir = existentes.stream()
                .filter(v -> !idsRecebidos.contains(v.getId()))
                .filter(v -> request.variaveis().stream()
                        .noneMatch(item ->
                                Objects.equals(item.getId(), v.getId())))
                .toList();
        variaveisRepository.deleteAll(paraExcluir);
        List<Variaveis> salvas = variaveisRepository.saveAll(paraSalvar);

        return salvas;
    }

    @Override
    public VariaveisRequest listarVariaveis(String aplicacaoId, Authentication authentication) {
        List<Variaveis> variaveis =variaveisRepository.listarSemSegredos(aplicacaoId);
        return new VariaveisRequest(variaveis);
    }
}
