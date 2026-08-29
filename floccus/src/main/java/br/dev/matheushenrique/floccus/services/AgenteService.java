package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.AgenteDTO;

import br.dev.matheushenrique.floccus.dto.AgenteMetricasDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AgenteService {
    AgenteDTO criarAgente (AgenteDTO agenteDTO) throws Exception;
    Page<AgenteDTO> listarAgentes (Pageable pageable);
    AgenteDTO buscarAgente (String agenteId) throws Exception;
    AgenteDTO atualizarAgente (String agenteId, AgenteDTO agenteDTO)  throws Exception;
    AgenteDTO atualizarChave (String agenteId) throws Exception;
    void receberHeartbeat(String agenteId, AgenteMetricasDTO metricas);
    void republicarMetricas(String agenteId);
}
