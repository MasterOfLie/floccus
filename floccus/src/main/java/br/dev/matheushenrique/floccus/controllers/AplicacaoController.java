package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.AplicacaoDTO;
import br.dev.matheushenrique.floccus.dto.CriarAplicacaoDTO;
import br.dev.matheushenrique.floccus.dto.ResponseDTO;
import br.dev.matheushenrique.floccus.repository.AplicacaoRepository;
import br.dev.matheushenrique.floccus.services.AplicacaoService;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/aplicacoes")
@RequiredArgsConstructor
public class AplicacaoController {
    private final GrpcClientService grpcClientService;
    private final AplicacaoService aplicacaoService;
    private final AplicacaoRepository aplicacaoRepository;

    @GetMapping()
    public ResponseEntity<?> listarAplicacoes(Authentication authentication){
        return ResponseEntity.ok(aplicacaoService.listarAplicacoes(authentication));

    }
    @PostMapping
    public ResponseEntity<?> criarAplicacao(@RequestBody CriarAplicacaoDTO aplicacaoDTO, Authentication authentication){
            return ResponseEntity.ok(aplicacaoService.criarAplicacao(aplicacaoDTO, authentication));
    }
    @GetMapping("/iniciar/{aplicacaoId}")
    public ResponseEntity<?> iniciarAplicacao(@PathVariable String aplicacaoId){
        try {
            return ResponseEntity.ok(aplicacaoService.iniciarContainer(aplicacaoId));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }
    @GetMapping("/parar/{aplicacaoId}")
    public ResponseEntity<?> pararAplicacao(@PathVariable String aplicacaoId){
        try {
            return ResponseEntity.ok(aplicacaoService.pararContainer(aplicacaoId));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }
    @GetMapping("/reiniciar/{aplicacaoId}")
    public ResponseEntity<?> reiniciarAplicacao(@PathVariable String aplicacaoId){
        try {
            return ResponseEntity.ok(aplicacaoService.reiniciarContainer(aplicacaoId));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }
    @GetMapping("/{aplicacaoId}")
    public ResponseEntity<?> detalharAplicacao(@PathVariable String aplicacaoId){
        try {
            return ResponseEntity.ok(aplicacaoService.detalharContainer(aplicacaoId));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }
    @PostMapping("/deploy/{aplicacaoId}")
    public ResponseEntity<Void> deploy(@PathVariable String aplicacaoId, Authentication authentication) {
        aplicacaoService.iniciarDeploy(aplicacaoId, authentication, false);
        return ResponseEntity.accepted().build();
    }
    @PostMapping("/redeploy/{aplicacaoId}")
    public ResponseEntity<Void> redeploy(@PathVariable String aplicacaoId, Authentication authentication) {
        aplicacaoService.iniciarDeploy(aplicacaoId, authentication, true);
        return ResponseEntity.accepted().build();
    }
    @PutMapping("/{aplicacaoId}")
    public ResponseEntity<AplicacaoDTO> atualizar(@PathVariable String aplicacaoId, @RequestBody AplicacaoDTO aplicacaoDTO, Authentication authentication) {
       return ResponseEntity.ok(aplicacaoService.atualizaAplicacao(aplicacaoId, aplicacaoDTO, authentication));
    }
    @DeleteMapping("/{aplicacaoId}")
    public ResponseEntity<?>  removerAplicacao(@PathVariable String aplicacaoId, Authentication authentication) {
        aplicacaoService.removerAplicacao(aplicacaoId, authentication);
        return ResponseEntity.accepted().build();
    }
}
