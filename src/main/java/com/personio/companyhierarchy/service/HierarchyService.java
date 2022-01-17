package com.personio.companyhierarchy.service;


import com.personio.companyhierarchy.dto.Employee;
import com.personio.companyhierarchy.exception.ApiExceptions;

public interface HierarchyService {
    public Employee saveHierarchy(String hierarchy) throws ApiExceptions;
}
