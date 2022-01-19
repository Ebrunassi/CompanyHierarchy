package com.personio.companyhierarchy.service;

import com.personio.companyhierarchy.dto.EmployeeDTO;
import com.personio.companyhierarchy.entity.Employee;
import com.personio.companyhierarchy.exception.ApiExceptions;
import com.personio.companyhierarchy.exception.ErrorConstants;
import com.personio.companyhierarchy.repository.EmployeeRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HierarchyServiceImpl implements HierarchyService{

    private Map<String,String> employees = new HashMap<>();
    private EmployeeDTO employeeDTOTree;
    private JSONObject json = new JSONObject();
    private static int treeHeight = 0;

    @Autowired
    private EmployeeRepository employeeRepository;

    public HierarchyServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * This function receives the request, convert it into a HashMap and throw an error if there is
     * loop or more than one boss in the hierarchy
     * @param hierarchy: resquest's body
     * @return
     * @throws ApiExceptions
     */
    private EmployeeDTO buildMapStructure(String hierarchy) throws ApiExceptions {
        try{
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
                }else{                                      // Checking loop
                    if(entry.getKey().equalsIgnoreCase(employees.get(entry.getValue()))){
                        throw ErrorConstants.LOOP_SUPERVISORS(entry.getValue(), employees.get(entry.getValue()));
                    }
                }
            }
            if (bossName.size() == 1) {                    // There is only one boss, he/she is the root of the tree
                return EmployeeDTO.builder()
                            .name(bossName.get(0))
                            .subordinates(new ArrayList<EmployeeDTO>())
                        .build();
            }
            else if (bossName.size() > 1)
                throw ErrorConstants.MORE_THAN_ONE_BOSS;
            else
                throw ErrorConstants.NO_BOSS;

        }catch (JSONException jsonException){
            if(jsonException.getMessage().contains("Duplicate key"))
                throw ErrorConstants.MULTIPLE_SUPERVISORS(jsonException.getMessage());
        }
        return null;
    }

    /**
     * This function uses the Hash structure to build a tree with the employees and their subordinates
     * @param employeeDTO: The employee that will be putted on tree
     */
    private void buildEmployeeTree(EmployeeDTO employeeDTO){
        treeHeight++;
        employeeDTO.getSubordinates().addAll(getSubordinates(employeeDTO.getName()));
        if(employeeDTO.getSubordinates().isEmpty())
            return;
        for(EmployeeDTO emp : employeeDTO.getSubordinates()) {
            buildEmployeeTree(emp);                         // Reach the bottom of the tree
        }
    }

    /**
     * Auxiliar recursive function used to add the subordinates
     * @param name: the name of the subordinate
     * @return
     */
    private List<EmployeeDTO> getSubordinates(String name) {
        List<EmployeeDTO> subordinates = new ArrayList<EmployeeDTO>();
        for(Map.Entry<String,String> entry : employees.entrySet()){
            if(entry.getValue().equalsIgnoreCase(name)){          // Is a subordinate of the current element
                subordinates.add(new EmployeeDTO(entry.getKey(), new ArrayList<EmployeeDTO>()));
            }
        }
        return subordinates;
    }

    /**
     * This functon process the hierarchy in order to be shown as they requested way
     * @param employeeDTO: the root of the tree
     * @return
     */
    private JSONObject printEmployeeTree(EmployeeDTO employeeDTO) {
        JSONObject obj = new JSONObject();

        for(EmployeeDTO emp : employeeDTO.getSubordinates()){
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
        return new JSONObject().put(employeeDTO.getName(),obj);
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
            if(employee.isPresent()){                       // If the register already exists, just fix the fields values
                emp = employee.get();
                emp.setManagerId(null);
            }
                empSaved = employeeRepository.save(emp);     // Save the root and return the id

            idManager = empSaved.getEmployeeId();
        }

        for(EmployeeDTO employeeDTO : employeeDTOTree.getSubordinates()){
            Employee employee = new Employee(employeeDTO, idManager);
            Optional<Employee> emp = employeeRepository.findByName(employee.getName());
            if(emp.isPresent()){
                employee = emp.get();
                employee.setManagerId(idManager);
            }
            empSaved = employeeRepository.save(employee);
            saveDatabase(employeeDTO, false, empSaved.getEmployeeId());
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
            EmployeeDTO emp = new EmployeeDTO(list.get(i));
            EmployeeDTO add = addSubordinates(++i,list);
            if(add != null)
                emp.getSubordinates().add(add);
            return emp;
        }
        return null;
    }

    @Override
    public JSONObject saveHierarchy(String hierarchy) throws ApiExceptions {

        employeeDTOTree = buildMapStructure(hierarchy);                 // Convert the request to a HashMap and throw errors if exists
        buildEmployeeTree(employeeDTOTree);                             // Build a tree based on the HashMap
        if(treeHeight-1 != employees.size())        // Removing the root
            throw ErrorConstants.LOOP_SUPERVISORS;

        saveDatabase(employeeDTOTree, true, null);      // Save the relations into the database
        System.out.println(printEmployeeTree(employeeDTOTree));
        treeHeight = 0;
        return printEmployeeTree(employeeDTOTree);
    }


    @Override
    public JSONObject searchForSupervisors(Employee employee) throws ApiExceptions {
        List<Employee> list = employeeRepository.findSupervisorAndSupervisorsSupervisorFromGivenName(employee.getName());
        EmployeeDTO tree = new EmployeeDTO();
        if(!list.isEmpty())
            tree = addSubordinates(0, list);
        else
            return null;

        return printEmployeeTree(tree);
    }

}
