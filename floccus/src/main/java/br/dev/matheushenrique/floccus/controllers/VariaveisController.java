package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.AgenteDTO;
import br.dev.matheushenrique.floccus.dto.AgenteMetricasDTO;
import br.dev.matheushenrique.floccus.dto.VariaveisRequest;
import br.dev.matheushenrique.floccus.models.Variaveis;
import br.dev.matheushenrique.floccus.repository.AgenteRepository;
import br.dev.matheushenrique.floccus.services.AgenteService;
import br.dev.matheushenrique.floccus.services.VariaveisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/variaveis")
@RequiredArgsConstructor
public class VariaveisController {

    private final VariaveisService variaveisService;

    @PostMapping("/{aplicacaoId}")
    public ResponseEntity<List<Variaveis>> salvarTodas(
            @PathVariable String aplicacaoId,
            @RequestBody VariaveisRequest request, Authentication authentication) {

        return ResponseEntity.ok(
                variaveisService.salvarTodas(request, aplicacaoId, authentication)
        );
    }
    @GetMapping("/{aplicacaoId}")
    public ResponseEntity<VariaveisRequest> listarVariaveis(
            @PathVariable String aplicacaoId, Authentication authentication) {

        return ResponseEntity.ok(
                variaveisService.listarVariaveis(aplicacaoId, authentication)
        );
    }
    @PutMapping("/{aplicacaoId}")
    public ResponseEntity<List<Variaveis>> atualizarVariaveis(
            @PathVariable String aplicacaoId,@RequestBody VariaveisRequest request,  Authentication authentication) {

        return ResponseEntity.ok(
                variaveisService.atualizarTodas(request, aplicacaoId, authentication)
        );
    }

}
