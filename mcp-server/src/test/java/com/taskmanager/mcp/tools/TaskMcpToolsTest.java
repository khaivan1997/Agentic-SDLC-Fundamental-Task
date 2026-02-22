package com.taskmanager.mcp.tools;

import com.taskmanager.mcp.dto.TaskInput;
import com.taskmanager.mcp.dto.TaskSummary;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskMcpToolsTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new TaskMcpTools(taskRepository);
    }

    @Test
    void help_returnsAllToolDescriptions() {
        Map<String, Object> result = tools.help();

        @SuppressWarnings("unchecked")
        Map<String, String> toolMap = (Map<String, String>) result.get("tools");

        assertEquals("mcp-server", result.get("module"));
        assertTrue(toolMap.containsKey("mcp-help"));
        assertTrue(toolMap.containsKey("mcp-schema-tasks"));
        assertTrue(toolMap.containsKey("mcp-tasks"));
        assertTrue(toolMap.containsKey("mcp-tasks-summary"));
    }

    @Test
    void schemaTasks_returnsExpectedSchemaShape() {
        Map<String, Object> schema = tools.schemaTasks();

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> title = (Map<String, Object>) properties.get("title");
        @SuppressWarnings("unchecked")
        Map<String, Object> description = (Map<String, Object>) properties.get("description");
        @SuppressWarnings("unchecked")
        Map<String, Object> status = (Map<String, Object>) properties.get("status");

        assertEquals("tasks", schema.get("table"));
        assertTrue(properties.containsKey("id"));
        assertTrue(properties.containsKey("title"));
        assertTrue(properties.containsKey("description"));
        assertTrue(properties.containsKey("status"));
        assertTrue(properties.containsKey("dueDate"));
        assertEquals(Task.TITLE_MAX_LENGTH, title.get("maxLength"));
        assertEquals(Task.DESCRIPTION_MAX_LENGTH, description.get("maxLength"));
        assertEquals(List.of("TODO", "IN_PROGRESS", "DONE"), status.get("enum"));
    }

    @Test
    void insertTasks_withSingleTask_insertsAndReturnsCounts() {
        when(taskRepository.count()).thenReturn(1L);

        TaskInput input = new TaskInput();
        input.setTitle("Task 1");
        input.setDescription("Desc");
        input.setStatus("DONE");
        input.setDueDate(LocalDate.of(2026, 2, 22));

        Map<String, Object> result = tools.insertTasks(List.of(input));

        ArgumentCaptor<List<Task>> captor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).saveAll(captor.capture());
        List<Task> savedTasks = captor.getValue();

        assertEquals(1, savedTasks.size());
        assertEquals("Task 1", savedTasks.get(0).getTitle());
        assertEquals(TaskStatus.DONE, savedTasks.get(0).getStatus());
        assertEquals(1, result.get("received"));
        assertEquals(1, result.get("inserted"));
        assertEquals(0, result.get("rejected"));
    }

    @Test
    void insertTasks_withoutStatus_defaultsToTodo() {
        when(taskRepository.count()).thenReturn(1L);

        TaskInput input = new TaskInput();
        input.setTitle("No status");
        input.setDescription("Desc");

        tools.insertTasks(List.of(input));

        ArgumentCaptor<List<Task>> captor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).saveAll(captor.capture());
        Task savedTask = captor.getValue().get(0);
        assertEquals(TaskStatus.TODO, savedTask.getStatus());
    }

    @Test
    void insertTasks_withTenTasks_insertsAll() {
        when(taskRepository.count()).thenReturn(10L);

        List<TaskInput> inputs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TaskInput input = new TaskInput();
            input.setTitle("Task " + i);
            input.setDescription("Desc " + i);
            input.setStatus(i % 2 == 0 ? "TODO" : "IN_PROGRESS");
            inputs.add(input);
        }

        Map<String, Object> result = tools.insertTasks(inputs);

        ArgumentCaptor<List<Task>> captor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).saveAll(captor.capture());
        List<Task> savedTasks = captor.getValue();

        assertEquals(10, savedTasks.size());
        assertEquals(10, result.get("received"));
        assertEquals(10, result.get("inserted"));
        assertEquals(0, result.get("rejected"));
    }

    @Test
    void insertTasks_withInvalidTask_rejectsAndDoesNotSave() {
        when(taskRepository.count()).thenReturn(0L);

        TaskInput invalid = new TaskInput();
        invalid.setTitle("   ");
        invalid.setStatus("TODO");

        Map<String, Object> result = tools.insertTasks(List.of(invalid));

        verify(taskRepository, never()).saveAll(anyList());
        assertEquals(1, result.get("received"));
        assertEquals(0, result.get("inserted"));
        assertEquals(1, result.get("rejected"));
        assertTrue(result.containsKey("errors"));
    }

    @Test
    void tasksSummary_returnsCountsFromRepository() {
        when(taskRepository.count()).thenReturn(6L);
        when(taskRepository.countByStatus(TaskStatus.TODO)).thenReturn(2L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(3L);
        when(taskRepository.countByStatus(TaskStatus.DONE)).thenReturn(1L);

        TaskSummary summary = tools.tasksSummary();

        assertEquals(6L, summary.getTotal());
        assertEquals(2L, summary.getByStatus().get("TODO"));
        assertEquals(3L, summary.getByStatus().get("IN_PROGRESS"));
        assertEquals(1L, summary.getByStatus().get("DONE"));
    }

    @Test
    void tasksSummary_emptyDatabase_returnsZeroCounts() {
        when(taskRepository.count()).thenReturn(0L);
        when(taskRepository.countByStatus(TaskStatus.TODO)).thenReturn(0L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(0L);
        when(taskRepository.countByStatus(TaskStatus.DONE)).thenReturn(0L);

        TaskSummary summary = tools.tasksSummary();

        assertEquals(0L, summary.getTotal());
        assertEquals(0L, summary.getByStatus().get("TODO"));
        assertEquals(0L, summary.getByStatus().get("IN_PROGRESS"));
        assertEquals(0L, summary.getByStatus().get("DONE"));
    }
}
