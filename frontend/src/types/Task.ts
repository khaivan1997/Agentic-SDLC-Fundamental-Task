export const TaskStatus = {
  TODO: 'TODO',
  IN_PROGRESS: 'IN_PROGRESS',
  DONE: 'DONE',
} as const;

export type TaskStatusType = typeof TaskStatus[keyof typeof TaskStatus];

export interface Task {
  id?: number;
  title: string;
  description?: string;
  status: TaskStatusType;
  dueDate?: string; // YYYY-MM-DD
}
