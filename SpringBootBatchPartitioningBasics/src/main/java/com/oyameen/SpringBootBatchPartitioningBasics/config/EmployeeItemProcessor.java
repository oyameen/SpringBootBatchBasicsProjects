package com.oyameen.SpringBootBatchPartitioningBasics.config;

import com.oyameen.SpringBootBatchPartitioningBasics.model.Employee;
import org.springframework.batch.item.ItemProcessor;

public class EmployeeItemProcessor implements ItemProcessor<Employee, Employee> {
    @Override
    public Employee process(Employee employee) throws Exception {

        //here you can put conditions to filter the employees that should be persisted to db.
        return employee;
    }
}
