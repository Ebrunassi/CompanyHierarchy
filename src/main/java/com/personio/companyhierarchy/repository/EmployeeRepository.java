package com.personio.companyhierarchy.repository;

import com.personio.companyhierarchy.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("SELECT emp " +
            "FROM Employee emp " +
            "WHERE " +
            "emp.name = ?1 " +
            "OR emp.employeeId IN(" +
            "SELECT emp1.managerId FROM Employee emp1 " +
            "WHERE emp1.name = ?1 " +
            ") " +
            "OR emp.employeeId IN (SELECT emp2.managerId FROM Employee emp2 " +
            "WHERE emp2.employeeId IN (SELECT emp3.managerId FROM Employee emp3 WHERE emp3.name = ?1) )")
    List<Employee> findSupervisorAndSupervisorsSupervisorFromGivenName(String name);
    Optional<Employee> findByName(String name);
}
