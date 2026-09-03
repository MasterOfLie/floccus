package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.ResponseDTO;
import br.dev.matheushenrique.floccus.repository.AplicacaoRepository;
import br.dev.matheushenrique.floccus.services.AplicacaoService;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> listarAplicacoes(){
        return ResponseEntity.ok(aplicacaoRepository.findAll());
//        try {
//            AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
//            var containersList = grpcClientService.floccusAgente(
//                    agenteConexaoDto,
//                    AgenteDockerServiceGrpc::newBlockingStub,
//                    stub -> stub.listarContainers(
//                            ListarContainersRequest.newBuilder().setMostrarTodos(true).build()
//                    ).getContainersList()
//            );
//            List<ContainerDTO> response = containersList.stream()
//                    .map(c -> new ContainerDTO(c.getId(), c.getNome(), c.getImagem(), c.getStatus(), null, null, null, null, null))
//                    .toList();
//            return ResponseEntity.ok(response);
//        }catch (Exception e){
//            return ResponseEntity.ok(e.getMessage());
//        }
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
}
