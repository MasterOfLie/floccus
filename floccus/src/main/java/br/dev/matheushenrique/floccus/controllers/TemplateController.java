package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.TemplateDTO;
import br.dev.matheushenrique.floccus.models.Template;
import br.dev.matheushenrique.floccus.services.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TemplateController {
    private final TemplateService templateService;
    @PostMapping
    public ResponseEntity<Template> criar(@RequestBody TemplateDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(templateService.criarTemplate(dto));
    }

    @GetMapping
    public ResponseEntity<List<Template>> listar() {
        return ResponseEntity.ok(templateService.listarTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(templateService.buscarTemplate(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Template> atualizar(
            @PathVariable String id,
            @RequestBody TemplateDTO dto
    ) {
        return ResponseEntity.ok(
                templateService.atualizarTemplate(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        templateService.excluirTemplate(id);
        return ResponseEntity.noContent().build();
    }


}
