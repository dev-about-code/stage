package io.aboutcode.stage.component;

import io.aboutcode.stage.dependency.DependencyException;
import io.aboutcode.stage.lifecycle.LifeCycleException;
import io.aboutcode.stage.lifecycle.LifeCycleStatus;
import io.aboutcode.stage.util.Action;
import io.aboutcode.stage.util.ThrowingAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A container for {@link Component}s which takes care of the components' lifecycle and dependency
 * management.
 */
public class ComponentContainer {
   private final Object monitor = new Object();
   private final Map<Object, ComponentController> controllers = new HashMap<>();
   private final List<Object> controllerOrder = new ArrayList<>();
   private String identifier;
   private Logger logger;
   private final StateMachine stateMachine = new StateMachineBuilder()
       .addState("New", new New())
       .addState("Initializing", new Initializing())
       .addState("Initialized", new Initialized())
       .addState("Starting", new Starting())
       .addState("Started", new Started())
       .addState("Running", new Running())
       .addState("Stopping", new Stopping())
       .addState("Stopped", new Stopped())
       .addState("Destroying", new Destroying())
       .addState("Destroyed", new Destroyed())
       .addTransition("New", "Initializing", "Destroyed")
       .addTransition("Initializing", "Initialized", "Destroying")
       .addTransition("Initialized", "Starting", "Stopped")
       .addTransition("Starting", "Started", "Stopping")
       .addTransition("Started", "Running", "Stopping")
       .addTransition("Running", "Stopping", "Stopping")
       .addTransition("Stopping", "Stopped", "Stopped")
       .addTransition("Stopped", "Destroying", "Destroying")
       .addTransition("Destroying", "Destroyed", "Destroyed")
       .addTransition("Destroyed", null, null)
       .withInitialState("New");
   private Action shutdownFunction;

   /**
    * Creates a new instance with the specified identifier and shutdown callback.
    *
    * @param identifier       The identifier of this component container, used in log messages to
    *                         identify the source of each message
    * @param shutdownCallback The callback that is invoked if a component within the container
    *                         requests the container to shut down
    */
   public ComponentContainer(String identifier, Action shutdownCallback) {
      this.identifier = identifier;
      this.shutdownFunction = Objects.requireNonNull(shutdownCallback);
      logger = LoggerFactory.getLogger(getClass().getName() + "_" + identifier);
   }

   /**
    * Returns whether the container is still running.
    *
    * @return True if the container is currently running, false otherwise
    */
   public boolean isRunning() {
      return !stateMachine.isFinished();
   }

   /**
    * Starts the component container if it has not been started before. Otherwise, does nothing.
    */
   public void start() {
      if (!stateMachine.isInInitialState()) {
         logger.warn(String.format("ComponentContainer '%s' is already started", identifier));
         return;
      }

      stateMachine.transitionTo("Running");
   }

   /**
    * Stops the component container if it is in started state. Otherwise, does nothing.
    */
   public void stop() {
      if (stateMachine.isInInitialState()) {
         logger.warn(String.format("ComponentContainer '%s' is not yet started", identifier));
         return;
      }

      if (stateMachine.isFinished()) {
         logger.warn(String.format("ComponentContainer '%s' is already stopped", identifier));
         return;
      }

      stateMachine.transitionTo("Destroyed");
   }

   /**
    * Add a component to the container and register it for lifecycle management.
    *
    * @param identifier The unique identifier for this component
    * @param component  The component to manage
    *
    * @return The component itself
    */
   public <ComponentT extends Component> ComponentT addComponent(Object identifier,
                                                                 ComponentT component) {
      synchronized (monitor) {
         if (!stateMachine.isInInitialState()) {
            throw new IllegalStateException(String.format(
                "Trying to add component '%s' to container after initialization", identifier));
         }

         Object actualIdentifier = identifier == null ? component.getClass() : identifier;

         if (controllerOrder.contains(actualIdentifier)) {
            throw new IllegalStateException(
                String.format("Duplicate component found: %s", actualIdentifier));
         }

         Logger logger = LoggerFactory
             .getLogger(ComponentContainer.this.identifier + "_" + actualIdentifier);
         ComponentController controller = new ComponentController(actualIdentifier, component,
                                                                  logger);

         controllers.put(actualIdentifier, controller);
         synchronized (controllerOrder) {
            controllerOrder.add(actualIdentifier);
         }
      }

      return component;
   }

   private interface State {
      State transition();

      String getIdentifier();
   }

   private interface StateMachine {
      boolean isInInitialState();

      void transitionTo(String started);

      boolean isFinished();
   }

   private class ComponentController implements ComponentContext {
      private final Object monitor = new Object();
      private Object identifier;
      private Component component;
      private Logger logger;
      private LifeCycleStatus status = LifeCycleStatus.New;

      ComponentController(Object identifier, Component component, Logger logger) {
         this.identifier = identifier;
         this.component = component;
         this.logger = logger;
      }

      @Override
      public Object getIdentifier() {
         return identifier;
      }

      @Override
      public LifeCycleStatus getStatus() {
         synchronized (monitor) {
            return status;
         }
      }

      void init() throws LifeCycleException {
         transitState(LifeCycleStatus.Initializing, LifeCycleStatus.Initialized,
                      () -> component.init(this));
      }

      void start() throws LifeCycleException {
         transitState(LifeCycleStatus.Starting, LifeCycleStatus.Started, component::start);
      }

      void stop() {
         try {
            transitState(LifeCycleStatus.Stopping, LifeCycleStatus.Stopped, component::stop);
         } catch (LifeCycleException e) {
            // never thrown
         }
      }

      void destroy() {
         try {
            transitState(LifeCycleStatus.Destroying, LifeCycleStatus.Destroyed, component::destroy);
         } catch (LifeCycleException e) {
            // never thrown
         }
      }

      private void transitState(LifeCycleStatus start, LifeCycleStatus end,
                                ThrowingAction<LifeCycleException> action)
          throws LifeCycleException {
         synchronized (monitor) {
            status = start;
         }
         try {
            action.tryAccept();
         } catch (Exception e) {
            throw new LifeCycleException(e);
         }
         synchronized (monitor) {
            status = end;
         }
      }

      @Override
      public void requestTermination(String reason, Exception cause) {
         logger.warn(String
                         .format("Component requests termination '%s' because: %s", getIdentifier(),
                                 reason), cause);
         shutdownFunction.accept();
      }
   }

   private class StateMachineBuilder {
      private final Map<String, StateActivity> identifierToInternalState;
      private final Map<String, State> identifierToState;

      private StateMachineBuilder() {
         this.identifierToInternalState = new HashMap<>();
         this.identifierToState = new HashMap<>();
      }

      StateMachineBuilder addState(String identifier, StateActivity activity) {
         identifierToInternalState.put(identifier, activity);
         return this;
      }

      StateMachine withInitialState(String identifier) {
         return new StateMachine() {
            private State currentState = identifierToState.get(identifier);
            private boolean inInitialState = true;

            @Override
            public void transitionTo(String targetState) {
               inInitialState = false;
               do {
                  currentState = currentState.transition();
               }
               while (currentState != null && !Objects
                   .equals(targetState, currentState.getIdentifier()));
            }

            @Override
            public boolean isInInitialState() {
               return inInitialState;
            }

            @Override
            public boolean isFinished() {
               return currentState == null;
            }
         };
      }

      StateMachineBuilder addTransition(String from, String successState, String errorState) {
         identifierToState.put(from, new State() {
            @Override
            public State transition() {
               try {
                  if (identifierToInternalState.get(from).process()) {
                     return identifierToState.get(successState);
                  }
               } catch (Exception e) {
                  logger.warn(String.format("Exception while processing state '%s': %s", from,
                                            e.getMessage()), e);
               }
               return identifierToState.get(errorState);
            }

            @Override
            public String getIdentifier() {
               return from;
            }

            @Override
            public String toString() {
               return String.format("%s -> (%s | %s)", from, successState, errorState);
            }
         });
         return this;
      }
   }

   private abstract class StateActivity {
      boolean forEachController(Function<ComponentController, Boolean> processingFunction) {
         ArrayList<Object> order;
         synchronized (controllerOrder) {
            order = new ArrayList<>(controllerOrder);
         }

         return order
             .stream()
             .map(componentKey -> processingFunction.apply(controllers.get(componentKey)))
             .filter(result -> !result)
             .findFirst()
             .orElse(true);
      }

      abstract boolean process();
   }

   private class New extends StateActivity {
      @Override
      public boolean process() {
         logger.info("Starting application...");
         return true;
      }
   }

   private class Initializing extends StateActivity {
      @Override
      public boolean process() {
         return forEachController(controller -> {
            try {
               controller.init();
               logger.info("Initialized component " + controller.getIdentifier());
               return true;
            } catch (Exception e) {
               logger.error(String.format("Exception initializing component '%s': %s",
                                          controller.getIdentifier(), e.getMessage()), e);
            }

            return false;
         });
      }
   }

   private class Initialized extends StateActivity {
      @Override
      public boolean process() {
         List<Object> componentOrder;
         try {
            componentOrder = DependencyTreeBuilder
                .buildTree(controllers
                               .entrySet()
                               .stream()
                               .collect(Collectors.toMap(Map.Entry::getKey,
                                                         entry -> entry.getValue().component)));
         } catch (DependencyException e) {
            logger.error(String.format("Dependency exception detected: %s", e.getMessage()));
            return false;
         }

         synchronized (controllerOrder) {
            controllerOrder.clear();
            controllerOrder.addAll(componentOrder);
         }

         logger.info("All components initialized");

         return true;
      }
   }

   private class Starting extends StateActivity {
      @Override
      public boolean process() {
         return forEachController(controller -> {
            try {
               controller.start();
               logger.info("Started component " + controller.getIdentifier());
               return true;
            } catch (Exception e) {
               logger.error(String.format("Exception starting component '%s': %s",
                                          controller.getIdentifier(), e.getMessage()), e);
            }

            return false;
         });
      }
   }

   private class Started extends StateActivity {
      @Override
      public boolean process() {

         logger.info("Application started");

         return true;
      }
   }

   private class Running extends StateActivity {
      @Override
      public boolean process() {
         return true;
      }
   }

   private class Stopping extends StateActivity {
      @Override
      public boolean process() {
         return forEachController(controller -> {

            try {
               controller.stop();
               if (controller.getStatus() == LifeCycleStatus.Stopped) {
                  logger.info("Stopped component " + controller.getIdentifier());
               }
            } catch (Exception e) {
               logger.error(String.format("Exception stopping component '%s': %s",
                                          controller.getIdentifier(), e.getMessage()), e);
            }
            return true; // we ignore the exception - all components must shut down
         });
      }
   }

   private class Stopped extends StateActivity {
      @Override
      public boolean process() {
         return true;
      }
   }

   private class Destroying extends StateActivity {
      @Override
      public boolean process() {
         return forEachController(controller -> {
            try {
               controller.destroy();
               if (controller.getStatus() == LifeCycleStatus.Destroyed) {
                  logger.info("Destroyed component " + controller.getIdentifier());
               }
            } catch (Exception e) {
               logger.error(String.format("Exception destroying component '%s': %s",
                                          controller.getIdentifier(), e.getMessage()), e);
            }
            return true; // we ignore the exception - all components must shut down
         });
      }
   }

   private class Destroyed extends StateActivity {
      @Override
      public boolean process() {
         logger.info("Application stopped");
         return true;
      }
   }
}