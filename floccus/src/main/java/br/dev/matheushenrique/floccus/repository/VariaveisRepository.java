package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.models.Variaveis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VariaveisRepository extends JpaRepository<Variaveis, String> {

    List<Variaveis> findAllByAplicacaoId(String aplicacaoId);
    List<Variaveis> findAllByAplicacaoIdAndSecretaFalse(String aplicacaoId);
    List<Variaveis> findByAplicacaoId(String aplicacaoId);
    boolean existsByAplicacaoId(String aplicacaoId);
    void deleteAllByAplicacaoId(String aplicacaoId);
    @Query("""
    SELECT new br.dev.matheushenrique.floccus.models.Variaveis(
        v.id,
        v.aplicacaoId,
        v.chave,
        CASE WHEN v.secreta = true THEN null ELSE v.valor END,
        v.secreta
    )
    FROM Variaveis v
    WHERE v.aplicacaoId = :aplicacaoId
    """)
    List<Variaveis> listarSemSegredos(
            @Param("aplicacaoId") String aplicacaoId
    );
}
