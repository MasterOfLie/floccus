export interface VariavelAplicacao {
    id?: string;
    chave: string;
    valor: string;
    secreta: boolean;
}
export interface VariaveisRequest {
    variaveis: VariavelAplicacao[];
}