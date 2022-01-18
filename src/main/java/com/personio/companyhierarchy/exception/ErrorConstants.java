package com.personio.companyhierarchy.exception;

public class ErrorConstants extends RuntimeException{
    public static final ApiExceptions MORE_THAN_ONE_BOSS = new ApiExceptions("There is more than one boss in this hierarchy");
    public static final ApiExceptions NO_BOSS = new ApiExceptions("There is no boss in this hierarchy");
    public static final ApiExceptions MULTIPLE_SUPERVISORS(String errorMessage){
        String employeeName = null;
        employeeName = errorMessage.substring(
                errorMessage.indexOf("\"")+1,
                errorMessage.lastIndexOf("\"")
        );
        return new ApiExceptions("The employee '" + employeeName + "' must not have more than one supervisor.");
    }
}
