import utils.Calendar;
import utils.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.time.LocalDate;
import java.util.*;

public class Project implements Comparable<Project> {
    private static Random randomizer = new Random(06112020);
    private String code;                // unique identifier of a project
    private String title;
    private LocalDate startDate;        // the first working day of the project;
    private LocalDate endDate;          // the last working day of the project;
    private Map<Employee,Integer> committedHoursPerDay;
                                        // daily committed work hours on the project by employee
                                        // one employee may work on multiple different projects each day
                                        // employees will no overtime if more than 8 hours per day are committed

    public Project(String projectCode) {
        this.code = projectCode;
        this.title = "Project " + projectCode;
        this.committedHoursPerDay = new HashMap<>();
    }

    public Project(int projectNr) {
        this("P" + projectNr);
        this.title = calculateTitle(projectNr);
    }

    public Project() {
        this(100000 + randomizer.nextInt(MAX_PROJECTS));
    }

    public Project(LocalDate startDate, LocalDate endDate) {
        this();
        this.startDate = Calendar.firstWorkingDayFrom(startDate);
        this.endDate = Calendar.lastWorkingDayUntil(endDate);
    }

    public Project(String code, String title,
                   LocalDate startDate, LocalDate endDate) {
        this(code);
        this.title = title;
        this.startDate = Calendar.firstWorkingDayFrom(startDate);
        this.endDate = Calendar.lastWorkingDayUntil(endDate);
    }

    @Override
    public int compareTo(Project o) {
        return this.code.compareTo(o.code);
    }

    /**
     * provides the number of available working days for the project,
     * excluding weekend days
     * @return
     */
    public int getNumWorkingDays() {
        return utils.Calendar.getNumWorkingDays(this.startDate, this.endDate);
    }

    /**
     * provides a collection of dates that represent each of the available working days for the project,
     * excluding weekend days
     * @return
     */
    public Set<LocalDate> getWorkingDays() {
        return Calendar.getWorkingDays(this.startDate, this.endDate);
    }

    // TODO make sure Projects can be printed. The format is 'title(code)'
    public String toString(){
        return title + "(" + code + ")";
    }

    // TODO make sure Projects can be added to a HashMap, HashSet
    //  every project shall have a unique code
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Project other = (Project) obj;
        if (code != other.code)
            return false;
        return true;
    }

    /**
     * add the specified hoursPerDay commitment for the specified employee on the project
     * these hours should be added to any existing commitment of the employee on the project
     * there is no check on maximum allocation of hours per day;
     * if the total exceeds 8 per day on any day, the exployee will be expected to do overtime
     * @param employee
     * @param hoursPerDay
     */
    public void addCommitment(Employee employee, int hoursPerDay) {

        committedHoursPerDay.put(employee,hoursPerDay);



        // also register this project assignment for this employee,
        // in case that had not been done before
        employee.getAssignedProjects().add(this);
    }

    /**
     * Calculate total manpower budget for the project
     * from the committed hours per employee per working day
     * and the hourlyRate per employee
     * @return
     */
    public int calculateManpowerBudget() {
        // TODO
        int totalPayAllEmployees = 0;
        for (Map.Entry<Employee, Integer> entry : committedHoursPerDay.entrySet()){
        int dailyWorkedHours = entry.getKey().getHourlyWage() * entry.getValue();
        int totalPayEmployee = dailyWorkedHours * getNumWorkingDays();

        totalPayAllEmployees+= totalPayEmployee;


        }
        return totalPayAllEmployees;



    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Map<Employee, Integer> getCommittedHoursPerDay() {
        return committedHoursPerDay;
    }

    // Below are helper attributes and methods for sample generation
    // and XML import and export

    private static final int N_FLOORS = 10;

    private static String calculateTitle(int projectNr) {
        int floor = projectNr % N_FLOORS;
        projectNr /= N_FLOORS;
        int subjectIdx = projectNr % subjects.length;
        int locationIdx = (projectNr / subjects.length) % locations.length;
        return  subjects[subjectIdx] + " - " + locations[locationIdx] + "-0" + floor;
    }

    private final static String[] subjects = {
            "WIFI network upgrade",
            "WIFI network installation",
            "Surveillance cameras installation",
            "Virtual workplaces",
            "Floor insulation",
            "Lighting replacements",
            "Funiture replacements",
            "Toilets refurbishment",
            "Workspace rearrangement" };

    private final static String[] locations = {
            "KSH",
            "BPH",
            "WBH",
            "MLH",
            "LWB",
            "TTH",
            "CON",
            "IWO",
            "SCP" };

    private static final int MAX_PROJECTS =
            N_FLOORS * subjects.length * locations.length;


    public void updateReferences(Employee employee) {
        // replace the employee key of the commitment
        Integer hoursPerDay;
        if ((hoursPerDay = this.getCommittedHoursPerDay().remove(employee)) != null) {
            this.getCommittedHoursPerDay().put(employee,hoursPerDay);
        }
    }

    public static Set<Project> importProjectsFromXML(XMLParser xmlParser, Set<Project> projects) throws XMLStreamException {
        if (xmlParser.nextBeginTag("projects")) {
            xmlParser.nextTag();
            if (projects != null) {
                Project project;
                while ((project = importFromXML(xmlParser)) != null) {
                    projects.add(project);
                }
            }

            xmlParser.findAndAcceptEndTag("projects");
            return projects;
        }
        return null;
    }

    public static Project importFromXML(XMLParser xmlParser) throws XMLStreamException {
        if (xmlParser.nextBeginTag("project")) {
            String code = xmlParser.getAttributeValue(null, "code");
            xmlParser.nextTag();

            String title = "";
            if (xmlParser.nextBeginTag("title")) {
                title = xmlParser.getElementText();
                xmlParser.findAndAcceptEndTag("title");
            }

            LocalDate startDate = null;
            if (xmlParser.nextBeginTag("startDate")) {
                startDate = LocalDate.parse(xmlParser.getElementText());
                xmlParser.findAndAcceptEndTag("startDate");
            }

            LocalDate endDate = null;
            if (xmlParser.nextBeginTag("endDate")) {
                endDate = LocalDate.parse(xmlParser.getElementText());
                xmlParser.findAndAcceptEndTag("endDate");
            }

            Project project = new Project(code, title, startDate, endDate);

            if (xmlParser.nextBeginTag("commitments")) {
                xmlParser.nextTag();
                while ((xmlParser.nextBeginTag("hoursPerDay"))) {
                    int number = xmlParser.getIntegerAttributeValue(null, "employee", 0);
                    int hoursPerDay = Integer.valueOf(xmlParser.getElementText());

                    // use an incomplete employee object to register the commitment
                    // will be refreshed by employee import
                    project.committedHoursPerDay.put(new Employee(number), hoursPerDay);
                    xmlParser.findAndAcceptEndTag("hoursPerDay");
                }
                xmlParser.findAndAcceptEndTag("commitments");
            }

            xmlParser.findAndAcceptEndTag("project");
            return project;
        }
        return null;
    }
    public static Project importReferenceFromXML(XMLParser xmlParser, Set<Project> projects) throws XMLStreamException {
        if (xmlParser.nextBeginTag("project")) {
            String code = xmlParser.getAttributeValue(null, "code");
            Project project = projects.stream()
                    .filter(p -> p.code.equals(code))
                    .findAny()
                    .orElse(new Project(code));
            xmlParser.findAndAcceptEndTag("project");
            return project;
        }
        return null;
    }
}
