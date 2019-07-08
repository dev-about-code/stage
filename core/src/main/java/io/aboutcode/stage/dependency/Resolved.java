package io.aboutcode.stage.dependency;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Fields annotated with this type partake in the dependency resolving mechanism. Using this
 * annotation removes the need for manually implementing the {@link DependencyAware#resolve(DependencyContext)}
 * method.</p>
 * <p>Note that if {@link DependencyAware#resolve(DependencyContext)} is implemented alongside the
 * use of this annotation, the assignments in the method take preference! However, both the
 * annotation as well as the method are processed, hence if the dependency declarations do not
 * match, both have still to be valid.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Resolved {
    /**
     * <p>The identifier of the dependency that should be retrieved if multiple identical
     * dependencies are available.</p>
     * <p>Notice that the type of the identifier is <code>String</code> due to limitations of java
     * annotations. Since using <code>String</code> as identifier type is discouraged, it is
     * recommended to implement the {@link DependencyAware#resolve(DependencyContext)} method
     * instead when using identifiers.</p>
     *
     * @return The identifier of the dependency to retrieve
     */
    String identifier() default "";

    /**
     * Returns whether the dependency is mandatory or not. If this is false and the dependency is
     * not found, the member is assigned <code>null</code> instead. Otherwise, an exception is
     * thrown.
     *
     * @return True if the dependency is required, false otherwise
     */
    boolean mandatory() default true;
}
