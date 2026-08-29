package br.dev.matheushenrique.service;
import br.dev.matheushenrique.dto.AgenteMetricasDTO;
import lombok.RequiredArgsConstructor;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

public class MetricasCollectorService {

    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hal;
    private final OperatingSystem os;
    private long[] prevTicks;

    public MetricasCollectorService() {
        this.systemInfo = new SystemInfo();
        this.hal = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
        this.prevTicks = hal.getProcessor().getSystemCpuLoadTicks();
    }

    public AgenteMetricasDTO coletarMetricas() {
        // --- 1. CPU (%) ---
        CentralProcessor processor = hal.getProcessor();
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100.0;
        this.prevTicks = processor.getSystemCpuLoadTicks(); // Atualiza os ticks para a próxima leitura

        // --- 2. Memória RAM (MB) ---
        GlobalMemory memory = hal.getMemory();
        long ramTotalBytes = memory.getTotal();
        long ramDisponivelBytes = memory.getAvailable();
        long ramUsadaBytes = ramTotalBytes - ramDisponivelBytes;

        long ramTotalMb = ramTotalBytes / (1024 * 1024);
        long ramUsadaMb = ramUsadaBytes / (1024 * 1024);

        // --- 3. Armazenamento / Disco (GB) ---
        FileSystem fileSystem = os.getFileSystem();


        OSFileStore rootStore = fileSystem.getFileStores().stream()
                .filter(fs -> "/".equals(fs.getMount()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Não foi possível encontrar o filesystem raiz (/)"
                        )
                );
        long totalEspacoBytes = rootStore.getTotalSpace();
        long espacoLivreBytes = rootStore.getUsableSpace();

        long discoUsadoBytes =
                totalEspacoBytes - espacoLivreBytes;

        double discoTotalGb =
                totalEspacoBytes / (1024.0 * 1024.0 * 1024.0);

        double discoUsadoGb =
                discoUsadoBytes / (1024.0 * 1024.0 * 1024.0);

        // --- 4. Uptime (Segundos) ---
        long uptime = os.getSystemUptime();

        // --- 5. Construção do DTO ---
        return AgenteMetricasDTO.builder()
                .cpuUsage(Math.round(cpuUsage * 100.0) / 100.0) // Arredondado com 2 casas decimais
                .ramTotalMb(ramTotalMb)
                .ramUsadaMb(ramUsadaMb)
                .discoTotalGb(discoTotalGb)
                .discoUsadoGb(discoUsadoGb)
                .uptime(uptime)
                .online(true)
                .build();
    }
}
