import java.util.*;

public class LLD_OrgChart {
    public static void main(String[] args) {
        
    }
}

class Manager extends Employee {
    HashSet<Employee> reports;

    public Manager() {
        employeeType = EmployeeType.MANAGER;
        reports = new HashSet<Employee>();
    }

    @Override
    public void createEmployee(String name, double ctc, String org, Manager manager, int id) {
        super.createEmployee(name, ctc, org, manager, id);
    }

    public double getAggregatedCTC() {
        return dfsCtcHelper(this);
    }

    private double dfsCtcHelper(Employee node) {
        double sumCtc = node.getCtc();
        if(node.employeeType == EmployeeType.MANAGER) {
            Manager manager = (Manager) node;       // else will throw ClassCastException
            for(Employee next:manager.reports) {
                sumCtc += dfsCtcHelper(next);
            }
        }
        
        return sumCtc;
    }

    @Override
    public void deleteEmployee() {
        super.deleteEmployee();
        Manager manager = (Manager) parent;
        for(Employee report:reports) {
            manager.reports.add(report);
            report.parent = manager;
        }
    }

    public void convertManagerToEmployee() {
        this.deleteEmployee();
        new Employee().createEmployee(this.name, this.ctc, this.org, this.parent, this.id);
    }

    public List<String> expandReportsToNLevel(int n) {
        List<String> resList = new ArrayList<>();
        dfsExpandBelowToN(this, n, 0, resList);
        return resList;
    }

    private void dfsExpandBelowToN(Employee node, int n, int depth, List<String> resList) {
        String ind = "";
        for(int i=0; i<depth; ++i) {
            ind += "\t";
        }
        resList.add(ind + node.name + "\t\t\t\t" + node.org);
        if(node.employeeType == EmployeeType.MANAGER && depth < n) {
            Manager manager = (Manager) node;
            for(Employee next:manager.reports) {
                dfsExpandBelowToN(node, n, depth+1, resList);
            }
        }
    }

    public List<String> fullExpand() {
        List<String> resList = new ArrayList<>();
        int depth = dfsExpandTillRoot(this, resList);
        resList.remove(resList.size()-1);
        dfsExpandBelowToN(this, Integer.MAX_VALUE, depth, resList);
        return resList;
    }
}

class Employee {
    int id;
    double ctc;
    String name, org;
    EmployeeType employeeType;
    Manager parent;

    public Employee() {
        employeeType = EmployeeType.EMPLOYEE;
    }

    // returns id
    public void createEmployee(String name, double ctc, String org, Manager manager, int id) {
        this.id = id;
        this.name = name;
        this.ctc = ctc;
        this.org = org;
        if(manager != null) {
            this.changeManager(manager);
        }
    }

    public void changeManager(Manager manager) {
        if(this.parent != null) {
            parent.reports.remove(this);
        }
        this.parent = manager;
    }

    public void deleteEmployee() {
        Manager manager = (Manager) parent;
        manager.reports.remove(this);
    }

    public void convertEmployeeToManager() {
        this.deleteEmployee();
        new Manager().createEmployee(this.name, this.ctc, this.org, this.parent, this.id);
    }

    public double getCtc() {
        return this.ctc;
    }

    public List<String> expandTillTop() {
        List<String> resList = new ArrayList<>();
        dfsExpandTillRoot(this, resList);
        return resList;
    }

    protected int dfsExpandTillRoot(Employee node, List<String> resList) {
        int depth = 0;
        if(node.parent != null) {
            depth = dfsExpandTillRoot(node.parent, resList);
        }
        String res = "";
        for(int i=0; i<depth; ++i) {
            res += "\t";
        }
        res += node.name + "\t\t\t\t" + node.org;
        resList.add(res);
        return depth+1;
    }
}

enum EmployeeType {
    EMPLOYEE, MANAGER
}

final class EmployeeIdGenerator {
    private static int empId = -1;

    private EmployeeIdGenerator() {
    }

    public static synchronized int getEmpId() {
        empId++;
        return empId;
    }
}

/*

Org chart -- kind of tree structure - composite data pattern

Functions to perform:

1. Add employee to chart
2. Convert a employee to manager
3. Convert a manager to normal employee
4. Delete employee
5. Delete manager -- when manager is deleted, all direct reports will report to the prev manager
6. Get CTC of employee
7. Get CTC of the overall org


Approach:

1. Implement as composite design pattern
2. Use tree like structure -- i will use graphs for better visualization
3. Leaf nodes will only be employees, non-leaf nodes will always be manager
4. For every employee/manager, we will store the CTC details individually
5. For every manager, we will store a list of direct reports
6. For every employee/manager, we will store the parent manager
7. Every employee/manager will be mapped with an unique id (empId) (normal index for now), which shall be used to repesent the graph edges


Algorithms:

1. Normal DFS traversal
2. Trie/n-ary Trees/Graphs
3. Find the aggregate of a subtree


Classes and relations:

Employee
- ID
- Name
- Org
- CTC
- EmployeeType


Manager extends Employee
- Reports<Integer>


enum EmployeeType {EMPLOYEE, MANAGER}


Assumptions:

1. Root node will be created by default
2. Root node will never be deleted

Additional functionalities:

- Add to database 

*/