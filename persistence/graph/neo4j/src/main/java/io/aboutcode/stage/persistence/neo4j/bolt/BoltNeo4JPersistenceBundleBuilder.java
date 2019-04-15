package io.aboutcode.stage.persistence.neo4j.bolt;

import io.aboutcode.stage.application.ApplicationAssemblyContext;
import io.aboutcode.stage.component.ComponentBundle;
import io.aboutcode.stage.component.ComponentContainer;
import io.aboutcode.stage.configuration.ApplicationConfigurationContext;
import io.aboutcode.stage.configuration.ConfigurationParameter;
import io.aboutcode.stage.persistence.Persistence;
import io.aboutcode.stage.persistence.neo4j.Neo4JDatabaseConfiguration;
import io.aboutcode.stage.persistence.neo4j.Neo4JPersistence;

/**
 * This adds a Bolt connected Neo4J persistence component to the application. The bundle will add
 * the correct parameters to the application to allow configuration of the datastore.
 */
public final class BoltNeo4JPersistenceBundleBuilder {
   private static final int DEFAULT_PORT = 7687;
   private String configurationPrefix;
   private Object componentIdentifier;

   private BoltNeo4JPersistenceBundleBuilder() {
   }

   private BoltNeo4JPersistenceBundleBuilder(String configurationPrefix,
                                             Object componentIdentifier) {
      this.componentIdentifier = componentIdentifier;
      this.configurationPrefix = configurationPrefix;
   }

   /**
    * Creates a new builder for a BoltNeo4jPersistence {@link ComponentBundle}.
    *
    * @return A new builder for a BoltNeo4jPersistence {@link ComponentBundle}
    */
   public static BoltNeo4JPersistenceBundleBuilder create() {
      return new BoltNeo4JPersistenceBundleBuilder();
   }

   /**
    * Assigns the prefix that all configuration parameters will use. Defaults to an empty string.
    *
    * @param configurationPrefix An identifier of this bundle; prefixes parameter names. <em>Omit
    *                            trailing dashes</em>
    *
    * @return A new builder instance
    */
   public BoltNeo4JPersistenceBundleBuilder withPrefix(String configurationPrefix) {
      return new BoltNeo4JPersistenceBundleBuilder(
          configurationPrefix,
          this.componentIdentifier
      );
   }

   /**
    * Assigns the identifier for the {@link Persistence} component. Defaults to null.
    *
    * @param componentIdentifier An identifier for the {@link Persistence} component that will be
    *                            added to the {@link ComponentContainer}. This can later be used to
    *                            retrieve a specified {@link Persistence} component if multiple are
    *                            added to the container
    *
    * @return A new builder instance
    */
   public BoltNeo4JPersistenceBundleBuilder withIdentifier(Object componentIdentifier) {
      return new BoltNeo4JPersistenceBundleBuilder(
          this.configurationPrefix,
          componentIdentifier
      );
   }

   /**
    * Builds the component bundle.
    *
    * @return The component bundle that can be added to a {@link ComponentContainer}
    */
   public ComponentBundle build() {
      return new ComponentBundle() {
         private String host;
         private Integer port;
         private String username;
         private String password;

         @Override
         public void configure(ApplicationConfigurationContext context) {
            String parameterIdentifier = configurationPrefix;
            if (!parameterIdentifier.isEmpty() && !parameterIdentifier.endsWith("-")) {
               parameterIdentifier += "-";
            }

            context.addConfigurationParameter(ConfigurationParameter
                                                  .String(parameterIdentifier + "database-host",
                                                          "The database server host name to connect to",
                                                          true, null, value -> host = value));
            context.addConfigurationParameter(ConfigurationParameter
                                                  .Integer(parameterIdentifier + "database-port",
                                                           "The port to which to connect on the database server",
                                                           false, null, value -> port = value));
            context.addConfigurationParameter(ConfigurationParameter
                                                  .String(parameterIdentifier + "database-username",
                                                          "The username to connect to the database with",
                                                          true, null, value -> username = value));
            context.addConfigurationParameter(ConfigurationParameter
                                                  .String(parameterIdentifier + "database-password",
                                                          "The password to connect to the database with",
                                                          true, null, value -> password = value));
         }

         @Override
         public void assemble(ApplicationAssemblyContext context) {
            Neo4JPersistence neo4JPersistence =
                new Neo4JPersistence(
                    Neo4JDatabaseConfiguration.bolt(host,
                                                    port == null ? DEFAULT_PORT : port,
                                                    username,
                                                    password));
            context.addComponent(componentIdentifier, neo4JPersistence);
         }
      };
   }
}
