package org.example;

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

        student.addGrade(4);
        student.addGrade(5);
        student.addGrade(3);

        List<Object> expectedGrades = new ArrayList<>();
        expectedGrades.add(4);
        expectedGrades.add(5);
        expectedGrades.add(3);

        assertEquals(expectedGrades, student.getGrades());

        student.removeGrade(5);

        expectedGrades.remove((Object) 5);
        assertEquals(expectedGrades, student.getGrades());
    }

    @Test
    public void testUndoLastAction() {
        Predicate<Object> isGradeValid = grade -> true;
        Student student = new Student("Charlie", isGradeValid);

        student.addGrade("A");
        student.addGrade("B");
        student.setName("David");

        List<Object> expectedGrades = new ArrayList<>();
        expectedGrades.add("A");
        expectedGrades.add("B");

        assertEquals("David", student.getName());
        assertEquals(expectedGrades, student.getGrades());

        student.undoLastAction();

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
        student.addGrade(5);
        assertTrue(student.getGrades().contains(5));
    }

    @Test
    public void testRemoveGrade() {
        Student student = new Student("Alice");
        student.addGrade(5);
        student.removeGrade(5);
        assertFalse(student.getGrades().contains(5));
    }

    @Test
    public void testChangeName() {
        Student student = new Student("Alice");
        student.setName("Bob");
        assertEquals("Bob", student.getName());
    }

    @Test
    public void testEquals() {
        Student student1 = new Student("Alice");
        student1.addGrade(5);
        student1.addGrade(4);

        Student student2 = new Student("Bob");
        student2.addGrade(5);
        student2.addGrade(4);

        Student student3 = new Student("Alice");
        student3.addGrade(5);
        student3.addGrade(4);

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
        student.addGrade(5);
        student.addGrade(4);

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
}


