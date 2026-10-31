package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.TemplateDTO;
import br.dev.matheushenrique.floccus.models.Template;

import java.util.List;

public interface TemplateService {

    Template criarTemplate(TemplateDTO templateDTO);
    List<Template> listarTemplates();
    Template buscarTemplate(String id);
    Template atualizarTemplate(String id, TemplateDTO templateDTO);
    void excluirTemplate(String id);
}
