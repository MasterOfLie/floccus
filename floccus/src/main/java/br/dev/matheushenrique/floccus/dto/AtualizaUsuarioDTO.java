package br.dev.matheushenrique.floccus.dto;

import br.dev.matheushenrique.floccus.enums.Role;

public record AtualizaUsuarioDTO (
        String nome,
        String email,
        String senha
) {
}
