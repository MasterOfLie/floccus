package br.dev.matheushenrique.floccus.dto;

import lombok.Data;
import lombok.Getter;

public record TemplateDTO(

         String linguagem,
         String imagem,
         String framework,
         String runtime,
         String buildTool,
         String versao,
         String buildCommand,
         String startCommand

) {
}
