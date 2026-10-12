package com.employee.leavemanagement;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.leavemanagement.model.Employee;
import com.employee.leavemanagement.model.LeaveRequest;
import com.employee.leavemanagement.repository.EmployeeRepository;
import com.employee.leavemanagement.repository.LeaveRequestRepository;

@Service
public class LeaveManagementService {

	private final EmployeeRepository employeeRepository;
	private final LeaveRequestRepository leaveRequestRepository;

	public LeaveManagementService(EmployeeRepository employeeRepository,
			LeaveRequestRepository leaveRequestRepository) {
		this.employeeRepository = employeeRepository;
		this.leaveRequestRepository = leaveRequestRepository;
	}

	@Transactional(readOnly = true)
	public List<Employee> getEmployees() {
		return employeeRepository.findAllByOrderByNameAsc();
	}

	@Transactional(readOnly = true)
	public Employee getEmployee(Long employeeId) {
		return employeeRepository.findById(employeeId)
				.orElseThrow(() -> new IllegalArgumentException("Employee was not found."));
	}

	@Transactional(readOnly = true)
	public List<LeaveRequest> getLeaveRequests() {
		return leaveRequestRepository.findAllByOrderByCreatedAtDesc();
	}

	@Transactional(readOnly = true)
	public LeaveRequest getLeaveRequest(Long requestId) {
		return leaveRequestRepository.findWithEmployeeById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Leave request was not found."));
	}

	@Transactional
	public Employee addEmployee(String name, String email, String department, int annualLeaveBalance) {
		String cleanName = required(name, "Employee name is required.");
		String cleanEmail = required(email, "Email address is required.").toLowerCase(Locale.ROOT);
		String cleanDepartment = required(department, "Department is required.");
		if (!cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
			throw new IllegalArgumentException("Enter a valid email address.");
		}
		if (annualLeaveBalance < 0 || annualLeaveBalance > 60) {
			throw new IllegalArgumentException("Leave balance must be between 0 and 60 days.");
		}
		if (employeeRepository.findByEmailIgnoreCase(cleanEmail).isPresent()) {
			throw new IllegalArgumentException("An employee with this email already exists.");
		}
		return employeeRepository.save(new Employee(cleanName, cleanEmail, cleanDepartment, annualLeaveBalance));
	}

	@Transactional
	public LeaveRequest submitRequest(Long employeeId, LeaveRequest.LeaveType leaveType, LocalDate startDate,
			LocalDate endDate, String reason) {
		if (employeeId == null) {
			throw new IllegalArgumentException("Select an employee.");
		}
		if (leaveType == null) {
			throw new IllegalArgumentException("Select a leave type.");
		}
		if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
			throw new IllegalArgumentException("Choose a valid date range.");
		}
		String cleanReason = required(reason, "A reason is required.");
		if (cleanReason.length() > 1000) {
			throw new IllegalArgumentException("The reason must be 1000 characters or fewer.");
		}
		int daysRequested = countBusinessDays(startDate, endDate);
		if (daysRequested == 0) {
			throw new IllegalArgumentException("The selected range contains no working days.");
		}

		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new IllegalArgumentException("The selected employee was not found."));
		boolean overlaps = leaveRequestRepository.findByEmployeeId(employeeId).stream()
				.filter(request -> request.getStatus() != LeaveRequest.Status.REJECTED)
				.anyMatch(request -> !startDate.isAfter(request.getEndDate())
						&& !endDate.isBefore(request.getStartDate()));
		if (overlaps) {
			throw new IllegalArgumentException("This request overlaps another active request for this employee.");
		}
		return leaveRequestRepository.save(new LeaveRequest(employee, leaveType, startDate, endDate,
				cleanReason, daysRequested));
	}

	@Transactional
	public LeaveRequest reviewRequest(Long requestId, String decision, String reviewNote) {
		LeaveRequest request = leaveRequestRepository.findWithEmployeeById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Leave request was not found."));
		if (request.getStatus() != LeaveRequest.Status.PENDING) {
			throw new IllegalArgumentException("This leave request has already been reviewed.");
		}
		LeaveRequest.Status status;
		try {
			status = LeaveRequest.Status.valueOf(decision.trim().toUpperCase(Locale.ROOT));
		} catch (NullPointerException | IllegalArgumentException exception) {
			throw new IllegalArgumentException("Choose approve or reject.");
		}
		if (status != LeaveRequest.Status.APPROVED && status != LeaveRequest.Status.REJECTED) {
			throw new IllegalArgumentException("Choose approve or reject.");
		}
		String cleanNote = reviewNote == null ? "" : reviewNote.trim();
		if (cleanNote.length() > 500) {
			throw new IllegalArgumentException("The review note must be 500 characters or fewer.");
		}
		if (status == LeaveRequest.Status.APPROVED) {
			Employee employee = employeeRepository.findByIdForUpdate(request.getEmployee().getId())
					.orElseThrow(() -> new IllegalArgumentException("Employee was not found."));
			if (employee.getAnnualLeaveBalance() < request.getDaysRequested()) {
				throw new IllegalArgumentException("The employee does not have enough leave days remaining.");
			}
			employee.setAnnualLeaveBalance(employee.getAnnualLeaveBalance() - request.getDaysRequested());
		}
		request.review(status, cleanNote);
		return request;
	}

	private int countBusinessDays(LocalDate startDate, LocalDate endDate) {
		int days = 0;
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
				days++;
			}
		}
		return days;
	}

	private String required(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value.trim();
	}
}