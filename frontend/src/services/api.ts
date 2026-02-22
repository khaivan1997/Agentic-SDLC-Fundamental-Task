import axios from 'axios';
import type { Task } from '../types/Task';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/tasks';
const apiClient = axios.create({
    baseURL: API_BASE_URL
});

export const getTasks = async (): Promise<Task[]> => {
    const response = await apiClient.get('');
    return response.data;
};

export const getTaskById = async (id: number): Promise<Task> => {
    const response = await apiClient.get(`/${id}`);
    return response.data;
};

export const createTask = async (task: Task): Promise<Task> => {
    const response = await apiClient.post('', task);
    return response.data;
};

export const updateTask = async (id: number, task: Task): Promise<Task> => {
    const response = await apiClient.put(`/${id}`, task);
    return response.data;
};

export const deleteTask = async (id: number): Promise<void> => {
    await apiClient.delete(`/${id}`);
};
