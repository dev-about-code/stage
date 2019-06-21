package io.aboutcode.stage.persistence.neo4j;

import com.zaxxer.hikari.HikariConfig;
import io.aboutcode.stage.application.ApplicationAssemblyContext;
import io.aboutcode.stage.component.ComponentBundle;
import io.aboutcode.stage.component.ComponentContainer;
import io.aboutcode.stage.configuration.ApplicationConfigurationContext;
import io.aboutcode.stage.configuration.Parameter;
import io.aboutcode.stage.persistence.Persistence;
import io.aboutcode.stage.persistence.jdbc.JDBCDatabaseConfiguration;
import io.aboutcode.stage.persistence.jdbc.JDBCPersistence;
import io.aboutcode.stage.persistence.neo4j.bolt.BoltNeo4JDatabaseConfiguration;
import io.aboutcode.stage.persistence.neo4j.bolt.BoltNeo4JPersistence;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.jdbc.Neo4jDataSource;

/**
 * This adds a Neo4J persistence component to the application. The bundle will add the correct
 * parameters to the application to allow configuration of the datastore.
 */
public final class Neo4JPersistenceBundleBuilder {
    private static final int DEFAULT_PORT = 7687;
    private String configurationPrefix;
    private Object componentIdentifier;
    private Object jdbcIdentifier;

    private Neo4JPersistenceBundleBuilder() {
    }

    private Neo4JPersistenceBundleBuilder(String configurationPrefix,
                                          Object componentIdentifier,
                                          Object jdbcIdentifier) {
        this.componentIdentifier = componentIdentifier;
        this.configurationPrefix = configurationPrefix;
        this.jdbcIdentifier = jdbcIdentifier;
    }

    /**
     * Creates a new builder for a BoltNeo4jPersistence {@link ComponentBundle}.
     *
     * @return A new builder for a BoltNeo4jPersistence {@link ComponentBundle}
     */
    public static Neo4JPersistenceBundleBuilder create() {
        return new Neo4JPersistenceBundleBuilder();
    }

    /**
     * Assigns the prefix that all configuration parameters will use. Defaults to an empty string.
     *
     * @param configurationPrefix An identifier of this bundle; prefixes parameter names. <em>Omit
     *                            trailing dashes</em>
     *
     * @return A new builder instance
     */
    public Neo4JPersistenceBundleBuilder withPrefix(String configurationPrefix) {
        return new Neo4JPersistenceBundleBuilder(
                configurationPrefix,
                this.componentIdentifier,
                this.jdbcIdentifier
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
    public Neo4JPersistenceBundleBuilder withIdentifier(Object componentIdentifier) {
        return new Neo4JPersistenceBundleBuilder(
                this.configurationPrefix,
                componentIdentifier,
                this.jdbcIdentifier
        );
    }

    /**
     * Also adds a JDBC-wrapped connection to the Neo4J database to the application and assigns it
     * the specified identifier.
     *
     * @param jdbcIdentifier The identifier for the {@link Persistence} component that will be added
     *                       to the {@link ComponentContainer}. This can later be used to retrieve a
     *                       specified {@link Persistence} component if multiple are added to the
     *                       container. Must not be null.
     *
     * @return A new builder instance
     */
    public Neo4JPersistenceBundleBuilder addJDBCWrapper(Object jdbcIdentifier) {
        return new Neo4JPersistenceBundleBuilder(
                this.configurationPrefix,
                this.componentIdentifier,
                jdbcIdentifier
        );
    }

    /**
     * Builds the component bundle.
     *
     * @return The component bundle that can be added to a {@link ComponentContainer}
     */
    public ComponentBundle build() {
        return new ComponentBundle() {
            private Neo4JDatabaseConfiguration configuration;

            @Override
            public void configure(ApplicationConfigurationContext context) {
                String parameterIdentifier = configurationPrefix;
                if (!parameterIdentifier.isEmpty() && !parameterIdentifier.endsWith("-")) {
                    parameterIdentifier += "-";
                }

                configuration = context.addConfigurationObject(parameterIdentifier,
                                                               new Neo4JDatabaseConfiguration());
            }

            @Override
            public void assemble(ApplicationAssemblyContext context) {
                BoltNeo4JPersistence boltNeo4JPersistence = new BoltNeo4JPersistence(configuration);
                context.addComponent(componentIdentifier, boltNeo4JPersistence);

                if (!Objects.isNull(jdbcIdentifier)) {
                    context.addComponent(jdbcIdentifier, new JDBCPersistence(configuration));
                }
            }
        };
    }

    private class Neo4JDatabaseConfiguration implements JDBCDatabaseConfiguration,
            BoltNeo4JDatabaseConfiguration {
        @Parameter(name = "database-host", description = "The database server host name to connect to")
        private String host;
        @Parameter(name = "database-port", description = "The port to which to connect on the database server", mandatory = false)
        private Integer port = DEFAULT_PORT;
        @Parameter(name = "database-name", description = "The name of the database (schema) to connect to")
        private String database;
        @Parameter(name = "database-username", description = "The username to connect to the database with")
        private String username;
        @Parameter(name = "database-password", description = "The password to connect to the database with")
        private String password;

        @Override
        public HikariConfig apply(HikariConfig targetConfiguration) {
            targetConfiguration.setDataSourceClassName(Neo4jDataSource.class.getName());
            targetConfiguration.addDataSourceProperty("serverName", host);
            if (port != null) {
                targetConfiguration.addDataSourceProperty("port", port);
            }
            targetConfiguration.addDataSourceProperty("databaseName", database);
            targetConfiguration.addDataSourceProperty("user", username);
            targetConfiguration.addDataSourceProperty("password", password);
            return targetConfiguration;
        }

        @Override
        public Driver apply() {
            Config config = Config
                    .build()
                    .withEncryption()
                    .withLeakedSessionsLogging()
                    .withLoadBalancingStrategy(Config.LoadBalancingStrategy.LEAST_CONNECTED)
                    .toConfig();
            URI uri;
            try {
                uri = new URI("bolt", null, host, port, null, null, null);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Could not create connection url because: " +
                                                   e.getMessage(), e);
            }
            return GraphDatabase.driver(uri, AuthTokens.basic(username, password), config);
        }
    }
}
