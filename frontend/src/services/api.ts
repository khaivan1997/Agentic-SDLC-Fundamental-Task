import axios from 'axios';
import type { Task } from '../types/Task';

const API_BASE_URL = 'http://localhost:8080/api/tasks';

export const getTasks = async (): Promise<Task[]> => {
    const response = await axios.get(API_BASE_URL);
    return response.data;
};

export const getTaskById = async (id: number): Promise<Task> => {
    const response = await axios.get(`${API_BASE_URL}/${id}`);
    return response.data;
};

export const createTask = async (task: Task): Promise<Task> => {
    const response = await axios.post(API_BASE_URL, task);
    return response.data;
};

export const updateTask = async (id: number, task: Task): Promise<Task> => {
    const response = await axios.put(`${API_BASE_URL}/${id}`, task);
    return response.data;
};

export const deleteTask = async (id: number): Promise<void> => {
    await axios.delete(`${API_BASE_URL}/${id}`);
};
