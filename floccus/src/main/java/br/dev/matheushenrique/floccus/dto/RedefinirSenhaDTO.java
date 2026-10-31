package br.dev.matheushenrique.floccus.dto;

public record RedefinirSenhaDTO (
        String token,
        String email,
        String novaSenha
){
}
