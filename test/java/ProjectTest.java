import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectTest {
    private Project project1, project1a, project2, project3;
    private Employee employee1, employee2, employee3;

    @BeforeEach
    private void setup() {
        project1 = new Project("P1001", "TestProject-1", LocalDate.of(2019,2,1), LocalDate.of(2019,4,30));
        project1a = new Project("P100" + 1, "TestProject-1", LocalDate.of(2019,2,1), LocalDate.of(2019,4,30));
        project2 = new Project("P2002", "TestProject-2", LocalDate.of(2019,4,1), LocalDate.of(2019,5,31));
        project3 = new Project("P3003", "TestProject-3", LocalDate.of(2019,3,15), LocalDate.of(2019,4,15));

        employee1 = new Employee(900001,20);
        employee2 = new Employee(990002,30);
        employee3 = new Employee(990003,40);

        project1.addCommitment(employee1,3);
        project1.addCommitment(employee2,4);
        project2.addCommitment(employee1,1);
        project2.addCommitment(employee3,8);
    }

    @Test
    void checkBasics() {
        assertEquals("TestProject-1(P1001)", project1a.toString());
        assertEquals(project1, project1a);
        assertEquals(project1.hashCode(), project1a.hashCode());
        assertTrue(project1.equals(project1a));
        assertTrue(project1 instanceof Comparable);
    }

    @Test
    void checkBudgets() {
        assertEquals((3*20+4*30)*project1.getNumWorkingDays(), project1.calculateManpowerBudget(),"manpower budget project1");
        assertEquals((1*20+8*40)*project2.getNumWorkingDays(), project2.calculateManpowerBudget(),"manpower budget project2");
        assertEquals(0, project3.calculateManpowerBudget(),"manpower budget project3");
    }
}
