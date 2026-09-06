package br.dev.matheushenrique.floccus.controllers;

import br.dev.matheushenrique.floccus.dto.AuthResponseDTO;
import br.dev.matheushenrique.floccus.dto.LoginRequestDTO;
import br.dev.matheushenrique.floccus.dto.RegisterRequestDTO;
import br.dev.matheushenrique.floccus.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.login(request));
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarConta(request));
    }
}

