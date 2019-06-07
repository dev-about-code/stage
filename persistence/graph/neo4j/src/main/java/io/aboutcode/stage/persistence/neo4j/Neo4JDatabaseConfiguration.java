package io.aboutcode.stage.persistence.neo4j;


import java.net.URI;
import java.net.URISyntaxException;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * <p>This provides easy access to different types of Neo4J database configurations.</p>
 * <p><em>Note</em> that this class is work in progress</p>
 */
public interface Neo4JDatabaseConfiguration {
    /**
     * Creates a new {@link Neo4JDatabaseConfiguration} using Bolt as target protocol with the
     * specified parameters.
     *
     * @param serverName The name of the server to connect to
     * @param port       The port to connect on
     * @param username   The user to connect with
     * @param password   The password of the database user
     *
     * @return An instance of {@link Neo4JDatabaseConfiguration}
     */
    static Neo4JDatabaseConfiguration bolt(String serverName, int port, String username,
                                           String password) {
        // todo: add logging
        //final Logger logger = LoggerFactory.getLogger(Neo4JDatabaseConfiguration.class);
        Config config = Config
                .build()
                .withEncryption()
                .withLeakedSessionsLogging()
                //.withLogging()
                // todo: make configurable
                .withLoadBalancingStrategy(Config.LoadBalancingStrategy.LEAST_CONNECTED)
                .toConfig();
        URI uri;
        try {
            uri = new URI(
                    "bolt",
                    null,
                    serverName,
                    port,
                    null,
                    null,
                    null
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    String.format("Could not create connection url because: %s", e.getMessage()),
                    e);
        }
        return () -> GraphDatabase.driver(uri, AuthTokens.basic(username, password), config);
    }

    /**
     * Creates the database {@link Driver} with the corresponding parameters for the underlying
     * Neo4J database.
     *
     * @return The resulting configuration
     */
    Driver apply();
}
