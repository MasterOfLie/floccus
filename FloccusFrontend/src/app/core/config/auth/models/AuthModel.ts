export interface LoginRequest {
    email: string;
    senha: string;
}

export interface RegisterRequest {
    nome: string;
    email: string;
    senha: string;
}

export interface AuthResponse {
    token: string;
    id: string;
    nome: string;
    email: string;
    permissoes: string[];
}

export interface RedefinirSenha {
    email: string;
    token: string;
    novaSenha: string;
}