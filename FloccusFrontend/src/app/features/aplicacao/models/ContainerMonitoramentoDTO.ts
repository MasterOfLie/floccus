export interface ContainerMonitoramentoDTO {
    aplicacaoId: string;
    containerId: string;
    status: string;
    cpuPercentual: number;
    memoriaUso: number;
    memoriaLimite: number;
    memoriaPercentual: number;
    redeRecebida: number;
    redeEnviada: number;
}

export interface ContainerHeartbeatDTO {
    agenteId: string;
    timestamp: number;
    containers: ContainerMonitoramentoDTO[];
}


export interface MonitoramentoResponseDTO {
    [agenteId: string]: ContainerHeartbeatDTO;
}