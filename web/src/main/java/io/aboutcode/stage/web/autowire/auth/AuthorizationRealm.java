package io.aboutcode.stage.web.autowire.auth;

import io.aboutcode.stage.web.request.Request;

/**
 * An implementation of this authorizes a subject within a specific context of an application (a.k.a
 * "realm").
 */
public interface AuthorizationRealm {
    /**
     * Returns whether the specified request is authorized to be executed.
     *
     * @param request The request that the subject would like to execute
     *
     * @return True if the request is authorized, false otherwise
     */
    boolean isAuthorized(Request request);
}
