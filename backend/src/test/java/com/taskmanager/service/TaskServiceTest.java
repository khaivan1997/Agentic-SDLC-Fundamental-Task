package com.taskmanager.service;

import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
    }

    @Test
    void getAllTasks_returnsRepositoryResults() {
        Task first = new Task();
        first.setId(1L);
        first.setTitle("Task A");
        first.setStatus(TaskStatus.TODO);

        Task second = new Task();
        second.setId(2L);
        second.setTitle("Task B");
        second.setStatus(TaskStatus.DONE);

        when(taskRepository.findAll()).thenReturn(List.of(first, second));

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(2, tasks.size());
        assertEquals("Task A", tasks.get(0).getTitle());
        assertEquals("Task B", tasks.get(1).getTitle());
    }

    @Test
    void getTaskById_missing_throwsNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(999L));
    }

    @Test
    void createTask_withoutStatus_defaultsToTodo() {
        Task input = new Task();
        input.setTitle("No status task");
        input.setStatus(null);

        when(taskRepository.save(input)).thenReturn(input);

        Task created = taskService.createTask(input);

        assertEquals(TaskStatus.TODO, created.getStatus());
        verify(taskRepository).save(input);
    }

    @Test
    void updateTask_withNullStatus_defaultsToTodoAndSaves() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Old");
        existing.setDescription("Old desc");
        existing.setStatus(TaskStatus.DONE);

        Task update = new Task();
        update.setTitle("New");
        update.setDescription("New desc");
        update.setStatus(null);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task result = taskService.updateTask(1L, update);

        assertEquals("New", result.getTitle());
        assertEquals("New desc", result.getDescription());
        assertEquals(TaskStatus.TODO, result.getStatus());
        verify(taskRepository).save(existing);
    }

    @Test
    void deleteTask_existing_deletesResolvedEntity() {
        Task existing = new Task();
        existing.setId(7L);
        existing.setTitle("Delete me");
        existing.setStatus(TaskStatus.TODO);

        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));

        taskService.deleteTask(7L);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).delete(captor.capture());
        assertEquals(7L, captor.getValue().getId());
    }
}
