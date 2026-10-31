package br.dev.matheushenrique.floccus.dto;


import br.dev.matheushenrique.floccus.enums.TipoAplicacao;
import lombok.Data;


public record CriarAplicacaoDTO (
     String nome,
     String repositorioId,
     String imagem,
     String agenteId,
     String redeId,
     TipoAplicacao tipo,
     Integer portaContainer,
     Integer portaExterna,
     String dominio,
     String linguagem,
     String framework,
     String buildTool,
     String branch,
     String idGitApp,
     String runtime,
     Long memoriaLimite,
     Long cpuLimite
){}