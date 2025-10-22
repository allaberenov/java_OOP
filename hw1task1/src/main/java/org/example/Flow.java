package org.example;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Flow — ленивый поток значений с возможностью цепочек операций.
 * Операции function (map) и filter выполняют только запись операции (O(1)).
 */
public class Flow<T> {
    private final Supplier<Iterator<T>> sourceSupplier;
    private final List<UnaryOperator<Object>> maps; // касты для хранения разных типов
    private final List<Predicate<Object>> filters;

    private Flow(Supplier<Iterator<T>> sourceSupplier) {
        this.sourceSupplier = sourceSupplier;
        this.maps = new ArrayList<>();
        this.filters = new ArrayList<>();
    }

    // фабрики
    public static <T> Flow<T> of(List<T> list) {
        Objects.requireNonNull(list);
        return new Flow<>(() -> list.iterator());
    }

    @SafeVarargs
    public static <T> Flow<T> of(T... values) {
        List<T> list = Arrays.asList(values);
        return new Flow<>(() -> list.iterator());
    }

    public static <T> Flow<T> generate(T seed, UnaryOperator<T> next, Predicate<T> until) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(until);
        return new Flow<>(() -> new Iterator<T>() {
            T current = seed;
            boolean started = false;
            boolean finished = false;

            @Override
            public boolean hasNext() {
                if (finished) return false;
                if (!started) {
                    started = true;
                    if (until.test(current)) {
                        finished = true;
                        return false;
                    }
                    return true;
                }
                T nxt = next.apply(current);
                if (until.test(nxt)) {
                    finished = true;
                    return false;
                }
                current = nxt;
                return true;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return current;
            }
        });
    }

    /**
     * Функция-преобразование: применяется к каждому элементу при materialize (reduce/collect).
     * O(1) — просто добавляет операцию.
     */
    public <R> Flow<R> map(Function<? super T, ? extends R> fn) {
        Objects.requireNonNull(fn);
        Flow<R> casted = (Flow<R>) this;
        casted.maps.add((UnaryOperator<Object>) obj -> fn.apply((T) obj));
        return casted;
    }

    /**
     * Фильтр: сохраняется как операция (O(1)).
     */
    public Flow<T> filter(Predicate<? super T> pred) {
        Objects.requireNonNull(pred);
        this.filters.add(obj -> pred.test((T) obj));
        return this;
    }

    /**
     * Сокращение (reduce): выполняет последовательное объединение элементов и возвращает Optional результата.
     */
    public Optional<T> reduce(BinaryOperator<T> op) {
        Objects.requireNonNull(op);
        Iterator<T> it = sourceSupplier.get();
        boolean seen = false;
        T acc = null;
        while (it.hasNext()) {
            Object el = it.next();
            if (!passesFilters(el)) continue;
            Object mapped = applyMaps(el);
            @SuppressWarnings("unchecked")
            T val = (T) mapped;
            if (!seen) {
                acc = val;
                seen = true;
            } else {
                acc = op.apply(acc, val);
            }
        }
        return seen ? Optional.of(acc) : Optional.empty();
    }

    /**
     * Коллекционирование: supplier создаёт начальное состояние коллекции, accumulator добавляет элемент.
     */
    public <C> C collect(Supplier<C> supplier, BiConsumer<C, ? super T> accumulator) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        C container = supplier.get();
        Iterator<T> it = sourceSupplier.get();
        while (it.hasNext()) {
            Object el = it.next();
            if (!passesFilters(el)) continue;
            Object mapped = applyMaps(el);
            @SuppressWarnings("unchecked")
            T val = (T) mapped;
            accumulator.accept(container, val);
        }
        return container;
    }

    // вспомогательные методы
    private boolean passesFilters(Object el) {
        for (Predicate<Object> p : filters) {
            if (!p.test(el)) return false;
        }
        return true;
    }

    private Object applyMaps(Object el) {
        Object cur = el;
        for (UnaryOperator<Object> m : maps) {
            cur = m.apply(cur);
        }
        return cur;
    }
}