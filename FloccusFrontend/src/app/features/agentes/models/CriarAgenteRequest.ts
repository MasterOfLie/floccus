export interface CriarAgenteRequest {

    ipAgente: string;

    nome: string;

    portaAgente: number;

    ipBackend: string;

    portaBackend: string;

    dockerHost: string;

}