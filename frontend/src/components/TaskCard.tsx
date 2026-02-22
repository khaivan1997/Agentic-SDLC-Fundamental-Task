import { TaskStatus } from '../types/Task';
import type { Task, TaskStatusType } from '../types/Task';

interface TaskCardProps {
    task: Task;
    onEdit: (task: Task) => void;
    onDelete: (id: number) => void;
    onStatusChange: (id: number, status: TaskStatusType) => void;
}

export default function TaskCard({ task, onEdit, onDelete, onStatusChange }: TaskCardProps) {
    const getStatusColor = (status: TaskStatusType) => {
        switch (status) {
            case TaskStatus.TODO: return 'bg-yellow-100 text-yellow-800 border-yellow-200';
            case TaskStatus.IN_PROGRESS: return 'bg-blue-100 text-blue-800 border-blue-200';
            case TaskStatus.DONE: return 'bg-green-100 text-green-800 border-green-200';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-shadow duration-200">
            <div className="flex justify-between items-start mb-3">
                <h3 className="text-lg font-semibold text-gray-800 line-clamp-1">{task.title}</h3>
                <select
                    value={task.status}
                    onChange={(e) => onStatusChange(task.id!, e.target.value as TaskStatusType)}
                    className={`text-xs font-medium px-2.5 py-1 rounded-full border cursor-pointer appearance-none ${getStatusColor(task.status)}`}
                >
                    <option value={TaskStatus.TODO}>TODO</option>
                    <option value={TaskStatus.IN_PROGRESS}>IN PROGRESS</option>
                    <option value={TaskStatus.DONE}>DONE</option>
                </select>
            </div>

            {task.description && (
                <p className="text-gray-600 text-sm mb-4 line-clamp-2">{task.description}</p>
            )}

            <div className="flex justify-between items-end mt-4 pt-4 border-t border-gray-50">
                <div className="text-xs text-gray-500 font-medium">
                    {task.dueDate ? `Due: ${new Date(task.dueDate).toLocaleDateString()}` : 'No due date'}
                </div>
                <div className="flex space-x-2">
                    <button
                        onClick={() => onEdit(task)}
                        className="text-indigo-600 hover:text-indigo-800 text-sm font-medium transition-colors"
                    >
                        Edit
                    </button>
                    <button
                        onClick={() => onDelete(task.id!)}
                        className="text-red-600 hover:text-red-800 text-sm font-medium transition-colors"
                    >
                        Delete
                    </button>
                </div>
            </div>
        </div>
    );
}
