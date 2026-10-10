package com.employee.leavemanagement.repository;

import java.util.List;
import java.util.Optional;

import com.employee.leavemanagement.model.Employee;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	List<Employee> findAllByOrderByNameAsc();
	Optional<Employee> findByEmailIgnoreCase(String email);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select employee from Employee employee where employee.id = :id")
	Optional<Employee> findByIdForUpdate(@Param("id") Long id);
}