import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import TaskList from './TaskList'
import type { Task } from '../types/Task'
import { TaskStatus } from '../types/Task'
import {
  createTask,
  deleteTask,
  getTasks,
  updateTask,
} from '../services/api'

vi.mock('../services/api', () => ({
  getTasks: vi.fn(),
  createTask: vi.fn(),
  updateTask: vi.fn(),
  deleteTask: vi.fn(),
}))

const mockedGetTasks = vi.mocked(getTasks)
const mockedCreateTask = vi.mocked(createTask)
const mockedUpdateTask = vi.mocked(updateTask)
const mockedDeleteTask = vi.mocked(deleteTask)

const baseTasks: Task[] = [
  {
    id: 1,
    title: 'Task One',
    description: 'Alpha work',
    status: TaskStatus.TODO,
    dueDate: '2026-03-01',
  },
  {
    id: 2,
    title: 'Task Two',
    description: 'Beta work',
    status: TaskStatus.IN_PROGRESS,
    dueDate: '2026-03-02',
  },
]

const cloneBaseTasks = () => baseTasks.map((task) => ({ ...task }))

describe('TaskList', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    vi.resetAllMocks()
    mockedGetTasks.mockResolvedValue(cloneBaseTasks())
  })

  it('loads tasks and renders grouped columns', async () => {
    render(<TaskList />)

    expect(await screen.findByText('Task One')).toBeInTheDocument()
    expect(screen.getByText('Task Two')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /To Do/ })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /In Progress/ })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /Done/ })).toBeInTheDocument()
  })

  it('filters tasks using search', async () => {
    const user = userEvent.setup()
    render(<TaskList />)
    await screen.findByText('Task One')

    await user.type(screen.getByPlaceholderText('Search by title or description'), 'zzz')
    expect(screen.getByText('No tasks match your search.')).toBeInTheDocument()

    await user.clear(screen.getByPlaceholderText('Search by title or description'))
    await user.type(screen.getByPlaceholderText('Search by title or description'), 'alpha')
    expect(screen.getByText('Task One')).toBeInTheDocument()
    expect(screen.queryByText('Task Two')).not.toBeInTheDocument()
  })

  it('creates a task from form submission', async () => {
    const user = userEvent.setup()
    const afterCreate: Task[] = [
      ...baseTasks,
      { id: 3, title: 'New Task', status: TaskStatus.TODO, description: undefined, dueDate: undefined },
    ]
    mockedGetTasks.mockResolvedValueOnce(cloneBaseTasks()).mockResolvedValueOnce(afterCreate)
    mockedCreateTask.mockResolvedValue({
      id: 3,
      title: 'New Task',
      status: TaskStatus.TODO,
    } as Task)

    render(<TaskList />)
    await screen.findByText('Task One')

    await user.type(screen.getByPlaceholderText('What needs to be done?'), '  New Task  ')
    await user.click(screen.getByRole('button', { name: 'Add Task' }))

    await waitFor(() => {
      expect(mockedCreateTask).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'New Task',
          status: TaskStatus.TODO,
        })
      )
    })
  })

  it('updates task status from the card dropdown', async () => {
    const user = userEvent.setup()
    mockedUpdateTask.mockResolvedValue({
      ...baseTasks[0],
      status: TaskStatus.DONE,
    } as Task)
    mockedGetTasks.mockResolvedValueOnce(cloneBaseTasks()).mockResolvedValueOnce(cloneBaseTasks())

    render(<TaskList />)
    await screen.findByText('Task One')

    const taskOneHeading = screen.getByRole('heading', { name: 'Task One' })
    const taskOneCard = taskOneHeading.closest('div[class*="p-5"]')
    expect(taskOneCard).not.toBeNull()

    const statusSelect = within(taskOneCard as HTMLElement).getByDisplayValue('TODO')
    await user.selectOptions(statusSelect, 'DONE')

    await waitFor(() => {
      expect(mockedUpdateTask).toHaveBeenCalledWith(
        1,
        expect.objectContaining({ status: TaskStatus.DONE })
      )
    })
  })

  it('deletes task after confirmation', async () => {
    const user = userEvent.setup()
    mockedDeleteTask.mockResolvedValue()
    mockedGetTasks
      .mockResolvedValueOnce(cloneBaseTasks())
      .mockResolvedValueOnce([baseTasks[1]])

    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true)
    render(<TaskList />)
    await screen.findByText('Task One')

    await user.click(screen.getAllByRole('button', { name: 'Delete' })[0])
    await waitFor(() => {
      expect(mockedDeleteTask).toHaveBeenCalledWith(1)
      expect(confirmSpy).toHaveBeenCalled()
    })
    confirmSpy.mockRestore()
  })

  it('does not delete when confirmation is cancelled', async () => {
    const user = userEvent.setup()
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false)

    render(<TaskList />)
    await screen.findByText('Task One')

    await user.click(screen.getAllByRole('button', { name: 'Delete' })[0])

    expect(confirmSpy).toHaveBeenCalled()
    expect(mockedDeleteTask).not.toHaveBeenCalled()
  })

  it('shows fetch error message when initial load fails', async () => {
    mockedGetTasks.mockRejectedValueOnce(new Error('backend down'))
    render(<TaskList />)

    expect(await screen.findByText('Could not connect to the backend server. Make sure Spring Boot is running.'))
      .toBeInTheDocument()
  })

  it('shows empty state when no tasks exist', async () => {
    mockedGetTasks.mockResolvedValueOnce([])
    render(<TaskList />)

    expect(await screen.findByText('No tasks found. Get started by adding one above!')).toBeInTheDocument()
  })

  it('validates required title before save', async () => {
    const user = userEvent.setup()
    render(<TaskList />)
    await screen.findByText('Task One')

    await user.click(screen.getByRole('button', { name: 'Add Task' }))
    expect(screen.getByText('Title is required')).toBeInTheDocument()
    expect(mockedCreateTask).not.toHaveBeenCalled()
  })

  it('validates title length limit before save', async () => {
    const user = userEvent.setup()
    render(<TaskList />)
    await screen.findByText('Task One')

    const titleInput = screen.getByPlaceholderText('What needs to be done?')
    fireEvent.change(titleInput, { target: { value: 'A'.repeat(101) } })
    await user.click(screen.getByRole('button', { name: 'Add Task' }))

    expect(screen.getByText('Title cannot exceed 100 characters')).toBeInTheDocument()
    expect(mockedCreateTask).not.toHaveBeenCalled()
  })

  it('validates description length limit before save', async () => {
    const user = userEvent.setup()
    render(<TaskList />)
    await screen.findByText('Task One')

    await user.type(screen.getByPlaceholderText('What needs to be done?'), 'Valid title')
    const descriptionInput = screen.getByPlaceholderText('Add details (optional)')
    fireEvent.change(descriptionInput, { target: { value: 'D'.repeat(501) } })
    await user.click(screen.getByRole('button', { name: 'Add Task' }))

    expect(screen.getByText('Description cannot exceed 500 characters')).toBeInTheDocument()
    expect(mockedCreateTask).not.toHaveBeenCalled()
  })

  it('shows API field errors from failed create', async () => {
    const user = userEvent.setup()
    mockedCreateTask.mockRejectedValueOnce({
      response: {
        data: {
          errors: { title: 'Title invalid', description: 'Description invalid' },
        },
      },
    })

    render(<TaskList />)
    await screen.findByText('Task One')

    await user.type(screen.getByPlaceholderText('What needs to be done?'), 'Will fail')
    await user.click(screen.getByRole('button', { name: 'Add Task' }))

    expect(await screen.findByText('Title invalid, Description invalid')).toBeInTheDocument()
  })

  it('opens edit mode, updates task, and exits edit mode', async () => {
    const user = userEvent.setup()
    mockedUpdateTask.mockResolvedValueOnce({
      ...baseTasks[0],
      title: 'Task One Updated',
    } as Task)

    render(<TaskList />)
    await screen.findByText('Task One')

    await user.click(screen.getAllByRole('button', { name: 'Edit' })[0])
    expect(screen.getByRole('heading', { name: 'Edit Task' })).toBeInTheDocument()

    const titleInput = screen.getByDisplayValue('Task One')
    await user.clear(titleInput)
    await user.type(titleInput, 'Task One Updated')
    await user.click(screen.getByRole('button', { name: 'Update Task' }))

    await waitFor(() => {
      expect(mockedUpdateTask).toHaveBeenCalledWith(
        1,
        expect.objectContaining({ title: 'Task One Updated' })
      )
    })
    expect(screen.getByRole('heading', { name: 'Create New Task' })).toBeInTheDocument()
  })

  it('cancels edit mode without saving', async () => {
    const user = userEvent.setup()
    render(<TaskList />)
    await screen.findByText('Task One')

    await user.click(screen.getAllByRole('button', { name: 'Edit' })[0])
    expect(screen.getByRole('heading', { name: 'Edit Task' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Cancel' }))
    expect(screen.getByRole('heading', { name: 'Create New Task' })).toBeInTheDocument()
    expect(mockedUpdateTask).not.toHaveBeenCalled()
  })

  it('shows delete error message when delete API fails', async () => {
    const user = userEvent.setup()
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    mockedDeleteTask.mockRejectedValueOnce(new Error('delete failed'))

    render(<TaskList />)
    await screen.findByText('Task One')

    await user.click(screen.getAllByRole('button', { name: 'Delete' })[0])
    expect(await screen.findByText('Failed to delete task.')).toBeInTheDocument()
  })

  it('sorts by due date when selected', async () => {
    const user = userEvent.setup()
    const sortTasks: Task[] = [
      { id: 1, title: 'Late', status: TaskStatus.TODO, dueDate: '2026-12-01' },
      { id: 2, title: 'Early', status: TaskStatus.TODO, dueDate: '2026-01-01' },
    ]
    mockedGetTasks.mockResolvedValueOnce(sortTasks)

    render(<TaskList />)
    await screen.findByText('Late')

    await user.selectOptions(screen.getByLabelText('Sort by'), 'dueDate')
    const todoColumnHeading = screen.getByRole('heading', { name: /To Do/ })
    const todoColumn = todoColumnHeading.closest('div[class*="rounded-xl"]')
    const todoCards = within(todoColumn as HTMLElement).getAllByRole('heading', { level: 3 })
    expect(todoCards[1]).toHaveTextContent('Early')
  })
})
