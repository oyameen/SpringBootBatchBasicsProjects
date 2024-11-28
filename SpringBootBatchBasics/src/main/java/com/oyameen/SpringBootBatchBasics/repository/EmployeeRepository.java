package com.oyameen.SpringBootBatchBasics.repository;

import com.oyameen.SpringBootBatchBasics.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
