package io.aboutcode.stage.web.request;

import io.aboutcode.stage.web.response.Response;

/**
 * A handler for a request. Since request handlers can be chained, it is not guaranteed that an
 * instance produces the result actually returned to the client.
 */
public interface RequestHandler {
    /**
     * Processes the specified requests and returns an appropriate response that can be returned to
     * the caller.
     *
     * @param request         The request to process
     * @param currentResponse The current response created by any request handlers that were called
     *                        before this one
     *
     * @return A response that this handler would like to return to the caller. Note that subsequent
     * request handlers can modify this response, hence it is not guaranteed that this response will
     * reach the caller
     *
     * @throws Exception Thrown if processing the request caused an exception
     */
    Response process(Request request, Response currentResponse) throws Exception;
}
