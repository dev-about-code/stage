package io.aboutcode.stage.util;

/**
 * A representation of two related elements. The main use is to easily return two elements in a java
 * stream-based operation. Use with caution, 99% of the time there are better ways to return values
 * from methods (e.g. dedicated pojo's).
 */
public final class Tuple2<T1, T2> {
    private T1 one;
    private T2 two;

    private Tuple2(T1 one, T2 two) {
        this.one = one;
        this.two = two;
    }

    /**
     * Creates a new tuple of the specified elements.
     *
     * @param one     The first element
     * @param two     The second element
     * @param <Type1> The type of the first element
     * @param <Type2> The type of the second element
     *
     * @return A new tuple of the specified elements
     */
    public static <Type1, Type2> Tuple2<Type1, Type2> of(Type1 one, Type2 two) {
        return new Tuple2<>(one, two);
    }

    /**
     * Returns the first element.
     *
     * @return The first element
     */
    public T1 one() {
        return one;
    }

    /**
     * Returns the second element.
     *
     * @return The second element
     */
    public T2 two() {
        return two;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;

        if (one != null ? !one.equals(tuple2.one) : tuple2.one != null) {
            return false;
        }
        return two != null ? two.equals(tuple2.two) : tuple2.two == null;
    }

    @Override
    public int hashCode() {
        int result = one != null ? one.hashCode() : 0;
        result = 31 * result + (two != null ? two.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tuple2 {" +
               "one=" + one +
               ", two=" + two +
               '}';
    }
}
