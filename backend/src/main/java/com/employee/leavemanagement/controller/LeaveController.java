package com.employee.leavemanagement.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.employee.leavemanagement.LeaveManagementService;
import com.employee.leavemanagement.model.LeaveRequest;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveController {

	private final LeaveManagementService leaveManagementService;

	public LeaveController(LeaveManagementService leaveManagementService) {
		this.leaveManagementService = leaveManagementService;
	}

	@GetMapping
	public List<LeaveRequest> getRequests(@RequestParam(required = false) LeaveRequest.Status status) {
		List<LeaveRequest> requests = leaveManagementService.getLeaveRequests();
		return status == null ? requests : requests.stream()
				.filter(request -> request.getStatus() == status).toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getRequest(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(leaveManagementService.getLeaveRequest(id));
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
		}
	}

	@PostMapping
	public ResponseEntity<?> submitRequest(@RequestBody LeaveRequestInput input) {
		try {
			LeaveRequest request = leaveManagementService.submitRequest(input.employeeId(), input.leaveType(),
					input.startDate(), input.endDate(), input.reason());
			return ResponseEntity.status(HttpStatus.CREATED).body(request);
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
		}
	}

	@PostMapping("/{id}/decision")
	public ResponseEntity<?> reviewRequest(@PathVariable Long id, @RequestBody DecisionInput input) {
		try {
			return ResponseEntity.ok(leaveManagementService.reviewRequest(id, input.decision(), input.reviewNote()));
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
		}
	}

	public record LeaveRequestInput(Long employeeId, LeaveRequest.LeaveType leaveType,
			LocalDate startDate, LocalDate endDate, String reason) { }

	public record DecisionInput(String decision, String reviewNote) { }
}