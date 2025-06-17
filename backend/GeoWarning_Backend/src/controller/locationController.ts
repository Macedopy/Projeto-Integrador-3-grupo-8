import { Request, Response } from "express";
import locationService from "../service/locationService";

export const saveLocation = async (req: Request, res: Response): Promise<void> => {
    try {
        const { latitude, longitude, userId, imageBase64, title, description, riskLevel } = req.body;

        if (!latitude || !longitude || !userId) {
            res.status(400).json({ message: "Latitude, longitude e userId são obrigatórios" });
            return;
        }

        const location = await locationService.saveLocation(
            latitude,
            longitude,
            userId,
            imageBase64,
            title,
            description,
            riskLevel
        );
        console.log(`Localização salva: Latitude=${latitude}, Longitude=${longitude}, UserId=${userId}, Imagem=${imageBase64}, Título=${title}, Descrição=${description}, Nível de Risco=${riskLevel}`);
        res.status(201).json(location);
    } catch (error: any) {
        console.error("Erro ao salvar localização:", error);
        res.status(500).json({ message: error.message });
    }
};

export const listLocations = async (req: Request, res: Response): Promise<void> => {
    try {
        const locations = await locationService.listLocations();
        res.status(200).json(locations);
    } catch (error: any) {
        console.error("Erro ao listar localizações:", error);
        res.status(500).json({ message: error.message });
    }
};

export const listLocationsRelatorio = async (req: Request, res: Response): Promise<void> => {
    try {
        const locations = await locationService.listLocationsRelatorio();
        res.status(200).json(locations);
    } catch (error: any) {
        console.error("Erro ao listar localizações:", error);
        res.status(500).json({ message: error.message });
    }
};