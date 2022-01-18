package com.personio.companyhierarchy.service;


import com.personio.companyhierarchy.dto.EmployeeDTO;
import com.personio.companyhierarchy.entity.Employee;
import com.personio.companyhierarchy.exception.ApiExceptions;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface HierarchyService {
    public JSONObject saveHierarchy(String hierarchy) throws ApiExceptions;
    public JSONObject searchForSupervisors(Employee employee) throws ApiExceptions;
}
