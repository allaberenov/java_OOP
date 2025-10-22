package org.example;

import java.util.Scanner;
import java.util.function.Predicate;

// Главный Метод
public class StudentApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Student<?> student = null;

        while (true) {
            System.out.println("Menu:");
            System.out.println("1. Create a student");
            System.out.println("2. Add a grade");
            System.out.println("3. Remove a grade");
            System.out.println("4. Print student info");
            System.out.println("5. Quit");

            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter student name: ");
                    String name = scanner.nextLine();
                    student = createStudent(name);
                }
                case 2 -> {
                    if (student != null) {
                        System.out.print("Enter a grade: ");
                        String grade = scanner.nextLine();
                        addGrade(student, grade);
                    } else {
                        System.out.println("Create a student first.");
                    }
                }
                case 3 -> {
                    if (student != null) {
                        System.out.print("Enter a grade to remove: ");
                        String grade = scanner.nextLine();
                        removeGrade(student, grade);
                    } else {
                        System.out.println("Create a student first.");
                    }
                }
                case 4 -> {
                    if (student != null) {
                        printStudentInfo(student);
                    } else {
                        System.out.println("Create a student first.");
                    }
                }
                case 5 -> System.exit(0);
                default -> System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    private static Student<?> createStudent(String name) {
        Predicate<Object> isGradeValid = grade -> true;
        return new Student<>(name, isGradeValid);
    }

    private static void addGrade(Student student, String grade) {
        student.addGrade(grade);
    }

    private static void removeGrade(Student student, String grade) {
        student.removeGrade(grade);
    }

    private static void printStudentInfo(Student student) {
        System.out.println(student.toString());
    }
}
