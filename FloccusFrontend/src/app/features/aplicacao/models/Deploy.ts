interface DeployEtapa {
    tipo: string;
    mensagem: string;
    concluida: boolean;
    atual: boolean;
}

interface DeployEvento {
    aplicacaoId: string;
    tipo: string;
    mensagem: string;
    containerId: string;
}