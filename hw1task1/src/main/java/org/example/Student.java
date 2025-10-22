package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.Deque;
import java.util.ArrayDeque;

public class Student<T>  {
    private String name; // имя студента
    private List<T> grades; // оценки
    private Predicate<T> isGradeValid; // Предик для проверки правильности оценки

    private Deque<Action<?>> action_order; // Хранилище для действий
    // Конструкторы:
    // Создание студента с указанием имени
    // Создание студента с указанием имени и оценок
    public Student(String name) {
        this.name = name;
        this.grades = new ArrayList<>();
        this.action_order = new ArrayDeque<>();
        this.isGradeValid = is -> (true);
    }

    public Student(String name, List<T> grades) {
        this(name);
        this.grades = grades;
    }

    public Student(String name, Predicate<T> isGradeValid) {
        this(name);
        this.isGradeValid = isGradeValid;
    }

    public Student(String name, List<T> grades, Predicate<T> isGradeValid) {
        this(name, grades);
        this.isGradeValid = isGradeValid;
    }

    // Метод для получения имени
    public String getName() {
        return name;
    }

    // Метод для установки нового имени
    public void setName(String name) {
        Action<?> action = new Action<>(ActionType.NAME_CHANGE, this.name);
        this.name = name;
        action_order.push(action);
    }

    // Метод для получения оценок
    public List<T> getGrades() {
        return grades;
    }

    // Метод для добавления оценки
    public void addGrade(T grade) {
        if (isGradeValid.test(grade)) {
            this.grades.add(grade);
            Action<T> action = new Action<>(ActionType.GRADE_ADD, grade);
            action_order.push(action);
        } else {
            throw new IllegalArgumentException("Invalid grade");
        }
    }

    // Метод для удаления конткретной оценки
    public void removeGrade(T grade) {
        for(int i = grades.size() - 1; i >= 0; --i) {
            if (grades.get(i).equals(grade)) {
                grades.remove(i);
                Action<?> action = new Action<>(ActionType.GRADE_REMOVE, grade);
                action_order.push(action);
                return;
            }
        }
    }

    // Метод Отмены:
    // Использован Очередь-Действий, куда записываем
    // последовательно действия выполненные над студентом, а именно:
    // Изменение имени || Добавление/оценки
    // Останавливаемся на состание, когда создался студент
    public void undoLastAction() {
        if (!action_order.isEmpty()) {
            Action<?> lastAction = action_order.pop();
            if (lastAction.getType() == ActionType.NAME_CHANGE) {
                this.name = (String) lastAction.getData();
            } else if (lastAction.getType() == ActionType.GRADE_ADD) {
                this.grades.remove(lastAction.getData());
            } else if (lastAction.getType() == ActionType.GRADE_REMOVE) {
                this.grades.add((T)lastAction.getData());
            }
        }
    }

    // Проверка на равность студентов:
    // Два Студента равны если у них одинаковые имена и список оценок
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(name, student.name) && Objects.equals(grades, student.grades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, grades);
    }

    // Текстовое представление студента имеет вид: “Имя: [оценка1, оценка2,…,оценкаN]”
    @Override
    public String toString() {
        return name + ": " + grades;
    }

    private enum ActionType {
        NAME_CHANGE, GRADE_ADD, GRADE_REMOVE
    }

    private static class Action<E> {
        private ActionType type;
        private E data;

        public Action(ActionType type, E data) {
            this.type = type;
            this.data = data;
        }

        public ActionType getType() {
            return type;
        }

        public Object getData() {
            return data;
        }
    }
}
