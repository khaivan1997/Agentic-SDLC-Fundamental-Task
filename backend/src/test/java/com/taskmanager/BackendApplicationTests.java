package com.taskmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TaskRepository taskRepository;

	@BeforeEach
	void resetData() {
		taskRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createTask_validPayload_returnsCreatedTask() throws Exception {
		Task payload = new Task("Write tests", "Create integration tests", TaskStatus.TODO, LocalDate.of(2026, 2, 28));

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(payload)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.title").value("Write tests"))
			.andExpect(jsonPath("$.status").value("TODO"));
	}

	@Test
	void createTask_withoutStatus_defaultsToTodo() throws Exception {
		String payload = """
			{
			  "title": "Task without status",
			  "description": "status should default"
			}
			""";

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("TODO"));
	}

	@Test
	void createTask_missingTitle_returnsBadRequest() throws Exception {
		String payload = """
			{
			  "title": "",
			  "status": "TODO"
			}
			""";

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.errors.title").exists());
	}

	@Test
	void createTask_titleTooLong_returnsBadRequest() throws Exception {
		String payload = """
			{
			  "title": "%s",
			  "status": "TODO"
			}
			""".formatted("A".repeat(101));

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.title").exists());
	}

	@Test
	void createTask_descriptionTooLong_returnsBadRequest() throws Exception {
		String payload = """
			{
			  "title": "Valid title",
			  "description": "%s",
			  "status": "TODO"
			}
			""".formatted("D".repeat(501));

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.description").exists());
	}

	@Test
	void getAllTasks_returnsSavedTasks() throws Exception {
		taskRepository.save(new Task("Task A", "First", TaskStatus.TODO, null));
		taskRepository.save(new Task("Task B", "Second", TaskStatus.IN_PROGRESS, null));

		mockMvc.perform(get("/api/tasks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void getTaskById_existing_returnsTask() throws Exception {
		Task task = taskRepository.save(new Task("Find me", "lookup", TaskStatus.TODO, null));

		mockMvc.perform(get("/api/tasks/{id}", task.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(task.getId()))
			.andExpect(jsonPath("$.title").value("Find me"));
	}

	@Test
	void getTaskById_missing_returnsNotFound() throws Exception {
		mockMvc.perform(get("/api/tasks/{id}", 999999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void updateTask_updatesFields() throws Exception {
		Task task = taskRepository.save(new Task("Old title", "Old desc", TaskStatus.TODO, null));
		Task updatePayload = new Task("New title", "New desc", TaskStatus.DONE, LocalDate.of(2026, 3, 1));

		mockMvc.perform(put("/api/tasks/{id}", task.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatePayload)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("New title"))
			.andExpect(jsonPath("$.status").value("DONE"));
	}

	@Test
	void updateTask_missing_returnsNotFound() throws Exception {
		Task updatePayload = new Task("New title", "New desc", TaskStatus.DONE, LocalDate.of(2026, 3, 1));

		mockMvc.perform(put("/api/tasks/{id}", 999999)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatePayload)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void deleteTask_thenGetById_returnsNotFound() throws Exception {
		Task task = taskRepository.save(new Task("Delete me", "temporary", TaskStatus.TODO, null));

		mockMvc.perform(delete("/api/tasks/{id}", task.getId()))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/tasks/{id}", task.getId()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void deleteTask_missing_returnsNotFound() throws Exception {
		mockMvc.perform(delete("/api/tasks/{id}", 999999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void cors_preflight_allowsFrontendOrigin() throws Exception {
		mockMvc.perform(options("/api/tasks")
				.header("Origin", "http://localhost:5173")
				.header("Access-Control-Request-Method", "GET"))
			.andExpect(status().isOk())
			.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
	}
}
