package com.personio.companyhierarchy.service;

import com.google.gson.Gson;
import com.personio.companyhierarchy.dto.EmployeeDTO;
import com.personio.companyhierarchy.entity.Employee;
import com.personio.companyhierarchy.exception.ApiExceptions;
import com.personio.companyhierarchy.exception.ErrorConstants;
import com.personio.companyhierarchy.repository.EmployeeRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HierarchyServiceImpl implements HierarchyService{

    Logger logger = LogManager.getLogger();
    Gson gson = new Gson();
    private JSONObject json = new JSONObject();
    private static int treeHeight = 0;

    @Autowired
    private EmployeeRepository employeeRepository;

    public HierarchyServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     *
     * This function receives the request, convert it into a HashMap and throw an error if there is
     * a loop or more than one boss in the hierarchy
     *
     * @param employees: The Map structure which will contain the relations
     * @param hierarchy: resquest's body
     * @return - will return the EmployeeDTO tree structure only with boss present
     * @throws ApiExceptions
     */
    private EmployeeDTO buildMapStructure(Map<String, String> employees, String hierarchy) throws ApiExceptions {
        try{
            JSONObject jsonObject = new JSONObject(hierarchy.trim());
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {                        // Iterating throw the json and adding each relation in a HashMap and finding the boss
                String key = keys.next();
                employees.put(key, String.valueOf(jsonObject.get(key)));
            }

            logger.info("Finding the boss of the structure...");        // Finding the most high-level boss
            List<String> bossName = new ArrayList<String>();
            for (Map.Entry<String, String> entry : employees.entrySet()) {
                if (!employees.containsKey(entry.getValue())) {                         // The name must not exists in keys
                    if((bossName.size() >= 1 &&                                         // If already exists elements in the list, check if its duplicated
                                !bossName.get(0).equalsIgnoreCase(entry.getValue()))
                            || bossName.isEmpty()) {       // Duplicate case
                        bossName.add(entry.getValue());
                        logger.info("Boss founded: " + entry.getValue());
                    }
                }else{                                      // Checking loop
                    if(entry.getKey().equalsIgnoreCase(employees.get(entry.getValue()))){
                        logger.error("There is a loop relation between '{}' and '{}'", entry.getValue(), employees.get(entry.getValue()));
                        throw ErrorConstants.LOOP_SUPERVISORS(entry.getValue(), employees.get(entry.getValue()));
                    }
                }
            }
            if (bossName.size() == 1) {                    // There is only one boss, he/she is the root of the tree
                logger.info("HashMap containing the relation has been created. Number of elements: {}", employees.size());
                return EmployeeDTO.builder()
                            .name(bossName.get(0))
                            .subordinates(new ArrayList<EmployeeDTO>())
                        .build();
            }
            else if (bossName.size() > 1){
                logger.error("There are more than one boss found in this structure.");
                throw ErrorConstants.MORE_THAN_ONE_BOSS;
            }
            else {
                logger.error("There is no boss in this structure.");
                throw ErrorConstants.NO_BOSS;
            }

        }catch (JSONException jsonException){
            if(jsonException.getMessage().contains("Duplicate key")) {
                throw ErrorConstants.MULTIPLE_SUPERVISORS(jsonException.getMessage());
            }
        }
        return null;
    }

    /**
     * This function uses the Hash structure to build a tree with the employees and their subordinates
     *
     * @param employees: Map structure containing all the relations between the employees
     * @param employeeDTO: The employee that will be putted on tree
     */
    private void buildEmployeeTree(Map<String, String> employees, EmployeeDTO employeeDTO){
        treeHeight++;
        employeeDTO.getSubordinates().addAll(getSubordinates(employees, employeeDTO.getName()));
        if(employeeDTO.getSubordinates().isEmpty())
            return;
        for(EmployeeDTO emp : employeeDTO.getSubordinates()) {
            buildEmployeeTree(employees, emp);                         // Reach the bottom of the tree
        }
    }

    /**
     * Auxiliar recursive function used to add the subordinates
     * @param employees: Map structure containing all the relations between the employees
     * @param name: the name of the subordinate
     * @return
     */
    private List<EmployeeDTO> getSubordinates(Map<String, String> employees, String name) {
        List<EmployeeDTO> subordinates = new ArrayList<EmployeeDTO>();
        for(Map.Entry<String,String> entry : employees.entrySet()){
            if(entry.getValue().equalsIgnoreCase(name)){          // Check if it is a subordinate of the current element
                subordinates.add(new EmployeeDTO(entry.getKey(), new ArrayList<EmployeeDTO>()));
            }
        }
        return subordinates;
    }

    /**
     * This function process the hierarchy in order to be shown as the requested way
     * @param employeeDTO: the root of the tree
     * @return
     */
    private JSONObject printEmployeeTree(EmployeeDTO employeeDTO) {
        JSONObject obj = new JSONObject();

        for(EmployeeDTO emp : employeeDTO.getSubordinates()){       // Iterate through their subordinates
            JSONObject o = printEmployeeTree(emp);
            if(!obj.isEmpty()) {
                Iterator<String> keys = o.keys();
                while(keys.hasNext()){
                    String key = keys.next();
                    obj.put(key, o.get(key));                       // Put in JSONObject: {"employee_name":"their_subordinate"}
                }
            }else
                obj = o;
        }
        return new JSONObject().put(employeeDTO.getName(),obj);     // Reach the bottom of the tree
    }

    /**
     * Recursive funcion used to save in database the whole hierarchy
     * @param employeeDTOTree: root of the three
     * @param isRoot: flag to know if the current element is the root of the tree or not
     * @param idManager: the id of the current employee's supervisor.
     */
    private void saveDatabase(EmployeeDTO employeeDTOTree, Boolean isRoot, Long idManager) {

        Employee empSaved;
        if(isRoot) {
            Employee emp = new Employee(employeeDTOTree, null);
            Optional<Employee> employee = employeeRepository.findByName(emp.getName());
            if(employee.isPresent()){                       // If the register already exists, just update the fields values
                emp = employee.get();
                emp.setManagerId(null);
            }
            empSaved = employeeRepository.save(emp);        // Save the root and return the saved employee
            logger.info("Saved into database: " + gson.toJson(empSaved));

            idManager = empSaved.getEmployeeId();           // Save the id of current saved element to set it in the id_manager of it's subordinates
        }

        for(EmployeeDTO employeeDTO : employeeDTOTree.getSubordinates()){       // Iterate through all the subordinates
            Employee employee = new Employee(employeeDTO, idManager);
            Optional<Employee> emp = employeeRepository.findByName(employee.getName());
            if(emp.isPresent()){
                employee = emp.get();
                employee.setManagerId(idManager);           // Use the id of it's supervisor to set in the manager_id
            }
            empSaved = employeeRepository.save(employee);   // Save the root and return the saved employee
            logger.info("Saved into table 'employee': " + gson.toJson(empSaved));
            saveDatabase(employeeDTO, false, empSaved.getEmployeeId());     // Recursion giving the id to their subordinates saves in id_manager
        }
    }

    /**
     * This function is used to iterate through the list of supervisors found in database
     * and create a tree with that list in order to be returned to client
     * @param i: iteration index
     * @param list: list containing the employee rescued from database
     * @return
     */
    private EmployeeDTO addSubordinates(int i, List<Employee> list){
        if(i < list.size()){
            EmployeeDTO emp = new EmployeeDTO(list.get(i));     // Get the i position of the list
            EmployeeDTO add = addSubordinates(++i,list);        // Add it as subordinate of previously employee
            if(add != null)
                emp.getSubordinates().add(add);
            return emp;
        }
        return null;
    }

    @Override
    public JSONObject saveHierarchy(String hierarchy) throws ApiExceptions {

        Map<String,String> employees = new HashMap<>();         // The map structure that will contain the hierarchy relation

        EmployeeDTO employeeDTOTree = buildMapStructure(employees, hierarchy);     // Convert the request to a HashMap and throw errors if exists
        logger.info("Converting the structure into a tree...");
        buildEmployeeTree(employees, employeeDTOTree);                             // Build a tree based on the HashMap
        if(treeHeight-1 != employees.size()) {                  // Removing the root in the count
            logger.error("There is a loop in the hierarchy.");
            treeHeight = 0;
            throw ErrorConstants.LOOP_SUPERVISORS;
        }
        logger.info("Strucutre tree: " + gson.toJson(employeeDTOTree));

        logger.info("Saving the structure into database...");
        saveDatabase(employeeDTOTree, true, null);                  // Save the relations into the database

        treeHeight = 0;
        JSONObject displayTree = printEmployeeTree(employeeDTOTree);                // Contains the tree ready to be returned as response
        logger.info("Returning response: " + displayTree.toString());
        return displayTree;
    }


    @Override
    public JSONObject searchForSupervisors(Employee employeeDTO) throws ApiExceptions {
        // Search the supervisor and supervisor's supervisor by the employee name
        List<Employee> list = employeeRepository.findSupervisorAndSupervisorsSupervisorFromGivenName(employeeDTO.getName());
        logger.info("It was found {} employees related to '{}'", list.size(), employeeDTO.getName());
        EmployeeDTO tree = new EmployeeDTO();
        if(!list.isEmpty()) {
            tree = addSubordinates(0, list);
            logger.info("Supervisors of '{}' found in database: {}", employeeDTO.getName(), gson.toJson(tree));
        }else
            return new JSONObject("{}");
        JSONObject displayTree = printEmployeeTree(tree);           // Convert the tree structure to the requested response
        logger.info("Returning response: " + displayTree.toString());
        return displayTree;
    }

}
