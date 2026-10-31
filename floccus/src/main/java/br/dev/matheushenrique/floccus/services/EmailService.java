package br.dev.matheushenrique.floccus.services;

public interface EmailService {

    void enviarRecuperacaoSenha(String destinatario, String link);
}
