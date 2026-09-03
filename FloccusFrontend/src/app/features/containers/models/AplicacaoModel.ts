export enum TipoAplicacao {
    HTTP = 'HTTP',
    TCP = 'TCP',
    UDP = 'UDP'
}

export interface AplicacaoModel {
    id?: string;
    status: string;
    nome: string;
    imagem: string;
    agenteId: string;
    containerId?: string;
    tipo: TipoAplicacao;
    portaContainer?: number;
    portaExterna?: number;
    dominio?: string;
    memoriaLimite?: number;
    memoria: string;
    cpuLimite?: number;
    cpu: string;
    criadoEm?: string | Date;
    atualizadoEm?: string | Date;
    tempoStatus: string;
}

