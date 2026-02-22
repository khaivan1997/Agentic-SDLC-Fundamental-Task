import { useState, useEffect } from 'react';
import TaskForm from './TaskForm';
import TaskCard from './TaskCard';
import { TaskStatus } from '../types/Task';
import type { Task, TaskStatusType } from '../types/Task';
import { getTasks, createTask, updateTask, deleteTask } from '../services/api';

export default function TaskList() {
    const [tasks, setTasks] = useState<Task[]>([]);
    const [taskToEdit, setTaskToEdit] = useState<Task | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [sortBy, setSortBy] = useState<'created' | 'status' | 'dueDate'>('created');
    const [searchTerm, setSearchTerm] = useState('');

    const fetchTasks = async () => {
        try {
            setIsLoading(true);
            const data = await getTasks();
            setTasks(data);
            setError(null);
        } catch (err: any) {
            setError('Could not connect to the backend server. Make sure Spring Boot is running.');
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchTasks();
    }, []);

    const handleSave = async (task: Task) => {
        if (task.id) {
            await updateTask(task.id, task);
            setTaskToEdit(null);
        } else {
            await createTask(task);
        }
        await fetchTasks();
    };

    const handleStatusChange = async (id: number, status: TaskStatusType) => {
        try {
            const task = tasks.find(t => t.id === id);
            if (task) {
                // Optimistic update
                setTasks(tasks.map(t => t.id === id ? { ...t, status } : t));
                await updateTask(id, { ...task, status });
            }
        } catch (err) {
            // Revert on error
            await fetchTasks();
            alert('Failed to update status');
        }
    };

    const handleDelete = async (id: number) => {
        if (window.confirm('Are you sure you want to delete this task?')) {
            try {
                await deleteTask(id);
                await fetchTasks();
            } catch (err) {
                alert('Failed to delete task');
            }
        }
    };

    const getStatusRank = (status: TaskStatusType) => {
        switch (status) {
            case TaskStatus.TODO:
                return 0;
            case TaskStatus.IN_PROGRESS:
                return 1;
            case TaskStatus.DONE:
                return 2;
            default:
                return 3;
        }
    };

    const filteredTasks = tasks.filter((task) => {
        if (!searchTerm.trim()) {
            return true;
        }

        const keyword = searchTerm.trim().toLowerCase();
        return task.title.toLowerCase().includes(keyword) ||
            (task.description ?? '').toLowerCase().includes(keyword);
    });

    const sortedTasks = [...filteredTasks].sort((a, b) => {
        if (sortBy === 'status') {
            const statusDiff = getStatusRank(a.status) - getStatusRank(b.status);
            if (statusDiff !== 0) {
                return statusDiff;
            }
            return (a.id ?? 0) - (b.id ?? 0);
        }

        if (sortBy === 'dueDate') {
            const aTime = a.dueDate ? new Date(a.dueDate).getTime() : Number.POSITIVE_INFINITY;
            const bTime = b.dueDate ? new Date(b.dueDate).getTime() : Number.POSITIVE_INFINITY;
            if (aTime !== bTime) {
                return aTime - bTime;
            }
            return (a.id ?? 0) - (b.id ?? 0);
        }

        return (a.id ?? 0) - (b.id ?? 0);
    });

    // Group tasks by status for a Kanban-ish layout (Optional Bonus feature)
    const groupedTasks = {
        [TaskStatus.TODO]: sortedTasks.filter(t => t.status === TaskStatus.TODO),
        [TaskStatus.IN_PROGRESS]: sortedTasks.filter(t => t.status === TaskStatus.IN_PROGRESS),
        [TaskStatus.DONE]: sortedTasks.filter(t => t.status === TaskStatus.DONE),
    };

    return (
        <div className="max-w-6xl mx-auto px-4 py-8 h-full flex flex-col items-center">
            <div className="w-full max-w-3xl mb-10">
                <h1 className="text-4xl font-extrabold text-gray-900 text-center mb-8 tracking-tight">
                    Task <span className="text-indigo-600">Manager</span>
                </h1>
                <TaskForm
                    taskToEdit={taskToEdit}
                    onSave={handleSave}
                    onCancel={() => setTaskToEdit(null)}
                />
            </div>

            <div className="w-full">
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4">
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full md:max-w-sm rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-700 bg-white"
                        placeholder="Search by title or description"
                    />
                    <label className="text-sm text-gray-600 flex items-center gap-2 justify-end">
                        Sort by
                        <select
                            value={sortBy}
                            onChange={(e) => setSortBy(e.target.value as 'created' | 'status' | 'dueDate')}
                            className="rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-700 bg-white"
                        >
                            <option value="created">Created order</option>
                            <option value="status">Status</option>
                            <option value="dueDate">Due date</option>
                        </select>
                    </label>
                </div>

                {error ? (
                    <div className="bg-red-50 text-red-700 p-4 rounded-xl text-center shadow-sm border border-red-100">
                        {error}
                    </div>
                ) : isLoading ? (
                    <div className="flex justify-center items-center py-20">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
                    </div>
                ) : tasks.length === 0 ? (
                    <div className="text-center py-20 bg-gray-50 rounded-2xl border border-dashed border-gray-300">
                        <p className="text-gray-500 text-lg">No tasks found. Get started by adding one above!</p>
                    </div>
                ) : filteredTasks.length === 0 ? (
                    <div className="text-center py-20 bg-gray-50 rounded-2xl border border-dashed border-gray-300">
                        <p className="text-gray-500 text-lg">No tasks match your search.</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
                        {/* TODO Column */}
                        <div className="bg-gray-50 p-4 rounded-xl h-full border border-gray-100">
                            <h3 className="font-bold text-gray-700 mb-4 flex items-center justify-between">
                                <span>To Do</span>
                                <span className="bg-yellow-100 text-yellow-800 text-xs px-2 py-1 rounded-full">{groupedTasks[TaskStatus.TODO].length}</span>
                            </h3>
                            <div className="space-y-4">
                                {groupedTasks[TaskStatus.TODO].map(task => (
                                    <TaskCard
                                        key={task.id}
                                        task={task}
                                        onEdit={setTaskToEdit}
                                        onDelete={handleDelete}
                                        onStatusChange={handleStatusChange}
                                    />
                                ))}
                            </div>
                        </div>

                        {/* IN PROGRESS Column */}
                        <div className="bg-gray-50 p-4 rounded-xl h-full border border-gray-100">
                            <h3 className="font-bold text-gray-700 mb-4 flex items-center justify-between">
                                <span>In Progress</span>
                                <span className="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full">{groupedTasks[TaskStatus.IN_PROGRESS].length}</span>
                            </h3>
                            <div className="space-y-4">
                                {groupedTasks[TaskStatus.IN_PROGRESS].map(task => (
                                    <TaskCard
                                        key={task.id}
                                        task={task}
                                        onEdit={setTaskToEdit}
                                        onDelete={handleDelete}
                                        onStatusChange={handleStatusChange}
                                    />
                                ))}
                            </div>
                        </div>

                        {/* DONE Column */}
                        <div className="bg-gray-50 p-4 rounded-xl h-full border border-gray-100">
                            <h3 className="font-bold text-gray-700 mb-4 flex items-center justify-between">
                                <span>Done</span>
                                <span className="bg-green-100 text-green-800 text-xs px-2 py-1 rounded-full">{groupedTasks[TaskStatus.DONE].length}</span>
                            </h3>
                            <div className="space-y-4">
                                {groupedTasks[TaskStatus.DONE].map(task => (
                                    <TaskCard
                                        key={task.id}
                                        task={task}
                                        onEdit={setTaskToEdit}
                                        onDelete={handleDelete}
                                        onStatusChange={handleStatusChange}
                                    />
                                ))}
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
