package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.RedeDTO;
import br.dev.matheushenrique.floccus.dto.ResponseDTO;
import br.dev.matheushenrique.floccus.services.NetworkService;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService networkService;

    @GetMapping("/rede")
    public ResponseEntity<?> criarRede(){
        try {
            return ResponseEntity.ok(networkService.criarRede(null));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }

    @GetMapping("/rede/{redeId}")
    public ResponseEntity<?> excluirRede(@PathVariable String redeId){
        RedeDTO redeDTO = RedeDTO.builder()
                .id(redeId)
                .nome("Teste")
                .usuario("Matheus")
                .build();
        try {
            return ResponseEntity.ok(networkService.excluirRede(redeDTO));
        }catch (StatusRuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
        }
    }

}
