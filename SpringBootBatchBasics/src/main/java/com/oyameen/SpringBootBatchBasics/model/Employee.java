package com.oyameen.SpringBootBatchBasics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "EMPLOYEE_INFO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @Id
    private int id;
    private String firstName;
    private String lastName;
    private String country;
    private String phoneNo;
    private int salary;
    private int age;
    private String jobTitle;


}
