export interface RedeModel {
    id?: string;
    nome: string;
    subnet: string | null;
    gateway: string | null;
    agenteId: string;
    nomeAgente: string;
}