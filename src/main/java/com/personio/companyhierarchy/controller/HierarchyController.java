package com.personio.companyhierarchy.controller;

import com.personio.companyhierarchy.dto.EmployeeDTO;
import com.personio.companyhierarchy.entity.Employee;
import com.personio.companyhierarchy.exception.ApiErrors;
import com.personio.companyhierarchy.exception.ApiExceptions;
import com.personio.companyhierarchy.service.HierarchyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Tag(name = "Hierarchy Controller")
@RestController
@RequestMapping("/personio/hierarchy")
@EnableAutoConfiguration
public class HierarchyController {

    @Autowired
    HierarchyService hierarchyService;
    private ModelMapper modelMapper = new ModelMapper();
    Logger logger = LogManager.getLogger();

    @Operation(description = "Post method where you can submit a String in JSON format" +
            " and receive back a JSON containing the proposed company hierarchy")
    @ApiResponse(responseCode = "201", description = "OK")
    @PostMapping(produces = "application/json")
    public ResponseEntity<Map<String,Object>> getHierarchy(@RequestBody String body) throws ApiExceptions {
        logger.info("Creating a new hierarchy. Submitted structure: " + body);
        return new ResponseEntity<Map<String,Object>>(hierarchyService.saveHierarchy(body).toMap(),HttpStatus.CREATED);
    }

    @Operation(description = "Returns the supervisor and the supervisor's supervisor of a given employee")
    @ApiResponse(responseCode = "200")
    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String,Object> getSupervisors(@PathVariable(value = "name")
                                                 @Parameter(description = "Employee's name") String name){
        logger.info("Searching for supervisor and supervisor's supervisor of the employee '{}'", name);
        return hierarchyService.searchForSupervisors(name).toMap();
    }


    // Exception handlers
    @ExceptionHandler(ApiExceptions.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessException(ApiExceptions exception){
        return new ApiErrors(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException exception){
        BindingResult bindingResult = exception.getBindingResult();
        return new ApiErrors(bindingResult);
    }
}
