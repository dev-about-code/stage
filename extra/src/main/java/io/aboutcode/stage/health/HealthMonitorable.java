package io.aboutcode.stage.health;

/**
 * An implementor can be monitored by the {@link HealthManager}.
 */
public interface HealthMonitorable {
   /**
    * Returns true if the monitorable is healthy, false otherwise.
    *
    * @return True if the monitorable is healthy, false otherwise
    */
   boolean isHealthy();

   /**
    * If {@link HealthMonitorable#isHealthy()} returns false, this method is invoked to attempt and
    * recover the implementor.
    */
   void recover();
}
