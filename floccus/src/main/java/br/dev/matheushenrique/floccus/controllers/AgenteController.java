package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.AgenteDTO;
import br.dev.matheushenrique.floccus.dto.AgenteMetricasDTO;
import br.dev.matheushenrique.floccus.repository.AgenteRepository;
import br.dev.matheushenrique.floccus.services.AgenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/agentes")
@RequiredArgsConstructor
public class AgenteController {
    private final AgenteService agenteService;
    private final AgenteRepository agenteRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<?> criarAgente(@RequestBody AgenteDTO agenteDTO) throws Exception {
        return ResponseEntity.ok(agenteService.criarAgente(agenteDTO));
    }

    @GetMapping
    public ResponseEntity<?> listarAgentes(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(defaultValue = "true") boolean paginado, @RequestParam(defaultValue = "") String busca)
    {
        if (paginado) {
            return ResponseEntity.ok(
                    agenteService.listarAgentes(pageable, busca)
            );
        }
        return ResponseEntity.ok(agenteService.listarAgentes());
    }

    @PutMapping("/{agenteId}")
    public ResponseEntity<?> atualizarAgente(@PathVariable String agenteId, @RequestBody AgenteDTO agenteDTO) throws Exception{
        return ResponseEntity.ok(agenteService.atualizarAgente(agenteId, agenteDTO));
    }
    @PutMapping("/{agenteId}/chave")
    public ResponseEntity<?> atualizarChave(@PathVariable String agenteId) throws Exception{
        return ResponseEntity.ok(agenteService.atualizarChave(agenteId));
    }

    @GetMapping("/{agenteId}")
    public ResponseEntity<?> buscarAgente(@PathVariable String agenteId) throws Exception {
        return ResponseEntity.ok(agenteService.buscarAgente(agenteId));
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<Void> receberHeartbeat(
            @PathVariable String id,
            @RequestBody AgenteMetricasDTO metricas) {
        agenteService.receberHeartbeat(id, metricas);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{agenteId}")
    public ResponseEntity<?> deletarAgente(@PathVariable String agenteId) throws Exception {
        agenteService.deletarAgente(agenteId);
        return ResponseEntity.accepted().build();
    }
}
