package io.aboutcode.stage.web.response;

import io.aboutcode.stage.web.request.Request;

/**
 * All commonly used HTTP headers.
 */
public enum HttpHeader {
    CONTENT_TYPE("Content-Type");

    private String name;

    HttpHeader(String name) {
        this.name = name;
    }

    /**
     * Sets the specified responses header of this type to the specified value.
     *
     * @param response The response to set the header on
     * @param value    The value to set the header to
     */
    public void set(Response response, String value) {
        response.header(name, value);
    }

    /**
     * Returns the requests value for the header of this type.
     *
     * @param request The request to retrieve the header value from
     * @return The corresponding value or null if the header is not set
     */
    public String get(Request request) {
        return request.header(name).orElse(null);
    }
}
