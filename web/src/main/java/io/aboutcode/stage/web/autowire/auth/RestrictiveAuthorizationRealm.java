package io.aboutcode.stage.web.autowire.auth;

import io.aboutcode.stage.web.request.Request;

/**
 * Default security realm that authorizes no subject for any request.
 */
public final class RestrictiveAuthorizationRealm implements AuthorizationRealm {
    /**
     * Always returns false
     *
     * @param request   The request that the subject would like to execute
     *
     * @return False
     */
    @Override
    public boolean isAuthorized(Request request) {
        return false;
    }
}
