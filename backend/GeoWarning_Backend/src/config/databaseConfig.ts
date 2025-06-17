import mongoose from 'mongoose';

const mongoURI: string = 'mongodb+srv://bruno:geowarning@cluster0.qgbv0kb.mongodb.net/**GeoWarning**?retryWrites=true&w=majority&appName=Cluster0';

const connectDB = async () => {
    try {
        await mongoose.connect(mongoURI);
        console.log('✅ Conectado ao MongoDB com sucesso');
    } catch (error) {
        console.error('❌ Erro ao conectar no MongoDB:', error);
    }
};

export default connectDB;
