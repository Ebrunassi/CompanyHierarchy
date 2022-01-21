package com.personio.companyhierarchy.service;


import com.personio.companyhierarchy.dto.EmployeeDTO;
import com.personio.companyhierarchy.entity.Employee;
import com.personio.companyhierarchy.exception.ApiExceptions;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface HierarchyService {
    public JSONObject saveHierarchy(String hierarchy) throws ApiExceptions;
    public JSONObject searchForSupervisors(String name) throws ApiExceptions;
}
