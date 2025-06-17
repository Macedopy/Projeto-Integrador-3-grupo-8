import UserModel from "../models/userModel";
import { User } from "../types/userType";

class UserRepository {
    // Método para criar um novo usuário
    async createUser(userData: User) {
        try {
            console.log("Criando usuário:", userData);
            const user = new UserModel({
                email: userData.email,
                password: userData.password,
                position: userData.position ?? 'user' // valor padrão se não vier
            });

            const savedUser = await user.save();
            console.log("Usuário salvo com sucesso:", savedUser);
            return savedUser;
        } catch (error) {
            console.error("Erro ao criar usuário:", error);
            throw error;
        }
    }
    async findUserByEmailAndPassword(email: string, password: string) {
        const result = await UserModel.findOne({email: email, password: password});
        console.log('Resultado:', result);
        return result;
    }
    
}

export default new UserRepository();