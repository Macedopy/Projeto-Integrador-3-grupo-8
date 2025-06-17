import { Schema, model } from 'mongoose';

const locationSchema = new Schema({
    latitude: {
        type: Number,
        required: true
    },
    longitude: {
        type: Number,
        required: true
    },
    userId: {
        type: String,
        required: true
    },
    imageBase64: {
        type: String,
        required: false
    },
    title: {
        type: String,
        required: false
    },
    description: {
        type: String,
        required: false
    },
    riskLevel: {
        type: String,
        required: false,
        enum: ['Estável', 'Moderado', 'Perigoso'] // Restrict to allowed values
    },
    timestamp: {
        type: Date,
        required: true
    }
});

const LocationModel = model('Location', locationSchema);

export default LocationModel;