package com.personio.companyhierarchy.repository;

import com.personio.companyhierarchy.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest    // Will use the H2 memory database to execute the tests
public class EmployeeRepositoryTest {

    @Autowired
    TestEntityManager entityManager;
    @Autowired
    EmployeeRepository bookRepository;

    @Test
    @DisplayName("Save an employee successfully in database")
    public void saveEmployeeSuccessfully(){

        Employee employee = new Employee(null,"John",null);     // Scenario

        entityManager.persist(employee);                        // Save the data in h2 memory database
        Employee saved = bookRepository.save(employee);

        assertThat(saved.getEmployeeId()).isNotNull();          // Employee must not be null
    }

    @Test
    @DisplayName("Find an employee successfully by the name")
    public void findEmployeeByNameSuccessfully(){

        Employee employee = new Employee(null,"Paul", null);     // Scenario

        entityManager.persist(employee);                        // Save the data in h2 memory database
        Optional<Employee> founded = bookRepository.findByName("Paul");

        assertThat(founded.get()).isNotNull();                              // Employee must not be null
        assertThat(founded.get().getName()).isEqualToIgnoringCase("Paul");  // The name must be equal that the used to search
        assertThat(founded.get().getEmployeeId()).isNotNull();              // Employee id must not be null
    }

    @Test
    @DisplayName("Not found an employee by the name")
    public void notFoundEmployeeByName(){
        Optional<Employee> founded = bookRepository.findByName("Paul");
        assertThat(founded.isEmpty()).isTrue();                              // The option must not have an element inside of it
    }


}
