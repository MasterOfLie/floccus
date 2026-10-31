package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.models.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, String> {
}
