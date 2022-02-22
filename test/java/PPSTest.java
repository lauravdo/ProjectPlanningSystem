import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PPSTest {
    Project project1, project2, project3;
    Employee employee1, employee2, employee3;
    private PPS pps;

    @BeforeEach
    void setup() {
        project1 = new Project("P1001", "TestProject-1", LocalDate.of(2019,2,1), LocalDate.of(2019,4,30));
        project2 = new Project("P2002", "TestProject-2", LocalDate.of(2019,4,1), LocalDate.of(2019,5,31));
        project3 = new Project("P3003", "TestProject-3", LocalDate.of(2019,3,15), LocalDate.of(2019,4,15));
        employee1 = new Employee(60006, 20);
        employee2 = new Employee(77007, 25);
        employee3 = new Employee(88808, 30);
        pps = new PPS.Builder()
                    .addEmployee(employee1)
                    .addEmployee(employee3)
                    .addProject(project1, employee1)
                    .addProject(project2, new Employee(60006))
                        .addProject(project3, employee2)
                        .addCommitment("P1001", 60006, 4)
                        .addCommitment("P1001", 77007, 3)
                        .addCommitment("P1001", 88808, 2)
                        .addCommitment("P2002", 88808, 3)
                        .addCommitment("P2002", 88808, 1)
                    .build();
    }

    @Test
    void checkPPSBuilder() {
        assertEquals(3, pps.getEmployees().size(), pps.getEmployees().toString());
        assertEquals(3, pps.getProjects().size(), pps.getProjects().toString());
        assertEquals((4*20+3*25+2*30)*project1.getNumWorkingDays(), project1.calculateManpowerBudget());
        assertEquals((4*30)*project2.getNumWorkingDays(), project2.calculateManpowerBudget());
        assertEquals(project1.calculateManpowerBudget()+project2.calculateManpowerBudget(), employee1.calculateManagedBudget(),"managed budget employee1");
    }

    @Test
    void checkStatistics_e1_p1() {
        PPS pps = PPS.importFromXML("HvA2011_e1_p1.xml");

        pps.printPlanningStatistics();

        assertEquals(67.0, pps.calculateAverageHourlyWage(),"average hourly rate");
        assertEquals("Workspace rearrangement - BPH-08(P100618)", pps.calculateLongestProject().toString(),"longest project");
        assertEquals(4154, pps.calculateTotalManpowerBudget(),"total manpower budget");
    }

    @Test
    void checkStatistics_e2_p2() {
        PPS pps = PPS.importFromXML("HvA2012_e2_p2.xml");

        pps.printPlanningStatistics();

        assertEquals(27.5, pps.calculateAverageHourlyWage(),"average hourly rate");
        assertEquals("Virtual workplaces - LWB-09(P100029)", pps.calculateLongestProject().toString(),"longest project");
        assertEquals(27225, pps.calculateTotalManpowerBudget(),"total manpower budget");
    }

    @Test
    void checkStatistics_e5_p5() {
        PPS pps = PPS.importFromXML("HvA2015_e5_p5.xml");

        pps.printPlanningStatistics();

        assertEquals(50.4, pps.calculateAverageHourlyWage(),"average hourly rate");
        assertEquals("Floor insulation - BPH-05(P100575)", pps.calculateLongestProject().toString(),"longest project");
        assertEquals(159441, pps.calculateTotalManpowerBudget(),"total manpower budget");
    }
}
