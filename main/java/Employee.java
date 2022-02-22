import utils.Names;
import utils.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.util.*;

public class Employee implements Comparable<Employee> {
    public static final int MAX_JUNIOR_WAGE = 26;
    public static final int MAX_WAGE = 75;

    private static Random randomizer = new Random(06112020);
    private static int lastNumber = 100000;

    private int number;                     // unique identifier of an Employee
    private String name;
    private int hourlyWage;                 // hourly charge rate of an Employee
                                            // used to calculate cost of project manpower
    private Set<Project> managedProjects;   // the projects that are managed by this employee
    private Set<Project> assignedProjects;  // the projects that this employee is working on
                                            // (the project manager is also assigned to his/her project)

    public Employee(int number) {
        this.number = number;
        name = Names.nextFullNameWithMI(number);
        lastNumber = Math.max(number, lastNumber);
        hourlyWage = 16 + randomizer.nextInt(MAX_WAGE -15);
        managedProjects = new HashSet<>();
        assignedProjects = new HashSet<>();
    }

    public Employee(int number, int hourlyWage) {
        this(number);
        this.hourlyWage = hourlyWage;
    }

    public Employee(int number, String name, int hourlyWage) {
        this(number, hourlyWage);
        this.name = name;
    }

    public Employee() {
        this(lastNumber + 1 + randomizer.nextInt(8));
    }

    @Override
    public int compareTo(Employee o) {
        return number - o.number;

    }



    /**
     * Calculates the total budget of all committed manpower
     * across all projects that this employee is managing
     * this is a.k.a. the total budget responsibility of the employee
     * @return
     */
    public int calculateManagedBudget() {
        return managedProjects
                .stream()
                .mapToInt(p -> p.calculateManpowerBudget())
                .sum();


    }
    //depends only on the unique employee number
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + number;
        return result;
    }

    //compare only employee number
    @Override
    public boolean equals(Object obj){
        if(this==obj)
            return true;
       if(obj==null)
           return false;
       if (getClass() != obj.getClass())
           return false;
       Employee other = (Employee) obj;
       if(number !=other.number)
           return false;
       return true;


    }


    public String toString(){
        return name + "("+ number + ")";
    }
    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public int getHourlyWage() {
        return hourlyWage;
    }

    public Set<Project> getManagedProjects() {
        return managedProjects;
    }

    public Set<Project> getAssignedProjects() {
        return assignedProjects;
    }

    public int getsizeAssignedProjects(){return assignedProjects.size();}

    // Below are helper attributes and methods for sample generation
    // and XML import and export

    public static Set<Employee> importEmployeesFromXML(XMLParser xmlParser, Set<Employee> employees,
                    Set<Project> projects) throws XMLStreamException {
        if (xmlParser.nextBeginTag("employees")) {
            xmlParser.nextTag();
            if (employees != null) {
                Employee employee;
                while ((employee = importFromXML(xmlParser, projects)) != null) {
                    employees.add(employee);
                }
            }
            xmlParser.findAndAcceptEndTag("employees");
        }
        return employees;
    }

    public static Employee importFromXML(XMLParser xmlParser, Set<Project> projects) throws XMLStreamException {
        if (xmlParser.nextBeginTag("employee")) {
            int number = xmlParser.getIntegerAttributeValue(null, "number", 0);
            xmlParser.nextTag();

            String name = "";
            if (xmlParser.nextBeginTag("name")) {
                name = xmlParser.getElementText();
                xmlParser.findAndAcceptEndTag("name");
            }

            int hourlyRate = 1;
            if (xmlParser.nextBeginTag("hourlyWage")) {
                hourlyRate = Integer.valueOf(xmlParser.getElementText());
                xmlParser.findAndAcceptEndTag("hourlyWage");
            }

            Employee employee = new Employee(number, name, hourlyRate);

            if (xmlParser.nextBeginTag("managedProjects")) {
                xmlParser.nextTag();
                Project project;
                while ((project = Project.importReferenceFromXML(xmlParser, projects)) != null) {
                    employee.managedProjects.add(project);

                    // replace the placeholder references in the project, if any
                    project.updateReferences(employee);
                }
                xmlParser.findAndAcceptEndTag("managedProjects");
            }
            if (xmlParser.nextBeginTag("allocatedProjects")) {
                xmlParser.nextTag();
                Project project;
                while ((project = Project.importReferenceFromXML(xmlParser, projects)) != null) {
                    employee.assignedProjects.add(project);

                    // replace the placeholder references in the project, if any
                    project.updateReferences(employee);
                }
                xmlParser.findAndAcceptEndTag("allocatedProjects");
            }

            xmlParser.findAndAcceptEndTag("employee");
            return employee;
        }
        return null;
    }

    public void updateReferences(Project project) {
        // replace the employee's references to project
        if (managedProjects.removeIf(p -> p.equals(project))) {
            managedProjects.add(project);
        }
        if (assignedProjects.removeIf(p -> p.equals(project))) {
            assignedProjects.add(project);
        }
    }
}
