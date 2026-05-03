import java.util.*;

public class LLD_EmployeeAccessManagement {
    public static void main(String[] args) {
        
    }
}

class AccessManagement {
    HashMap<String, Resource> resourceMap;
    HashMap<String, Employee> employeeMap;

    // assign resources
    // assign employees

    public boolean addAccessLevel(String resourceId, String employeeId, AccessLevel accessLevel) {
        Resource resource = resourceMap.get(resourceId);
        Employee employee = employeeMap.get(employeeId);
        resource.addAccessLevel(employeeId, accessLevel);
        employee.addAccessLevel(resourceId, accessLevel);
        return true;
    }

    public boolean checkAccess(String resourceId, String employeeId, AccessLevel accessLevel) {
        Employee employee = employeeMap.get(employeeId);
        if(employee.resourceAccessMap.containsKey(resourceId)) {
            HashSet<AccessLevel> set = employee.resourceAccessMap.get(resourceId);
            return set.contains(accessLevel);
        }
        return false;
    }

    // removeAccess
    // countEmployeesHavingSpecificAccess

}

class Resource {
    String resourceId;
    HashMap<String, HashSet<AccessLevel>> employeeAccessMap;
    List<HashSet<String>> accessLevelEmployees; 

    public Resource(String resourceId) {
        this.resourceId = resourceId;
        employeeAccessMap = new HashMap<>();
        accessLevelEmployees = new ArrayList<>(3);
        for(int i=0; i<3; ++i) {
            accessLevelEmployees.add(new HashSet<>());
        }
    }

    public boolean addAccessLevel(String employeeId, AccessLevel accessLevel) {
        if(!employeeAccessMap.containsKey(employeeId)) {
            employeeAccessMap.put(employeeId, new HashSet<>());
        }
        employeeAccessMap.get(employeeId).add(accessLevel);
        accessLevelEmployees.get(accessLevel.getIndex()).add(employeeId);
        return true;
    }

    public boolean removeFullAccess(String employeeId) {
        if(employeeAccessMap.containsKey(employeeId)) {
            employeeAccessMap.remove(employeeId);
        }
        for(HashSet<String> employeeSet:accessLevelEmployees) {
            if(employeeSet.contains(employeeId)) {
                employeeSet.remove(employeeId);
            }
        }
        return true;
    }

    public boolean removeSpecificAccess(String employeeId, AccessLevel accessLevel) {
        if(employeeAccessMap.containsKey(employeeId)) {
            if(employeeAccessMap.get(employeeId).contains(accessLevel)) {
                employeeAccessMap.get(employeeId).remove(accessLevel);
            }
        }
        if(accessLevelEmployees.get(accessLevel.getIndex()).contains(employeeId)) {
            accessLevelEmployees.get(accessLevel.getIndex()).remove(employeeId);
        }
        return true;
    }
}

class Employee {
    String employeeId;
    HashMap<String, HashSet<AccessLevel>> resourceAccessMap;

    public Employee(String employeeId) {
        this.employeeId = employeeId;
        this.resourceAccessMap = new HashMap<>();
    }

    public boolean addAccessLevel(String resourceId, AccessLevel accessLevel) {
        if(!resourceAccessMap.containsKey(resourceId)) {
            resourceAccessMap.put(resourceId, new HashSet<>());
        }
        resourceAccessMap.get(resourceId).add(accessLevel);
        return true;
    }

    public boolean removeFullAccessFromResource(String resourceId) {
        if(resourceAccessMap.containsKey(resourceId)) {
            resourceAccessMap.remove(resourceId);
        }
        return true;
    }

    public boolean removeSpecificAccessFromResource(String resourceId, AccessLevel accessLevel) {
        if(resourceAccessMap.containsKey(resourceId)) {
            if(resourceAccessMap.get(resourceId).contains(accessLevel)) {
                resourceAccessMap.get(resourceId).remove(accessLevel);
            }
        }
        return true;
    }
}

enum AccessLevel {
    READ(0), 
    WRITE(1), 
    ADMIN(2);

    private final int index;

    private AccessLevel(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}