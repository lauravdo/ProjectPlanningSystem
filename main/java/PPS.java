import utils.Names;
import utils.SLF4J;
import utils.XMLParser;

import javax.xml.stream.XMLStreamConstants;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PPS {

    private static final Random randomizer = new Random(06112020);

    private String name;                // the name of the planning system refers to its xml source file
    private int planningYear;                   // the year indicates the period of start and end dates of the projects
    private final Set<Employee> employees;
    private final Set<Project> projects;

    private PPS() {
        name = "none";
        planningYear = 2000;
        projects = new TreeSet<>();
        employees = new TreeSet<>();
    }

    private PPS(String resourceName, int year) {
        this();
        name = resourceName;
        planningYear = year;
    }

    private static int getRandomNumber(int size) {
        return new Random().nextInt(size);
    }

    /**
     * Loads a complete configuration from an XML file
     *
     * @param resourceName the XML file name to be found in the resources folder
     * @return
     */
    public static PPS importFromXML(String resourceName) {
        XMLParser xmlParser = new XMLParser(resourceName);

        try {
            xmlParser.nextTag();
            xmlParser.require(XMLStreamConstants.START_ELEMENT, null, "projectPlanning");
            int year = xmlParser.getIntegerAttributeValue(null, "year", 2000);
            xmlParser.nextTag();

            PPS pps = new PPS(resourceName, year);

            Project.importProjectsFromXML(xmlParser, pps.projects);
            Employee.importEmployeesFromXML(xmlParser, pps.employees, pps.projects);

            return pps;

        } catch (Exception ex) {
            SLF4J.logException("XML error in '" + resourceName + "'", ex);
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("PPS_e%d_p%d", this.employees.size(), this.projects.size());
    }

    /**
     * Reports the statistics of the project planning year
     */
    public void printPlanningStatistics() {

        System.out.printf("\nProject Statistics of '%s' in the year %d\n", name, planningYear);
        if (employees == null || projects == null || employees.size() == 0 || projects.size() == 0) {
            System.out.println("No employees or projects have been set up...");
            return;
        }

        System.out.printf("%d employees have been assigned to %d projects:\n\n",
                employees.size(), projects.size());


        System.out.println("1. The average hourly wage of all employees is " + calculateAverageHourlyWage() + "\n");

        System.out.println("2. The longest project is '" + (calculateLongestProject()) + "' with " + (calculateLongestProject().getNumWorkingDays()) + " available working days " + "\n");

        System.out.println("3. The follow employees have the broadest assignment in no less than " + (calculateMostInvolvedEmployees().size()) + " different projects:" + " \n" +
                "[" + (calculateMostInvolvedEmployees()) + "]" + " \n");

        System.out.println("4. The total budget of committed project manpower is " + (calculateTotalManpowerBudget()) + "\n");

        System.out.println("5. Below is an overview of total managed budget by junior employees (hourly wage <= 26):" + "\n");


        for (Employee employee : employees
        ) {
            if (employee.getHourlyWage() <= 26) {
                System.out.println("{" + employee.getName() + "(" + employee.getNumber() + ") = " + employee.calculateManagedBudget() + "}" + "\n");
            }

        }

        System.out.println("6.Below is an overview of employees working at least 8 hours per day:" + "[" + getFulltimeEmployees() + "]" + " \n");


        System.out.println("7. Below is an overview of cumulative monthly project spends:" + " \n" + calculateCumulativeMonthlySpends());


    }


    /**
     * calculates the average hourly wage of all known employees in this system
     *
     * @return
     */
    public double calculateAverageHourlyWage() {
        try {
            OptionalDouble averageHourlyWage = getEmployees()
                    .stream()
                    .mapToDouble(e -> (double) e.getHourlyWage())
                    .average();
            return averageHourlyWage.getAsDouble();
        } catch (NumberFormatException exception) {
            System.out.println("not a number");
        }

        return 0;
    }

    /**
     * finds the project with the highest number of available working days.
     * (if more than one project with the highest number is found, any one is returned)
     *
     * @return
     */
    public Project calculateLongestProject() {
        Optional<Project> projectMax = getProjects().stream().max(Comparator.comparing(Project::getNumWorkingDays));
        return getProjects()
                .stream()
                .filter(projects -> projects.getNumWorkingDays() == projectMax.get().getNumWorkingDays())
                .findFirst()
                .orElseGet(null);
    }

    /**
     * calculates the total budget for assigned employees across all projects and employees in the system
     * based on the registration of committed hours per day per employee,
     * the number of working days in each project
     * and the hourly rate of each employee
     *
     * @return
     */
    public int calculateTotalManpowerBudget() {

        int calculatedTotalManpowerBudget = 0;

        for (Project project : projects) {
            for (Map.Entry<Employee, Integer> entry : project.getCommittedHoursPerDay().entrySet()) {

                int totalWorkdays = project.getNumWorkingDays();
                int totalCommittedHours = entry.getValue();
                int workedHours = totalWorkdays * totalCommittedHours;
                int hourlyWage = entry.getKey().getHourlyWage();
                int totalManPowerBudget = hourlyWage * workedHours;

                calculatedTotalManpowerBudget += totalManPowerBudget;
            }


        }
        return calculatedTotalManpowerBudget;
////
//        int totalWorkdays = getProjects()
//                .stream()
//                .mapToInt(p -> p.getNumWorkingDays())
//                .sum();
//        int totalCommittedHours = getProjects().stream().collect(Collectors.summingInt(Project::calculateManpowerBudget));
//
//        int totalHourlyWage = getEmployees().stream().collect(Collectors.summingInt(Employee::getHourlyWage));
//
//        return totalWorkdays + totalCommittedHours + totalHourlyWage;


    }


    /**
     * finds the employees that are assigned to the highest number of different projects
     * (if multiple employees are assigned to the same highest number of projects,
     * all these employees are returned in the set)
     *
     * @return
     */
    public Set<Employee> calculateMostInvolvedEmployees() {
        if (employees.isEmpty()) {
            return Collections.emptySet();
        }


        Optional<Employee> employeeMax = getEmployees().stream().max(Comparator.comparing(Employee::getsizeAssignedProjects));
        return getEmployees().
                stream().
                filter(employees -> employees.getsizeAssignedProjects() == employeeMax.get()
                        .getsizeAssignedProjects())
                .collect(Collectors.toSet());

    }

    /**
     * Calculates an overview of total managed budget per employee that complies with the filter predicate
     * The total managed budget of an employee is the sum of all man power budgets of all projects
     * that are being managed by this employee
     *
     * @param
     * @return
     */
    public static Predicate<Employee> filter(){
        return e -> e.getHourlyWage() <= 26;
    }
    public Map<Employee, Integer> calculateManagedBudgetOverview(Predicate<Employee> filter) {

        Map<Employee, Integer> employeeManagedBudget = null;


        for (Employee e : this.employees) {
            if (filter.test(e)) {
                employeeManagedBudget.put(e, e.calculateManagedBudget());
            }
        }
        return employeeManagedBudget;
    }


    /**
     * Calculates and overview of total monthly spends across all projects in the system
     * The monthly spend of a single project is the accumulated manpower cost of all employees assigned to the
     * project across all working days in the month.
     *
     * @return
     */
    public Map<Month, Integer> calculateCumulativeMonthlySpends() {
        int costPerDay = 0;
        Map<Month, Integer> result = new HashMap<>();



        Set<Project> projects = getProjects();
        for (Project p : projects) {
            for (Map.Entry<Employee, Integer> entry : p.getCommittedHoursPerDay().entrySet()) {
                costPerDay += entry.getKey().getHourlyWage() * entry.getValue();

            }

            Map<Month, Long> workingDaysPerMonth = new HashMap<>();
            workingDaysPerMonth = p.getWorkingDays()
                    .stream()
                    .collect(Collectors.groupingBy(w -> w.getMonth(), Collectors.counting()));

            for (Map.Entry<Month, Long> entry : workingDaysPerMonth.entrySet()) {
                result.merge(entry.getKey(), (int) (entry.getValue() * costPerDay), (oldValue, newValue) -> oldValue + newValue);

            }
        }

        return result;

    }

    /**
     * Returns a set containing all the employees that work at least fulltime for at least one day per week on a project.
     *
     * @return
     */
    public Set<Employee> getFulltimeEmployees() {
        if (employees.isEmpty()) {
            return Collections.emptySet();
        }

        return getProjects().
                stream().filter(p -> p.getCommittedHoursPerDay().entrySet().stream().anyMatch(y -> y.getValue() >= 8))
                .map(e -> new Employee())
                .collect(Collectors.toSet());


    }

    public String getName() {
        return name;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    /**
     * A builder helper class to compose a small PPS using method-chaining of builder methods
     */
    public static class Builder {

        public PPS register = new PPS();
        String projectCode;
        int employeeNr;
        int hoursPerDay;
        private Employee employee;
        private Project project;

        public Builder() {

            register = new PPS();

        }

        private Employee addOrGetEmployee(Employee employee) {
            for (Employee e : this.register.employees) {
                if (e.equals(employee)) {
                    return e;
                }
            }
            this.register.employees.add(employee);
            return employee;

        }


        /**
         * Add another employee to the PPS being build
         *
         * @param employee
         * @return
         */
        public Builder addEmployee(Employee employee) {
            this.addOrGetEmployee(employee);

            return this;
        }


        private Project addOrGetProject(Project project) {
            for (Project p : this.register.projects) {
                if (p.equals(project)) {
                    return p;
                }
            }
            this.register.projects.add(project);
            return project;

        }

        /**
         * Add another project to the PPS
         * register the specified manager as the manager of the new
         *
         * @param project
         * @param manager
         * @return
         */
        public Builder addProject(Project project, Employee manager) {

            Employee employee = addOrGetEmployee(manager);

            employee.getManagedProjects().add(project);
            this.addOrGetProject(project);
            return this;

        }

        /**
         * Add a commitment to work hoursPerDay on the project that is identified by projectCode
         * for the employee who is identified by employeeNr
         * This commitment is added to any other commitment that the same employee already
         * has got registered on the same project,
         *
         * @param projectCode
         * @param employeeNr
         * @param hoursPerDay
         * @return
         */
        public Builder addCommitment(String projectCode, int employeeNr, int hoursPerDay) {


            for (Project p : this.register.projects
            ) {
                if (p.getCode().equals(projectCode)) {
                    for (Employee e : this.register.employees
                    ) {
                        if (e.getNumber() == employeeNr) {
                            if (p.getCommittedHoursPerDay().containsKey(e)) {

                                p.getCommittedHoursPerDay().put(e, p.getCommittedHoursPerDay().get(e) + hoursPerDay);
                            } else {
                                p.getCommittedHoursPerDay().put(e, hoursPerDay);
                            }

                        }
                    }

                }

            }


            return this;
        }


        /**
         * Complete the PPS being build
         *
         * @return
         */
        public PPS build() {


            return this.register;
        }
    }
}
