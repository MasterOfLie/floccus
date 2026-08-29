package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.AgenteConexaoDto;
import br.dev.matheushenrique.floccus.dto.ContainerDTO;
import br.dev.matheushenrique.floccus.dto.ResponseDTO;
import br.dev.matheushenrique.floccus.services.ContainerService;
import br.dev.matheushenrique.floccus.services.GrpcClientService;
import br.dev.matheushenrique.grpc.AgenteDockerServiceGrpc;
import br.dev.matheushenrique.grpc.ListarContainersRequest;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/container")
@RequiredArgsConstructor
public class ContainerController {
    private final GrpcClientService grpcClientService;
    private final ContainerService containerService;
    @GetMapping()
    public ResponseEntity<?> listarContainers(){
        try {
            AgenteConexaoDto agenteConexaoDto = new AgenteConexaoDto("localhost", 50051, "floccus-super-secret-token-token-xyz");
            var containersList = grpcClientService.floccusAgente(
                    agenteConexaoDto,
                    AgenteDockerServiceGrpc::newBlockingStub,
                    stub -> stub.listarContainers(
                            ListarContainersRequest.newBuilder().setMostrarTodos(true).build()
                    ).getContainersList()
            );
            List<ContainerDTO> response = containersList.stream()
                    .map(c -> new ContainerDTO(c.getId(), c.getNome(), c.getImagem(), c.getStatus(), null, null, null, null, null))
                    .toList();
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.ok(e.getMessage());
        }
    }
    @GetMapping("/iniciar/{containerId}")
    public ResponseEntity<?> iniciarContainer(@PathVariable String containerId){
        try {
            return ResponseEntity.ok(containerService.iniciarContainer(containerId));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }
    @GetMapping("/parar/{containerId}")
    public ResponseEntity<?> pararContainer(@PathVariable String containerId){
        try {
            return ResponseEntity.ok(containerService.pararContainer(containerId));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }
    @GetMapping("/reiniciar/{containerId}")
    public ResponseEntity<?> reiniciarContainer(@PathVariable String containerId){
        try {
            return ResponseEntity.ok(containerService.reiniciarContainer(containerId));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }
    @GetMapping("/{containerId}")
    public ResponseEntity<?> detalharContainer(@PathVariable String containerId){
        try {
            return ResponseEntity.ok(containerService.detalharContainer(containerId));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }
}
