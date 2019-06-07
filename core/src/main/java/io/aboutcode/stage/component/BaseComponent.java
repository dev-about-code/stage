package io.aboutcode.stage.component;

import io.aboutcode.stage.lifecycle.LifeCycleException;

/**
 * Default implementation of a {@link Component} that keeps track of its context.
 */
public abstract class BaseComponent implements Component {
    private ComponentContext componentContext;

    /**
     * Returns the {@link ComponentContext} that was retained during initialization of the
     * component
     *
     * @return The {@link ComponentContext} that was retained during initialization of the component
     */
    protected ComponentContext getComponentContext() {
        return componentContext;
    }

    /**
     * Initialized the component in-lieu of the {@link Component#init(Object)} method, which is used
     * to only assign the component context.
     *
     * @throws LifeCycleException Should be thrown if initializing the component fails for some
     *                            reason
     */
    protected void init() throws LifeCycleException {}

    /**
     * Assigns the component context internally and calls {@link BaseComponent#init()}
     *
     * @param context The context to retain
     *
     * @throws LifeCycleException Rethrows any {@link LifeCycleException} thrown by {@link
     *                            BaseComponent#init()}
     */
    @Override
    public final void init(ComponentContext context) throws LifeCycleException {
        this.componentContext = context;
        init();
    }
}
