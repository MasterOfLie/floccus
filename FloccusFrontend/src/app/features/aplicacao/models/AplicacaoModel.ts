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
    redeId?: string;
    tipo: TipoAplicacao;
    portaContainer?: number;
    portaExterna?: number;
    dominio?: string;
    linguagem?: string;
    repositorioId?: string;
    framework?: string;
    runtime?: string;
    idGitApp?: string;
    branch?: string;
    buildTool?: string;
    memoriaLimite?: number;
    memoria: string;
    cpuLimite?: number;
    cpu: string;
    criadoEm?: string | Date;
    atualizadoEm?: string | Date;
    tempoStatus: string;
    nomeAgente: string;
    nomeRede: string;
}

