package com.employee.leavemanagement.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

	public enum LeaveType { ANNUAL, SICK, PERSONAL }
	public enum Status { PENDING, APPROVED, REJECTED }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LeaveType leaveType;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false, length = 1000)
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.PENDING;

	@Column(nullable = false)
	private int daysRequested;

	@Column(length = 500)
	private String reviewNote;

	@Column(nullable = false)
	private Instant createdAt;

	private Instant reviewedAt;

	protected LeaveRequest() { }

	public LeaveRequest(Employee employee, LeaveType leaveType, LocalDate startDate, LocalDate endDate,
			String reason, int daysRequested) {
		this.employee = employee;
		this.leaveType = leaveType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.reason = reason;
		this.daysRequested = daysRequested;
	}

	@PrePersist
	void setCreatedAt() {
		if (createdAt == null) createdAt = Instant.now();
	}

	public Long getId() { return id; }
	public Employee getEmployee() { return employee; }
	public LeaveType getLeaveType() { return leaveType; }
	public LocalDate getStartDate() { return startDate; }
	public LocalDate getEndDate() { return endDate; }
	public String getReason() { return reason; }
	public Status getStatus() { return status; }
	public int getDaysRequested() { return daysRequested; }
	public String getReviewNote() { return reviewNote; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getReviewedAt() { return reviewedAt; }

	public void review(Status status, String reviewNote) {
		this.status = status;
		this.reviewNote = reviewNote;
		this.reviewedAt = Instant.now();
	}
}