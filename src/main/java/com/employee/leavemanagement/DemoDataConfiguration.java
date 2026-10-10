package com.employee.leavemanagement;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.employee.leavemanagement.model.Employee;
import com.employee.leavemanagement.model.LeaveRequest;
import com.employee.leavemanagement.repository.EmployeeRepository;
import com.employee.leavemanagement.repository.LeaveRequestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataConfiguration {

	@Bean
	CommandLineRunner seedDemoData(EmployeeRepository employeeRepository,
			LeaveRequestRepository leaveRequestRepository) {
		return args -> {
			if (employeeRepository.count() != 0) {
				return;
			}
			Employee morgan = employeeRepository.save(new Employee("Morgan Lee", "morgan.lee@northstar.test",
					"Product", 18));
			employeeRepository.save(new Employee("Avery Chen", "avery.chen@northstar.test", "Engineering", 22));
			employeeRepository.save(new Employee("Jordan Patel", "jordan.patel@northstar.test", "People", 16));
			employeeRepository.save(new Employee("Taylor Brooks", "taylor.brooks@northstar.test", "Finance", 20));
			employeeRepository.save(new Employee("Riley Scott", "riley.scott@northstar.test", "Engineering", 18));
			employeeRepository.save(new Employee("Sam Nguyen", "sam.nguyen@northstar.test", "Product", 19));
			employeeRepository.save(new Employee("Priya Raman", "priya.raman@northstar.test", "People", 17));
			employeeRepository.save(new Employee("Leo Martinez", "leo.martinez@northstar.test", "Finance", 21));
			employeeRepository.save(new Employee("Chloe Kim", "chloe.kim@northstar.test", "Engineering", 24));
			employeeRepository.save(new Employee("Harper Davis", "harper.davis@northstar.test", "Marketing", 15));
			employeeRepository.save(new Employee("Ethan Foster", "ethan.foster@northstar.test", "Operations", 20));
			employeeRepository.save(new Employee("Sofia Alvarez", "sofia.alvarez@northstar.test", "Design", 23));
			employeeRepository.save(new Employee("Noah Bennett", "noah.bennett@northstar.test", "Sales", 18));
			employeeRepository.save(new Employee("Zoe Turner", "zoe.turner@northstar.test", "Support", 22));
			employeeRepository.save(new Employee("Daniel Brooks", "daniel.brooks@northstar.test", "Engineering", 16));
			employeeRepository.save(new Employee("Maya Singh", "maya.singh@northstar.test", "Product", 20));
			employeeRepository.save(new Employee("Lucas Reed", "lucas.reed@northstar.test", "Finance", 19));
			employeeRepository.save(new Employee("Emma Flores", "emma.flores@northstar.test", "People", 21));
			employeeRepository.save(new Employee("Lucas Young", "lucas.young@northstar.test", "Operations", 18));
			employeeRepository.save(new Employee("Isabella Cole", "isabella.cole@northstar.test", "Engineering", 24));
			employeeRepository.save(new Employee("Mason Hughes", "mason.hughes@northstar.test", "Support", 17));
			employeeRepository.save(new Employee("Olivia Clarke", "olivia.clarke@northstar.test", "Marketing", 20));
			employeeRepository.save(new Employee("Henry Price", "henry.price@northstar.test", "Sales", 19));
			employeeRepository.save(new Employee("Grace Ward", "grace.ward@northstar.test", "Design", 22));
			employeeRepository.save(new Employee("Ava Murphy", "ava.murphy@northstar.test", "Finance", 18));

			LocalDate startDate = nextWeekday(LocalDate.now().plusDays(4));
			LocalDate endDate = nextWeekday(startDate.plusDays(2));
			leaveRequestRepository.save(new LeaveRequest(morgan, LeaveRequest.LeaveType.ANNUAL,
					startDate, endDate, "A short break with family.", countWeekdays(startDate, endDate)));
		};
	}

	private LocalDate nextWeekday(LocalDate date) {
		while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			date = date.plusDays(1);
		}
		return date;
	}

	private int countWeekdays(LocalDate startDate, LocalDate endDate) {
		int days = 0;
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
				days++;
			}
		}
		return days;
	}
}