export interface Agente {
    id: string;
    ipAgente: string;
    portaAgente: number;
    chaveAgente: string;
    online: boolean;
    ipBackend: string;
    portaBackend: string;
    dockerHost: string;
    nome: string;
}