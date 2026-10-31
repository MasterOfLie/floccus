interface PerfilResponse {
    id: string;
    nome: string;
    email: string;
}

interface AtualizaUsuarioDTO {
    nome: string;
    email: string;
    senha: string | null;
}