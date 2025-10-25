package org.example;

import java.util.*;
import java.util.function.*;

/**
 * Класс {@code Student} описывает студента, у которого есть имя, список оценок
 * и валидатор для проверки корректности оценок.
 * Также реализована история изменений, позволяющая откатывать последние действия.
 *
 * @param <T> тип оценок (например, Integer, String, LocalDate и т.д.)
 */
public class Student<T> {
    private String name;
    private final List<T> marks;
    private final Predicate<T> validator;
    private final List<Action<?>> history = new ArrayList<>();

    /**
     * Типы действий, которые могут быть выполнены над объектом Student.
     */
    private enum ActionTypes {
        /** Добавление оценки. */
        ADD_GRADE,
        /** Удаление оценки. */
        REMOVE_GRADE,
        /** Изменение имени. */
        CHANGE_NAME
    }

    /**
     * Класс {@code Action} представляет одно действие,
     * которое может быть отменено методом {@link #Restore()}.
     *
     * @param <E> тип данных, связанный с действием
     */
    private static class Action<E> {
        private final ActionTypes action;
        private final E data;

        public Action(ActionTypes type, E data) {
            this.action = type;
            this.data = data;
        }

        private ActionTypes getActionType() { return this.action; }
        private E getData() { return this.data; }
    }

    // ------------------ Методы доступа ------------------

    /**
     * Возвращает имя студента.
     * @return имя студента
     */
    public String getName() { return name; }

    /**
     * Возвращает копию списка оценок студента.
     * @return список оценок
     */
    public List<T> getGrades() { return new ArrayList<>(marks); }

    // ------------------ Приватные методы управления ------------------

    private void changeName(String newName) { name = newName; }

    private void addGrade(T mark) {
        if (!validator.test(mark))
            throw new IllegalArgumentException("Некорректная оценка: " + mark);
        marks.add(mark);
    }

    private void removeGrade(T mark) {
        for (int i = marks.size() - 1; i >= 0; i--) {
            if (marks.get(i).equals(mark)) {
                marks.remove(i);
                return;
            }
        }
    }

    // ------------------ Публичные операции ------------------

    /**
     * Изменяет имя студента и сохраняет предыдущее значение в историю.
     *
     * @param newName новое имя
     * @throws IllegalArgumentException если имя пустое
     */
    public void ChangeName(String newName) {
        if (newName.isEmpty())
            throw new IllegalArgumentException("Пожалуйста, укажите корректное имя");
        addNewLastAction(new Action<>(ActionTypes.CHANGE_NAME, this.name));
        changeName(newName);
    }

    /**
     * Добавляет новую оценку студенту и сохраняет действие в историю.
     *
     * @param mark новая оценка
     * @throws IllegalArgumentException если оценка некорректна
     */
    public void AddGrade(T mark) {
        addGrade(mark);
        addNewLastAction(new Action<>(ActionTypes.ADD_GRADE, mark));
    }

    /**
     * Удаляет оценку и сохраняет действие в историю.
     *
     * @param mark оценка, которую нужно удалить
     */
    public void RemoveGrade(T mark) {
        removeGrade(mark);
        addNewLastAction(new Action<>(ActionTypes.REMOVE_GRADE, mark));
    }

    /**
     * Отменяет последнее действие (undo).
     * Если история пуста — ничего не происходит.
     */
    public void Restore() {
        if (!history.isEmpty()) {
            applyAction(getLastAction());
            removeLastAction();
        }
    }

    // ------------------ Методы сравнения и отображения ------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Student<?> other)) return false;
        return Objects.equals(name, other.name) && Objects.equals(marks, other.marks);
    }

    @Override
    public int hashCode() { return Objects.hash(name, marks); }

    @Override
    public String toString() { return name + ": " + marks; }

    // ------------------ Конструкторы ------------------

    /**
     * Создает студента с именем и предикатом проверки оценок.
     * @param name имя студента
     * @param validator функция, проверяющая корректность оценок
     */
    public Student(String name, Predicate<T> validator) {
        this(name, new ArrayList<>(), validator);
    }

    /**
     * Создает студента с именем без проверки оценок (все значения допустимы).
     * @param name имя студента
     */
    public Student(String name) {
        this(name, new ArrayList<>(), x -> true);
    }

    /**
     * Создает студента с именем, списком оценок и проверкой валидности.
     * @param name имя
     * @param marks список оценок
     * @param validator валидатор
     * @throws IllegalArgumentException если имя пустое или оценки некорректны
     */
    public Student(String name, List<T> marks, Predicate<T> validator) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Некорректное имя студента");
        for (T mark : marks)
            if (!validator.test(mark))
                throw new IllegalArgumentException("Некорректная оценка студента: " + mark);
        this.name = name;
        this.marks = new ArrayList<>(marks);
        this.validator = validator;
    }

    /**
     * Создает студента с именем и готовым списком оценок (без проверки).
     * @param name имя
     * @param marks список оценок
     */
    public Student(String name, List<T> marks) {
        this(name, marks, x -> true);
    }

    // ------------------ История изменений ------------------

    private Action<?> getLastAction() { return history.getLast(); }
    private void removeLastAction() { history.remove(getLastAction()); }
    private void addNewLastAction(Action<?> action) { history.add(action); }

    @SuppressWarnings("unchecked")
    private void applyAction(Action<?> action) {
        switch (action.getActionType()) {
            case ADD_GRADE -> removeGrade((T) action.getData());
            case REMOVE_GRADE -> addGrade((T) action.getData());
            case CHANGE_NAME -> changeName((String) action.getData());
        }
    }

    // ------------------ Вложенный класс Flow ------------------

    /**
     * Мини-аналог Stream API для обработки коллекций.
     *
     * @param <T> тип элементов потока
     */
    public static class Flow<T> {
        private final Iterable<T> source;
        private final List<Function<T, T>> functions = new ArrayList<>();
        private final List<Predicate<T>> filters = new ArrayList<>();

        private Flow(Iterable<T> src) { this.source = src; }

        /**
         * Создает поток из списка элементов.
         * @param values список элементов
         * @return поток Flow
         */
        public static <T> Flow<T> of(List<T> values) {
            return new Flow<>(values);
        }

        /**
         * Создает поток из произвольного количества элементов.
         * @param values элементы
         * @return поток Flow
         */
        @SafeVarargs
        public static <T> Flow<T> of(T... values) {
            return new Flow<>(Arrays.asList(values));
        }

        /**
         * Создает поток, аналогичный {@code Stream.iterate}.
         *
         * @param seed начальное значение
         * @param next функция генерации следующего элемента
         * @param hasNext условие продолжения
         * @return поток Flow
         */
        public static <T> Flow<T> iterate(T seed, UnaryOperator<T> next, Predicate<T> hasNext) {
            Iterable<T> iterable = () -> new Iterator<>() {
                private T current = seed;
                public boolean hasNext() { return hasNext.test(current); }
                public T next() {
                    T value = current;
                    current = next.apply(value);
                    return value;
                }
            };
            return new Flow<>(iterable);
        }

        /**
         * Добавляет преобразующую функцию (map).
         * @param func функция преобразования
         * @return текущий объект Flow
         */
        public Flow<T> function(Function<T, T> func) {
            functions.add(func);
            return this;
        }

        /**
         * Добавляет фильтр (filter).
         * @param pred предикат-фильтр
         * @return текущий объект Flow
         */
        public Flow<T> filter(Predicate<T> pred) {
            filters.add(pred);
            return this;
        }

        /**
         * Сокращает (агрегирует) поток, применяя бинарную операцию.
         * Аналог {@code Stream.reduce()}.
         *
         * @param binop бинарная операция
         * @return результат свёртки
         */
        public T reduce(BinaryOperator<T> binop) {
            T result = null;
            for (T item : source) {
                boolean pass = filters.stream().allMatch(f -> f.test(item));
                if (!pass) continue;

                T value = item;
                for (Function<T, T> func : functions)
                    value = func.apply(value);

                result = (result == null) ? value : binop.apply(result, value);
            }
            return result;
        }

        /**
         * Сохраняет результаты потока в коллекцию.
         * Аналог {@code Stream.collect()}.
         *
         * @param supplier поставщик контейнера
         * @param accumulator функция накопления
         * @param <R> тип контейнера
         * @return заполненный контейнер
         */
        public <R> R collect(Supplier<R> supplier, BiConsumer<R, T> accumulator) {
            R container = supplier.get();
            for (T item : source) {
                boolean pass = filters.stream().allMatch(f -> f.test(item));
                if (!pass) continue;

                T value = item;
                for (Function<T, T> func : functions)
                    value = func.apply(value);

                accumulator.accept(container, value);
            }
            return container;
        }
    }
}
