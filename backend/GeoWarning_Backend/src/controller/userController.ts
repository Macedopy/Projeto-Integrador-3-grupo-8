import { Request, Response } from 'express';
import userService from '../service/userService';
import { User } from '../types/userType';

export const createUser = async (req: Request, res: Response): Promise<void> => {
    console.log("Create")
    try {
        const userData: User = req.body;
        const newUser = await userService.createUser(userData); // Chama o serviço para criar o usuário
        res.status(201).json(newUser); // Retorna o novo usuário com status 201 (Criado)
    } catch (error: any) {
        res.status(500).json({ message: error.message }); // Retorna um erro em caso de falha
    }
};

export const getUser = async (req: Request, res: Response): Promise<void> => {
    try {
        console.log("Get")
        const { email, password } = req.query;

        if (!email || !password) {
            res.status(400).json({ message: 'Email e senha são obrigatórios.' });
        }

        const user = await userService.getUser(email as string, password as string);
        console.log("Usuário", user)
        if (user) {
            res.status(200).json(user);
        } else {
            res.status(404).json({ message: 'Usuário não encontrado' });
        }
    } catch (error: any) {
        res.status(500).json({ message: error.message });
    }
};
