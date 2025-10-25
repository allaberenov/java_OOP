package org.example;

import org.example.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class StudentTest {
    @Test
    public void testStudentCreation() {
        Predicate<Object> isGradeValid = grade -> true;
        Student student = new Student("Alice", isGradeValid);

        assertNotNull(student);
        assertEquals("Alice", student.getName());
        assertTrue(student.getGrades().isEmpty());
    }

    @Test
    public void testAddAndRemoveGrades() {
        Predicate<Object> isGradeValid = grade -> grade instanceof Integer && (int) grade >= 2 && (int) grade <= 5;
        Student student = new Student("Bob", isGradeValid);

        student.AddGrade(4);
        student.AddGrade(5);
        student.AddGrade(3);

        List<Object> expectedGrades = new ArrayList<>();
        expectedGrades.add(4);
        expectedGrades.add(5);
        expectedGrades.add(3);

        assertEquals(expectedGrades, student.getGrades());

        student.RemoveGrade(5);

        expectedGrades.remove((Object) 5);
        assertEquals(expectedGrades, student.getGrades());
    }

    @Test
    public void testUndoLastAction() {
        Predicate<Object> isGradeValid = grade -> true;
        Student student = new Student("Charlie", isGradeValid);

        student.AddGrade("A");
        student.AddGrade("B");
        student.ChangeName("David");

        List<Object> expectedGrades = new ArrayList<>();
        expectedGrades.add("A");
        expectedGrades.add("B");

        assertEquals("David", student.getName());
        assertEquals(expectedGrades, student.getGrades());

        student.Restore();

        assertEquals("Charlie", student.getName());
        assertEquals(expectedGrades, student.getGrades());
    }

    @Test
    void testGetName() {
        Student student = new Student("Alice");
        assertEquals("Alice", student.getName());
    }

    @Test
    public void testAddGrade() {
        Student student = new Student("Alice");
        student.AddGrade(5);
        assertTrue(student.getGrades().contains(5));
    }

    @Test
    public void testRemoveGrade() {
        Student student = new Student("Alice");
        student.AddGrade(5);
        student.RemoveGrade(5);
        assertFalse(student.getGrades().contains(5));
    }

    @Test
    public void testChangeName() {
        Student student = new Student("Alice");
        student.ChangeName("Bob");
        assertEquals("Bob", student.getName());
    }

    @Test
    public void testEquals() {
        Student student1 = new Student("Alice");
        student1.AddGrade(5);
        student1.AddGrade(4);

        Student student2 = new Student("Bob");
        student2.AddGrade(5);
        student2.AddGrade(4);

        Student student3 = new Student("Alice");
        student3.AddGrade(5);
        student3.AddGrade(4);

        assertEquals(student1, student3);
        assertNotEquals(student1, student2);
    }

    @Test
    public void testConstructorWithGradeValidation() {
        Student student = new Student("Alice", List.of(5, 4), grade -> grade instanceof Integer);
        assertTrue(student.getGrades().contains(5));
        assertTrue(student.getGrades().contains(4));

    }

    @Test
    public void testToString() {
        Student student = new Student("Alice");
        student.AddGrade(5);
        student.AddGrade(4);

        String expected = "Alice: [5, 4]";
        assertEquals(expected, student.toString());
    }

    @Test
    public void testConstructorWithDefaultGradeValidation() {
        Student student = new Student("Alice", List.of(5, 4));
        assertTrue(student.getGrades().contains(5));
        assertTrue(student.getGrades().contains(4));

        Student invalidStudent = new Student("Bob", List.of("A", "B"));
        assertFalse(invalidStudent.getGrades().isEmpty());
    }

    @Test
    public void testEmptyNameThrowsError() {
        assertThrows(IllegalArgumentException.class, () -> new Student(""));
    }

    @Test
    public void testAddInvalidGradeThrowsError() {
        Predicate<Object> onlyFives = grade -> grade.equals(5);
        Student student = new Student("Alice", onlyFives);

        assertDoesNotThrow(() -> student.AddGrade(5));
        assertThrows(IllegalArgumentException.class, () -> student.AddGrade(4));
    }

    @Test
    public void testRemoveGradeThatDoesNotExist() {
        Student student = new Student("Alice");
        student.AddGrade(5);
        student.RemoveGrade(3);  // ничего не произойдет
        assertEquals(List.of(5), student.getGrades());
    }

    @Test
    public void testRestoreOnEmptyHistoryDoesNothing() {
        Student student = new Student("Alice");
        student.Restore();  // не должно бросать исключение
        assertEquals("Alice", student.getName());
        assertTrue(student.getGrades().isEmpty());
    }

    @Test
    public void testEqualsWithNullAndDifferentType() {
        Student student = new Student("Alice");
        assertNotEquals(student, null);
        assertNotEquals(student, "NotAStudent");
    }

    @Test
    public void testAddStringGrades() {
        Predicate<Object> stringOnly = grade -> grade instanceof String;
        Student student = new Student("Bob", stringOnly);

        student.AddGrade("Excellent");
        student.AddGrade("Good");

        assertEquals(List.of("Excellent", "Good"), student.getGrades());
    }

    @Test
    public void testAddDoubleGrades() {
        Predicate<Object> positive = grade -> grade instanceof Double && (Double) grade > 0;
        Student student = new Student("Eve", positive);

        student.AddGrade(3.5);
        student.AddGrade(4.0);

        assertEquals(List.of(3.5, 4.0), student.getGrades());
    }

    @Test
    public void testChangeNameToEmptyString() {
        Student student = new Student("Alice");
        assertThrows(IllegalArgumentException.class, () -> student.ChangeName(""));
    }

    @Test
    public void testMultipleRestores() {
        Student student = new Student("Alice");
        student.AddGrade(5);
        student.AddGrade(4);
        student.RemoveGrade(4);
        student.ChangeName("Bob");

        student.Restore(); // undo ChangeName
        student.Restore(); // undo RemoveGrade
        student.Restore(); // undo AddGrade(4)
        student.Restore(); // undo AddGrade(5)

        assertEquals("Alice", student.getName());
        assertTrue(student.getGrades().isEmpty());
    }

    @Test
    public void testToStringEmptyGrades() {
        Student student = new Student("NoGrades");
        assertEquals("NoGrades: []", student.toString());
    }

    // --- Flow Tests ---
    @Test
    public void testFlowFilterAndReduce() {
        int sum = Student.Flow.of(1, -2, 3, 4)
                .filter(x -> x > 0)
                .reduce(Integer::sum);
        assertEquals(8, sum); // 1 + 3 + 4
    }

    @Test
    public void testFlowFunctionAndReduce() {
        int sum = Student.Flow.of(1, 2, 3)
                .function(x -> x * 2)
                .reduce(Integer::sum);
        assertEquals(12, sum); // (2 + 4 + 6)
    }

    @Test
    public void testFlowCollectToList() {
        List<Integer> collected = Student.Flow.of(1, 2, 3)
                .filter(x -> x != 2)
                .collect(ArrayList::new, List::add);
        assertEquals(List.of(1, 3), collected);
    }

    @Test
    public void testFlowIterateGeneration() {
        var flow = Student.Flow.iterate(1, x -> x + 1, x -> x < 5);
        int sum = flow.reduce(Integer::sum);
        assertEquals(10, sum); // 1+2+3+4
    }

    @Test
    public void testFlowEmptyReduce() {
        Student.Flow<Integer> flow = Student.Flow.of();
        Integer result = flow.reduce(Integer::sum);
        assertNull(result);
    }

    @Test
    public void testFlowCollectEmpty() {
        Student.Flow<Integer> flow = Student.Flow.of();
        List<Integer> collected = flow.collect(ArrayList::new, List::add);
        assertTrue(collected.isEmpty());
    }

    // --- Student constructors and validator edge cases ---
    @Test
    public void testConstructorRejectsInvalidGrade() {
        List<Integer> invalidMarks = List.of(5, -1);
        assertThrows(IllegalArgumentException.class, () ->
                new Student<>("Alex", invalidMarks, x -> (Integer) x > 0));
    }

    @Test
    public void testConstructorWithValidGrades() {
        List<Integer> marks = List.of(3, 4, 5);
        Student<Integer> s = new Student<>("Anna", marks, x -> x > 0);
        assertEquals(marks, s.getGrades());
    }

    @Test
    public void testEqualsSameReference() {
        Student<Integer> s = new Student<>("Sam");
        assertEquals(s, s);
    }

    @Test
    public void testHashCodeConsistency() {
        Student<Integer> s1 = new Student<>("Tom");
        Student<Integer> s2 = new Student<>("Tom");
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testRestoreDoesNothingIfEmpty() {
        Student<Integer> s = new Student<>("Kate");
        s.Restore(); // просто ничего не должно сломаться
        assertTrue(s.getGrades().isEmpty());
    }

    @Test
    public void testApplyActionAddRemoveGrade() {
        Student<Integer> s = new Student<>("Neo");
        s.AddGrade(5);
        s.RemoveGrade(5);
        s.Restore(); // откатывает remove -> снова добавит 5
        assertTrue(s.getGrades().contains(5));
    }

    @Test
    public void testToStringWithGrades() {
        Student<Integer> s = new Student<>("Leo");
        s.AddGrade(5);
        s.AddGrade(4);
        String output = s.toString();
        assertTrue(output.contains("Leo"));
        assertTrue(output.contains("5"));
        assertTrue(output.contains("4"));
    }

}


