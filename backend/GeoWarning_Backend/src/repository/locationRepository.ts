import LocationModel from "../models/locationModel";

class LocationRepository {
    async saveLocation(locationData: {
        latitude: number,
        longitude: number,
        userId: string,
        imageBase64?: string,
        title?: string,
        description?: string,
        riskLevel?: string,
        timestamp: Date
    }) {
        try {
            const location = new LocationModel(locationData);
            const savedLocation = await location.save();
            return savedLocation;
        } catch (error) {
            console.error("Erro ao salvar localização:", error);
            throw error;
        }
    }

    async getAllLocations() {
        return await LocationModel.find({}).sort({ timestamp: -1 });
    }

    async getAllLocationsRelatorio()
    {
        return await LocationModel.find({}, { imageBase64: 0 }).sort({ timestamp: -1 });
    }
}

export default new LocationRepository();