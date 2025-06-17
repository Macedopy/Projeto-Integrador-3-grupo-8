export interface User {
    id?: string; // O ID do usuário, opcional, pois será gerado pelo MongoDB
    email: string;
    password: string;     // O nome do usuário, obrigatório
    position: string; // O cargo do usuário, obrigatório
}