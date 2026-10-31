package br.dev.matheushenrique.floccus.dto;

import br.dev.matheushenrique.floccus.enums.Role;

import java.util.Set;

public record UsuarioDTO(
        String id,
        String nome,
        String email,
        Set<Role> roles
) { }
