package io.aboutcode.stage.util;

import java.util.function.Supplier;

/**
 * A supplier that can also throw an exception.
 *
 * @param <R>          The type object the supplier generates
 * @param <ExceptionT> The type of exception this supplier can throw
 */
public interface ThrowingSupplier<R, ExceptionT extends Throwable> extends Supplier<R> {
    /**
     * Returns the result.
     *
     * @return the result
     *
     * @throws ExceptionT Can be thrown by the operation
     */
    R getThrowing() throws ExceptionT;

    @Override
    default R get() {
        try {
            return getThrowing();
        } catch (Throwable exceptionT) {
            throw new RuntimeException(exceptionT);
        }
    }
}
