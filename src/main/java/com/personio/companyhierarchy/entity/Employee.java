package com.personio.companyhierarchy.entity;

import com.personio.companyhierarchy.dto.EmployeeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;
    private String name;
    private Long managerId;

    public Employee(EmployeeDTO employeeDTO, Long managerId) {
        this.name = employeeDTO.getName();
        this.managerId = managerId;
    }
}
