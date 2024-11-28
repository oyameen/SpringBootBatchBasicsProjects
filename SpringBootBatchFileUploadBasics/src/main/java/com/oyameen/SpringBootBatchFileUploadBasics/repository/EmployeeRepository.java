package com.oyameen.SpringBootBatchFileUploadBasics.repository;

import com.oyameen.SpringBootBatchFileUploadBasics.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
