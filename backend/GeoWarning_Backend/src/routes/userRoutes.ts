import { Router } from "express";
import { createUser, getUser } from "../controller/userController";
import { saveLocation } from "../controller/locationController";

const router = Router();
router.post('/users', createUser);
router.get('/getUser', getUser);
router.post('/localizacao', saveLocation)
export default router;
    