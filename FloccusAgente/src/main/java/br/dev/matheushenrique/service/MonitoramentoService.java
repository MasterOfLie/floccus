package br.dev.matheushenrique.service;

import br.dev.matheushenrique.dto.ContainerMonitoramentoDTO;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.CpuStatsConfig;
import com.github.dockerjava.api.model.Statistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class MonitoramentoService {

    private final DockerClient dockerClient;


    public List<ContainerMonitoramentoDTO> monitorarContainer() {
        List<ContainerMonitoramentoDTO> monitoramentos = new ArrayList<>();

        List<Container> containers = dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .withLabelFilter(Map.of("floccus.aplicacao", "true"))
                .exec();

        for (Container container : containers) {
            try {
                Map<String, String> labels = container.getLabels();
                if (labels == null) {
                    continue;
                }

                String aplicacaoId = labels.get("floccus.aplicacao.id");
                if (aplicacaoId == null) {
                    continue;
                }

                String containerId = container.getId();

                InspectContainerResponse inspect = dockerClient
                        .inspectContainerCmd(containerId)
                        .exec();

                String status = "unknown";
                if (inspect.getState() != null && inspect.getState().getStatus() != null) {
                    status = inspect.getState().getStatus();
                }

                final Statistics[] statsHolder = new Statistics[1];

                dockerClient.statsCmd(containerId)
                        .withNoStream(true)
                        .exec(new ResultCallbackTemplate<ResultCallbackTemplate<?, Statistics>, Statistics>() {
                            @Override
                            public void onNext(Statistics statistics) {
                                statsHolder[0] = statistics;
                            }
                        })
                        .awaitCompletion();

                Statistics statistics = statsHolder[0];
                if (statistics == null) {
                    continue;
                }

                long memoriaUso = 0;
                long memoriaLimite = 0;
                double memoriaPercentual = 0;

                if (statistics.getMemoryStats() != null) {
                    if (statistics.getMemoryStats().getUsage() != null) {
                        memoriaUso = statistics.getMemoryStats().getUsage() / (1024 * 1024);
                    }
                    if (statistics.getMemoryStats().getLimit() != null) {
                        memoriaLimite = statistics.getMemoryStats().getLimit() / (1024 * 1024);
                    }
                    if (memoriaLimite > 0) {
                        memoriaPercentual = ((double) memoriaUso / memoriaLimite) * 100.0;
                    }
                }

                long redeRecebida = 0;
                long redeEnviada = 0;

                if (statistics.getNetworks() != null) {
                    for (var network : statistics.getNetworks().values()) {
                        if (network.getRxBytes() != null) {
                            redeRecebida += network.getRxBytes();
                        }
                        if (network.getTxBytes() != null) {
                            redeEnviada += network.getTxBytes();
                        }
                    }
                }

                double cpuPercentual = calcularPercentualCpu(
                        statistics.getCpuStats(),
                        statistics.getPreCpuStats()
                );

                ContainerMonitoramentoDTO monitoramento =
                        new ContainerMonitoramentoDTO(
                                aplicacaoId,
                                containerId,
                                status,
                                cpuPercentual,
                                memoriaUso,
                                memoriaLimite,
                                memoriaPercentual,
                                redeRecebida,
                                redeEnviada
                        );

                monitoramentos.add(monitoramento);

            } catch (Exception e) {
                log.error("Erro ao coletar monitoramento do container {}", container.getId(), e);
            }
        }

        return monitoramentos;
    }

    private double calcularPercentualCpu(CpuStatsConfig cpuStats, CpuStatsConfig preCpuStats) {
        if (cpuStats == null || preCpuStats == null ||
                cpuStats.getCpuUsage() == null ||
                preCpuStats.getCpuUsage() == null) {
            return 0.0;
        }

        Long totalUsage = cpuStats.getCpuUsage().getTotalUsage();
        Long preTotalUsage = preCpuStats.getCpuUsage().getTotalUsage();

        if (totalUsage == null || preTotalUsage == null) {
            return 0.0;
        }

        long cpuDelta = totalUsage - preTotalUsage;

        Long systemUsage = cpuStats.getSystemCpuUsage();
        Long previousSystemUsage = preCpuStats.getSystemCpuUsage();

        if (systemUsage == null || previousSystemUsage == null) {
            return 0.0;
        }

        long systemDelta = systemUsage - previousSystemUsage;

        if (systemDelta > 0 && cpuDelta > 0) {
            Long onlineCpus = cpuStats.getOnlineCpus();
            long cpus = (onlineCpus != null && onlineCpus > 0) ? onlineCpus : 1;

            return ((double) cpuDelta / systemDelta) * cpus * 100.0;
        }

        return 0.0;
    }

}
