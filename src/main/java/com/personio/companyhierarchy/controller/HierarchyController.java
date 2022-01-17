package com.personio.companyhierarchy.controller;

import com.personio.companyhierarchy.dto.Employee;
import com.personio.companyhierarchy.exception.ApiErrors;
import com.personio.companyhierarchy.exception.ApiExceptions;
import com.personio.companyhierarchy.service.HierarchyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;

@Tag(name = "Hierarchy Controller")
@RestController
@RequestMapping("/personio/hierarchy")
@EnableAutoConfiguration
public class HierarchyController {

    @Autowired
    HierarchyService hierarchyService;

    @Operation(description = "Returns the hierarchy")
    @ApiResponse(responseCode = "201", description = "OK")
    @PostMapping(produces = "application/json")
    public Employee getHierarchy(@RequestBody String body) throws ApiExceptions {
        return hierarchyService.saveHierarchy(body);
    }

    @ExceptionHandler(ApiExceptions.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessException(ApiExceptions exception){
        return new ApiErrors(exception);
    }

}
