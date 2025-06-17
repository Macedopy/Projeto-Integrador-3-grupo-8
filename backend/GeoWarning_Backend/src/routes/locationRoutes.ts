import { Router } from 'express';
import { saveLocation, listLocations, listLocationsRelatorio } from '../controller/locationController';

const router = Router();

router.post('/saveLocation', saveLocation);
router.get('/listLocations', listLocations);
router.get('/listLocationsRelatorio', listLocationsRelatorio);

export default router;
