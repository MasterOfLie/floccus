package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.TemplateDTO;
import br.dev.matheushenrique.floccus.models.Template;
import br.dev.matheushenrique.floccus.repository.TemplateRepository;
import br.dev.matheushenrique.floccus.services.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {
    private final TemplateRepository templateRepository;

    @Override
    public Template criarTemplate(TemplateDTO templateDTO) {
        Template template = Template.builder()
                .linguagem(templateDTO.linguagem())
                .imagem(templateDTO.imagem())
                .framework(templateDTO.framework())
                .runtime(templateDTO.runtime())
                .buildTool(templateDTO.buildTool())
                .versao(templateDTO.versao())
                .buildCommand(templateDTO.buildCommand())
                .startCommand(templateDTO.startCommand())
                .build();

        return templateRepository.save(template);
    }

    @Override
    public List<Template> listarTemplates() {
        return templateRepository.findAll();
    }

    @Override
    public Template buscarTemplate(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
    }

    @Override
    public Template atualizarTemplate(String id, TemplateDTO templateDTO) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        template.setLinguagem(templateDTO.linguagem());
        template.setImagem(templateDTO.imagem());
        template.setFramework(templateDTO.framework());
        template.setRuntime(templateDTO.runtime());
        template.setBuildTool(templateDTO.buildTool());
        template.setVersao(templateDTO.versao());
        template.setBuildCommand(templateDTO.buildCommand());
        template.setStartCommand(templateDTO.startCommand());
        return templateRepository.save(template);
    }

    @Override
    public void excluirTemplate(String id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        templateRepository.delete(template);
    }
}
