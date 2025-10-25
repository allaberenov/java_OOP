package org.example;

import java.util.Scanner;

// Главный Метод
public class StudentApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Student student = null;

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
                    student = new Student<>(name);
                    System.out.println("Student with name " + name + " was created");
                }
                case 2 -> {
                    if (student != null) {
                        System.out.print("Enter a grade: ");
                        String grade = scanner.nextLine();
                        student.AddGrade(grade);
                    } else {
                        System.out.println("Create a student first.");
                    }
                }
                case 3 -> {
                    if (student != null) {
                        System.out.print("Enter a grade to remove: ");
                        String grade = scanner.nextLine();
                        student.RemoveGrade(grade);
                    } else {
                        System.out.println("Create a student first.");
                    }
                }
                case 4 -> {
                    if (student != null) {
                        System.out.println(student.toString());
                    } else {
                        System.out.println("Create a student first.");
                    }
                }
                case 5 -> System.exit(0);
                default -> System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

}
