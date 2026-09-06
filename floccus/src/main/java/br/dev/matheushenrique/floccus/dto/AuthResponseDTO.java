package br.dev.matheushenrique.floccus.dto;

import br.dev.matheushenrique.floccus.enums.Role;
import lombok.Builder;

import java.util.Set;

@Builder
public record AuthResponseDTO(
        String token,
        String id,
        String nome,
        String email,
        Set<Role> permissoes
) {
}
