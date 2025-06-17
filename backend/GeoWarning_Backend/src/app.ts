import express from 'express';
import userRoutes from './routes/userRoutes';
import locationRoutes from './routes/locationRoutes';
import connectDB from './config/databaseConfig';

const bodyParser = require('body-parser');

const app = express();
const PORT = 3000;

connectDB();

app.use(bodyParser.json({ limit: '1000mb' }));
app.use(bodyParser.urlencoded({ limit: '1000mb', extended: true }));

app.use(express.json());

// Definir as rotas da API
app.use('/api', userRoutes);
app.use('/api', locationRoutes);

// Iniciar o servidor
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});