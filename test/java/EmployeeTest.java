import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeTest {
    private Project project1, project2, project3;
    private Employee employee1, employee1a, employee2, employee3,employee4,employee5;

    @BeforeEach
    private void setup() {
        employee1 = new Employee(900001,20);
        employee1a = new Employee(900001,20);
        employee2 = new Employee(990002,30);
        employee3 = new Employee(990003,40);
        employee4 = new Employee(990004,400);
        employee5 = new Employee(990005,4);
        project1 = new Project("P1001", "TestProject-1", LocalDate.of(2019,2,1), LocalDate.of(2019,4,30));
        project2 = new Project("P2002", "TestProject-2", LocalDate.of(2019,4,1), LocalDate.of(2019,5,31));
        project3 = new Project("P3003", "TestProject-3", LocalDate.of(2019,3,15), LocalDate.of(2019,4,15));

        employee1.getManagedProjects().addAll(Set.of(project1, project2));
        project1.addCommitment(employee1,3);
        project1.addCommitment(employee2,4);
        project2.addCommitment(employee1,1);
        project2.addCommitment(employee3,8);
    }

    @Test
    void checkBasics() {
        assertEquals("Mary N. PETERSON(900001)", employee1.toString());
        assertEquals(employee1, employee1a);
        assertEquals(employee1.hashCode(), employee1a.hashCode());
        assertTrue(employee1.equals(employee1a));
        assertTrue(employee1 instanceof Comparable);
    }

    @Test
    void checkBudgets() {
        assertEquals((3*20+4*30)*project1.getNumWorkingDays()+(1*20+8*40)*project2.getNumWorkingDays(), employee1.calculateManagedBudget(),"managed budget");
        assertEquals(0, employee2.calculateManagedBudget(),"managed budget");
    }

    @Test
    void checkHourlyWage(){
        assertEquals(20,employee1.getHourlyWage());
        assertEquals(40,employee3.getHourlyWage());
        assertEquals(400,employee4.getHourlyWage());
        assertEquals(4,employee5.getHourlyWage());
    }
}
