package io.aboutcode.stage.web.autowire.auth;

import io.aboutcode.stage.web.request.Request;

/**
 * Default security realm that authorizes every subject for every request.
 */
public final class PermissiveAuthorizationRealm implements AuthorizationRealm {
    /**
     * Always returns true
     *
     * @param request   The request that the subject would like to execute
     *
     * @return True
     */
    @Override
    public boolean isAuthorized(Request request) {
        return true;
    }
}
