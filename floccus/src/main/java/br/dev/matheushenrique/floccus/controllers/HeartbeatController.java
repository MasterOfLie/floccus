package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.ContainerHeartbeat;
import br.dev.matheushenrique.floccus.services.HeartbeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/heartbeat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class HeartbeatController {

    private final HeartbeatService heartbeatService;

    @PostMapping
    public ResponseEntity<Void> receber(
            @RequestBody ContainerHeartbeat request) {
        heartbeatService.heartbeatContainer(request);
        return ResponseEntity.ok().build();
    }
    @GetMapping
    public Map<String, ContainerHeartbeat> listarTodos() {
        return heartbeatService.listarTodos();
    }

}
