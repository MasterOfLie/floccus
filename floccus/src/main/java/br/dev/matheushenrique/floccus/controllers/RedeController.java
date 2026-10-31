package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.RedeDTO;
import br.dev.matheushenrique.floccus.dto.ResponseDTO;
import br.dev.matheushenrique.floccus.services.RedeService;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/rede")
@RequiredArgsConstructor
public class RedeController {

    private final RedeService redeService;

//    @GetMapping("/rede")
//    public ResponseEntity<?> criarRede(){
//        try {
//            return ResponseEntity.ok(redeService.criarRede(null));
//        }catch (StatusRuntimeException e){
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
//        }
//    }
    @PostMapping("/criar")
    public ResponseEntity<?> criarRede(@RequestBody RedeDTO redeDTO, Authentication authentication){
        return ResponseEntity.ok(redeService.criarRede(redeDTO, authentication));

    }


    @GetMapping()
    public ResponseEntity<?> listarAgentes(Authentication authentication, @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable, @RequestParam(defaultValue = "true") boolean paginado) {
        if(paginado){
            return ResponseEntity.ok(redeService.listarRedes(pageable, authentication));
        }else{
            return ResponseEntity.ok(redeService.listarRedes(authentication));
        }

    }

    @DeleteMapping("/{redeId}")
    public void excluirRede(@PathVariable String redeId ,Authentication authentication){
        redeService.excluirRede(redeId, authentication);
    }

//
//    @GetMapping("/rede/{redeId}")
//    public ResponseEntity<?> excluirRede(@PathVariable String redeId){
//        RedeDTO redeDTO = RedeDTO.builder()
//                .id(redeId)
//                .nome("Teste")
//                .usuario("Matheus")
//                .build();
//        try {
//            return ResponseEntity.ok(redeService.excluirRede(redeDTO));
//        }catch (StatusRuntimeException e){
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO(e.getStatus().getDescription(), false));
//        }
//    }

}
