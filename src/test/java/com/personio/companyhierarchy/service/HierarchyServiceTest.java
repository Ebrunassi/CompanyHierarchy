package com.personio.companyhierarchy.service;

import com.google.gson.Gson;
import com.personio.companyhierarchy.dto.EmployeeDTO;
import com.personio.companyhierarchy.entity.Employee;
import com.personio.companyhierarchy.exception.ApiExceptions;
import com.personio.companyhierarchy.exception.ErrorConstants;
import com.personio.companyhierarchy.repository.EmployeeRepository;
import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        Mockito.when(employeeRepository.save(Mockito.any(Employee.class)))          // Simple save method
                .thenReturn(root);
        Mockito.when(employeeRepository.findByName(Mockito.anyString()))            // Return false when try to find by name
                .thenReturn(Optional.ofNullable(null));

        JSONObject companyHierarchy = hierarchyService.saveHierarchy(body);         // Execution

        assertThat(root.getEmployeeId()).isNotNull();       // ID of register must not be null after saved in database
        assertThat(root.getManagerId()).isNull();           // The boss must not have a manager
        assertThat(companyHierarchy).isNotNull();           // The returned object is not null

        String json = companyHierarchy.toString();

        assertThat(json).contains("Jonas");                 // Exists the name "Jonas" in the returned hierarchy
        assertThat(json).contains("Sophie");                // Exists the name "Sophie" in the returned hierarchy
        assertThat(json).contains("Nick");                  // Exists the name "Nick" in the returned hierarchy
        assertThat(json).contains("Barbara");               // Exists the name "Barbara" in the returned hierarchy

        verify(employeeRepository,times(4)).findByName(Mockito.anyString());        // Due to recursion, this method must be called 4 times
        verify(employeeRepository,times(4)).save(Mockito.any(Employee.class));      // Due to recursion, this method must be called 4 times
    }

    @Test
    @DisplayName("Must throw an error due to more than one boss")
    public void throwMoreThanOneBossError(){
        String body = "{\t \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Nick\":\"Barbara\",\n" +
                "\t\"Barbara\":\"Jonas\",\t\n" +
                "\t\"John\":\"Paul\"\t\n" +
                "}";
        Employee root = new Employee(1L,"Jonas", null);

        Mockito.when(employeeRepository.save(Mockito.any(Employee.class)))
                .thenReturn(root);
        Mockito.when(employeeRepository.findByName("Jonas"))
                .thenReturn(Optional.ofNullable(null));

        Throwable exception = Assertions.catchThrowable(() -> hierarchyService.saveHierarchy(body));        // Catch the exception

        // Check the exception
        assertThat(exception)                           // Check the instance and the message of thrown exception
                .isInstanceOf(ApiExceptions.class)
                .hasMessage("There is more than one boss in this hierarchy");

        Mockito.verify(employeeRepository, Mockito.never()).findByName(Mockito.anyString());                // Verify if the method 'save' wasn't called
        Mockito.verify(employeeRepository, Mockito.never()).save(Mockito.any(Employee.class));              // Verify if the method 'save' wasn't called
    }

    @Test
    @DisplayName("Must throw an error due to having no boss in the hierarchy")
    public void throwNoBossError(){
        String body = "{\t \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Nick\":\"Barbara\",\n" +
                "\t\"Barbara\":\"Jonas\",\t\n" +
                "\t\"Jonas\":\"Nick\"\t\n" +
                "}";
        Employee root = new Employee(1L,"Jonas", null);

        Mockito.when(employeeRepository.save(Mockito.any(Employee.class)))
                .thenReturn(root);
        Mockito.when(employeeRepository.findByName("Jonas"))
                .thenReturn(Optional.ofNullable(null));

        Throwable exception = Assertions.catchThrowable(() -> hierarchyService.saveHierarchy(body));        // Catch the exception

        // Check the exception
        assertThat(exception)                           // Check the instance and the message of thrown exception
                .isInstanceOf(ApiExceptions.class)
                .hasMessage("There is no boss in this hierarchy");

        Mockito.verify(employeeRepository, Mockito.never()).findByName(Mockito.anyString());                // Verify if the method 'save' wasn't called
        Mockito.verify(employeeRepository, Mockito.never()).save(Mockito.any(Employee.class));              // Verify if the method 'save' wasn't called
    }

    @Test
    @DisplayName("Must throw an error due to having more than one supervisor")
    public void throwMoreThanOneSupervisorError(){
        String body = "{\t \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Nick\":\"Barbara\",\n" +
                "\t\"Barbara\":\"Jonas\",\t\n" +
                "\t\"Barbara\":\"Sophie\"\t\n" +
                "}";
        Employee root = new Employee(1L,"Jonas", null);

        Mockito.when(employeeRepository.save(Mockito.any(Employee.class)))
                .thenReturn(root);
        Mockito.when(employeeRepository.findByName("Jonas"))
                .thenReturn(Optional.ofNullable(null));

        Throwable exception = Assertions.catchThrowable(() -> hierarchyService.saveHierarchy(body));        // Catch the exception

        // Check the exception
        assertThat(exception)                           // Check the instance and the message of thrown exception
                .isInstanceOf(ApiExceptions.class)
                .hasMessage("The employee 'Barbara' must not have more than one supervisor.");

        Mockito.verify(employeeRepository, Mockito.never()).findByName(Mockito.anyString());                // Verify if the method 'save' wasn't called
        Mockito.verify(employeeRepository, Mockito.never()).save(Mockito.any(Employee.class));              // Verify if the method 'save' wasn't called
    }

    @Test
    @DisplayName("Must throw an error due to having more than one supervisor")
    public void throwDirectRelationLoopError(){
        String body = "{\t \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Nick\":\"Barbara\",\n" +
                "\t\"Barbara\":\"Nick\"\t\n" +
                "}";
        Employee root = new Employee(1L,"Jonas", null);

        Mockito.when(employeeRepository.save(Mockito.any(Employee.class)))
                .thenReturn(root);
        Mockito.when(employeeRepository.findByName("Jonas"))
                .thenReturn(Optional.ofNullable(null));

        Throwable exception = Assertions.catchThrowable(() -> hierarchyService.saveHierarchy(body));        // Catch the exception

        // Check the exception
        assertThat(exception)                           // Check the instance and the message of thrown exception
                .isInstanceOf(ApiExceptions.class)
                .hasMessage("There is a loop relation between employees 'Nick' and 'Barbara'");

        Mockito.verify(employeeRepository, Mockito.never()).findByName(Mockito.anyString());                // Verify if the method 'save' wasn't called
        Mockito.verify(employeeRepository, Mockito.never()).save(Mockito.any(Employee.class));              // Verify if the method 'save' wasn't called
    }

    @Test
    @DisplayName("Must return the supervisor and supervisor's supervisor of given employee name")
    public void searchForEmployeeSupervisorAndSuperior(){
        Employee employee = new Employee(null,"Barbara",null);
        Employee boss1 = new Employee(1L,"Sophie",null);
        Employee boss2 = new Employee(2L,"Nick",1L);
        Employee currentEmployee = new Employee(3L,"Barbara",2L);

        List<Employee> hierarchy = Arrays.asList(boss1,boss2,currentEmployee);

        Mockito.when(employeeRepository.findSupervisorAndSupervisorsSupervisorFromGivenName(employee.getName()))
                .thenReturn(hierarchy);

        JSONObject companyHierarchy = hierarchyService.searchForSupervisors(employee);         // Execution

        assertThat(companyHierarchy).isNotNull();           // The returned object is not null
        String json = companyHierarchy.toString();

        assertThat(json).doesNotContain("Jonas");           // "Jonas" is the supervisor of "Sophie", he must not be shown
        assertThat(json).contains("Sophie");                // Exists the name "Sophie" in the returned hierarchy
        assertThat(json).contains("Nick");                  // Exists the name "Nick" in the returned hierarchy
        assertThat(json).contains("Barbara");               // Exists the name "Barbara" in the returned hierarchy
    }

    @Test
    @DisplayName("Must return an empty json due to not found the name in the database")
    public void searchForInexistingName(){
        Employee employee = new Employee(null,"Barbara",null);
        Mockito.when(employeeRepository.findSupervisorAndSupervisorsSupervisorFromGivenName(Mockito.anyString()))
                .thenReturn(new ArrayList<Employee>());     // Return an empty list

        JSONObject companyHierarchy = hierarchyService.searchForSupervisors(employee);         // Execution
        String json = companyHierarchy.toString();

        assertThat(companyHierarchy).isNotNull();           // The returned object is not null
        assertThat(json).contains("{}");                    // Will return an empty json
    }
}
