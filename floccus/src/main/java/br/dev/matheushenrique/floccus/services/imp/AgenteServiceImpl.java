package br.dev.matheushenrique.floccus.services.imp;

import br.dev.matheushenrique.floccus.dto.AgenteDTO;
import br.dev.matheushenrique.floccus.dto.AgenteMetricasDTO;
import br.dev.matheushenrique.floccus.models.Agente;
import br.dev.matheushenrique.floccus.repository.AgenteRepository;
import br.dev.matheushenrique.floccus.repository.AplicacaoRepository;
import br.dev.matheushenrique.floccus.services.AgenteService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgenteServiceImpl implements AgenteService {

    private final AgenteRepository agenteRepository;
    private final Cache<String, AgenteMetricasDTO> heartbeatCache;
    private final SimpMessagingTemplate messagingTemplate;
    private final AplicacaoRepository aplicacaoRepository;

    @Override
    public AgenteDTO criarAgente(AgenteDTO agenteDTO) throws Exception {
        if (agenteRepository.existsByIpAgente(agenteDTO.ipAgente())) {
            throw new Exception("Já existe um agente com esse IP");
        }

        byte[] bytes = new byte[64];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);

        String chaveAgente = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Agente agente = Agente.builder()
                .nome(agenteDTO.nome())
                .ipAgente(agenteDTO.ipAgente())
                .portaAgente(agenteDTO.portaAgente())
                .chaveAgente("sk_floccus_" + chaveAgente)
                .ipBackend(agenteDTO.ipBackend())
                .portaBackend(agenteDTO.portaBackend())
                .dockerHost(agenteDTO.dockerHost())
                .build();

        Agente agenteSalvo = agenteRepository.save(agente);

        return new AgenteDTO(agenteSalvo.getNome(), agenteSalvo.getId(), agenteSalvo.getIpAgente(), agenteSalvo.getPortaAgente(), agenteSalvo.getChaveAgente(), agenteSalvo.getIpBackend(), agenteSalvo.getPortaBackend(), agenteSalvo.getDockerHost());
    }

    @Override
    public Page<AgenteDTO> listarAgentes(Pageable pageable, String busca) {
        if (busca == null || busca.isBlank()) {
            return agenteRepository.findAll(pageable)
                    .map(AgenteDTO::new);
        }
        return agenteRepository.buscarPaginado(busca.trim(), pageable)
                .map(AgenteDTO::new);
    }
    @Override
    public List<AgenteDTO> listarAgentes() {
        return agenteRepository.listarTodosDTO();
    }

    @Override
    public AgenteDTO buscarAgente(String agenteId) throws Exception {
        Optional<Agente> agente = agenteRepository.findById(agenteId);
        if (agente.isEmpty()) {
            throw new Exception("Nenhum agente encontrado com esse Id.");
        }

        return new AgenteDTO(agente.get().getNome(), agente.get().getId(), agente.get().getIpAgente(), agente.get().getPortaAgente(), agente.get().getChaveAgente(), agente.get().getIpBackend(), agente.get().getPortaBackend(), agente.get().getDockerHost());
    }

    @Override
    public AgenteDTO atualizarAgente(String agenteId, AgenteDTO agenteDTO) {
        Agente agente = agenteRepository.findById(agenteId)
                .orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        boolean ipEmUso = agenteRepository.existsByIpAgenteAndIdNot(
                agenteDTO.ipAgente(),
                agenteId
        );
        if (ipEmUso) {
            throw new RuntimeException(
                    "Já existe um agente cadastrado com o IP: " + agenteDTO.ipAgente()
            );
        }
        agente.setIpAgente(agenteDTO.ipAgente());
        agente.setPortaAgente(agenteDTO.portaAgente());
        agente.setIpBackend(agenteDTO.ipBackend());
        agente.setPortaBackend(agenteDTO.portaBackend());
        agente.setDockerHost(agenteDTO.dockerHost());
        agente = agenteRepository.save(agente);
        return new AgenteDTO(agente);
    }

    @Override
    public AgenteDTO atualizarChave(String agenteId) throws Exception {
        Agente agente = agenteRepository.findById(agenteId)
                .orElseThrow(() -> new RuntimeException("Agente não encontrado"));
        byte[] bytes = new byte[64];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        String novaChave = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        agente.setChaveAgente("sk_floccus_" + novaChave);
        agente = agenteRepository.save(agente);
        return new AgenteDTO(agente);
    }

    @Override
    public void receberHeartbeat(String agenteId, AgenteMetricasDTO metricas) {
        boolean estavaOffline = heartbeatCache.getIfPresent(agenteId) == null;

        heartbeatCache.put(agenteId, metricas);

        messagingTemplate.convertAndSend("/topic/agentes/" + agenteId + "/status", metricas);

        if (estavaOffline) {

            notificarStatus(agenteId, metricas, true);
        }
    }

    @Override
    public void republicarMetricas(String agenteId) {
        AgenteMetricasDTO metricas = heartbeatCache.getIfPresent(agenteId);

        if (metricas == null) {
            return;
        }

        messagingTemplate.convertAndSend("/topic/agentes/" + agenteId + "/status", metricas);
    }

    @Override
    public void deletarAgente(String agenteId) {
        if(aplicacaoRepository.existsByAgenteId(agenteId)){
            throw new RuntimeException("Não é possível excluir o agente pois existem aplicações vinculadas a ele.");
        }
        agenteRepository.deleteById(agenteId);
    }

    private void notificarStatus(String agenteId, AgenteMetricasDTO metricas, boolean online) {
        AgenteMetricasDTO agenteMetricasDTO = AgenteMetricasDTO.builder().cpuUsage(metricas.cpuUsage()).discoTotalGb(metricas.discoTotalGb()).online(online).ramUsadaMb(metricas.ramUsadaMb()).ramTotalMb(metricas.ramTotalMb()).uptime(metricas.uptime()).discoUsadoGb(metricas.discoUsadoGb()).build();

        messagingTemplate.convertAndSend("/topic/agentes/" + agenteId + "/status", agenteMetricasDTO);
    }
}