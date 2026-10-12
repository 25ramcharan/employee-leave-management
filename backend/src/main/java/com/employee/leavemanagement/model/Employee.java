package com.employee.leavemanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String department;

	@Column(nullable = false)
	private int annualLeaveBalance;

	protected Employee() {
	}

	public Employee(String name, String email, String department, int annualLeaveBalance) {
		this.name = name;
		this.email = email;
		this.department = department;
		this.annualLeaveBalance = annualLeaveBalance;
	}

	public Long getId() { return id; }
	public String getName() { return name; }
	public String getEmail() { return email; }
	public String getDepartment() { return department; }
	public int getAnnualLeaveBalance() { return annualLeaveBalance; }
	public void setAnnualLeaveBalance(int annualLeaveBalance) { this.annualLeaveBalance = annualLeaveBalance; }
}