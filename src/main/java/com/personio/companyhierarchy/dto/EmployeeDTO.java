package com.personio.companyhierarchy.dto;

import com.personio.companyhierarchy.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    @NotNull(message = "The name of the employee must not be null")
    private String name;
    private List<EmployeeDTO> subordinates;

    public EmployeeDTO(Employee employee){
        this.name = employee.getName();
        this.subordinates = new ArrayList<>();
    }
}
