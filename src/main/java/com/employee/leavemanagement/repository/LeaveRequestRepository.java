package com.employee.leavemanagement.repository;

import java.util.List;
import java.util.Optional;

import com.employee.leavemanagement.model.LeaveRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
	@EntityGraph(attributePaths = "employee")
	List<LeaveRequest> findAllByOrderByCreatedAtDesc();

	@EntityGraph(attributePaths = "employee")
	Optional<LeaveRequest> findWithEmployeeById(Long id);

	List<LeaveRequest> findByEmployeeId(Long employeeId);
}