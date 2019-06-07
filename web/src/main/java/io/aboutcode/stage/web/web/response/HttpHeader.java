package io.aboutcode.stage.web.web.response;

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
}
