package io.aboutcode.stage.web.serialization;

import io.aboutcode.stage.web.autowire.exception.AutowiringException;
import io.aboutcode.stage.web.autowire.exception.UnauthorizedException;
import io.aboutcode.stage.web.response.InternalServerError;
import io.aboutcode.stage.web.response.NotAuthorized;
import io.aboutcode.stage.web.response.Response;
import java.util.function.Function;

/**
 * Implementations serialize an exception into a {@link Response} that can be send to the client.
 */
// todo: make extensible
public final class DefaultExceptionSerialization implements Function<Exception, Response> {
    /**
     * Attempts to smartly guess the status for a certain type of exception.
     *
     * @param exception The exception to serialize
     *
     * @return The response for the exception
     */
    @Override
    public Response apply(Exception exception) {
        try {
            throw exception;
        } catch (AutowiringException e) {
            return InternalServerError.with(e.getMessage());
        } catch (UnauthorizedException e) {
            return NotAuthorized.with(e.getMessage());
        } catch (Exception e) {
            return InternalServerError.with(e.getMessage());
        }
    }
}
