package com.employee.leavemanagement;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:leave-management-test;DB_CLOSE_DELAY=-1")
class LeaveManagementApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void employeeCanSubmitAndManagerCanReviewLeave() throws Exception {
		String email = "leave-test-" + UUID.randomUUID() + "@example.test";
		MvcResult employeeResult = mockMvc.perform(post("/api/employees")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
						"name", "Casey Rivera", "email", email, "department", "Design",
						"annualLeaveBalance", 12))))
			.andExpect(status().isCreated())
			.andReturn();
		long employeeId = objectMapper.readTree(employeeResult.getResponse().getContentAsString())
				.get("id").asLong();

		LocalDate firstDay = nextWeekday(LocalDate.now().plusDays(14));
		LocalDate secondDay = nextWeekday(firstDay.plusDays(1));
		long approvedRequestId = submitRequest(employeeId, firstDay, secondDay);
		mockMvc.perform(post("/api/leave-requests/{id}/decision", approvedRequestId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
						"decision", "APPROVED", "reviewNote", "Enjoy your break."))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("APPROVED"))
			.andExpect(jsonPath("$.employee.name").value("Casey Rivera"));

		MvcResult balanceResult = mockMvc.perform(get("/api/employees/{id}", employeeId))
				.andExpect(status().isOk())
				.andReturn();
		int balanceAfterApproval = objectMapper.readTree(balanceResult.getResponse().getContentAsString())
				.get("annualLeaveBalance").asInt();
		org.junit.jupiter.api.Assertions.assertEquals(10, balanceAfterApproval);

		LocalDate rejectedDay = nextWeekday(secondDay.plusDays(4));
		long rejectedRequestId = submitRequest(employeeId, rejectedDay, rejectedDay);
		mockMvc.perform(post("/api/leave-requests/{id}/decision", rejectedRequestId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("decision", "REJECTED", "reviewNote", ""))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("REJECTED"));
		mockMvc.perform(get("/api/employees/{id}", employeeId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.annualLeaveBalance").value(10));
	}

	private long submitRequest(long employeeId, LocalDate startDate, LocalDate endDate) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/leave-requests")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
						"employeeId", employeeId, "leaveType", "ANNUAL",
						"startDate", startDate, "endDate", endDate, "reason", "Personal plans."))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("PENDING"))
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private LocalDate nextWeekday(LocalDate date) {
		while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			date = date.plusDays(1);
		}
		return date;
	}

}
