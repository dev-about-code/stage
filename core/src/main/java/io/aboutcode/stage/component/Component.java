package io.aboutcode.stage.component;

import io.aboutcode.stage.dependency.DependencyAware;
import io.aboutcode.stage.dependency.DependencyContext;
import io.aboutcode.stage.lifecycle.LifeCycleAware;

/**
 * <p>Represents a self contained unit of an application. Each component has its own life cycle,
 * managed by the {@link ComponentContainer} the component is assigned to.</p>
 *
 * <p>A component's lifecycle is considered to contain the following phases:</p>
 * <dl>
 * <dt>Init</dt>
 * <dd>The component should initialize itself in this phase and cannot use other components. After
 * conclusion of this phase, the component should be ready to accept requests from other
 * components.</dd>
 * <dt>Start</dt>
 * <dd>The component should make call to other components in this phase and should perform any
 * <em>short lived</em> business logic in the course of this method. Long-running tasks should be
 * move to a separate thread.</dd>
 * <dt>Stop</dt>
 * <dd>The component should prepare itself to be stopped by releasing and/or initializing the
 * release of resources on other components</dd>
 * <dt>Destroy</dt>
 * <dd>The component should relinquish control of any external resources it uses (such as file
 * system handles etc.) and should be ready to be removed from the container after conclusion of
 * this phase</dd>
 * </dl>
 *
 * <p><em>Note</em> that above means that the method {@link DependencyAware#resolve(DependencyContext)}
 * will be called after {@link LifeCycleAware#init(Object)}, but before {@link
 * LifeCycleAware#start()}.</p>
 */
public interface Component extends LifeCycleAware<ComponentContext>, DependencyAware {
}
