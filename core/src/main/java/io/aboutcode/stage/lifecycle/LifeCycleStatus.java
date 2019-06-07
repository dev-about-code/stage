package io.aboutcode.stage.lifecycle;

/**
 * The status a {@link LifeCycleAware} resides in. A unit will always transit from a status with a
 * lower level to one with a higher level.
 */
public enum LifeCycleStatus {
    New(100),
    Initializing(200),
    Initialized(300),
    Starting(400),
    Started(500),
    Stopping(600),
    Stopped(700),
    Destroying(800),
    Destroyed(900);

    private int level;

    LifeCycleStatus(int level) {
        this.level = level;
    }

    /**
     * Returns the numeric level of the status
     *
     * @return The numeric level of the status
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns true if this status logically happens before the specified status, i.e. if this
     * status can transit into the specified status
     *
     * @param status The status to compare to
     *
     * @return True if this status is before the specified status, false otherwise
     */
    public boolean isBefore(LifeCycleStatus status) {
        return this.level < status.level;
    }
}
