package com.taskmanager.mcp.tools;

import com.taskmanager.mcp.dto.TaskInput;
import com.taskmanager.mcp.dto.TaskSummary;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class TaskMcpTools {

    private final TaskRepository taskRepository;

    public TaskMcpTools(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @McpTool(name = "mcp-help", description = "Returns available MCP tools and how to use them")
    public Map<String, Object> help() {
        Map<String, String> tools = new LinkedHashMap<>();
        tools.put("mcp-help", "Shows all available tools.");
        tools.put("mcp-schema-tasks", "Returns a JSON-style schema for the tasks table.");
        tools.put("mcp-tasks", "Accepts a JSON array of tasks and inserts them in bulk.");
        tools.put("mcp-tasks-summary", "Returns total task count and count by status.");

        return Map.of(
                "module", "mcp-server",
                "tools", tools
        );
    }

    @McpTool(name = "mcp-schema-tasks", description = "Returns the schema for the tasks table")
    public Map<String, Object> schemaTasks() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "integer", "readOnly", true));
        properties.put("title", Map.of("type", "string", "maxLength", Task.TITLE_MAX_LENGTH));
        properties.put("description", Map.of("type", "string", "maxLength", Task.DESCRIPTION_MAX_LENGTH, "nullable", true));
        properties.put("status", Map.of("type", "string", "enum", List.of("TODO", "IN_PROGRESS", "DONE")));
        properties.put("dueDate", Map.of("type", "string", "format", "date", "nullable", true));

        return Map.of(
                "table", "tasks",
                "type", "object",
                "required", List.of("title", "status"),
                "properties", properties
        );
    }

    @Transactional
    @McpTool(name = "mcp-tasks", description = "Bulk inserts tasks into PostgreSQL")
    public Map<String, Object> insertTasks(
            @McpToolParam(description = "A JSON array of task objects") List<TaskInput> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Map.of("inserted", 0, "rejected", 0, "message", "No tasks received");
        }

        List<Task> validTasks = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            TaskInput input = tasks.get(i);
            String validationError = validate(input);
            if (validationError != null) {
                errors.add("index " + i + ": " + validationError);
                continue;
            }

            Task task = new Task();
            task.setTitle(input.getTitle().trim());
            task.setDescription(normalizeDescription(input.getDescription()));
            task.setStatus(parseStatus(input.getStatus()));
            task.setDueDate(input.getDueDate());
            validTasks.add(task);
        }

        if (!validTasks.isEmpty()) {
            taskRepository.saveAll(validTasks);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("received", tasks.size());
        response.put("inserted", validTasks.size());
        response.put("rejected", errors.size());
        response.put("totalInDatabase", taskRepository.count());
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }
        return response;
    }

    @McpTool(name = "mcp-tasks-summary", description = "Returns count of tasks grouped by status")
    public TaskSummary tasksSummary() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put(TaskStatus.TODO.name(), taskRepository.countByStatus(TaskStatus.TODO));
        byStatus.put(TaskStatus.IN_PROGRESS.name(), taskRepository.countByStatus(TaskStatus.IN_PROGRESS));
        byStatus.put(TaskStatus.DONE.name(), taskRepository.countByStatus(TaskStatus.DONE));

        return new TaskSummary(taskRepository.count(), byStatus);
    }

    private String validate(TaskInput input) {
        if (input == null) {
            return "Task object is null";
        }
        if (input.getTitle() == null || input.getTitle().trim().isEmpty()) {
            return "title is required";
        }
        if (input.getTitle().trim().length() > Task.TITLE_MAX_LENGTH) {
            return "title exceeds " + Task.TITLE_MAX_LENGTH + " characters";
        }
        if (input.getDescription() != null && input.getDescription().length() > Task.DESCRIPTION_MAX_LENGTH) {
            return "description exceeds " + Task.DESCRIPTION_MAX_LENGTH + " characters";
        }
        try {
            parseStatus(input.getStatus());
        } catch (IllegalArgumentException ex) {
            return "status must be one of TODO, IN_PROGRESS, DONE";
        }
        return null;
    }

    private TaskStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return TaskStatus.TODO;
        }
        return TaskStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    }

    private String normalizeDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        return description.trim();
    }
}
