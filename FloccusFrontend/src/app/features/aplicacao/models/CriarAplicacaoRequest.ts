import { TipoAplicacao } from "./AplicacaoModel";

export interface CriarAplicacaoRequest {
    nome: string;
    repositorioId?: string;
    imagem: string;
    agenteId: string;
    tipo: TipoAplicacao;
    portaContainer?: number;
    portaExterna?: number;
    dominio?: string;
    memoriaLimite?: number;
    cpuLimite?: number;
}