package com.personio.companyhierarchy.service;

import com.personio.companyhierarchy.entity.Employee;
import com.personio.companyhierarchy.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class HierarchyServiceTest {

    HierarchyService hierarchyService;
    @MockBean
    EmployeeRepository employeeRepository;

    @BeforeEach
    public void setUp(){
        this.hierarchyService = new HierarchyServiceImpl(employeeRepository);
    }

    @Test
    @DisplayName("Must generate a valid hierarchy")
    public void generatingHierarchyTest(){
        String body = "{\t \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Nick\":\"Barbara\",\n" +
                "\t\"Barbara\":\"Jonas\"\t\n" +
                "}";
        Employee root = new Employee(1L,"Jonas", null);

        Mockito.when(employeeRepository.save(Mockito.any(Employee.class)))
                .thenReturn(root);
        Mockito.when(employeeRepository.findByName("Jonas"))
                .thenReturn(Optional.ofNullable(new Employee()));

        hierarchyService.saveHierarchy(body);
        assertThat(root.getEmployeeId()).isNotNull();

        verify(employeeRepository,times(4)).save(Mockito.any(Employee.class));

    }
}
