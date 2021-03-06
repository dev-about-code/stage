package io.aboutcode.stage.web.request;

import java.io.IOException;
import java.io.InputStream;

/**
 * A part of a form submitted as <code>multipart/form-data</code>.
 */
public interface Part {
    /**
     * Gets the content of this part as an <code>InputStream</code>
     *
     * @return The content of this part as an <code>InputStream</code>
     *
     * @throws IOException If an error occurs in retrieving the contet as an
     *                     <code>InputStream</code>
     */
    InputStream getInputStream() throws IOException;

    /**
     * Gets the content type of this part.
     *
     * @return The content type of this part.
     */
    String getContentType();

    /**
     * Gets the name of this part
     *
     * @return The name of this part as a <code>String</code>
     */
    String getName();

    /**
     * Gets the part name specified by the client
     *
     * @return the submitted name
     */
    String getSubmittedFileName();

    /**
     * Returns the size of this part.
     *
     * @return a <code>long</code> specifying the size of this part, in bytes.
     */
    long getSize();
}
