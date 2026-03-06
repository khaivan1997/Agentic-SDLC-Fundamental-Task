package com.taskmanager.mcp.dto;

import com.taskmanager.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class TaskInput {

    @NotBlank(message = "Title is required")
    @Size(max = Task.TITLE_MAX_LENGTH, message = "Title must not exceed " + Task.TITLE_MAX_LENGTH + " characters")
    private String title;

    @Size(max = Task.DESCRIPTION_MAX_LENGTH, message = "Description must not exceed " + Task.DESCRIPTION_MAX_LENGTH
            + " characters")
    private String description;
    private String status;
    private LocalDate dueDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
