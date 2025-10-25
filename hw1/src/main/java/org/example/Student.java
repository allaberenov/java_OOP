package org.example;

import java.util.*;
import java.util.function.*;


public class Student<T> {
    private String name;
    private List<T> marks;
    private Predicate<T> validator;
    private final List<Action<?>> history = new ArrayList<>();


    private enum ActionTypes{
        REMOVE_GRADE,
        ADD_GRADE,
        CHANGE_NAME
    }

    private static class Action<E> {
        ActionTypes action;
        E data;

        public Action(ActionTypes type, E data) {
            this.action = type;
            this.data = data;
        }

        private ActionTypes getActionType() {
            return this.action;
        }

        private E getData() {
            return this.data;
        }
    }


    public  String getName() {
        return name;
    }

    public List<T> getGrades() {
        return new ArrayList<>(marks);
    }

    private void changeName(String new_name) {
        name = new_name;
    }
    private void addGrade(T mark) {
        if (!validator.test(mark)) {
            throw new IllegalArgumentException("Некорректная оценка: " + mark);
        }
        marks.add(mark);
    }
    private void removeGrade(T mark) {
        for (int i = this.marks.size() - 1; i >= 0; --i) {
            if(this.marks.get(i).equals(mark)) {
                this.marks.remove(i);
                return;
            }
        }
    }


    public void ChangeName(String newName) {
        if (newName.isEmpty()) {
            throw new IllegalArgumentException("Пожалуйста, укажите корректное имя");
        }
        this.addNewLastAction(new Action<>(ActionTypes.CHANGE_NAME, this.name));
        this.changeName(newName);
    }
    public void  AddGrade(T mark) {
        this.addGrade(mark);
        this.addNewLastAction(new Action<>(ActionTypes.ADD_GRADE, mark));
    }
    public void RemoveGrade(T mark) {
        this.removeGrade(mark);
        addNewLastAction(new Action<>(ActionTypes.REMOVE_GRADE, mark));
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // та же ссылка
        if (obj == null || getClass() != obj.getClass()) return false;

        Student<?> other = (Student<?>) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.marks, other.marks);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, marks);
    }
    @Override public String toString() { return name + ": " + marks; }



    public Student(String new_name, Predicate<T> validator) {
       this(new_name, new ArrayList<>(), validator);
    }
    public Student(String name) {
        this(name, new ArrayList<>(),x -> true);
    }
    public Student(String new_name, List<T> new_marks, Predicate<T> new_validator) {
        if (new_name.equals("")) {
            throw new IllegalArgumentException("Некорретное имя студента");
        }
        this.name = new_name;
        for (int i = 0; i < new_marks.size(); ++i) {
            if (!new_validator.test(new_marks.get(i))) {
                throw new IllegalArgumentException("Некорректная оценка студента");
            }
        }
        this.marks = new ArrayList<T>(new_marks);
        this.validator = new_validator;
    }
    public Student(String new_name, List<T> new_marks) {
        this(new_name, new_marks, x -> true);
    }

    private Action<?> getLastAction() {
        return history.getLast();
    }
    private void removeLastAction() {
        history.remove(getLastAction());
    }
    private void addNewLastAction(Action<?> action) {
        history.add(action);
    }
    private void applyAction(Action<?> action) {
        if (action.getActionType() == ActionTypes.ADD_GRADE) {
            this.removeGrade((T) action.getData());
        } else if (action.getActionType() == ActionTypes.REMOVE_GRADE) {
            this.addGrade((T) action.getData());
        } else if (action.getActionType() == ActionTypes.CHANGE_NAME) {
            this.changeName((String) action.getData());
        }
    }

    public void Restore() {
        if (!history.isEmpty()) {
            this.applyAction(this.getLastAction());
            this.removeLastAction();
        }
    }


    static public class Flow<T> {
        private Iterable<T> source;

        private List<Function<T, T>> functions = new ArrayList<>();
        private List<Predicate<T>> filters = new ArrayList<>();

        private Flow(Iterable<T> src) {
            this.source = src;
        }

        public static <T> Flow<T> of(List<T> values) {
            return new Flow<>(values);
        }

        @SafeVarargs
        public static <T> Flow<T> of(T... values) {
            return new Flow<>(Arrays.asList(values));
        }

        public static <T> Flow<T> iterate(T seed, UnaryOperator<T> next, Predicate<T> hasNext) {
            Iterable<T> iterable = new Iterable<T>() {
                @Override
                public Iterator<T> iterator() {
                    return new Iterator<T>() {
                        private T current = seed;

                        @Override
                        public boolean hasNext() {
                            return hasNext.test(current);
                        }

                        @Override
                        public T next() {
                            T value = current;
                            current = next.apply(value);
                            return value;
                        }
                    };
                }
            };
            return new Flow<>(iterable);
        }

        public Flow<T> function(Function<T, T> func) {
            functions.add(func);
            return this;
        }

        public Flow<T> filter(Predicate<T> pred) {
            filters.add(pred);
            return this;
        }

        public T reduce(BinaryOperator<T> binop) {
            T result = null;
            for (T item : source) {
                boolean pass = true;
                for (Predicate<T> f : filters) {
                    if (!f.test(item)) {
                        pass = false;
                        break;
                    }
                }

                if (!pass) {
                    continue;
                }

                T value = item;
                for (Function<T, T> func : functions) {
                    value = func.apply(value);
                }

                if (result == null) {
                    result = value;
                } else {
                    result = binop.apply(result, value);
                }
            }
            return result;
        }

        public <R> R collect(Supplier<R> supplier, BiConsumer<R, T> accumulator) {
            R container = supplier.get();

            for (T item : source) {
                boolean pass = true;
                for (Predicate<T> filter : filters) {
                    if (!filter.test(item)) {
                        pass = false;
                        break;
                    }
                }
                if (!pass) continue;

                T value = item;
                for (Function<T, T> function : functions) {
                    value = function.apply(value);
                }

                accumulator.accept(container, value);
            }
            return container;
        }
    }
}
