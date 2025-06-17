import userRepository from "../repository/userRepository";
import { User } from "../types/userType";


class UserService {
    // Método para criar um novo usuário
    async createUser (userData: User) {
        return await userRepository.createUser (userData); // Chama o repositório para criar o usuário
    }
    async getUser(email: string, password: string) {
        return await userRepository.findUserByEmailAndPassword(email, password);
    }
}

export default new UserService();