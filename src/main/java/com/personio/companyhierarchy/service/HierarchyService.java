package com.personio.companyhierarchy.service;

import com.personio.companyhierarchy.entity.Employee;
import com.personio.companyhierarchy.exception.ApiExceptions;
import org.json.JSONObject;

public interface HierarchyService {
    public JSONObject saveHierarchy(String hierarchy) throws ApiExceptions;
    public JSONObject searchForSupervisors(Employee employee) throws ApiExceptions;
}
