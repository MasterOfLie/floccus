package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.models.TokenRecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRecuperacaoSenhaRepository extends JpaRepository<TokenRecuperacaoSenha, String> {
    Optional<TokenRecuperacaoSenha> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from TokenRecuperacaoSenha t where t.usuarioId = :usuarioId")
    void excluirTokensDoUsuario(@Param("usuarioId") String usuarioId);
}
