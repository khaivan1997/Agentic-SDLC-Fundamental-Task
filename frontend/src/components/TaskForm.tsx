import { useState, useEffect } from 'react';
import { TaskStatus } from '../types/Task';
import type { Task, TaskStatusType } from '../types/Task';

interface TaskFormProps {
    taskToEdit: Task | null;
    onSave: (task: Task) => Promise<void>;
    onCancel: () => void;
}

export default function TaskForm({ taskToEdit, onSave, onCancel }: TaskFormProps) {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [status, setStatus] = useState<TaskStatusType>(TaskStatus.TODO);
    const [dueDate, setDueDate] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (taskToEdit) {
            setTitle(taskToEdit.title);
            setDescription(taskToEdit.description || '');
            setStatus(taskToEdit.status);
            setDueDate(taskToEdit.dueDate || '');
        } else {
            setTitle('');
            setDescription('');
            setStatus(TaskStatus.TODO);
            setDueDate('');
        }
    }, [taskToEdit]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!title.trim()) {
            setError('Title is required');
            return;
        }
        if (title.length > 100) {
            setError('Title cannot exceed 100 characters');
            return;
        }
        if (description.length > 500) {
            setError('Description cannot exceed 500 characters');
            return;
        }

        try {
            setIsSubmitting(true);
            setError(null);
            await onSave({
                id: taskToEdit?.id,
                title: title.trim(),
                description: description.trim() || undefined,
                status,
                dueDate: dueDate || undefined
            });
            setIsSubmitting(false);

            // Reset form if creating
            if (!taskToEdit) {
                setTitle('');
                setDescription('');
                setStatus(TaskStatus.TODO);
                setDueDate('');
            }
        } catch (err: any) {
            const responseData = err?.response?.data;
            const fieldErrors = responseData?.errors ? Object.values(responseData.errors).join(', ') : null;
            setError(fieldErrors || responseData?.message || 'Failed to save task. Please try again.');
            setIsSubmitting(false);
        }
    };

    return (
        <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
            <h2 className="text-xl font-bold text-gray-800 mb-5 pb-3 border-b border-gray-100">
                {taskToEdit ? 'Edit Task' : 'Create New Task'}
            </h2>

            {error && (
                <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-lg text-sm font-medium border border-red-100">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Title *</label>
                    <input
                        type="text"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        className="w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 border p-2.5 outline-none transition-all"
                        placeholder="What needs to be done?"
                        disabled={isSubmitting}
                        maxLength={100}
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                    <textarea
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        rows={3}
                        className="w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 border p-2.5 outline-none transition-all resize-none"
                        placeholder="Add details (optional)"
                        disabled={isSubmitting}
                        maxLength={500}
                    />
                    <div className="text-right text-xs text-gray-400 mt-1">
                        {description.length}/500
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
                        <select
                            value={status}
                            onChange={(e) => setStatus(e.target.value as TaskStatusType)}
                            className="w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 border p-2.5 outline-none transition-all"
                            disabled={isSubmitting}
                        >
                            <option value={TaskStatus.TODO}>To Do</option>
                            <option value={TaskStatus.IN_PROGRESS}>In Progress</option>
                            <option value={TaskStatus.DONE}>Done</option>
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Due Date</label>
                        <input
                            type="date"
                            value={dueDate}
                            onChange={(e) => setDueDate(e.target.value)}
                            className="w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 border p-2.5 outline-none transition-all text-gray-700"
                            disabled={isSubmitting}
                        />
                    </div>
                </div>

                <div className="flex justify-end space-x-3 pt-4 border-t border-gray-100 mt-6 !mb-0">
                    {taskToEdit && (
                        <button
                            type="button"
                            onClick={onCancel}
                            disabled={isSubmitting}
                            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
                        >
                            Cancel
                        </button>
                    )}
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="px-6 py-2 text-sm font-medium text-white bg-indigo-600 border border-transparent rounded-lg hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition-colors shadow-sm"
                    >
                        {isSubmitting ? 'Saving...' : (taskToEdit ? 'Update Task' : 'Add Task')}
                    </button>
                </div>
            </form>
        </div>
    );
}
