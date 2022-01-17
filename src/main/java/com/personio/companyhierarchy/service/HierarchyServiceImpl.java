package com.personio.companyhierarchy.service;

import com.google.gson.Gson;
import com.personio.companyhierarchy.dto.Employee;
import com.personio.companyhierarchy.exception.ApiExceptions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HierarchyServiceImpl implements HierarchyService{

    private Map<String,String> employees = new HashMap<>();
    private Employee employeeTree;
    private JSONObject json = new JSONObject();


    private Employee buildMapStructure(String hierarchy) throws ApiExceptions {
        JSONObject jsonObject = new JSONObject(hierarchy.trim());
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {                        // Iterating throw the json and adding each relation in a HashMap and finding the boss
            String key = keys.next();
            employees.put(key, String.valueOf(jsonObject.get(key)));
        }

        // Finding the most high-level boss
        List<String> bossName = new ArrayList<String>();
        for (Map.Entry<String, String> entry : employees.entrySet()) {
            if (!employees.containsKey(entry.getValue())) {                         // The name must not exists in the keys
                if((bossName.size() >= 1 &&                                          // If already exists elements in the list, check if its duplicated
                            !bossName.get(0).equalsIgnoreCase(entry.getValue()))
                        || bossName.isEmpty()) {       // Duplicate case
                    bossName.add(entry.getValue());
                }

            }
        }

        if (bossName.size() == 1) {                    // There is only one boss, he/she is the root of the tree
//            employees.put(bossName.get(0), null);
            return Employee.builder()
                        .name(bossName.get(0))
                        .subordinates(new ArrayList<Employee>())
                    .build();
        }
        else
            throw new ApiExceptions("More than one boss");
    }
    private void buildEmployeeTree(Employee employee){
        employee.getSubordinates().addAll(getSubordinates(employee.getName()));
        if(employee.getSubordinates().isEmpty())
            return;
        for(Employee emp : employee.getSubordinates())
               buildEmployeeTree(emp);                         // Reach the bottom of the tree
    }

    private List<Employee> getSubordinates(String name) {
        List<Employee> subordinates = new ArrayList<Employee>();
        for(Map.Entry<String,String> entry : employees.entrySet()){
            if(entry.getValue().equalsIgnoreCase(name)){          // Is a subordinate of the current element
                subordinates.add(new Employee(entry.getKey(), new ArrayList<Employee>()));
            }
        }
        return subordinates;
    }

    private JSONObject printEmployeeTree(Employee employee) {
        JSONObject obj = new JSONObject();

        for(Employee emp : employee.getSubordinates()){
            JSONObject o = printEmployeeTree(emp);
            if(!obj.isEmpty()) {
                Iterator<String> keys = o.keys();
                while(keys.hasNext()){
                    String key = keys.next();
                    obj.put(key, o.get(key));
                }
            }else
                obj = o;
        }
        // Is in the bottom of tree
        return new JSONObject().put(employee.getName(),obj);


//        json.put(employee.getName(),);

    }

    @Override
    public Employee saveHierarchy(String hierarchy) throws ApiExceptions {

        employeeTree = buildMapStructure(hierarchy);
        buildEmployeeTree(employeeTree);
        System.out.println(printEmployeeTree(employeeTree));





        String jsonInString = new Gson().toJson(employeeTree);
        JSONObject mJSONObject = new JSONObject(jsonInString);
//        printJsonObject(mJSONObject);
//        for(Map.Entry<String,String> entry : employees.entrySet()){
//            System.out.println(entry.getKey() + " - " + entry.getValue());
//        }

        JSONObject obj = new JSONObject();
        JSONObject o = new JSONObject();
        o.put("new","te");
        obj.put("evandro","ste");
        obj.put("te","amo");
        obj.put("oi", o);
        System.out.println(obj.toString());

        return employeeTree;
    }

}
