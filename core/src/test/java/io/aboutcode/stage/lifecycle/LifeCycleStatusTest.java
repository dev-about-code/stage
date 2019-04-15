package io.aboutcode.stage.lifecycle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LifeCycleStatusTest {

   @Test
   public void before() {
      assertTrue(LifeCycleStatus.New.isBefore(LifeCycleStatus.Started));
   }

   @Test
   public void notBefore() {
      assertFalse(LifeCycleStatus.Stopped.isBefore(LifeCycleStatus.Started));
   }

   @Test
   public void equal() {
      assertFalse(LifeCycleStatus.Stopped.isBefore(LifeCycleStatus.Stopped));
   }
}