package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.models.Aplicacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AplicacaoRepository extends JpaRepository<Aplicacao, String> {
}
