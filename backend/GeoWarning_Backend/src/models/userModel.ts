import mongoose, { Document, Schema } from 'mongoose';

interface UserDocument extends Document {
    email: string;
    password: string;
    position: string;
}

const userSchema: Schema = new Schema({
    email: { type: String, required: true },
    password: { type: String, required: true },
    position: { type: String, required: true },
}, { timestamps: true });

const UserModel = mongoose.model<UserDocument>('User', userSchema, 'Users');

export default UserModel;
