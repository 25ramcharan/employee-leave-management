package com.employee.leavemanagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employee.leavemanagement.LeaveManagementService;
import com.employee.leavemanagement.model.Employee;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

	private final LeaveManagementService leaveManagementService;

	public EmployeeController(LeaveManagementService leaveManagementService) {
		this.leaveManagementService = leaveManagementService;
	}

	@GetMapping
	public List<Employee> getEmployees() {
		return leaveManagementService.getEmployees();
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getEmployee(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(leaveManagementService.getEmployee(id));
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
		}
	}

	@PostMapping
	public ResponseEntity<?> addEmployee(@RequestBody EmployeeInput input) {
		try {
			Employee employee = leaveManagementService.addEmployee(input.name(), input.email(), input.department(),
					input.annualLeaveBalance());
			return ResponseEntity.status(HttpStatus.CREATED).body(employee);
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
		}
	}

	public record EmployeeInput(String name, String email, String department, int annualLeaveBalance) { }
}