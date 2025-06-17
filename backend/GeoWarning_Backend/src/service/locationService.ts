import locationRepository from "../repository/locationRepository";

class LocationService {
    async saveLocation(
        latitude: number,
        longitude: number,
        userId: string,
        imageBase64?: string,
        title?: string,
        description?: string,
        riskLevel?: string
    ) {
        try {
            const locationData = {
                latitude,
                longitude,
                userId,
                imageBase64,
                title,
                description,
                riskLevel,
                timestamp: new Date()
            };

            // Chama o repositório para salvar a localização
            return await locationRepository.saveLocation(locationData);
        } catch (error) {
            console.error("Erro ao salvar localização:", error);
            throw new Error("Erro ao salvar localização");
        }
    }


    async listLocations() {
        return await locationRepository.getAllLocations();
    }

    async listLocationsRelatorio() {
        return await locationRepository.getAllLocationsRelatorio();
    }
}

export default new LocationService();