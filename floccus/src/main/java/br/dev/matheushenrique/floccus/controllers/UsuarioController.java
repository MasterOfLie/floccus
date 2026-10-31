package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.AgenteDTO;
import br.dev.matheushenrique.floccus.dto.AtualizaUsuarioDTO;
import br.dev.matheushenrique.floccus.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PutMapping("/me")
    public ResponseEntity<?> atualizarPerfil(@RequestBody AtualizaUsuarioDTO atualizaUsuarioDTO, Authentication authentication) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(atualizaUsuarioDTO, authentication));
    }
    @GetMapping("/me")
    public ResponseEntity<?> buscarPerfil(Authentication authentication) throws Exception {
        return ResponseEntity.ok(usuarioService.buscarPerfil(authentication));
    }

}
