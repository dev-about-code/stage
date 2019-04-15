package io.aboutcode.stage.persistence.jdbc.mysql;

import io.aboutcode.stage.application.ApplicationAssemblyContext;
import io.aboutcode.stage.component.ComponentBundle;
import io.aboutcode.stage.component.ComponentContainer;
import io.aboutcode.stage.configuration.ApplicationConfigurationContext;
import io.aboutcode.stage.configuration.Parameter;
import io.aboutcode.stage.persistence.Persistence;
import io.aboutcode.stage.persistence.jdbc.JDBCDatabaseConfiguration;
import io.aboutcode.stage.persistence.jdbc.JDBCPersistence;

/**
 * This adds a MySQL connected persistence component to the application. The bundle will add the
 * correct parameters to the application to allow configuration of the MySQL database.
 */
public final class MySQLPersistenceBundleBuilder {

   private final Class datasourceClass;
   private String configurationPrefix;
   private Object componentIdentifier;

   private MySQLPersistenceBundleBuilder(Class datasourceClass) {
      this.datasourceClass = datasourceClass;
   }

   private MySQLPersistenceBundleBuilder(Class datasourceClass, String configurationPrefix,
                                         Object componentIdentifier) {
      this(datasourceClass);
      this.componentIdentifier = componentIdentifier;
      this.configurationPrefix = configurationPrefix;
   }

   /**
    * Creates a new builder for a MySQLPersistence {@link ComponentBundle}.
    *
    * @param datasourceClass The datasource class to use for the connection, usually retrieved from
    *                        the driver for the target database
    *
    * @return A new builder for a MySQL Persistence {@link ComponentBundle}
    */
   public static MySQLPersistenceBundleBuilder createFor(Class datasourceClass) {
      return new MySQLPersistenceBundleBuilder(datasourceClass);
   }

   /**
    * Assigns the prefix that all configuration parameters will use. Defaults to an empty string.
    *
    * @param configurationPrefix An identifier of this bundle; prefixes parameter names. <em>Omit
    *                            trailing dashes</em>
    *
    * @return A new builder instance
    */
   public MySQLPersistenceBundleBuilder withPrefix(String configurationPrefix) {
      return new MySQLPersistenceBundleBuilder(
          this.datasourceClass,
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
   public MySQLPersistenceBundleBuilder withIdentifier(Object componentIdentifier) {
      return new MySQLPersistenceBundleBuilder(
          this.datasourceClass,
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
         private MySQLConfiguration configuration;

         @Override
         public void configure(ApplicationConfigurationContext context) {
            configuration = context
                .addConfigurationObject(configurationPrefix, new MySQLConfiguration());
         }

         @Override
         public void assemble(ApplicationAssemblyContext context) {
            JDBCPersistence jdbcPersistence =
                new JDBCPersistence(JDBCDatabaseConfiguration.MySQL(datasourceClass,
                                                                    configuration.getHost(),
                                                                    configuration.getDatabase(),
                                                                    configuration.getUsername(),
                                                                    configuration.getPassword(),
                                                                    configuration.getPort()));
            context.addComponent(componentIdentifier, jdbcPersistence);
         }
      };
   }


   private class MySQLConfiguration {
      @Parameter(name = "database-host", description = "The database server host name to connect to")
      private String host;
      @Parameter(name = "database-port", description = "The port to which to connect on the database server", mandatory = false)
      private Integer port;
      @Parameter(name = "database-name", description = "The name of the database (schema) to connect to")
      private String database;
      @Parameter(name = "database-username", description = "The username to connect to the database with")
      private String username;
      @Parameter(name = "database-password", description = "The password to connect to the database with")
      private String password;

      MySQLConfiguration() {
      }

      private String getHost() {
         return host;
      }

      private Integer getPort() {
         return port;
      }

      private String getUsername() {
         return username;
      }

      private String getPassword() {
         return password;
      }

      private String getDatabase() {
         return database;
      }
   }
}
