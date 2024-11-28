package com.oyameen.SpringBootBatchPartitioningBasics.config;

import com.oyameen.SpringBootBatchPartitioningBasics.model.Employee;
import com.oyameen.SpringBootBatchPartitioningBasics.repository.EmployeeRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmployeeItemWriter implements ItemWriter<Employee> {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void write(Chunk<? extends Employee> chunk) throws Exception {

        System.out.println("Used Thread name = " + Thread.currentThread().getName());
        employeeRepository.saveAll(chunk);

    }
}
