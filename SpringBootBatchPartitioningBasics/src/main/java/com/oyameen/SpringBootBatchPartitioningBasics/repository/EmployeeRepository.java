package com.oyameen.SpringBootBatchPartitioningBasics.repository;

import com.oyameen.SpringBootBatchPartitioningBasics.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
